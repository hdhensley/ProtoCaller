package com.overzealouspelican.util;

/**
 * Utility for generating API call names from URLs.
 * Follows the Single Responsibility Principle - only handles name generation.
 */
public class ApiCallNameGenerator {

    /**
     * Generate a suggested name for the API call based on the URL
     */
    public static String generateFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "Imported cURL";
        }

        try {
            // Extract the path from URL
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("https?://[^/]+/(.+?)(?:\\?|$)");
            java.util.regex.Matcher matcher = pattern.matcher(url);

            if (matcher.find()) {
                String path = matcher.group(1);
                // Take the last segment of the path
                String[] segments = path.split("/");
                if (segments.length > 0) {
                    String lastSegment = segments[segments.length - 1];
                    return formatNameFromSegment(lastSegment);
                }
            }

            // Fallback to domain name
            pattern = java.util.regex.Pattern.compile("https?://([^/]+)");
            matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            // Ignore and return default
        }

        return "Imported cURL";
    }

    /**
     * Format a name from a URL segment by converting kebab-case or snake_case to Title Case
     */
    private static String formatNameFromSegment(String segment) {
        // Convert kebab-case or snake_case to Title Case
        String nameWithSpaces = segment.replaceAll("[-_]", " ");

        // Capitalize first letter of each word
        StringBuilder titleCase = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : nameWithSpaces.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                titleCase.append(c);
            } else if (capitalizeNext) {
                titleCase.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                titleCase.append(c);
            }
        }

        return titleCase.toString();
    }
}

