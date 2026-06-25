package com.overzealouspelican.panel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.overzealouspelican.controller.EnvironmentFormController;
import com.overzealouspelican.model.Environment;
import com.overzealouspelican.util.SaveButtonStyler;
import com.overzealouspelican.util.UITheme;

/**
 * UI panel for editing environment variables.
 * Single responsibility: lay out the environment form and delegate state management to the controller.
 */
public class EnvironmentEditorPanel extends JPanel {

    private static final int INITIAL_KEY_VALUE_ROWS = 3;

    private JComboBox<String> environmentDropdown;
    private List<JTextField> keyFields;
    private List<JTextField> valueFields;
    private List<JButton> removeButtons;
    private JPanel keyValueRowsContainer;
    private JButton saveButton;
    private boolean isLoadingEnvironment;

    private final EnvironmentFormController formController;

    public EnvironmentEditorPanel() {
        keyFields = new ArrayList<>();
        valueFields = new ArrayList<>();
        removeButtons = new ArrayList<>();
        formController = new EnvironmentFormController();
        isLoadingEnvironment = false;
        initializePanel();
        loadEnvironmentsFromDisk();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_MD, UITheme.SPACING_SM, UITheme.SPACING_MD, UITheme.SPACING_SM));

        // Top section: dropdown + header
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(UIManager.getColor("Panel.background"));

        topPanel.add(createDropdownPanel());
        topPanel.add(Box.createVerticalStrut(UITheme.SPACING_MD));
        topPanel.add(createKeyValueHeader());
        topPanel.add(Box.createVerticalStrut(UITheme.SPACING_XS));

        add(topPanel, BorderLayout.NORTH);

        // Center: scrollable key-value rows
        keyValueRowsContainer = new JPanel();
        keyValueRowsContainer.setLayout(new BoxLayout(keyValueRowsContainer, BoxLayout.Y_AXIS));
        keyValueRowsContainer.setBackground(UIManager.getColor("Panel.background"));

        for (int i = 0; i < INITIAL_KEY_VALUE_ROWS; i++) {
            addKeyValueRow();
        }

        JScrollPane scrollPane = new JScrollPane(keyValueRowsContainer);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // Bottom: buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACING_SM, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_SM, 0, 0, 0));
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));

        JButton addRowButton = new JButton("+ Add Variable");
        addRowButton.setPreferredSize(new Dimension(140, UITheme.BUTTON_HEIGHT));
        addRowButton.addActionListener(e -> {
            addKeyValueRow();
            keyValueRowsContainer.revalidate();
            keyValueRowsContainer.repaint();
            updateSaveButtonState();
        });

        saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(80, UITheme.BUTTON_HEIGHT));
        saveButton.setOpaque(true);
        saveButton.addActionListener(e -> handleSave());

        buttonPanel.add(addRowButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createDropdownPanel() {
        JPanel panel = new JPanel(new BorderLayout(UITheme.SPACING_SM, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.INPUT_HEIGHT));
        panel.setBackground(UIManager.getColor("Panel.background"));

        environmentDropdown = new JComboBox<>();
        environmentDropdown.addActionListener(e -> loadSelectedEnvironment());

        JButton newEnvButton = new JButton("+");
        newEnvButton.setToolTipText("Create a new environment");
        newEnvButton.setPreferredSize(new Dimension(40, UITheme.INPUT_HEIGHT));
        newEnvButton.addActionListener(e -> handleNewEnvironment());

        panel.add(environmentDropdown, BorderLayout.CENTER);
        panel.add(newEnvButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createKeyValueHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout(8, 0));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        headerPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel keyHeader = new JLabel("Key");
        keyHeader.setFont(keyHeader.getFont().deriveFont(Font.BOLD, UITheme.FONT_SIZE_SM));
        keyHeader.setForeground(UIManager.getColor("Label.disabledForeground"));
        keyHeader.setPreferredSize(new Dimension(100, 20));

        JLabel valueHeader = new JLabel("Value");
        valueHeader.setFont(valueHeader.getFont().deriveFont(Font.BOLD, UITheme.FONT_SIZE_SM));
        valueHeader.setForeground(UIManager.getColor("Label.disabledForeground"));

        JLabel spacer = new JLabel("");
        spacer.setPreferredSize(new Dimension(32, 20));

        headerPanel.add(keyHeader, BorderLayout.WEST);
        headerPanel.add(valueHeader, BorderLayout.CENTER);
        headerPanel.add(spacer, BorderLayout.EAST);

        return headerPanel;
    }

    private void addKeyValueRow() {
        JPanel rowPanel = new JPanel(new BorderLayout(4, 0));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.INPUT_HEIGHT));
        rowPanel.setBorder(BorderFactory.createEmptyBorder(2, UITheme.SPACING_XS, 2, UITheme.SPACING_XS));
        rowPanel.setBackground(UIManager.getColor("Panel.background"));

        JTextField keyField = new JTextField();
        keyField.setPreferredSize(new Dimension(100, 26));
        keyField.setMinimumSize(new Dimension(100, 26));
        keyField.setMaximumSize(new Dimension(100, 26));

        JTextField valueField = new JTextField();

        DocumentListener changeListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateSaveButtonState(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateSaveButtonState(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateSaveButtonState(); }
        };

        keyField.getDocument().addDocumentListener(changeListener);
        valueField.getDocument().addDocumentListener(changeListener);

        JButton removeButton = new JButton("\u00D7");
        removeButton.setPreferredSize(new Dimension(32, 26));
        removeButton.setToolTipText("Remove this variable");
        removeButton.setFont(removeButton.getFont().deriveFont(UITheme.FONT_SIZE_TITLE));
        removeButton.setMargin(new Insets(0, 0, 0, 0));

        keyFields.add(keyField);
        valueFields.add(valueField);
        removeButtons.add(removeButton);

        removeButton.addActionListener(e -> {
            removeButton.setEnabled(false);
            int index = removeButtons.indexOf(removeButton);

            if (index >= 0 && index < keyFields.size()) {
                keyFields.remove(index);
                valueFields.remove(index);
                removeButtons.remove(index);
                keyValueRowsContainer.remove(rowPanel);
                keyValueRowsContainer.revalidate();
                keyValueRowsContainer.repaint();
                updateSaveButtonState();
            }
        });

        rowPanel.add(keyField, BorderLayout.WEST);
        rowPanel.add(valueField, BorderLayout.CENTER);
        rowPanel.add(removeButton, BorderLayout.EAST);

        keyValueRowsContainer.add(rowPanel);
    }

    // --- Action handlers ---

    private void loadEnvironmentsFromDisk() {
        Map<String, Environment> environments = formController.loadEnvironments();

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

            String currentEnv = com.overzealouspelican.model.ApplicationState.getInstance().getSelectedEnvironment();
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

        Environment env = formController.loadEnvironment(selectedName);

        keyValueRowsContainer.removeAll();
        keyFields.clear();
        valueFields.clear();
        removeButtons.clear();

        if (env != null && !env.getVariables().isEmpty()) {
            formController.applyEnvironmentToAppState(selectedName, env.getVariables());
            formController.setOriginalState(env.getVariables());

            for (Map.Entry<String, String> entry : env.getVariables().entrySet()) {
                addKeyValueRow();
                int lastIndex = keyFields.size() - 1;
                keyFields.get(lastIndex).setText(entry.getKey());
                valueFields.get(lastIndex).setText(entry.getValue());
            }
        } else {
            formController.applyEnvironmentToAppState(selectedName, new HashMap<>());
            formController.setOriginalState(new HashMap<>());

            for (int i = 0; i < INITIAL_KEY_VALUE_ROWS; i++) {
                addKeyValueRow();
            }
        }

        keyValueRowsContainer.revalidate();
        keyValueRowsContainer.repaint();

        isLoadingEnvironment = false;
        updateSaveButtonState();
    }

    private void handleSave() {
        String selectedEnvironment = (String) environmentDropdown.getSelectedItem();
        Map<String, String> currentState = formController.buildCurrentState(keyFields, valueFields);

        if (formController.save(this, selectedEnvironment, currentState)) {
            updateSaveButtonState();
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

            if (formController.environmentExists(newEnvName)) {
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

    private void updateSaveButtonState() {
        if (isLoadingEnvironment) return;

        Map<String, String> currentState = formController.buildCurrentState(keyFields, valueFields);
        boolean hasChanges = formController.hasChanges(currentState);

        if (hasChanges) {
            SaveButtonStyler.styleAsChanged(saveButton);
        } else {
            SaveButtonStyler.styleAsSaved(saveButton);
        }
    }

    // --- Public API ---

    public void refresh() {
        loadEnvironmentsFromDisk();
    }

    /**
     * Refresh UI elements after theme changes.
     */
    public void refreshAfterThemeChange() {
        setBackground(UIManager.getColor("Panel.background"));
        updateComponentsForTheme(getComponents());
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
            if (comp instanceof JComponent) {
                JComponent jComp = (JComponent) comp;
                if (jComp.getBorder() != null &&
                    jComp.getBorder().getClass().getSimpleName().contains("Line")) {
                    jComp.setBorder(null);
                }
            }
        }
    }
}
