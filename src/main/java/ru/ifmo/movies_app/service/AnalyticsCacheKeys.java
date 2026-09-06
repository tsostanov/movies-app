package ru.ifmo.movies_app.service;

public final class AnalyticsCacheKeys {

    private static final int MAX_NAME_SEARCH_LENGTH = 100;

    private AnalyticsCacheKeys() {
    }

    public static String nameSearch(String substring) {
        if (substring == null || substring.isBlank()) {
            throw new IllegalArgumentException("Search substring must not be blank");
        }
        String normalized = substring.trim();
        if (normalized.length() > MAX_NAME_SEARCH_LENGTH) {
            throw new IllegalArgumentException("Search substring must be at most %d characters".formatted(MAX_NAME_SEARCH_LENGTH));
        }
        return normalized;
    }
}
