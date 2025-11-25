package com.overzealouspelican.service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for substituting environment variables in strings.
 * Follows the Single Responsibility Principle - only handles variable substitution.
 */
public class VariableSubstitutionService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * Substitute {{key}} placeholders with environment variable values
     */
    public String substitute(String input, Map<String, String> variables) {
        if (input == null || variables == null) {
            return input;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = variables.get(key);
            if (value != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(value));
            } else {
                // Keep the placeholder if no value found
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Substitute variables in a map of key-value pairs
     */
    public Map<String, String> substituteMap(Map<String, String> input, Map<String, String> variables) {
        if (input == null || variables == null) {
            return input;
        }

        Map<String, String> result = new java.util.HashMap<>();
        for (Map.Entry<String, String> entry : input.entrySet()) {
            String key = substitute(entry.getKey(), variables);
            String value = substitute(entry.getValue(), variables);
            result.put(key, value);
        }
        return result;
    }

    /**
     * Check if a string contains unresolved variables
     */
    public boolean hasUnresolvedVariables(String input) {
        if (input == null) {
            return false;
        }
        return input.contains("{{") && input.contains("}}");
    }

    /**
     * Extract unresolved variable names from a string
     */
    public java.util.List<String> getUnresolvedVariables(String input) {
        java.util.List<String> unresolved = new java.util.ArrayList<>();
        if (input == null) {
            return unresolved;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(input);
        while (matcher.find()) {
            unresolved.add(matcher.group(1));
        }
        return unresolved;
    }
}

