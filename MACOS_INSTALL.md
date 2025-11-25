# macOS Installation Guide

## Gatekeeper Warning

When you download and try to open ProtoCaller API Client on macOS, you may see a warning:

> **"Apple could not verify "ProtoCaller API Client" is free of malware that may harm your Mac or compromise your privacy."**

This is normal for open-source applications that aren't distributed through the Mac App Store or signed with an Apple Developer certificate (which costs $99/year). **ProtoCaller API Client is completely safe** - it's open source and you can review all the code yourself.

## How to Install ProtoCaller API Client on macOS

### Method 1: Right-click and Open (Recommended)

1. Download `ProtoCaller-API-Client-1.0.0.dmg` from the [Releases page](https://github.com/hdhensley/protocaller/releases)
2. Double-click the DMG to mount it
3. **Right-click** (or Control+click) on the ProtoCaller API Client app
4. Select **"Open"** from the context menu
5. Click **"Open"** in the dialog that appears
6. macOS will remember your choice and allow the app to run

### Method 2: System Settings Override

1. Try to open ProtoCaller API Client normally (it will be blocked)
2. Open **System Settings** → **Privacy & Security**
3. Scroll down to the **Security** section
4. You'll see a message: *"ProtoCaller API Client was blocked from use because it is not from an identified developer"*
5. Click **"Open Anyway"**
6. Click **"Open"** in the confirmation dialog

### Method 3: Remove Quarantine Flag (Advanced)

If you're comfortable with the Terminal:

```bash
# Navigate to where you downloaded the DMG
cd ~/Downloads

# Remove the quarantine attribute
xattr -d com.apple.quarantine ProtoCaller-API-Client-1.0.0.dmg

# Now open the DMG normally
open ProtoCaller-API-Client-1.0.0.dmg
```

Or after copying the app to your Applications folder:

```bash
# Remove quarantine from the app
sudo xattr -dr com.apple.quarantine "/Applications/ProtoCaller API Client.app"

# Now you can open it normally
open "/Applications/ProtoCaller API Client.app"
```

## Why This Happens

macOS Gatekeeper blocks applications that aren't:
1. **Notarized** by Apple (requires Apple Developer Program membership - $99/year)
2. **Code-signed** with an Apple Developer certificate
3. **Distributed through** the Mac App Store

ProtoCaller API Client is an open-source, privacy-focused project. To keep it completely free and open:
- We don't charge for the app
- We don't collect your data
- We don't require you to create accounts
- Therefore, we can't justify the $99/year Apple Developer fee

## Is ProtoCaller API Client Safe?

**Yes!** ProtoCaller API Client is:
- ✅ **Open Source** - All code is visible on GitHub
- ✅ **Privacy-Focused** - All data stays on your local machine
- ✅ **No Network Tracking** - Only makes API calls YOU configure
- ✅ **Community Built** - Built by developers, for developers
- ✅ **Reproducible Builds** - You can build it yourself from source

You can verify the safety by:
1. **Reviewing the source code** on GitHub
2. **Building it yourself** from source (see README.md)
3. **Checking the build process** in `.github/workflows/release.yml`

## Alternative: Build from Source

If you prefer to build ProtoCaller API Client yourself:

```bash
# Clone the repository
git clone https://github.com/hdhensley/protocaller.git
cd protocaller

# Build with Maven
./mvnw clean package

# Run directly
java -jar target/protocaller-api-client-1.0-SNAPSHOT.jar

# Or create your own DMG
jpackage \
  --input target \
  --main-jar protocaller-api-client-1.0-SNAPSHOT.jar \
  --main-class com.overzealouspelican.Main \
  --name "ProtoCaller API Client" \
  --type dmg \
  --app-version 1.0.0 \
  --vendor "ProtoCaller" \
  --description "Privacy-focused desktop API client" \
  --java-options '-Xmx512m' \
  --mac-package-name ProtoCaller

# Sign it with your own ad-hoc signature
codesign --force --deep --sign - "ProtoCaller API Client.app"
```

## Need Help?

If you're still having issues:
1. Check [GitHub Issues](https://github.com/hdhensley/protocaller/issues)
2. Start a [Discussion](https://github.com/hdhensley/protocaller/discussions)
3. The community is here to help!

---

**Remember**: This warning appears for ALL open-source apps not distributed through the App Store. It doesn't mean the app is unsafe - it just means Apple hasn't verified it (which requires paying them $99/year).

