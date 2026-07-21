package com.example.aimentor.science.data.remote;

import android.util.Base64;

import com.example.aimentor.science.domain.service.GenerativeAiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/** Single, defensive HTTP gateway for all Gemini-powered Science features. */
public final class GeminiApiClient implements GenerativeAiClient {
    private static final String DEFAULT_MODEL = "gemini-3.5-flash";
    private static final Pattern MODEL_PATTERN = Pattern.compile("[A-Za-z0-9._-]{1,100}");

    @Override
    public String generateText(String apiKey,
                               String modelName,
                               String prompt,
                               byte[] imageBytes,
                               String imageMimeType,
                               double temperature,
                               int maxOutputTokens) throws Exception {
        String normalizedKey = apiKey == null ? "" : apiKey.trim();
        if (normalizedKey.isEmpty()) {
            throw new IllegalStateException(
                    "Gemini API key is missing. Open Science settings to add it.");
        }
        String normalizedModel = normalizeModel(modelName);
        String safePrompt = prompt == null ? "" : prompt.trim();
        if (safePrompt.isEmpty()) throw new IllegalArgumentException("The AI prompt is empty.");

        URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/"
                + normalizedModel + ":generateContent");
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(20_000);
            connection.setReadTimeout(45_000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("x-goog-api-key", normalizedKey);

            JSONObject payload = createPayload(
                    safePrompt,
                    imageBytes,
                    imageMimeType,
                    Math.max(0.0, Math.min(2.0, temperature)),
                    Math.max(16, Math.min(8192, maxOutputTokens))
            );
            try (OutputStream output = connection.getOutputStream()) {
                output.write(payload.toString().getBytes(StandardCharsets.UTF_8));
                output.flush();
            }

            int status = connection.getResponseCode();
            String body = readBody(connection, status);
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Gemini HTTP " + status + ": " + body);
            }
            if (body.trim().isEmpty()) throw new IllegalStateException("Gemini returned an empty response.");
            return extractText(new JSONObject(body));
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private JSONObject createPayload(String prompt,
                                     byte[] imageBytes,
                                     String imageMimeType,
                                     double temperature,
                                     int maxOutputTokens) throws Exception {
        JSONArray parts = new JSONArray().put(new JSONObject().put("text", prompt));
        if (imageBytes != null && imageBytes.length > 0) {
            String mime = imageMimeType == null || imageMimeType.trim().isEmpty()
                    ? "image/jpeg" : imageMimeType.trim();
            parts.put(new JSONObject().put("inline_data", new JSONObject()
                    .put("mime_type", mime)
                    .put("data", Base64.encodeToString(imageBytes, Base64.NO_WRAP))));
        }
        return new JSONObject()
                .put("contents", new JSONArray().put(new JSONObject()
                        .put("role", "user")
                        .put("parts", parts)))
                .put("generationConfig", new JSONObject()
                        .put("temperature", temperature)
                        .put("topP", 0.95)
                        .put("maxOutputTokens", maxOutputTokens));
    }

    private String readBody(HttpURLConnection connection, int status) throws Exception {
        InputStream stream = status >= 200 && status < 300
                ? connection.getInputStream() : connection.getErrorStream();
        if (stream == null) return "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
            return body.toString();
        }
    }

    private String extractText(JSONObject response) throws Exception {
        JSONArray candidates = response.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0) {
            JSONObject feedback = response.optJSONObject("promptFeedback");
            String reason = feedback == null ? "" : feedback.optString("blockReason", "");
            throw new IllegalStateException(reason.isEmpty()
                    ? "AI response did not contain an answer."
                    : "AI response was blocked: " + reason);
        }
        JSONObject content = candidates.optJSONObject(0) == null
                ? null : candidates.optJSONObject(0).optJSONObject("content");
        JSONArray parts = content == null ? null : content.optJSONArray("parts");
        if (parts == null) throw new IllegalStateException("AI response did not contain text parts.");
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < parts.length(); i++) {
            JSONObject part = parts.optJSONObject(i);
            if (part != null) text.append(part.optString("text", ""));
        }
        String result = text.toString().trim();
        if (result.isEmpty()) throw new IllegalStateException("AI answer was empty.");
        return result;
    }

    private String normalizeModel(String modelName) {
        String value = modelName == null ? "" : modelName.trim();
        return MODEL_PATTERN.matcher(value).matches() ? value : DEFAULT_MODEL;
    }
}
