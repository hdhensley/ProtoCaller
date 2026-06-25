package com.overzealouspelican.controller;

import javax.swing.*;
import java.util.Map;
import com.overzealouspelican.frame.CallOutputFrame;
import com.overzealouspelican.model.ApiCall;
import com.overzealouspelican.model.ApplicationState;
import com.overzealouspelican.service.ApiCallService;
import com.overzealouspelican.service.HttpRequestExecutor;
import com.overzealouspelican.service.VariableSubstitutionService;

/**
 * Handles the execution of API calls and displaying results.
 * Single responsibility: orchestrate HTTP execution and present output.
 */
public class CallExecutionHandler {

    private final ApiCallService apiCallService;
    private final VariableSubstitutionService substitutionService;
    private final ApplicationState appState;

    public CallExecutionHandler(ApiCallService apiCallService) {
        this.apiCallService = apiCallService;
        this.substitutionService = new VariableSubstitutionService();
        this.appState = ApplicationState.getInstance();
    }

    /**
     * Execute an API call asynchronously and display the result.
     *
     * @param apiCall the API call to execute
     */
    public void execute(ApiCall apiCall) {
        appState.setStatusLoading();

        String environment = appState.getSelectedEnvironment();
        Map<String, String> environmentVariables = appState.getEnvironmentVariables();

        new Thread(() -> {
            HttpRequestExecutor.HttpCallResult result = apiCallService.executeApiCall(apiCall, environmentVariables);

            SwingUtilities.invokeLater(() -> {
                String headersDisplay = formatKeyValuePairs(apiCall.getHeaders(), environmentVariables);
                String bodyDisplay = formatKeyValuePairs(apiCall.getBody(), environmentVariables);

                CallOutputFrame outputFrame = CallOutputFrame.getInstance();
                outputFrame.displayCallOutput(
                    environment,
                    apiCall.getName(),
                    apiCall.getUrl(),
                    apiCall.getHttpMethod(),
                    headersDisplay,
                    bodyDisplay,
                    result.formatResponse(),
                    environmentVariables
                );

                if (result.isSuccess()) {
                    appState.setStatusSuccess("API call completed successfully");
                } else {
                    appState.setStatusError("API call failed");
                }
            });
        }).start();
    }

    private String formatKeyValuePairs(Map<String, String> pairs, Map<String, String> environmentVariables) {
        if (pairs == null || pairs.isEmpty()) {
            return "(None)";
        }

        StringBuilder display = new StringBuilder();
        pairs.forEach((key, value) -> {
            String resolvedKey = substitutionService.substitute(key, environmentVariables);
            String resolvedValue = substitutionService.substitute(value, environmentVariables);
            display.append(resolvedKey).append(": ").append(resolvedValue).append("\n");
        });
        return display.toString();
    }
}
