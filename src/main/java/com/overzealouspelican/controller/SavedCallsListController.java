package com.overzealouspelican.controller;

import java.util.*;
import com.overzealouspelican.model.ApiCall;
import com.overzealouspelican.service.ApiCallService;

/**
 * Controller for managing the saved API calls list state.
 * Single responsibility: organize calls by group and manage expand/collapse state.
 */
public class SavedCallsListController {

    private final ApiCallService apiCallService;
    private final Map<String, Boolean> groupExpandedState;

    public SavedCallsListController(ApiCallService apiCallService) {
        this.apiCallService = apiCallService;
        this.groupExpandedState = new HashMap<>();
    }

    /**
     * Result of organizing API calls into groups.
     */
    public static class GroupedCalls {
        private final Map<String, List<String>> groups;
        private final List<String> ungrouped;

        public GroupedCalls(Map<String, List<String>> groups, List<String> ungrouped) {
            this.groups = groups;
            this.ungrouped = ungrouped;
        }

        public Map<String, List<String>> getGroups() {
            return groups;
        }

        public List<String> getUngrouped() {
            return ungrouped;
        }
    }

    /**
     * Load and organize all API calls by group.
     */
    public GroupedCalls loadGroupedCalls() {
        Map<String, ApiCall> apiCalls = apiCallService.loadApiCalls();
        Map<String, List<String>> groups = new LinkedHashMap<>();
        List<String> ungrouped = new ArrayList<>();

        for (Map.Entry<String, ApiCall> entry : apiCalls.entrySet()) {
            String name = entry.getKey();
            String groupName = entry.getValue().getGroupName();

            if (groupName != null && !groupName.trim().isEmpty()) {
                groups.computeIfAbsent(groupName, k -> new ArrayList<>()).add(name);
            } else {
                ungrouped.add(name);
            }
        }

        return new GroupedCalls(groups, ungrouped);
    }

    /**
     * Check if a group is expanded.
     */
    public boolean isGroupExpanded(String groupName) {
        return groupExpandedState.getOrDefault(groupName, true);
    }

    /**
     * Toggle a group's expanded state.
     */
    public void toggleGroup(String groupName) {
        groupExpandedState.put(groupName, !isGroupExpanded(groupName));
    }

    /**
     * Check if any group is currently expanded.
     */
    public boolean isAnyGroupExpanded(Set<String> groupNames) {
        if (groupExpandedState.isEmpty()) {
            return true; // default state is expanded
        }
        for (String groupName : groupNames) {
            if (isGroupExpanded(groupName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Expand or collapse all groups.
     */
    public void setAllGroupsExpanded(boolean expanded) {
        Map<String, ApiCall> apiCalls = apiCallService.loadApiCalls();
        for (ApiCall call : apiCalls.values()) {
            String groupName = call.getGroupName();
            if (groupName != null && !groupName.trim().isEmpty()) {
                groupExpandedState.put(groupName, expanded);
            }
        }
    }

    /**
     * Load a specific API call by name.
     */
    public ApiCall loadApiCall(String name) {
        return apiCallService.loadApiCall(name);
    }

    /**
     * Delete an API call by name.
     */
    public void deleteApiCall(String name) throws Exception {
        apiCallService.deleteApiCall(name);
    }
}
