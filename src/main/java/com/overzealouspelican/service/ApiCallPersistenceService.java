package com.overzealouspelican.service;

import com.overzealouspelican.model.ApiCall;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for persisting API calls to JSON files on the local filesystem.
 * Follows the Single Responsibility Principle - handles only API call persistence.
 */
public class ApiCallPersistenceService {

    private static final String API_CALLS_FILE = "api-calls.json";
    private final Gson gson;
    private final StoragePathService storagePathService;

    public ApiCallPersistenceService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.storagePathService = new StoragePathService();
        ensureDataDirectoryExists();
    }

    /**
     * Ensure the data directory exists
     */
    private void ensureDataDirectoryExists() {
        try {
            Path dataDirectory = storagePathService.getDataDirectory();
            storagePathService.ensureDirectoryExists(dataDirectory);
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the path to the API calls file
     */
    private Path getApiCallsFile() {
        return storagePathService.getDataDirectory().resolve(API_CALLS_FILE);
    }

    /**
     * Load all API calls from the JSON file
     */
    public Map<String, ApiCall> loadApiCalls() {
        Path apiCallsFile = getApiCallsFile();
        if (!Files.exists(apiCallsFile)) {
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(apiCallsFile.toFile())) {
            Type type = new TypeToken<Map<String, ApiCall>>(){}.getType();
            Map<String, ApiCall> apiCalls = gson.fromJson(reader, type);
            return apiCalls != null ? apiCalls : new HashMap<>();
        } catch (IOException e) {
            System.err.println("Failed to load API calls: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * Save all API calls to the JSON file
     */
    public void saveApiCalls(Map<String, ApiCall> apiCalls) throws IOException {
        Path apiCallsFile = getApiCallsFile();
        try (FileWriter writer = new FileWriter(apiCallsFile.toFile())) {
            gson.toJson(apiCalls, writer);
            System.out.println("Saved API calls to: " + apiCallsFile);
        }
    }

    /**
     * Save a single API call
     */
    public void saveApiCall(ApiCall apiCall) throws IOException {
        Map<String, ApiCall> apiCalls = loadApiCalls();
        apiCalls.put(apiCall.getName(), apiCall);
        saveApiCalls(apiCalls);
    }

    /**
     * Load a specific API call by name
     */
    public ApiCall loadApiCall(String name) {
        Map<String, ApiCall> apiCalls = loadApiCalls();
        return apiCalls.get(name);
    }

    /**
     * Delete an API call
     */
    public void deleteApiCall(String name) throws IOException {
        Map<String, ApiCall> apiCalls = loadApiCalls();
        apiCalls.remove(name);
        saveApiCalls(apiCalls);
    }

    /**
     * Check if an API call exists
     */
    public boolean apiCallExists(String name) {
        Map<String, ApiCall> apiCalls = loadApiCalls();
        return apiCalls.containsKey(name);
    }

    /**
     * Get the path to the API calls file
     */
    public String getApiCallsFilePath() {
        return getApiCallsFile().toString();
    }
}

