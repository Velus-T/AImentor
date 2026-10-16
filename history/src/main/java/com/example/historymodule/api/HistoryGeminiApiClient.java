package com.example.historymodule.api;

import android.util.Base64;

import com.example.historymodule.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Gemini REST client used only by History Chat and History Quiz.
 * The History module keeps its own API key and model configuration.
 */
/*
 * Role: AI/API class for History, responsible for preparing requests or prompts, calling the service, and converting response data.
 * Purpose: Separating AI communication from the UI makes error handling, model or endpoint changes, and logic testing easier.
 */
public final class HistoryGeminiApiClient {
    private static final int CONNECT_TIMEOUT_MS = 20_000;
    private static final int READ_TIMEOUT_MS = 120_000;
    private static final int MAX_ATTEMPTS = 4;

    private final String apiKey;
    private final String modelName;

    /*
     * Responsibility: Initializes the `HistoryGeminiApiClient` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public HistoryGeminiApiClient() {
        this(BuildConfig.HISTORY_GEMINI_API_KEY, BuildConfig.HISTORY_GEMINI_MODEL);
    }

    public HistoryGeminiApiClient(String apiKey, String modelName) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.modelName = modelName == null || modelName.trim().isEmpty()
                ? "gemini-flash-latest"
                : modelName.trim();
    }

    /*
     * Responsibility: Validates the conditions or rules handled by `is configured` and returns the result to the caller.
     * Purpose: A dedicated validation method keeps rules consistent and easier to test.
     */
    public boolean isConfigured() {
        return !apiKey.isEmpty();
    }

    public String getModelName() {
        return modelName;
    }

    /*
     * Responsibility: Generates data or content for `generate text` based on the input and module rules.
     * Purpose: Keeping content generation separate makes the strategy easier to change without affecting presentation logic.
     */
    public Result generateText(String prompt,
                               byte[] imageBytes,
                               String imageMimeType,
                               double temperature,
                               int maxOutputTokens) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException(
                    "Chưa cấu hình HISTORY_GEMINI_API_KEY trong api.properties.");
        }
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt không được để trống.");
        }

        Exception lastError = null;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            HttpURLConnection connection = null;
            try {
                String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                        + modelName + ":generateContent";
                connection = (HttpURLConnection) new URL(endpoint).openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(READ_TIMEOUT_MS);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("x-goog-api-key", apiKey);

                byte[] body = buildRequest(
                        prompt.trim(),
                        imageBytes,
                        imageMimeType,
                        temperature,
                        maxOutputTokens
                ).toString().getBytes(StandardCharsets.UTF_8);

                connection.setFixedLengthStreamingMode(body.length);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(body);
                }

                int statusCode = connection.getResponseCode();
                String responseBody = readResponse(connection, statusCode);
                if (statusCode >= 200 && statusCode < 300) {
                    return parseResponse(responseBody);
                }

                HistoryGeminiApiException apiError = createApiException(statusCode, responseBody);
                lastError = apiError;

                if (statusCode == 429 || (statusCode >= 500 && statusCode <= 599)) {
                    if (attempt + 1 < MAX_ATTEMPTS) {
                        Thread.sleep(Math.min(6_000L, 1_000L * (attempt + 1L)));
                        continue;
                    }
                }
                throw apiError;
            } catch (IOException networkError) {
                lastError = networkError;
                if (attempt + 1 < MAX_ATTEMPTS) {
                    Thread.sleep(Math.min(4_000L, 750L * (attempt + 1L)));
                    continue;
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        throw new IllegalStateException("Không thể kết nối Gemini sau nhiều lần thử.", lastError);
    }

    /*
     * Responsibility: Creates the object or data structure for `build request` using the class's consistent creation rules.
     * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
     */
    private JSONObject buildRequest(String prompt,
                                    byte[] imageBytes,
                                    String imageMimeType,
                                    double temperature,
                                    int maxOutputTokens) throws Exception {
        JSONArray parts = new JSONArray();

        if (imageBytes != null && imageBytes.length > 0) {
            String mimeType = imageMimeType == null || imageMimeType.trim().isEmpty()
                    ? "image/jpeg"
                    : imageMimeType.trim();
            JSONObject inlineData = new JSONObject()
                    .put("mime_type", mimeType)
                    .put("data", Base64.encodeToString(imageBytes, Base64.NO_WRAP));
            parts.put(new JSONObject().put("inline_data", inlineData));
        }

        parts.put(new JSONObject().put("text", prompt));

        JSONObject userContent = new JSONObject()
                .put("role", "user")
                .put("parts", parts);

        JSONObject generationConfig = new JSONObject()
                .put("temperature", Math.max(0.0, Math.min(1.0, temperature)))
                .put("maxOutputTokens", Math.max(64, Math.min(8192, maxOutputTokens)));

        return new JSONObject()
                .put("contents", new JSONArray().put(userContent))
                .put("generationConfig", generationConfig);
    }

    /*
     * Responsibility: Converts or normalizes data in `parse response` into the required format.
     * Purpose: A centralized conversion point keeps formatting consistent and makes invalid data easier to handle.
     */
    private Result parseResponse(String responseBody) throws Exception {
        JSONObject root = new JSONObject(responseBody);
        JSONArray candidates = root.optJSONArray("candidates");
        String finishReason = "";
        StringBuilder answer = new StringBuilder();

        if (candidates != null && candidates.length() > 0) {
            JSONObject candidate = candidates.optJSONObject(0);
            if (candidate != null) {
                finishReason = candidate.optString("finishReason", "");
                JSONObject content = candidate.optJSONObject("content");
                JSONArray parts = content == null ? null : content.optJSONArray("parts");
                if (parts != null) {
                    for (int i = 0; i < parts.length(); i++) {
                        JSONObject part = parts.optJSONObject(i);
                        String text = part == null ? "" : part.optString("text", "");
                        if (!text.trim().isEmpty()) {
                            if (answer.length() > 0) answer.append('\n');
                            answer.append(text.trim());
                        }
                    }
                }
            }
        }

        String result = answer.toString().trim();
        if (result.isEmpty()) {
            JSONObject feedback = root.optJSONObject("promptFeedback");
            String blockReason = feedback == null ? "" : feedback.optString("blockReason", "");
            throw new IllegalStateException(blockReason.isEmpty()
                    ? "Gemini trả về nội dung trống."
                    : "Gemini từ chối yêu cầu: " + blockReason);
        }

        JSONObject usage = root.optJSONObject("usageMetadata");
        int promptTokens = usage == null ? 0 : usage.optInt("promptTokenCount", 0);
        int candidateTokens = usage == null ? 0 : usage.optInt("candidatesTokenCount", 0);
        int totalTokens = usage == null ? 0 : usage.optInt("totalTokenCount", 0);

        return new Result(result, finishReason, promptTokens, candidateTokens, totalTokens);
    }

    /*
     * Responsibility: Reads or loads data for `read response` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    private String readResponse(HttpURLConnection connection, int statusCode) throws Exception {
        InputStream stream = statusCode >= 200 && statusCode < 300
                ? connection.getInputStream()
                : connection.getErrorStream();
        if (stream == null) return "";

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
            return result.toString();
        }
    }

    /*
     * Responsibility: Creates the object or data structure for `create api exception` using the class's consistent creation rules.
     * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
     */
    private HistoryGeminiApiException createApiException(int statusCode, String responseBody) {
        String message = responseBody == null ? "" : responseBody.trim();
        String apiStatus = "";
        try {
            JSONObject error = new JSONObject(message).optJSONObject("error");
            if (error != null) {
                message = error.optString("message", message);
                apiStatus = error.optString("status", "");
            }
        } catch (Exception ignored) {
            // Preserve raw response when it is not valid JSON.
        }
        return new HistoryGeminiApiException(statusCode, apiStatus, message);
    }

    public static final class Result {
        private final String text;
        private final String finishReason;
        private final int promptTokens;
        private final int candidateTokens;
        private final int totalTokens;

        /*
         * Responsibility: Performs the `result` responsibility of `HistoryGeminiApiClient` and returns or updates the result for the caller.
         * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
         */
        public Result(String text,
                      String finishReason,
                      int promptTokens,
                      int candidateTokens,
                      int totalTokens) {
            this.text = text == null ? "" : text.trim();
            this.finishReason = finishReason == null ? "" : finishReason.trim();
            this.promptTokens = promptTokens;
            this.candidateTokens = candidateTokens;
            this.totalTokens = totalTokens;
        }

        /*
         * Responsibility: Reads or loads data for `get text` from the appropriate data source.
         * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
         */
        public String getText() {
            return text;
        }

        public String getFinishReason() {
            return finishReason;
        }

        /*
         * Responsibility: Performs the `was truncated` responsibility of `HistoryGeminiApiClient` and returns or updates the result for the caller.
         * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
         */
        public boolean wasTruncated() {
            return "MAX_TOKENS".equalsIgnoreCase(finishReason);
        }

        public int getPromptTokens() {
            return promptTokens;
        }

        /*
         * Responsibility: Reads or loads data for `get candidate tokens` from the appropriate data source.
         * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
         */
        public int getCandidateTokens() {
            return candidateTokens;
        }

        public int getTotalTokens() {
            return totalTokens;
        }
    }
}
