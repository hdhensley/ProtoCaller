package com.overzealouspelican.service;

import com.formdev.flatlaf.*;
import javax.swing.*;
import java.util.prefs.Preferences;

/**
 * Service for managing application settings (theme and storage location).
 * Follows the Single Responsibility Principle - only handles settings persistence and retrieval.
 */
public class SettingsService {

    private static final Preferences prefs = Preferences.userNodeForPackage(SettingsService.class);
    private static final String THEME_KEY = "theme";
    private static final String STORAGE_LOCATION_KEY = "storage_location";
    private static final String DEFAULT_THEME = "FlatLaf IntelliJ";

    /**
     * Theme option data class
     */
    public static class ThemeOption {
        private final String displayName;
        private final String className;

        public ThemeOption(String displayName, String className) {
            this.displayName = displayName;
            this.className = className;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getClassName() {
            return className;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Get available themes
     */
    public ThemeOption[] getAvailableThemes() {
        return new ThemeOption[] {
            new ThemeOption("FlatLaf Light", FlatLightLaf.class.getName()),
            new ThemeOption("FlatLaf Dark", FlatDarkLaf.class.getName()),
            new ThemeOption("FlatLaf IntelliJ", FlatIntelliJLaf.class.getName()),
            new ThemeOption("FlatLaf Darcula", FlatDarculaLaf.class.getName())
        };
    }

    /**
     * Get the saved theme name
     */
    public String getSavedTheme() {
        return prefs.get(THEME_KEY, DEFAULT_THEME);
    }

    /**
     * Save the theme preference
     */
    public void saveTheme(String themeName) {
        prefs.put(THEME_KEY, themeName);
    }

    /**
     * Get the saved storage location
     */
    public String getSavedStorageLocation() {
        return prefs.get(STORAGE_LOCATION_KEY, "");
    }

    /**
     * Save the storage location preference
     */
    public void saveStorageLocation(String location) {
        prefs.put(STORAGE_LOCATION_KEY, location);
    }

    /**
     * Reset theme to default
     */
    public void resetTheme() {
        prefs.remove(THEME_KEY);
    }

    /**
     * Reset storage location to default
     */
    public void resetStorageLocation() {
        prefs.remove(STORAGE_LOCATION_KEY);
    }

    /**
     * Apply a theme by class name
     */
    public void applyTheme(String themeClassName) throws Exception {
        UIManager.setLookAndFeel(themeClassName);

        // Update all open windows
        for (java.awt.Window window : java.awt.Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }

    /**
     * Load and apply the saved theme at application startup
     */
    public void loadAndApplyTheme() {
        String savedThemeName = getSavedTheme();
        String themeClassName = getThemeClassName(savedThemeName);

        if (themeClassName != null) {
            try {
                UIManager.setLookAndFeel(themeClassName);
            } catch (Exception ex) {
                System.err.println("Failed to load saved theme: " + ex.getMessage());
                // Fall back to default
                try {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                } catch (Exception e) {
                    System.err.println("Failed to load default theme");
                }
            }
        }
    }

    /**
     * Get theme class name from display name
     */
    private String getThemeClassName(String displayName) {
        ThemeOption[] themes = getAvailableThemes();
        for (ThemeOption theme : themes) {
            if (theme.getDisplayName().equals(displayName)) {
                return theme.getClassName();
            }
        }
        return null;
    }

    /**
     * Get the default theme name
     */
    public String getDefaultTheme() {
        return DEFAULT_THEME;
    }
}

