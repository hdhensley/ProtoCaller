package com.overzealouspelican.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

/**
 * Service for determining and managing application storage paths.
 * Follows the Single Responsibility Principle - only handles storage path logic.
 */
public class StoragePathService {

    private static final String APP_DIR_NAME = ".protocaller";
    private static final String STORAGE_LOCATION_KEY = "storage_location";
    private static final Preferences prefs = Preferences.userNodeForPackage(StoragePathService.class);

    /**
     * Get the application data directory based on the OS and user preferences
     */
    public Path getDataDirectory() {
        // Check if custom storage location is configured
        String customLocation = getCustomStorageLocation();
        if (customLocation != null && !customLocation.isEmpty()) {
            return Paths.get(customLocation);
        }

        // Fall back to default location
        return getDefaultStorageLocation();
    }

    /**
     * Get the default storage location based on the OS
     */
    public Path getDefaultStorageLocation() {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return Paths.get(appData, "ProtoCaller");
            }
            return Paths.get(userHome, "AppData", "Roaming", "ProtoCaller");
        } else if (os.contains("mac")) {
            return Paths.get(userHome, "Library", "Application Support", "ProtoCaller");
        } else {
            return Paths.get(userHome, APP_DIR_NAME);
        }
    }

    /**
     * Get the custom storage location from preferences
     */
    public String getCustomStorageLocation() {
        return prefs.get(STORAGE_LOCATION_KEY, "");
    }

    /**
     * Set the custom storage location
     */
    public void setCustomStorageLocation(String location) {
        prefs.put(STORAGE_LOCATION_KEY, location);
    }

    /**
     * Ensure the data directory exists
     */
    public void ensureDirectoryExists(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            System.out.println("Created data directory: " + directory);
        }
    }
}

