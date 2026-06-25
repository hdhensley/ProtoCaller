package com.overzealouspelican.controller;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.overzealouspelican.model.ApplicationState;
import com.overzealouspelican.model.Environment;
import com.overzealouspelican.service.EnvironmentService;

/**
 * Controller for environment form persistence and dirty-state tracking.
 * Single responsibility: manage loading, saving, and change detection for environment variables.
 */
public class EnvironmentFormController {

    private final EnvironmentService environmentService;
    private final ApplicationState appState;
    private Map<String, String> originalState;

    public EnvironmentFormController() {
        this.environmentService = new EnvironmentService();
        this.appState = ApplicationState.getInstance();
        this.originalState = new HashMap<>();
    }

    /**
     * Load all environments and return their names.
     * If none exist, returns a set of defaults.
     */
    public Map<String, Environment> loadEnvironments() {
        return environmentService.loadEnvironments();
    }

    /**
     * Load a specific environment by name.
     */
    public Environment loadEnvironment(String name) {
        return environmentService.loadEnvironment(name);
    }

    /**
     * Check if an environment exists.
     */
    public boolean environmentExists(String name) {
        return environmentService.environmentExists(name);
    }

    /**
     * Set the original state for change tracking (called after load or save).
     */
    public void setOriginalState(Map<String, String> state) {
        this.originalState = new HashMap<>(state);
    }

    /**
     * Get the current original state.
     */
    public Map<String, String> getOriginalState() {
        return originalState;
    }

    /**
     * Check if the given current state differs from the original loaded state.
     */
    public boolean hasChanges(Map<String, String> currentState) {
        return !currentState.equals(originalState);
    }

    /**
     * Build the current state map from the form field values.
     */
    public Map<String, String> buildCurrentState(List<JTextField> keyFields, List<JTextField> valueFields) {
        Map<String, String> currentState = new HashMap<>();
        for (int i = 0; i < keyFields.size(); i++) {
            String key = keyFields.get(i).getText().trim();
            String value = valueFields.get(i).getText().trim();
            if (!key.isEmpty() && !value.isEmpty()) {
                currentState.put(key, value);
            }
        }
        return currentState;
    }

    /**
     * Save the environment.
     *
     * @param parentComponent parent for dialog display
     * @param environmentName the name of the environment to save
     * @param currentState the key-value pairs to save
     * @return true if saved successfully
     */
    public boolean save(Component parentComponent, String environmentName, Map<String, String> currentState) {
        // No changes check
        if (!hasChanges(currentState)) {
            appState.setStatus("No changes to save", "\u2139\uFE0F");
            return false;
        }

        appState.setStatus("Saving environment...", "\uD83D\uDD35");

        if (environmentName == null || environmentName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parentComponent,
                "Please select or create an environment first.",
                "No Environment Selected",
                JOptionPane.WARNING_MESSAGE);
            appState.setStatusError("No environment selected");
            return false;
        }

        try {
            Environment environment = new Environment(environmentName, currentState);
            environmentService.saveEnvironment(environment);

            appState.setEnvironmentVariables(currentState);
            appState.setStatusSuccess("Environment '" + environmentName + "' saved");

            // Update original state after successful save
            originalState = new HashMap<>(currentState);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentComponent,
                "Failed to save environment: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            appState.setStatusError("Failed to save environment");
            return false;
        }
    }

    /**
     * Update the application state with environment data after loading.
     */
    public void applyEnvironmentToAppState(String name, Map<String, String> variables) {
        appState.setSelectedEnvironment(name);
        appState.setEnvironmentVariables(variables);
    }
}
