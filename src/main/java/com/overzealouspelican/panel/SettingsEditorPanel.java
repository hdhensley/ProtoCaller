package com.overzealouspelican.panel;

import com.overzealouspelican.service.SettingsService;
import com.overzealouspelican.service.SettingsService.ThemeOption;
import com.overzealouspelican.service.StoragePathService;
import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * IntelliJ-style settings editor embedded in the sidebar.
 * Follows the Single Responsibility Principle - only handles UI rendering and user interaction.
 */
public class SettingsEditorPanel extends JPanel {

    private final SettingsService settingsService;
    private final StoragePathService storagePathService;

    private JComboBox<ThemeOption> themeComboBox;
    private JTextField storageLocationField;
    private JButton browseButton;
    private JButton saveButton;
    private JButton resetButton;

    public SettingsEditorPanel() {
        this.settingsService = new SettingsService();
        this.storagePathService = new StoragePathService();
        initializePanel();
        loadSettings();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        mainPanel.setBackground(UIManager.getColor("Panel.background"));

        // Theme section
        mainPanel.add(createSectionLabel("Appearance"));
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(createThemePanel());
        mainPanel.add(Box.createVerticalStrut(16));

        // Storage section
        mainPanel.add(createSectionLabel("Storage"));
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(createStoragePanel());
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(createInfoLabel());
        mainPanel.add(Box.createVerticalStrut(16));

        // Buttons
        mainPanel.add(createButtonPanel());

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createThemePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel.setBackground(UIManager.getColor("Panel.background"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel("Theme:");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        themeComboBox = new JComboBox<>(settingsService.getAvailableThemes());
        themeComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        themeComboBox.setPreferredSize(new Dimension(0, 28));
        themeComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(themeComboBox);

        return panel;
    }

    private JPanel createStoragePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.setBackground(UIManager.getColor("Panel.background"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel("Data Location:");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel fieldPanel = new JPanel(new BorderLayout(8, 0));
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        fieldPanel.setBackground(UIManager.getColor("Panel.background"));
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        storageLocationField = new JTextField();
        storageLocationField.setToolTipText("Leave empty to use default location");

        browseButton = new JButton("Browse...");
        browseButton.setPreferredSize(new Dimension(100, 28));
        browseButton.addActionListener(e -> browseForDirectory());

        fieldPanel.add(storageLocationField, BorderLayout.CENTER);
        fieldPanel.add(browseButton, BorderLayout.EAST);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(fieldPanel);

        return panel;
    }

    private JLabel createInfoLabel() {
        String defaultLocation = storagePathService.getDefaultStorageLocation().toString();
        JLabel infoLabel = new JLabel("<html><i>Default: " + defaultLocation + "</i></html>");
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.PLAIN, 10f));
        infoLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return infoLabel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        panel.setBackground(UIManager.getColor("Panel.background"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetToDefaults());

        saveButton = new JButton("Apply");
        saveButton.addActionListener(e -> saveSettings());

        panel.add(resetButton);
        panel.add(saveButton);

        return panel;
    }

    private void browseForDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        String currentLocation = storageLocationField.getText();
        if (!currentLocation.isEmpty()) {
            fileChooser.setCurrentDirectory(new File(currentLocation));
        }

        int result = fileChooser.showDialog(this, "Select");
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            storageLocationField.setText(selectedDir.getAbsolutePath());
        }
    }

    private void loadSettings() {
        String savedTheme = settingsService.getSavedTheme();
        for (int i = 0; i < themeComboBox.getItemCount(); i++) {
            if (themeComboBox.getItemAt(i).getDisplayName().equals(savedTheme)) {
                themeComboBox.setSelectedIndex(i);
                break;
            }
        }

        String savedLocation = settingsService.getSavedStorageLocation();
        storageLocationField.setText(savedLocation);
    }

    private void saveSettings() {
        ThemeOption selectedTheme = (ThemeOption) themeComboBox.getSelectedItem();
        if (selectedTheme != null) {
            settingsService.saveTheme(selectedTheme.getDisplayName());

            try {
                settingsService.applyTheme(selectedTheme.getClassName());

                // Refresh UI components to prevent border issues
                refreshAfterThemeChange();

                JOptionPane.showMessageDialog(this,
                    "Theme applied successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Failed to apply theme: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String storageLocation = storageLocationField.getText().trim();
        storagePathService.setCustomStorageLocation(storageLocation);

        if (!storageLocation.isEmpty()) {
            File storageDir = new File(storageLocation);
            if (!storageDir.exists()) {
                int choice = JOptionPane.showConfirmDialog(this,
                    "The specified directory does not exist. Create it?",
                    "Create Directory",
                    JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    if (!storageDir.mkdirs()) {
                        JOptionPane.showMessageDialog(this,
                            "Failed to create directory: " + storageLocation,
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
        }
    }

    private void resetToDefaults() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Reset all settings to defaults?",
            "Reset Settings",
            JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            String defaultTheme = settingsService.getDefaultTheme();
            for (int i = 0; i < themeComboBox.getItemCount(); i++) {
                if (themeComboBox.getItemAt(i).getDisplayName().equals(defaultTheme)) {
                    themeComboBox.setSelectedIndex(i);
                    break;
                }
            }

            storageLocationField.setText("");
            settingsService.resetTheme();
            settingsService.resetStorageLocation();

            JOptionPane.showMessageDialog(this,
                "Settings reset to defaults.",
                "Reset Complete",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Get the custom storage location from preferences
     * @deprecated Use StoragePathService instead
     */
    @Deprecated
    public static String getStorageLocation() {
        return new StoragePathService().getCustomStorageLocation();
    }

    /**
     * Load and apply the saved theme at application startup
     * @deprecated Use SettingsService instead
     */
    @Deprecated
    public static void loadAndApplyTheme() {
        new SettingsService().loadAndApplyTheme();
    }

    /**
     * Refresh UI elements after theme changes to prevent border issues
     */
    public void refreshAfterThemeChange() {
        // Update background colors
        setBackground(UIManager.getColor("Panel.background"));

        // Get all top-level windows and refresh their UI trees
        for (java.awt.Window window : java.awt.Window.getWindows()) {
            refreshUITreeForWindow(window);
        }

        // Force a complete repaint
        revalidate();
        repaint();
    }

    private void refreshUITreeForWindow(java.awt.Window window) {
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            // Find and refresh specific panel types that might have border issues
            refreshComponentTree(frame.getContentPane());
        }
    }

    private void refreshComponentTree(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof Container) {
                refreshComponentTree((Container) comp);
            }

            // Update background colors and remove problematic borders
            if (comp instanceof JComponent) {
                JComponent jComp = (JComponent) comp;

                // Update background if it's a panel
                if (comp instanceof JPanel) {
                    comp.setBackground(UIManager.getColor("Panel.background"));
                }

                // Remove any line borders that might cause theme issues
                if (jComp.getBorder() != null) {
                    String borderClass = jComp.getBorder().getClass().getSimpleName();
                    if (borderClass.contains("Line") || borderClass.contains("Matte")) {
                        jComp.setBorder(null);
                    }
                }
            }
        }
    }
}
