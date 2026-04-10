# Building ProtoCaller API Client

## Quick Build Guide

### Prerequisites
- **Recommended**: JDK 25 - Latest version with modern features
- **Minimum**: JDK 17 (LTS)
- **Maven Compiler Plugin**: 3.13.0+ (automatically configured in pom.xml for Java 25 support)

### Standard Build

```bash
# Build the JAR
mvn clean package

# Run the application
java -jar target/protocaller-api-client-1.0-SNAPSHOT.jar
```

## Creating Native Installers

### macOS (DMG)

First, generate an ICNS file from your PNG icon (run once whenever the icon changes):

```bash
mkdir -p AppIcon.iconset
sips -z 16 16 src/main/resources/icons/app-icon-512.png --out AppIcon.iconset/icon_16x16.png
sips -z 32 32 src/main/resources/icons/app-icon-512.png --out AppIcon.iconset/icon_16x16@2x.png
sips -z 32 32 src/main/resources/icons/app-icon-512.png --out AppIcon.iconset/icon_32x32.png
sips -z 64 64 src/main/resources/icons/app-icon-512.png --out AppIcon.iconset/icon_32x32@2x.png
sips -z 128 128 src/main/resources/icons/app-icon-512.png --out AppIcon.iconset/icon_128x128.png
sips -z 256 256 src/main/resources/icons/app-icon-512.png --out AppIcon.iconset/icon_128x128@2x.png
sips -z 256 256 src/main/resources/icons/app-icon-512.png --out AppIcon.iconset/icon_256x256.png
sips -z 512 512 src/main/resources/icons/app-icon-512.png --out AppIcon.iconset/icon_256x256@2x.png
sips -z 512 512 src/main/resources/icons/app-icon-512.png --out AppIcon.iconset/icon_512x512.png
iconutil -c icns AppIcon.iconset -o src/main/resources/icons/app-icon.icns
rm -rf AppIcon.iconset
```

Then build the DMG with Maven (this runs `jpackage` with the custom icon):

```bash
mvn clean package -Ppackage-dmg
```

Or run `jpackage` directly:

```bash
jpackage \
  --input target \
  --main-jar protocaller-api-client-1.0.0-SNAPSHOT.jar \
  --main-class com.overzealouspelican.Main \
  --name "ProtoCaller API Client" \
  --type dmg \
  --app-version 1.0.0 \
  --vendor "ProtoCaller" \
  --description "Privacy-focused desktop API client" \
  --java-options '-Xmx512m' \
  --mac-package-name ProtoCaller \
  --icon src/main/resources/icons/app-icon.icns
```

### Windows (EXE)

```powershell
jpackage `
  --input target `
  --main-jar protocaller-api-client-1.0-SNAPSHOT.jar `
  --main-class com.overzealouspelican.Main `
  --name "ProtoCaller API Client" `
  --type exe `
  --app-version 1.0.0 `
  --vendor "ProtoCaller" `
  --description "Privacy-focused desktop API client" `
  --java-options '-Xmx512m' `
  --win-dir-chooser `
  --win-menu `
  --win-shortcut
```

### Linux (DEB)

**Note**: JDK 25 has a known jlink bug on Linux. Use the workaround below:

```bash
# Create a custom runtime by copying the full JDK
cp -r $JAVA_HOME custom-runtime

# Use jpackage with the custom runtime to bypass jlink
jpackage \
  --input target \
  --main-jar yapmc-1.0-SNAPSHOT.jar \
  --main-class com.overzealouspelican.Main \
  --name yapmc \
  --type deb \
  --app-version 1.0 \
  --vendor "YAPMC" \
  --description "Privacy-focused desktop API client" \
  --java-options '-Xmx512m' \
  --runtime-image custom-runtime \
  --linux-package-name yapmc \
  --linux-app-category utils \
  --linux-shortcut
```

## Troubleshooting jpackage/jlink Errors

### Error: "ct.sym has been modified" or jlink fails (JDK 25 on Linux)

This is a known bug in JDK 25's jlink implementation on Linux.

#### Solution: Use --runtime-image (Recommended for JDK 25)

Bypass jlink by providing a pre-built runtime:

```bash
# Copy your JDK to use as a custom runtime
cp -r $JAVA_HOME custom-runtime

# Use it with jpackage
jpackage \
  --input target \
  --main-jar yapmc-1.0-SNAPSHOT.jar \
  --main-class com.overzealouspelican.Main \
  --name yapmc \
  --type deb \
  --app-version 1.0 \
  --vendor "YAPMC" \
  --description "Privacy-focused desktop API client" \
  --java-options '-Xmx512m' \
  --runtime-image custom-runtime \
  --linux-package-name yapmc \
  --linux-app-category utils \
  --linux-shortcut
```

**Note**: This creates a larger installer (~300MB) since it includes the full JDK instead of a minimal runtime (~50MB).

### Error: "release version 25 not supported"

This happens with older Maven compiler plugin versions.

#### Solution: Upgrade maven-compiler-plugin

The project's pom.xml already uses version 3.13.0 which supports Java 25. If you see this error:

1. Ensure you're using the latest pom.xml from the repository
2. Clean your Maven cache: `mvn clean`
3. Rebuild: `mvn package`

### Other Common Issues

#### Missing Dependencies on Linux

If you get errors about missing dependencies when building on Linux:

```bash
# Ubuntu/Debian
sudo apt-get install fakeroot

# Fedora/RHEL
sudo dnf install fakeroot
```

#### Permission Issues

If jpackage fails with permission errors:

```bash
# Make sure the target directory is accessible
chmod -R 755 target/
```

## GitHub Actions Builds

The project uses GitHub Actions for automated builds. See `.github/workflows/release.yml` for the configuration.

The workflow:
- Uses JDK 21 for all platforms
- Includes automatic fallback for the jlink bug on Linux
- Creates native installers for Windows, macOS, and Linux
- Attaches installers to GitHub Releases

To trigger a build:
```bash
git tag v1.0.0
git push origin v1.0.0
```

## Testing Installers

### After Building

**macOS:**
```bash
# Open the DMG
open YAPMC-1.0.dmg
```

**Windows:**
```powershell
# Run the installer
.\YAPMC-1.0.exe
```

**Linux:**
```bash
# Install the DEB package
sudo dpkg -i yapmc_1.0-1_amd64.deb

# Run the application
yapmc
```

## Additional Resources

- [jpackage Documentation](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html)
- [jlink Documentation](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jlink.html)
- [OpenJDK jpackage Guide](https://openjdk.org/jeps/392)
