package com.example.mathmodule.api;

import com.example.mathmodule.BuildConfig;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;

/**
 * Gemini REST client shared by Math Chat and Math Quiz.
 * The API-key handling follows the same approach as Science Module:
 * one central location, backup-key rotation, retry and structured errors.
 */
/*
 * Role: AI/API class for Math, responsible for preparing requests or prompts, calling the service, and converting response data.
 * Purpose: Separating AI communication from the UI makes error handling, model or endpoint changes, and logic testing easier.
 */
public final class MathGeminiApiClient {
    // Keep Math and Science on the same configured Gemini account/key pool.
    // Optional backup keys can be pasted into key 2 and key 3.
    private static final String API_KEY_1 = BuildConfig.MATH_GEMINI_API_KEY_1;
    private static final String API_KEY_2 = BuildConfig.MATH_GEMINI_API_KEY_2;
    private static final String API_KEY_3 = BuildConfig.MATH_GEMINI_API_KEY_3;

    private static final String[] CONFIGURED_KEYS = buildKeyList();
    private static final long[] DISABLED_UNTIL = new long[CONFIGURED_KEYS.length];
    private static int currentKeyIndex = 0;
    private static String currentModel = BuildConfig.MATH_GEMINI_MODEL;
    private static String currentStatus = "GOOD";

    private static final int CONNECT_TIMEOUT_MS = 20_000;
    private static final int READ_TIMEOUT_MS = 120_000;
    private static final int MAX_ATTEMPTS = 6;
    private static final long RATE_LIMIT_COOLDOWN_MS = 60_000L;

    public static final class Result {
        public final String text;
        public final int promptTokens;
        public final int candidateTokens;
        public final int totalTokens;
        public final String status;
        public final String finishReason;

        /*
         * Responsibility: Performs the `result` responsibility of `MathGeminiApiClient` and returns or updates the result for the caller.
         * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
         */
        public Result(String text, int promptTokens, int candidateTokens, int totalTokens,
                      String status, String finishReason) {
            this.text = text;
            this.promptTokens = promptTokens;
            this.candidateTokens = candidateTokens;
            this.totalTokens = totalTokens;
            this.status = status;
            this.finishReason = finishReason == null ? "" : finishReason.trim();
        }

        /*
         * Responsibility: Performs the `was truncated` responsibility of `MathGeminiApiClient` and returns or updates the result for the caller.
         * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
         */
        public boolean wasTruncated() {
            return "MAX_TOKENS".equalsIgnoreCase(finishReason);
        }

        @Override
        public String toString() {
            return text;
        }
    }

    /*
     * Responsibility: Reads or loads data for `get model name` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public String getModelName() {
        return currentModel;
    }

    public void setModelName(String modelName) {
        if (modelName != null && !modelName.trim().isEmpty()) {
            currentModel = modelName.trim();
        }
    }

    /*
     * Responsibility: Reads or loads data for `get status` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public String getStatus() {
        return currentStatus;
    }

    public synchronized String getActiveKeyLabel() {
        return CONFIGURED_KEYS.length == 0
                ? "no key"
                : "key " + (currentKeyIndex + 1) + "/" + CONFIGURED_KEYS.length;
    }

    /*
     * Responsibility: Validates the conditions or rules handled by `is configured` and returns the result to the caller.
     * Purpose: A dedicated validation method keeps rules consistent and easier to test.
     */
    public boolean isConfigured() {
        return CONFIGURED_KEYS.length > 0;
    }

    public Result testConnection() throws Exception {
        return generateText("Reply with exactly: OK", null, null, 0.0, 32);
    }

    /*
     * Responsibility: Generates data or content for `generate text` based on the input and module rules.
     * Purpose: Keeping content generation separate makes the strategy easier to change without affecting presentation logic.
     */
    public Result generateText(String prompt, byte[] imageBytes, String imageMimeType,
                               double temperature, int maxOutputTokens) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("No Gemini API key is configured.");
        }
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be empty.");
        }

        Exception lastError = null;
        int maximumAttempts = Math.max(MAX_ATTEMPTS, CONFIGURED_KEYS.length * 2);

        for (int attempt = 0; attempt < maximumAttempts; attempt++) {
            int keyIndex = nextAvailableKeyIndex();
            if (keyIndex < 0) {
                long wait = shortestCooldown();
                if (wait > 0 && wait <= 5_000L) {
                    Thread.sleep(wait);
                    continue;
                }
                throw new IllegalStateException(
                        "All Gemini API keys are temporarily unavailable.", lastError);
            }

            String apiKey = CONFIGURED_KEYS[keyIndex];
            HttpURLConnection connection = null;
            try {
                String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                        + currentModel + ":generateContent";
                connection = (HttpURLConnection) new URL(endpoint).openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(READ_TIMEOUT_MS);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("x-goog-api-key", apiKey);

                byte[] requestBytes = buildRequest(
                        prompt.trim(), imageBytes, imageMimeType,
                        temperature, maxOutputTokens
                ).toString().getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(requestBytes.length);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(requestBytes);
                }

                int statusCode = connection.getResponseCode();
                String responseBody = readResponse(connection, statusCode);

                if (statusCode >= 200 && statusCode < 300) {
                    synchronized (MathGeminiApiClient.class) {
                        currentKeyIndex = keyIndex;
                        DISABLED_UNTIL[keyIndex] = 0L;
                    }
                    currentStatus = attempt == 0 ? "GOOD" : "MEDIUM";
                    return parseFullResponse(responseBody);
                }

                MathGeminiApiException apiError = createApiException(statusCode, responseBody);
                lastError = apiError;
                currentStatus = "BUSY";

                if (statusCode == 401 || statusCode == 403) {
                    disableKey(keyIndex, Long.MAX_VALUE);
                    continue;
                }
                if (statusCode == 429) {
                    disableKey(keyIndex, System.currentTimeMillis() + RATE_LIMIT_COOLDOWN_MS);
                    continue;
                }
                if (statusCode >= 500 && statusCode <= 599) {
                    Thread.sleep(Math.min(8_000L, 1_000L * (attempt + 1L)));
                    continue;
                }
                throw apiError;
            } catch (IOException networkError) {
                lastError = networkError;
                currentStatus = "BUSY";
                if (attempt + 1 < maximumAttempts) {
                    Thread.sleep(Math.min(4_000L, 750L * (attempt + 1L)));
                    continue;
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        throw new IllegalStateException(
                "Gemini request failed after multiple attempts.", lastError);
    }

    /*
     * Responsibility: Performs the `to user message` responsibility of `MathGeminiApiClient` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    public static String toUserMessage(Throwable error, String fallback) {
        Throwable cause = findImportantCause(error);
        if (cause instanceof MathGeminiApiException) {
            int status = ((MathGeminiApiException) cause).getHttpStatus();
            if (status == 401 || status == 403) {
                return "Gemini API key không hợp lệ, đã hết hiệu lực hoặc đang bị giới hạn.";
            }
            if (status == 404) {
                return "Không tìm thấy Gemini model đã cấu hình.";
            }
            if (status == 429) {
                return "Gemini đã đạt giới hạn lượt gọi. Vui lòng thử lại sau.";
            }
            if (status >= 500) {
                return "Gemini đang bận hoặc tạm thời không khả dụng.";
            }
            String detail = safeMessage(cause);
            return "Gemini trả về lỗi HTTP " + status
                    + (detail.isEmpty() ? "." : ": " + detail);
        }
        if (cause instanceof UnknownHostException) {
            return "Không thể kết nối máy chủ Gemini. Hãy kiểm tra Internet hoặc DNS.";
        }
        if (cause instanceof SocketTimeoutException) {
            return "Gemini phản hồi quá lâu. Hãy thử câu hỏi ngắn hơn hoặc ảnh nhỏ hơn.";
        }
        if (cause instanceof SSLException) {
            return "Kết nối bảo mật tới Gemini thất bại.";
        }
        String detail = safeMessage(cause);
        return detail.isEmpty() ? fallback : fallback + "\nChi tiết: " + detail;
    }

    /*
     * Responsibility: Reads or loads data for `find important cause` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    private static Throwable findImportantCause(Throwable error) {
        Throwable current = error;
        Throwable last = error;
        while (current != null) {
            last = current;
            if (current instanceof MathGeminiApiException
                    || current instanceof UnknownHostException
                    || current instanceof SocketTimeoutException
                    || current instanceof SSLException) {
                return current;
            }
            current = current.getCause();
        }
        return last;
    }

    /*
     * Responsibility: Performs the `safe message` responsibility of `MathGeminiApiClient` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private static String safeMessage(Throwable error) {
        if (error == null || error.getMessage() == null) {
            return "";
        }
        return error.getMessage().trim();
    }

    /*
     * Responsibility: Creates the object or data structure for `build key list` using the class's consistent creation rules.
     * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
     */
    private static String[] buildKeyList() {
        List<String> values = new ArrayList<>();
        addUniqueKey(values, API_KEY_1);
        addUniqueKey(values, API_KEY_2);
        addUniqueKey(values, API_KEY_3);
        return values.toArray(new String[0]);
    }

    /*
     * Responsibility: Saves or adds data for `add unique key` to the appropriate storage or collection.
     * Purpose: Centralizing write operations helps maintain data consistency and handle storage errors.
     */
    private static void addUniqueKey(List<String> keys, String value) {
        if (value == null) {
            return;
        }
        String key = value.trim();
        if (!key.isEmpty() && !keys.contains(key)) {
            keys.add(key);
        }
    }

    /*
     * Responsibility: Performs the `next available key index` responsibility of `MathGeminiApiClient` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private static synchronized int nextAvailableKeyIndex() {
        if (CONFIGURED_KEYS.length == 0) {
            return -1;
        }
        long now = System.currentTimeMillis();
        for (int offset = 0; offset < CONFIGURED_KEYS.length; offset++) {
            int index = (currentKeyIndex + offset) % CONFIGURED_KEYS.length;
            if (DISABLED_UNTIL[index] <= now) {
                currentKeyIndex = index;
                return index;
            }
        }
        return -1;
    }

    /*
     * Responsibility: Performs the `disable key` responsibility of `MathGeminiApiClient` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private static synchronized void disableKey(int index, long until) {
        if (index >= 0 && index < DISABLED_UNTIL.length) {
            DISABLED_UNTIL[index] = until;
            if (CONFIGURED_KEYS.length > 0) {
                currentKeyIndex = (index + 1) % CONFIGURED_KEYS.length;
            }
        }
    }

    /*
     * Responsibility: Performs the `shortest cooldown` responsibility of `MathGeminiApiClient` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private static synchronized long shortestCooldown() {
        long now = System.currentTimeMillis();
        long shortest = Long.MAX_VALUE;
        for (long value : DISABLED_UNTIL) {
            if (value > now && value != Long.MAX_VALUE) {
                shortest = Math.min(shortest, value - now);
            }
        }
        return shortest == Long.MAX_VALUE ? -1L : shortest;
    }

    /*
     * Responsibility: Converts or normalizes data in `parse full response` into the required format.
     * Purpose: A centralized conversion point keeps formatting consistent and makes invalid data easier to handle.
     */
    private Result parseFullResponse(String responseBody) throws Exception {
        JSONObject root = new JSONObject(responseBody);
        StringBuilder answer = new StringBuilder();
        JSONArray candidates = root.optJSONArray("candidates");
        String finishReason = "";
        if (candidates != null && candidates.length() > 0) {
            JSONObject firstCandidate = candidates.getJSONObject(0);
            finishReason = firstCandidate.optString("finishReason", "");
            JSONObject content = firstCandidate.optJSONObject("content");
            JSONArray parts = content == null ? null : content.optJSONArray("parts");
            if (parts != null) {
                for (int i = 0; i < parts.length(); i++) {
                    String text = parts.getJSONObject(i).optString("text", "");
                    if (!text.isEmpty()) {
                        if (answer.length() > 0) {
                            answer.append('\n');
                        }
                        answer.append(text);
                    }
                }
            }
        }
        String text = answer.toString().trim();
        if (text.isEmpty()) {
            throw new IllegalStateException("Gemini returned an empty answer.");
        }

        JSONObject usage = root.optJSONObject("usageMetadata");
        int prompt = usage != null ? usage.optInt("promptTokenCount", 0) : 0;
        int candidate = usage != null ? usage.optInt("candidatesTokenCount", 0) : 0;
        int total = usage != null ? usage.optInt("totalTokenCount", 0) : 0;
        return new Result(text, prompt, candidate, total, currentStatus, finishReason);
    }

    /*
     * Responsibility: Creates the object or data structure for `build request` using the class's consistent creation rules.
     * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
     */
    private JSONObject buildRequest(String prompt, byte[] imageBytes, String imageMimeType,
                                    double temperature, int maxOutputTokens) throws Exception {
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
                .put("maxOutputTokens", Math.max(32, Math.min(8192, maxOutputTokens)));
        return new JSONObject()
                .put("contents", new JSONArray().put(userContent))
                .put("generationConfig", generationConfig);
    }

    /*
     * Responsibility: Reads or loads data for `read response` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    private String readResponse(HttpURLConnection connection, int statusCode) throws Exception {
        InputStream stream = statusCode >= 200 && statusCode < 300
                ? connection.getInputStream()
                : connection.getErrorStream();
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }
    }

    /*
     * Responsibility: Creates the object or data structure for `create api exception` using the class's consistent creation rules.
     * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
     */
    private MathGeminiApiException createApiException(int statusCode, String responseBody) {
        String message = responseBody == null ? "" : responseBody.trim();
        String apiStatus = "";
        try {
            JSONObject error = new JSONObject(message).optJSONObject("error");
            if (error != null) {
                message = error.optString("message", message);
                apiStatus = error.optString("status", "");
            }
        } catch (Exception ignored) {
            // Keep the raw response as the diagnostic message.
        }
        return new MathGeminiApiException(statusCode, apiStatus, message);
    }
}
