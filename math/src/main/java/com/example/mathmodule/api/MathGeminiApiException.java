package com.example.mathmodule.api;

/** Structured Gemini HTTP failure used to display an accurate error to the user. */
/*
 * Role: AI/API class for Math, responsible for preparing requests or prompts, calling the service, and converting response data.
 * Purpose: Separating AI communication from the UI makes error handling, model or endpoint changes, and logic testing easier.
 */
public final class MathGeminiApiException extends Exception {
    private final int httpStatus;
    private final String apiStatus;

    public MathGeminiApiException(int httpStatus, String apiStatus, String message) {
        super(message == null || message.trim().isEmpty()
                ? "Gemini request failed with HTTP " + httpStatus + "."
                : message.trim());
        this.httpStatus = httpStatus;
        this.apiStatus = apiStatus == null ? "" : apiStatus.trim();
    }

    /*
     * Responsibility: Reads or loads data for `get http status` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    public String getApiStatus() {
        return apiStatus;
    }
}
