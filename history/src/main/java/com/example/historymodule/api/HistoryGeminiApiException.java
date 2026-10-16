package com.example.historymodule.api;

/** HTTP error returned by the Gemini API used only by the History module. */
/*
 * Role: AI/API class for History, responsible for preparing requests or prompts, calling the service, and converting response data.
 * Purpose: Separating AI communication from the UI makes error handling, model or endpoint changes, and logic testing easier.
 */
public final class HistoryGeminiApiException extends Exception {
    private final int httpStatus;
    private final String apiStatus;

    public HistoryGeminiApiException(int httpStatus, String apiStatus, String message) {
        super(message == null ? "" : message.trim());
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
