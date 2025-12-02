package com.overzealouspelican.util;

import com.overzealouspelican.model.ApiCall;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing cURL commands into ApiCall objects.
 * Follows the Single Responsibility Principle - only handles cURL command parsing.
 */
public class CurlParser {

    /**
     * Parse a cURL command string into an ApiCall object
     */
    public static ApiCall parseCurl(String curlCommand) throws IllegalArgumentException {
        if (curlCommand == null || curlCommand.trim().isEmpty()) {
            throw new IllegalArgumentException("cURL command cannot be empty");
        }

        // Remove line continuations and extra whitespace
        String normalized = curlCommand
            .replaceAll("\\\\\\s*\\n\\s*", " ")
            .replaceAll("\\s+", " ")
            .trim();

        // Extract URL
        String url = extractUrl(normalized);
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Could not extract URL from cURL command");
        }

        // Extract HTTP method (default to GET)
        String method = extractMethod(normalized);

        // Extract headers
        Map<String, String> headers = extractHeaders(normalized);

        // Extract body data
        Map<String, String> body = extractBody(normalized);

        // Create ApiCall
        ApiCall apiCall = new ApiCall();
        apiCall.setUrl(url);
        apiCall.setHttpMethod(method);
        apiCall.setHeaders(headers);
        apiCall.setBody(body);

        return apiCall;
    }

    /**
     * Extract URL from cURL command
     */
    private static String extractUrl(String curl) {
        // Match curl 'url' or curl "url" or curl url
        Pattern pattern = Pattern.compile("curl\\s+['\"]([^'\"]+)['\"]|curl\\s+([^\\s-]+)");
        Matcher matcher = pattern.matcher(curl);

        if (matcher.find()) {
            String url = matcher.group(1);
            if (url == null) {
                url = matcher.group(2);
            }
            return url;
        }

        return null;
    }

    /**
     * Extract HTTP method from cURL command
     */
    private static String extractMethod(String curl) {
        // Look for -X METHOD or --request METHOD
        Pattern pattern = Pattern.compile("-X\\s+([A-Z]+)|--request\\s+([A-Z]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(curl);

        if (matcher.find()) {
            String method = matcher.group(1);
            if (method == null) {
                method = matcher.group(2);
            }
            return method.toUpperCase();
        }

        // Check for -d, --data, or --data-raw (implies POST)
        if (curl.contains("-d ") || curl.contains("--data") || curl.contains("--data-raw")) {
            return "POST";
        }

        // Default to GET
        return "GET";
    }

    /**
     * Extract headers from cURL command
     */
    private static Map<String, String> extractHeaders(String curl) {
        Map<String, String> headers = new HashMap<>();

        // Match -H 'header' or -H "header" or --header
        Pattern pattern = Pattern.compile("-H\\s+['\"]([^'\"]+)['\"]|--header\\s+['\"]([^'\"]+)['\"]");
        Matcher matcher = pattern.matcher(curl);

        while (matcher.find()) {
            String header = matcher.group(1);
            if (header == null) {
                header = matcher.group(2);
            }

            if (header != null) {
                // Split header into key and value
                int colonIndex = header.indexOf(':');
                if (colonIndex > 0) {
                    String key = header.substring(0, colonIndex).trim();
                    String value = header.substring(colonIndex + 1).trim();
                    headers.put(key, value);
                }
            }
        }

        return headers;
    }

    /**
     * Extract body data from cURL command
     */
    private static Map<String, String> extractBody(String curl) {
        Map<String, String> body = new HashMap<>();

        // Match -d 'data', --data 'data', or --data-raw 'data'
        Pattern pattern = Pattern.compile("(?:-d|--data|--data-raw)\\s+'([^']+)'|(?:-d|--data|--data-raw)\\s+\"([^\"]+)\"|(?:-d|--data|--data-raw)\\s+([^\\s-]+)");
        Matcher matcher = pattern.matcher(curl);

        if (matcher.find()) {
            String data = matcher.group(1);
            if (data == null) {
                data = matcher.group(2);
            }
            if (data == null) {
                data = matcher.group(3);
            }

            if (data != null) {
                // Try to parse as JSON or form data
                if (data.startsWith("{") && data.endsWith("}")) {
                    // Simple JSON parsing - extract key-value pairs
                    parseJsonToMap(data, body);
                } else {
                    // Store as raw data with a generic key
                    body.put("data", data);
                }
            }
        }

        return body;
    }

    /**
     * Simple JSON parsing to extract key-value pairs
     */
    private static void parseJsonToMap(String json, Map<String, String> map) {
        // Remove outer braces
        json = json.substring(1, json.length() - 1).trim();

        // Enhanced pattern to handle:
        // 1. "key": "value"
        // 2. "key": null
        // 3. "key": 123 (numbers)
        // 4. "key": true/false (booleans)
        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"([^\"]*)\"|null|true|false|[0-9]+(?:\\.[0-9]+)?)");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            String key = matcher.group(1);
            String fullValue = matcher.group(2);
            String quotedValue = matcher.group(3);

            if (key != null && fullValue != null) {
                String value;
                if (quotedValue != null) {
                    // It's a quoted string value
                    value = quotedValue;
                } else {
                    // It's null, boolean, or numeric - use the full value
                    value = fullValue;
                }
                map.put(key.trim(), value);
            }
        }
    }

    /**
     * Generate a suggested name for the API call based on the URL
     * @deprecated Use ApiCallNameGenerator.generateFromUrl() instead
     */
    @Deprecated
    public static String generateName(String url) {
        return ApiCallNameGenerator.generateFromUrl(url);
    }
}
