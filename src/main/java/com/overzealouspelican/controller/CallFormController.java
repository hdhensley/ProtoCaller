package com.overzealouspelican.controller;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import com.overzealouspelican.model.ApiCall;
import com.overzealouspelican.model.ApplicationState;
import com.overzealouspelican.service.ApiCallService;

/**
 * Controller for managing the API call form state (save, load, clear).
 * Single responsibility: translate between form data and ApiCall model.
 */
public class CallFormController {

    private final ApiCallService apiCallService;
    private final ApplicationState appState;
    private String currentGroupName;

    public CallFormController(ApiCallService apiCallService) {
        this.apiCallService = apiCallService;
        this.appState = ApplicationState.getInstance();
    }

    /**
     * Build an ApiCall from the current form state.
     */
    public ApiCall buildApiCall(String name, String url, String httpMethod,
                                String description, Map<String, String> headers,
                                Map<String, String> body) {
        ApiCall apiCall = new ApiCall(name, url, httpMethod, headers, body);

        if (currentGroupName != null) {
            apiCall.setGroupName(currentGroupName);
        }

        if (description != null && !description.trim().isEmpty()) {
            apiCall.setDescription(description.trim());
        }

        return apiCall;
    }

    /**
     * Save the given API call.
     *
     * @param parentComponent the parent component for dialog display
     * @param apiCall the API call to save
     * @return true if saved successfully
     */
    public boolean save(Component parentComponent, ApiCall apiCall) {
        appState.setStatus("Saving configuration...", "\uD83D\uDD35");

        String name = apiCall.getName();
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parentComponent,
                "Please enter a name for this API call.",
                "Name Required",
                JOptionPane.WARNING_MESSAGE);
            appState.setStatusError("Name is required");
            return false;
        }

        try {
            apiCallService.saveApiCall(apiCall);
            appState.setStatusSuccess("Configuration saved");
            appState.firePropertyChange("apiCallSaved", null, name);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentComponent,
                "Failed to save API call: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            appState.setStatusError("Failed to save configuration");
            return false;
        }
    }

    /**
     * Track the group name from a loaded API call so it can be preserved on save.
     */
    public void setCurrentGroupName(String groupName) {
        this.currentGroupName = groupName;
    }

    public String getCurrentGroupName() {
        return currentGroupName;
    }

    /**
     * Clear the tracked group name (called on form reset).
     */
    public void clearGroupName() {
        this.currentGroupName = null;
    }
}
