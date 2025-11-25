package com.overzealouspelican.panel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.overzealouspelican.model.ApplicationState;
import com.overzealouspelican.model.Environment;
import com.overzealouspelican.service.EnvironmentService;

/**
 * IntelliJ-style environment editor embedded in the sidebar.
 */
public class EnvironmentEditorPanel extends JPanel {

    private static final int INITIAL_KEY_VALUE_ROWS = 3;

    private JComboBox<String> environmentDropdown;
    private List<JTextField> keyFields;
    private List<JTextField> valueFields;
    private List<JButton> removeButtons;
    private JPanel keyValueRowsContainer;
    private ApplicationState appState;
    private EnvironmentService environmentService;
    private JButton saveButton;
    private JButton newEnvButton;
    private Map<String, String> originalEnvironmentState;
    private boolean isLoadingEnvironment;

    public EnvironmentEditorPanel() {
        keyFields = new ArrayList<>();
        valueFields = new ArrayList<>();
        removeButtons = new ArrayList<>();
        appState = ApplicationState.getInstance();
        environmentService = new EnvironmentService();
        originalEnvironmentState = new HashMap<>();
        isLoadingEnvironment = false;
        initializePanel();
        loadEnvironmentsFromDisk();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIManager.getColor("Panel.background"));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        mainPanel.setBackground(UIManager.getColor("Panel.background"));

        // Dropdown section
        mainPanel.add(createDropdownPanel());
        mainPanel.add(Box.createVerticalStrut(12));

        // Key-value pairs section
        mainPanel.add(createKeyValuePanel());
        mainPanel.add(Box.createVerticalStrut(12));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createDropdownPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.setBackground(UIManager.getColor("Panel.background"));

        environmentDropdown = new JComboBox<>();
        environmentDropdown.addActionListener(e -> loadSelectedEnvironment());

        newEnvButton = new JButton("+");
        newEnvButton.setToolTipText("Create a new environment");
        newEnvButton.setPreferredSize(new Dimension(40, 28));
        newEnvButton.addActionListener(e -> handleNewEnvironment());

        panel.add(environmentDropdown, BorderLayout.CENTER);
        panel.add(newEnvButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createKeyValuePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIManager.getColor("Panel.background"));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(8, 0));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        headerPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel keyHeader = new JLabel("Key");
        keyHeader.setFont(keyHeader.getFont().deriveFont(Font.BOLD, 11f));
        keyHeader.setForeground(UIManager.getColor("Label.foreground"));
        keyHeader.setPreferredSize(new Dimension(100, 20));

        JLabel valueHeader = new JLabel("Value");
        valueHeader.setFont(valueHeader.getFont().deriveFont(Font.BOLD, 11f));
        valueHeader.setForeground(UIManager.getColor("Label.foreground"));

        JLabel spacer = new JLabel("");
        spacer.setPreferredSize(new Dimension(32, 20));

        headerPanel.add(keyHeader, BorderLayout.WEST);
        headerPanel.add(valueHeader, BorderLayout.CENTER);
        headerPanel.add(spacer, BorderLayout.EAST);

        panel.add(headerPanel);
        panel.add(Box.createVerticalStrut(4));

        // Key-value input rows container with scroll
        keyValueRowsContainer = new JPanel();
        keyValueRowsContainer.setLayout(new BoxLayout(keyValueRowsContainer, BoxLayout.Y_AXIS));
        keyValueRowsContainer.setBackground(UIManager.getColor("Panel.background"));

        for (int i = 0; i < INITIAL_KEY_VALUE_ROWS; i++) {
            addKeyValueRow();
        }

        JScrollPane scrollPane = new JScrollPane(keyValueRowsContainer);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(0, 250));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(6));

        // Button panel with Add Variable and Save buttons on same row
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton addRowButton = new JButton("+ Add Variable");
        addRowButton.setPreferredSize(new Dimension(140, 28));
        addRowButton.addActionListener(e -> {
            addKeyValueRow();
            keyValueRowsContainer.revalidate();
            keyValueRowsContainer.repaint();
            checkForChanges();
        });

        saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(80, 28));
        saveButton.setOpaque(true); // Required for background color changes
        saveButton.addActionListener(e -> handleSave());

        buttonPanel.add(addRowButton);
        buttonPanel.add(saveButton);

        panel.add(buttonPanel);

        return panel;
    }

    private void addKeyValueRow() {
        JPanel rowPanel = new JPanel(new BorderLayout(4, 0));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        rowPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        rowPanel.setBackground(UIManager.getColor("Panel.background"));

        JTextField keyField = new JTextField();
        keyField.setPreferredSize(new Dimension(100, 24));
        keyField.setMinimumSize(new Dimension(100, 24));
        keyField.setMaximumSize(new Dimension(100, 24));

        JTextField valueField = new JTextField();

        // Add document listeners to track changes
        DocumentListener changeListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { checkForChanges(); }
            @Override
            public void removeUpdate(DocumentEvent e) { checkForChanges(); }
            @Override
            public void changedUpdate(DocumentEvent e) { checkForChanges(); }
        };

        keyField.getDocument().addDocumentListener(changeListener);
        valueField.getDocument().addDocumentListener(changeListener);

        JButton removeButton = new JButton("×");
        removeButton.setPreferredSize(new Dimension(32, 24));
        removeButton.setToolTipText("Remove this variable");
        removeButton.setFont(removeButton.getFont().deriveFont(16f));
        removeButton.setMargin(new Insets(0, 0, 0, 0));

        keyFields.add(keyField);
        valueFields.add(valueField);
        removeButtons.add(removeButton);

        removeButton.addActionListener(e -> {
            // Disable button immediately to prevent double-clicks
            removeButton.setEnabled(false);

            int index = removeButtons.indexOf(removeButton);

            if (index >= 0 && index < keyFields.size()) {
                // Remove from lists
                keyFields.remove(index);
                valueFields.remove(index);
                removeButtons.remove(index);

                // Remove from UI
                keyValueRowsContainer.remove(rowPanel);

                keyValueRowsContainer.revalidate();
                keyValueRowsContainer.repaint();
                checkForChanges();
            }
        });

        rowPanel.add(keyField, BorderLayout.WEST);
        rowPanel.add(valueField, BorderLayout.CENTER);
        rowPanel.add(removeButton, BorderLayout.EAST);

        keyValueRowsContainer.add(rowPanel);
    }

    private void loadEnvironmentsFromDisk() {
        Map<String, Environment> environments = environmentService.loadEnvironments();

        environmentDropdown.removeAllItems();

        if (environments.isEmpty()) {
            String[] defaultEnvs = {"Development", "Staging", "Production", "Testing", "Local"};
            for (String env : defaultEnvs) {
                environmentDropdown.addItem(env);
            }
            environmentDropdown.setSelectedItem("Development");
        } else {
            for (String envName : environments.keySet()) {
                environmentDropdown.addItem(envName);
            }

            String currentEnv = appState.getSelectedEnvironment();
            if (environments.containsKey(currentEnv)) {
                environmentDropdown.setSelectedItem(currentEnv);
            }
        }

        loadSelectedEnvironment();
    }

    private void loadSelectedEnvironment() {
        String selectedName = (String) environmentDropdown.getSelectedItem();
        if (selectedName == null) return;

        isLoadingEnvironment = true;

        // Update app state
        appState.setSelectedEnvironment(selectedName);

        Environment env = environmentService.loadEnvironment(selectedName);

        keyValueRowsContainer.removeAll();
        keyFields.clear();
        valueFields.clear();
        removeButtons.clear();

        if (env != null && !env.getVariables().isEmpty()) {
            // Update app state with the loaded environment variables
            appState.setEnvironmentVariables(env.getVariables());

            // Store the original state for change tracking
            originalEnvironmentState = new HashMap<>(env.getVariables());

            for (Map.Entry<String, String> entry : env.getVariables().entrySet()) {
                addKeyValueRow();
                int lastIndex = keyFields.size() - 1;
                keyFields.get(lastIndex).setText(entry.getKey());
                valueFields.get(lastIndex).setText(entry.getValue());
            }
        } else {
            // Clear environment variables in app state if environment is empty
            appState.setEnvironmentVariables(new HashMap<>());

            // Store empty original state
            originalEnvironmentState = new HashMap<>();

            for (int i = 0; i < INITIAL_KEY_VALUE_ROWS; i++) {
                addKeyValueRow();
            }
        }

        keyValueRowsContainer.revalidate();
        keyValueRowsContainer.repaint();

        isLoadingEnvironment = false;
        checkForChanges();
    }

    private void handleSave() {
        // Check if there are actually changes to save
        Map<String, String> currentState = new HashMap<>();
        for (int i = 0; i < keyFields.size(); i++) {
            String key = keyFields.get(i).getText().trim();
            String value = valueFields.get(i).getText().trim();

            if (!key.isEmpty() && !value.isEmpty()) {
                currentState.put(key, value);
            }
        }

        // If no changes, don't proceed with save
        if (currentState.equals(originalEnvironmentState)) {
            appState.setStatus("No changes to save", "ℹ️");
            return;
        }

        appState.setStatus("Saving environment...", "🔵");

        String selectedEnvironment = (String) environmentDropdown.getSelectedItem();
        if (selectedEnvironment == null || selectedEnvironment.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select or create an environment first.",
                "No Environment Selected",
                JOptionPane.WARNING_MESSAGE);
            appState.setStatusError("No environment selected");
            return;
        }

        try {
            Environment environment = new Environment(selectedEnvironment, currentState);
            environmentService.saveEnvironment(environment);

            appState.setEnvironmentVariables(currentState);
            appState.setStatusSuccess("Environment '" + selectedEnvironment + "' saved");

            // Update original state after successful save
            originalEnvironmentState = new HashMap<>(currentState);
            checkForChanges();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to save environment: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);

            appState.setStatusError("Failed to save environment");
        }
    }

    private void handleNewEnvironment() {
        String newEnvName = JOptionPane.showInputDialog(
            this,
            "Enter new environment name:",
            "New Environment",
            JOptionPane.PLAIN_MESSAGE
        );

        if (newEnvName != null && !newEnvName.trim().isEmpty()) {
            newEnvName = newEnvName.trim();

            if (environmentService.environmentExists(newEnvName)) {
                int result = JOptionPane.showConfirmDialog(
                    this,
                    "Environment '" + newEnvName + "' already exists. Do you want to edit it?",
                    "Environment Exists",
                    JOptionPane.YES_NO_OPTION
                );

                if (result == JOptionPane.YES_OPTION) {
                    environmentDropdown.setSelectedItem(newEnvName);
                }
            } else {
                environmentDropdown.addItem(newEnvName);
                environmentDropdown.setSelectedItem(newEnvName);
            }
        }
    }

    private void checkForChanges() {
        if (isLoadingEnvironment) return;

        // Get current state from the input fields
        Map<String, String> currentState = new HashMap<>();
        for (int i = 0; i < keyFields.size(); i++) {
            String key = keyFields.get(i).getText().trim();
            String value = valueFields.get(i).getText().trim();

            if (!key.isEmpty() && !value.isEmpty()) {
                currentState.put(key, value);
            }
        }

        // Check if current state differs from original state
        boolean hasChanges = !currentState.equals(originalEnvironmentState);

        // Update save button appearance based on changes
        if (hasChanges) {
            // Has changes - enable button with warning styling
            saveButton.setEnabled(true);

            // Try FlatLaf specific colors first, fall back to standard colors
            Color warningColor = UIManager.getColor("Actions.Yellow");
            if (warningColor == null) {
                warningColor = UIManager.getColor("Component.warningBorderColor");
            }
            if (warningColor == null) {
                warningColor = new Color(255, 193, 7); // Bootstrap warning color
            }

            saveButton.setBackground(warningColor);
            saveButton.setForeground(Color.BLACK);
            saveButton.putClientProperty("JButton.buttonType", null); // Reset to default button type
            saveButton.setBorder(BorderFactory.createLineBorder(warningColor.darker(), 1));
            saveButton.setToolTipText("Save changes to environment");
        } else {
            // No changes - keep enabled but style as success to show the color properly
            saveButton.setEnabled(true); // Keep enabled so colors show

            // Try FlatLaf specific colors first, fall back to standard colors
            Color successColor = UIManager.getColor("Actions.Green");
            if (successColor == null) {
                successColor = UIManager.getColor("Component.focusColor");
            }
            if (successColor == null) {
                successColor = new Color(40, 167, 69); // Bootstrap success color
            }

            saveButton.setBackground(successColor);
            saveButton.setForeground(Color.WHITE);
            saveButton.putClientProperty("JButton.buttonType", null);
            saveButton.setBorder(BorderFactory.createLineBorder(successColor.darker(), 1));
            saveButton.setToolTipText("No changes to save");

            // Make the button appear disabled by reducing opacity but keep it functional
            saveButton.putClientProperty("JComponent.opacity", 0.7f);
        }

        // Reset opacity for warning state
        if (hasChanges) {
            saveButton.putClientProperty("JComponent.opacity", 1.0f);
        }

        // Force visual update
        saveButton.invalidate();
        saveButton.revalidate();
        saveButton.repaint();

        // Update parent to ensure proper rendering
        if (saveButton.getParent() != null) {
            saveButton.getParent().repaint();
        }
    }

    public void refresh() {
        loadEnvironmentsFromDisk();
    }

    /**
     * Refresh UI elements after theme changes to ensure proper appearance
     */
    public void refreshAfterThemeChange() {
        // Update background colors to match new theme
        setBackground(UIManager.getColor("Panel.background"));

        // Update any theme-dependent colors
        Component[] components = getComponents();
        updateComponentsForTheme(components);

        // Force a complete repaint
        revalidate();
        repaint();
    }

    private void updateComponentsForTheme(Component[] components) {
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                comp.setBackground(UIManager.getColor("Panel.background"));
                if (comp instanceof Container) {
                    updateComponentsForTheme(((Container) comp).getComponents());
                }
            }
            // Remove any borders that might have theme-specific colors
            if (comp instanceof JComponent) {
                JComponent jComp = (JComponent) comp;
                // Keep only null borders or empty borders, remove line borders
                if (jComp.getBorder() != null &&
                    jComp.getBorder().getClass().getSimpleName().contains("Line")) {
                    jComp.setBorder(null);
                }
            }
        }
    }
}
