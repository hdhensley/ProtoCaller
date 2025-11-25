package com.overzealouspelican.service;

import com.overzealouspelican.model.ApiCall;
import com.overzealouspelican.service.HttpRequestExecutor.HttpCallResult;

import java.io.IOException;
import java.util.Map;

/**
 * Service for managing API calls.
 * Follows the Single Responsibility Principle - orchestrates between specialized services.
 * This is a facade that delegates to:
 * - ApiCallPersistenceService for data persistence
 * - VariableSubstitutionService for variable resolution
 * - HttpRequestExecutor for executing HTTP requests
 */
public class ApiCallService {

    private final ApiCallPersistenceService persistenceService;
    private final VariableSubstitutionService substitutionService;
    private final HttpRequestExecutor requestExecutor;

    public ApiCallService() {
        this.persistenceService = new ApiCallPersistenceService();
        this.substitutionService = new VariableSubstitutionService();
        this.requestExecutor = new HttpRequestExecutor();
    }

    /**
     * Load all API calls from storage
     */
    public Map<String, ApiCall> loadApiCalls() {
        return persistenceService.loadApiCalls();
    }

    /**
     * Save all API calls to storage
     */
    public void saveApiCalls(Map<String, ApiCall> apiCalls) throws IOException {
        persistenceService.saveApiCalls(apiCalls);
    }

    /**
     * Save a single API call
     */
    public void saveApiCall(ApiCall apiCall) throws IOException {
        persistenceService.saveApiCall(apiCall);
    }

    /**
     * Load a specific API call by name
     */
    public ApiCall loadApiCall(String name) {
        return persistenceService.loadApiCall(name);
    }

    /**
     * Delete an API call
     */
    public void deleteApiCall(String name) throws IOException {
        persistenceService.deleteApiCall(name);
    }

    /**
     * Check if an API call exists
     */
    public boolean apiCallExists(String name) {
        return persistenceService.apiCallExists(name);
    }

    /**
     * Get the path to the API calls file
     */
    public String getApiCallsFilePath() {
        return persistenceService.getApiCallsFilePath();
    }

    /**
     * Execute an API call with environment variable substitution
     */
    public HttpCallResult executeApiCall(ApiCall apiCall, Map<String, String> environmentVariables) {
        try {
            // Log environment variables for debugging
            System.out.println("Environment variables available: " + environmentVariables);

            // Substitute environment variables in URL
            String resolvedUrl = substitutionService.substitute(apiCall.getUrl(), environmentVariables);
            System.out.println("Original URL: " + apiCall.getUrl());
            System.out.println("Resolved URL: " + resolvedUrl);

            // Check if URL still contains unresolved variables
            if (substitutionService.hasUnresolvedVariables(resolvedUrl)) {
                java.util.List<String> missingVars = substitutionService.getUnresolvedVariables(resolvedUrl);
                throw new IllegalArgumentException(
                    "URL contains unresolved environment variables: " + String.join(", ", missingVars) +
                    "\nAvailable variables: " + environmentVariables.keySet()
                );
            }

            // Substitute environment variables in headers and body
            Map<String, String> resolvedHeaders = substitutionService.substituteMap(apiCall.getHeaders(), environmentVariables);
            Map<String, String> resolvedBody = substitutionService.substituteMap(apiCall.getBody(), environmentVariables);

            // Create a temporary ApiCall with resolved values
            ApiCall resolvedApiCall = new ApiCall();
            resolvedApiCall.setUrl(resolvedUrl);
            resolvedApiCall.setHttpMethod(apiCall.getHttpMethod());
            resolvedApiCall.setHeaders(resolvedHeaders);
            resolvedApiCall.setBody(resolvedBody);

            // Execute the request and return the result directly
            return requestExecutor.execute(resolvedApiCall, resolvedHeaders, resolvedBody);

        } catch (Exception e) {
            e.printStackTrace();
            return new HttpCallResult(
                0,
                "Error: " + e.getMessage(),
                new java.util.HashMap<>(),
                0,
                e
            );
        }
    }
}
