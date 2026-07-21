package com.example.aimentor.science.domain.service;

/**
 * Abstraction for the Science module's AI connection settings.
 * The API client depends on this interface instead of Android preferences directly (DIP).
 */
public interface ScienceApiSettings {
    String getApiKey();
    String getModelName();
    boolean isConfigured();
    void save(String apiKey, String modelName);
    void clear();
}
