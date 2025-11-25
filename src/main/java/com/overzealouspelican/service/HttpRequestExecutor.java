package com.overzealouspelican.service;

import com.overzealouspelican.model.ApiCall;
import com.google.gson.Gson;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for executing HTTP requests.
 * Follows the Single Responsibility Principle - only responsible for HTTP execution.
 */
public class HttpRequestExecutor {

    private final HttpClientFactory clientFactory;
    private final Gson gson;

    public HttpRequestExecutor() {
        this.clientFactory = new HttpClientFactory();
        this.gson = new Gson();
    }

    /**
     * Execute an HTTP request with the given API call details
     */
    public HttpCallResult execute(ApiCall apiCall, Map<String, String> resolvedHeaders,
                                   Map<String, String> resolvedBody) {
        String originalDisableHostnameVerification = null;
        boolean modifiedSystemProperty = false;

        try {
            String url = apiCall.getUrl();

            // Build the request
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30));

            // Add headers
            for (Map.Entry<String, String> header : resolvedHeaders.entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }

            // Set the HTTP method and body
            String method = apiCall.getHttpMethod().toUpperCase();
            String bodyContent = buildBodyContent(resolvedBody);

            applyHttpMethod(requestBuilder, method, bodyContent, resolvedHeaders);

            HttpRequest request = requestBuilder.build();

            // Choose the appropriate HTTP client based on URL
            HttpClient clientToUse = clientFactory.createStandardClient();
            boolean isLocalhost = isLocalhostUrl(url);

            if (isLocalhost) {
                System.out.println("Using insecure SSL context for localhost URL");

                try {
                    verifyHostnameResolution(url);

                    // Temporarily disable SSL endpoint identification for localhost
                    synchronized (HttpRequestExecutor.class) {
                        originalDisableHostnameVerification = System.getProperty("jdk.internal.httpclient.disableHostnameVerification");
                        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
                        modifiedSystemProperty = true;
                        System.out.println("Disabled hostname verification for localhost request");
                    }

                    clientToUse = clientFactory.createInsecureClient();
                } catch (UnknownHostException e) {
                    System.out.println("Could not resolve hostname via system DNS, falling back to default client");
                    modifiedSystemProperty = false;
                }
            }

            // Execute the request
            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = clientToUse.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            return new HttpCallResult(
                response.statusCode(),
                response.body(),
                response.headers().map(),
                duration,
                null
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new HttpCallResult(
                0,
                "Error: " + e.getMessage(),
                new HashMap<>(),
                0,
                e
            );
        } finally {
            // ALWAYS restore original system property if we changed it
            if (modifiedSystemProperty) {
                synchronized (HttpRequestExecutor.class) {
                    if (originalDisableHostnameVerification != null) {
                        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", originalDisableHostnameVerification);
                    } else {
                        System.clearProperty("jdk.internal.httpclient.disableHostnameVerification");
                    }
                    System.out.println("Restored hostname verification setting");
                }
            }
        }
    }

    /**
     * Apply the HTTP method and body to the request builder
     */
    private void applyHttpMethod(HttpRequest.Builder requestBuilder, String method,
                                  String bodyContent, Map<String, String> headers) {
        switch (method) {
            case "GET":
                requestBuilder.GET();
                break;
            case "POST":
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(bodyContent));
                if (!headers.containsKey("Content-Type")) {
                    requestBuilder.header("Content-Type", "application/json");
                }
                break;
            case "PUT":
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(bodyContent));
                if (!headers.containsKey("Content-Type")) {
                    requestBuilder.header("Content-Type", "application/json");
                }
                break;
            case "DELETE":
                requestBuilder.DELETE();
                break;
            case "PATCH":
                requestBuilder.method("PATCH", HttpRequest.BodyPublishers.ofString(bodyContent));
                if (!headers.containsKey("Content-Type")) {
                    requestBuilder.header("Content-Type", "application/json");
                }
                break;
            case "HEAD":
                requestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
                break;
            case "OPTIONS":
                requestBuilder.method("OPTIONS", HttpRequest.BodyPublishers.noBody());
                break;
            default:
                requestBuilder.GET();
        }
    }

    /**
     * Check if the URL is targeting localhost
     */
    private boolean isLocalhostUrl(String url) {
        if (url == null) {
            return false;
        }
        String lowerUrl = url.toLowerCase();
        return lowerUrl.contains("localhost") || lowerUrl.contains("127.0.0.1") || lowerUrl.contains("[::1]");
    }

    /**
     * Verify hostname resolution using system DNS before making the request
     */
    private void verifyHostnameResolution(String url) throws UnknownHostException {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();

            if (host != null) {
                InetAddress[] addresses = InetAddress.getAllByName(host);
                System.out.println("Resolved " + host + " to: ");
                for (InetAddress addr : addresses) {
                    System.out.println("  - " + addr.getHostAddress());
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Failed to resolve hostname: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Build JSON body content from key-value pairs
     */
    private String buildBodyContent(Map<String, String> body) {
        if (body == null || body.isEmpty()) {
            return "";
        }
        return gson.toJson(body);
    }

    /**
     * Result object for HTTP calls
     */
    public static class HttpCallResult {
        private final int statusCode;
        private final String body;
        private final Map<String, java.util.List<String>> headers;
        private final long duration;
        private final Exception error;

        public HttpCallResult(int statusCode, String body, Map<String, java.util.List<String>> headers,
                            long duration, Exception error) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
            this.duration = duration;
            this.error = error;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public Map<String, java.util.List<String>> getHeaders() {
            return headers;
        }

        public long getDuration() {
            return duration;
        }

        public Exception getError() {
            return error;
        }

        public boolean isSuccess() {
            return error == null && statusCode >= 200 && statusCode < 300;
        }

        public String formatResponse() {
            if (error != null) {
                return "Error: " + error.getMessage() + "\n\nStack trace:\n" + getStackTraceString(error);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Status: ").append(statusCode).append("\n");
            sb.append("Duration: ").append(duration).append(" ms\n\n");
            sb.append("Headers:\n");
            for (Map.Entry<String, java.util.List<String>> entry : headers.entrySet()) {
                sb.append("  ").append(entry.getKey()).append(": ");
                sb.append(String.join(", ", entry.getValue()));
                sb.append("\n");
            }

            // Pretty-print JSON if applicable
            String formattedBody = body;
            if (isJsonResponse()) {
                formattedBody = prettyPrintJson(body);
            }

            sb.append("\nBody:\n").append(formattedBody);

            return sb.toString();
        }

        /**
         * Check if the response is JSON based on Content-Type header
         */
        private boolean isJsonResponse() {
            if (headers == null) {
                return false;
            }

            // Check Content-Type header
            for (Map.Entry<String, java.util.List<String>> entry : headers.entrySet()) {
                String headerName = entry.getKey();
                if (headerName != null && headerName.equalsIgnoreCase("content-type")) {
                    String contentType = String.join(", ", entry.getValue()).toLowerCase();
                    if (contentType.contains("application/json") ||
                        contentType.contains("application/vnd.api+json") ||
                        contentType.contains("text/json")) {
                        return true;
                    }
                }
            }

            // Fallback: try to detect JSON by structure
            if (body != null && !body.isEmpty()) {
                String trimmed = body.trim();
                return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                       (trimmed.startsWith("[") && trimmed.endsWith("]"));
            }

            return false;
        }

        /**
         * Pretty-print JSON string
         */
        private String prettyPrintJson(String json) {
            if (json == null || json.isEmpty()) {
                return json;
            }

            try {
                com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
                com.google.gson.JsonElement jsonElement = com.google.gson.JsonParser.parseString(json);
                return gson.toJson(jsonElement);
            } catch (Exception e) {
                // If parsing fails, return original
                return json;
            }
        }

        private String getStackTraceString(Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        }
    }
}

