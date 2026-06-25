package com.overzealouspelican.controller;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;
import com.overzealouspelican.model.ApiCall;
import com.overzealouspelican.model.ApplicationState;
import com.overzealouspelican.service.ApiCallService;

/**
 * Handles drag-and-drop grouping logic for saved API calls.
 * Single responsibility: manage group assignment when items are dragged.
 */
public class ApiCallDragDropHandler {

    private final ApiCallService apiCallService;
    private final ApplicationState appState;

    public ApiCallDragDropHandler(ApiCallService apiCallService) {
        this.apiCallService = apiCallService;
        this.appState = ApplicationState.getInstance();
    }

    /**
     * Handle dropping an API call onto a group header.
     */
    public void dropOnGroup(String draggedName, String groupName) throws IOException {
        addApiCallToGroup(draggedName, groupName);
    }

    /**
     * Handle dropping an API call onto another API call item.
     *
     * @param parentComponent the parent for dialogs
     * @param draggedName the name of the dragged call
     * @param targetName the name of the target call
     * @return true if a change was made and the list should refresh
     */
    public boolean dropOnItem(Component parentComponent, String draggedName, String targetName) {
        try {
            Map<String, ApiCall> apiCalls = apiCallService.loadApiCalls();
            ApiCall draggedCall = apiCalls.get(draggedName);
            ApiCall targetCall = apiCalls.get(targetName);

            if (draggedCall == null || targetCall == null) {
                return false;
            }

            String draggedGroup = draggedCall.getGroupName();
            String targetGroup = targetCall.getGroupName();

            // If both are in different groups, don't allow nesting
            if (draggedGroup != null && targetGroup != null && !draggedGroup.equals(targetGroup)) {
                JOptionPane.showMessageDialog(parentComponent,
                    "Cannot nest groups. Please remove from current group first.",
                    "Invalid Operation",
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }

            // If target is in a group, add dragged to same group
            if (targetGroup != null && !targetGroup.trim().isEmpty()) {
                addApiCallToGroup(draggedName, targetGroup);
            } else if (draggedGroup != null && !draggedGroup.trim().isEmpty()) {
                // Dragged is already in a group, add target to that group
                addApiCallToGroup(targetName, draggedGroup);
            } else {
                // Neither in a group, prompt for new group name
                String groupName = JOptionPane.showInputDialog(parentComponent,
                    "Enter a name for the new group:",
                    "Create Group",
                    JOptionPane.PLAIN_MESSAGE);

                if (groupName != null && !groupName.trim().isEmpty()) {
                    addApiCallToGroup(draggedName, groupName);
                    addApiCallToGroup(targetName, groupName);
                } else {
                    return false;
                }
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parentComponent,
                "Failed to group API calls: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void addApiCallToGroup(String apiCallName, String groupName) throws IOException {
        Map<String, ApiCall> apiCalls = apiCallService.loadApiCalls();
        ApiCall apiCall = apiCalls.get(apiCallName);

        if (apiCall != null) {
            apiCall.setGroupName(groupName);
            apiCallService.saveApiCall(apiCall);
            appState.setStatusSuccess("Added '" + apiCallName + "' to group '" + groupName + "'");
        }
    }
}
