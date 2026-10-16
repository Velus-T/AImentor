package com.brightpath.science.data.remote;

import android.util.Base64;

import com.brightpath.science.BuildConfig;

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
import java.util.ArrayList;
import java.util.List;

/** Minimal Gemini REST client shared by Science Chat, Quiz and Study Planner. */
public final class GeminiApiClient {
    // API keys are injected from root api.properties through science/build.gradle.
    // This keeps secrets out of Java source and the submitted repository/ZIP.
    private static final String API_KEY_1 = BuildConfig.SCIENCE_GEMINI_API_KEY_1;
    private static final String API_KEY_2 = BuildConfig.SCIENCE_GEMINI_API_KEY_2;
    private static final String API_KEY_3 = BuildConfig.SCIENCE_GEMINI_API_KEY_3;

    private static final String[] CONFIGURED_KEYS = buildKeyList();
    private static final long[] DISABLED_UNTIL = new long[CONFIGURED_KEYS.length];
    private static int currentKeyIndex = 0;
    private static String currentModel = BuildConfig.SCIENCE_GEMINI_MODEL;
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
 * Responsibility: Initializes Result and receives or prepares the dependencies/state required by this Science Module component.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
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
 * Responsibility: Performs the `wasTruncated` operation for Result within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
public boolean wasTruncated() {
            return "MAX_TOKENS".equalsIgnoreCase(finishReason);
        }

                /*
 * Responsibility: Converts input data in `toString` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
@Override
        public String toString() {
            return text;
        }
    }

        /*
 * Responsibility: Retrieves the data/state required by `getModelName` from the appropriate AI/network or fallback service.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getModelName() { return currentModel; }

        /*
 * Responsibility: Updates the state or value handled by `setModelName` for GeminiApiClient.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
public void setModelName(String modelName) {
        if (modelName != null && !modelName.trim().isEmpty()) currentModel = modelName.trim();
    }

        /*
 * Responsibility: Retrieves the data/state required by `getStatus` from the appropriate AI/network or fallback service.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getStatus() { return currentStatus; }

        /*
 * Responsibility: Retrieves the data/state required by `getActiveKeyLabel` from the appropriate AI/network or fallback service.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public synchronized String getActiveKeyLabel() {
        return CONFIGURED_KEYS.length == 0 ? "no key" : "key " + (currentKeyIndex + 1) + "/" + CONFIGURED_KEYS.length;
    }

        /*
 * Responsibility: Checks the condition represented by `isConfigured` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean isConfigured() { return CONFIGURED_KEYS.length > 0; }

        /*
 * Responsibility: Performs the AI/service operation `testConnection` using the configured Science request/context.
 * Purpose: Keeps networking/fallback concerns inside the service layer instead of the UI.
 */
public Result testConnection() throws Exception {
        return generateText("Reply with exactly: OK", null, null, 0.0, 32);
    }

        /*
 * Responsibility: Generates the Science result handled by `generateText` using the configured generator/service and request context.
 * Purpose: Keeps content generation behind a focused method so the presentation layer stays independent from AI/fallback details.
 */
public Result generateText(String prompt, byte[] imageBytes, String imageMimeType,
                               double temperature, int maxOutputTokens) throws Exception {
        if (!isConfigured()) throw new IllegalStateException("No Gemini API key is configured.");
        if (prompt == null || prompt.trim().isEmpty()) throw new IllegalArgumentException("Prompt cannot be empty.");

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
                throw new IllegalStateException("All Gemini API keys are temporarily unavailable.", lastError);
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

                byte[] requestBytes = buildRequest(prompt.trim(), imageBytes, imageMimeType,
                        temperature, maxOutputTokens).toString().getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(requestBytes.length);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(requestBytes);
                }

                int statusCode = connection.getResponseCode();
                String responseBody = readResponse(connection, statusCode);

                if (statusCode >= 200 && statusCode < 300) {
                    synchronized (GeminiApiClient.class) {
                        currentKeyIndex = keyIndex;
                        DISABLED_UNTIL[keyIndex] = 0L;
                    }
                    currentStatus = attempt == 0 ? "GOOD" : "MEDIUM";
                    return parseFullResponse(responseBody);
                }

                GeminiApiException apiError = createApiException(statusCode, responseBody);
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
                if (connection != null) connection.disconnect();
            }
        }

        throw new IllegalStateException("Gemini request failed after multiple attempts.", lastError);
    }

        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `buildKeyList`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
private static String[] buildKeyList() {
        List<String> values = new ArrayList<>();
        addUniqueKey(values, API_KEY_1);
        addUniqueKey(values, API_KEY_2);
        addUniqueKey(values, API_KEY_3);
        return values.toArray(new String[0]);
    }

        /*
 * Responsibility: Adds the data, listener, duration, key, or UI element represented by `addUniqueKey`.
 * Purpose: Keeps the incremental update explicit and isolated from unrelated responsibilities.
 */
private static void addUniqueKey(List<String> keys, String value) {
        if (value == null) return;
        String key = value.trim();
        if (!key.isEmpty() && !keys.contains(key)) keys.add(key);
    }

        /*
 * Responsibility: Performs the AI/service operation `nextAvailableKeyIndex` using the configured Science request/context.
 * Purpose: Keeps networking/fallback concerns inside the service layer instead of the UI.
 */
private static synchronized int nextAvailableKeyIndex() {
        if (CONFIGURED_KEYS.length == 0) return -1;
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
 * Responsibility: Performs the AI/service operation `disableKey` using the configured Science request/context.
 * Purpose: Keeps networking/fallback concerns inside the service layer instead of the UI.
 */
private static synchronized void disableKey(int index, long until) {
        if (index >= 0 && index < DISABLED_UNTIL.length) {
            DISABLED_UNTIL[index] = until;
            if (CONFIGURED_KEYS.length > 0) currentKeyIndex = (index + 1) % CONFIGURED_KEYS.length;
        }
    }

        /*
 * Responsibility: Performs the AI/service operation `shortestCooldown` using the configured Science request/context.
 * Purpose: Keeps networking/fallback concerns inside the service layer instead of the UI.
 */
private static synchronized long shortestCooldown() {
        long now = System.currentTimeMillis();
        long shortest = Long.MAX_VALUE;
        for (long value : DISABLED_UNTIL) {
            if (value > now && value != Long.MAX_VALUE) shortest = Math.min(shortest, value - now);
        }
        return shortest == Long.MAX_VALUE ? -1L : shortest;
    }

        /*
 * Responsibility: Converts input data in `parseFullResponse` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
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
                        if (answer.length() > 0) answer.append('\n');
                        answer.append(text);
                    }
                }
            }
        }
        String text = answer.toString().trim();
        if (text.isEmpty()) throw new IllegalStateException("Gemini returned an empty answer.");

        JSONObject usage = root.optJSONObject("usageMetadata");
        int prompt = usage != null ? usage.optInt("promptTokenCount", 0) : 0;
        int candidate = usage != null ? usage.optInt("candidatesTokenCount", 0) : 0;
        int total = usage != null ? usage.optInt("totalTokenCount", 0) : 0;
        return new Result(text, prompt, candidate, total, currentStatus, finishReason);
    }

        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `buildRequest`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
private JSONObject buildRequest(String prompt, byte[] imageBytes, String imageMimeType,
                                    double temperature, int maxOutputTokens) throws Exception {
        JSONArray parts = new JSONArray();
        if (imageBytes != null && imageBytes.length > 0) {
            String mimeType = imageMimeType == null || imageMimeType.trim().isEmpty()
                    ? "image/jpeg" : imageMimeType.trim();
            JSONObject inlineData = new JSONObject()
                    .put("mime_type", mimeType)
                    .put("data", Base64.encodeToString(imageBytes, Base64.NO_WRAP));
            parts.put(new JSONObject().put("inline_data", inlineData));
        }
        parts.put(new JSONObject().put("text", prompt));
        JSONObject userContent = new JSONObject().put("role", "user").put("parts", parts);
        JSONObject generationConfig = new JSONObject()
                .put("temperature", Math.max(0.0, Math.min(1.0, temperature)))
                .put("maxOutputTokens", Math.max(32, Math.min(8192, maxOutputTokens)));
        return new JSONObject().put("contents", new JSONArray().put(userContent))
                .put("generationConfig", generationConfig);
    }

        /*
 * Responsibility: Performs the AI/service operation `readResponse` using the configured Science request/context.
 * Purpose: Keeps networking/fallback concerns inside the service layer instead of the UI.
 */
private String readResponse(HttpURLConnection connection, int statusCode) throws Exception {
        InputStream stream = statusCode >= 200 && statusCode < 300
                ? connection.getInputStream() : connection.getErrorStream();
        if (stream == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
            return result.toString();
        }
    }

        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `createApiException`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
private GeminiApiException createApiException(int statusCode, String responseBody) {
        String message = responseBody == null ? "" : responseBody.trim();
        String apiStatus = "";
        try {
            JSONObject error = new JSONObject(message).optJSONObject("error");
            if (error != null) {
                message = error.optString("message", message);
                apiStatus = error.optString("status", "");
            }
        } catch (Exception ignored) { }
        return new GeminiApiException(statusCode, apiStatus, message);
    }
}
