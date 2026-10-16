package com.brightpath.science.data.remote;

/** Structured Gemini HTTP failure used to show an accurate, user-friendly error. */
public final class GeminiApiException extends Exception {
    private final int httpStatus;
    private final String apiStatus;

        /*
 * Responsibility: Initializes GeminiApiException and receives or prepares the dependencies/state required by this AI/network or fallback service.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public GeminiApiException(int httpStatus, String apiStatus, String message) {
        super(message == null || message.trim().isEmpty()
                ? "Gemini request failed with HTTP " + httpStatus + "."
                : message.trim());
        this.httpStatus = httpStatus;
        this.apiStatus = apiStatus == null ? "" : apiStatus.trim();
    }

        /*
 * Responsibility: Retrieves the data/state required by `getHttpStatus` from the appropriate AI/network or fallback service.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getHttpStatus() {
        return httpStatus;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getApiStatus` from the appropriate AI/network or fallback service.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getApiStatus() {
        return apiStatus;
    }
}
