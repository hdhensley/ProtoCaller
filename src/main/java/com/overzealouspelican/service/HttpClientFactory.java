package com.overzealouspelican.service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.security.cert.X509Certificate;
import java.time.Duration;

/**
 * Factory for creating HTTP clients with different configurations.
 * Follows the Single Responsibility Principle - only responsible for creating HTTP clients.
 */
public class HttpClientFactory {

    /**
     * Create a standard HTTP client
     */
    public HttpClient createStandardClient() {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }

    /**
     * Create an HTTP client that trusts all certificates (for localhost development only)
     * Uses system DNS resolver to support custom hosts file entries
     * Disables hostname verification for self-signed certificates
     */
    public HttpClient createInsecureClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create custom SSLParameters that disable endpoint identification (hostname verification)
            javax.net.ssl.SSLParameters sslParams = sslContext.getDefaultSSLParameters();
            sslParams.setEndpointIdentificationAlgorithm(""); // Empty string disables hostname verification

            // Create HTTP client with custom SSL context that disables all verification
            return HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(sslParams)
                .connectTimeout(Duration.ofSeconds(30))
                .proxy(ProxySelector.getDefault()) // Use system proxy settings
                .build();

        } catch (Exception e) {
            System.err.println("Failed to create insecure HTTP client: " + e.getMessage());
            e.printStackTrace();
            // Fall back to the default secure client
            return createStandardClient();
        }
    }
}

