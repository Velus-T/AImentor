package com.example.aimentor.science.domain.service;

/**
 * Transport abstraction used by Science AI features.
 * Domain-facing services do not depend directly on HttpURLConnection or Gemini JSON details.
 */
public interface GenerativeAiClient {
    String generateText(String apiKey,
                        String modelName,
                        String prompt,
                        byte[] imageBytes,
                        String imageMimeType,
                        double temperature,
                        int maxOutputTokens) throws Exception;
}
