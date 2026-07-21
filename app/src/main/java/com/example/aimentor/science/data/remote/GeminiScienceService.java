package com.example.aimentor.science.data.remote;

import android.os.Handler;
import android.os.Looper;

import com.example.aimentor.science.domain.model.QuizQuestion;
import com.example.aimentor.science.domain.model.QuizRequest;
import com.example.aimentor.science.domain.model.ScienceAnswer;
import com.example.aimentor.science.domain.model.ScienceQuestion;
import com.example.aimentor.science.domain.service.GenerativeAiClient;
import com.example.aimentor.science.domain.service.ResultCallback;
import com.example.aimentor.science.domain.service.ScienceAnswerService;
import com.example.aimentor.science.domain.service.ScienceApiSettings;
import com.example.aimentor.science.domain.service.ScienceApiConnectionTester;
import com.example.aimentor.science.domain.service.SciencePromptFactory;
import com.example.aimentor.science.domain.service.ScienceQuizGenerator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class GeminiScienceService implements ScienceAnswerService, ScienceQuizGenerator, ScienceApiConnectionTester {
    private final ScienceApiSettings apiSettings;
    private final SciencePromptFactory promptFactory;
    private final GenerativeAiClient aiClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public GeminiScienceService(ScienceApiSettings apiSettings,
                                SciencePromptFactory promptFactory,
                                GenerativeAiClient aiClient) {
        this.apiSettings = apiSettings;
        this.promptFactory = promptFactory;
        this.aiClient = aiClient;
    }

    public boolean isConfigured() {
        return apiSettings.isConfigured();
    }

    @Override
    public void answer(ScienceQuestion question, ResultCallback<ScienceAnswer> callback) {
        executor.execute(() -> {
            try {
                String response = aiClient.generateText(
                        apiSettings.getApiKey(), apiSettings.getModelName(),
                        promptFactory.buildAnswerPrompt(question),
                        question.hasImage() ? question.getImageBytes() : null,
                        question.hasImage() ? question.getImageMimeType() : null,
                        0.7, 2500);
                ScienceAnswer answer = new ScienceAnswer(
                        response,
                        question.getSubject().getDisplayName(),
                        question.getEducationLevel().getDisplayName(),
                        false,
                        0
                );
                mainHandler.post(() -> callback.onSuccess(answer));
            } catch (Exception error) {
                mainHandler.post(() -> callback.onError(userMessage(error, "Unable to contact the AI service."), error));
            }
        });
    }

    @Override
    public void generateQuiz(QuizRequest request, ResultCallback<List<QuizQuestion>> callback) {
        executor.execute(() -> {
            try {
                String response = aiClient.generateText(
                        apiSettings.getApiKey(), apiSettings.getModelName(),
                        promptFactory.buildQuizPrompt(request), null, null,
                        0.9, 3500);
                List<QuizQuestion> questions = parseQuiz(response, request.getNumberOfQuestions());
                if (questions.size() < request.getNumberOfQuestions()) throw new IllegalStateException("AI returned too few quiz questions.");
                mainHandler.post(() -> callback.onSuccess(questions));
            } catch (Exception error) {
                mainHandler.post(() -> callback.onError(userMessage(error, "Unable to generate an AI quiz."), error));
            }
        });
    }

    @Override
    public void test(String apiKey, String modelName, ResultCallback<String> callback) {
        executor.execute(() -> {
            try {
                String response = aiClient.generateText(
                        apiKey, modelName, "Reply with exactly: CONNECTION_OK",
                        null, null, 0.1, 32);
                String result = response.toUpperCase(Locale.ROOT).contains("CONNECTION_OK")
                        ? "Connection successful."
                        : "The model responded successfully.";
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception error) {
                mainHandler.post(() -> callback.onError(
                        userMessage(error, "Unable to connect with these settings."), error));
            }
        });
    }

    private String userMessage(Exception error, String fallback) {
        String message = error.getMessage();
        if (message == null || message.trim().isEmpty()) return fallback;
        if (message.contains("API key is missing")) return message;
        if (message.contains("HTTP 400")) return "The AI request was rejected. Check the selected model in API settings.";
        if (message.contains("HTTP 401") || message.contains("HTTP 403")) {
            return "The Gemini API key is invalid or does not have permission. Update it in Science API settings.";
        }
        if (message.contains("HTTP 429")) return "The Gemini API quota is temporarily exhausted. Try again later.";
        return fallback;
    }


    private List<QuizQuestion> parseQuiz(String rawText, int maximumQuestions) throws Exception {
        String cleaned = rawText.replace("```json", "").replace("```", "").trim();
        int start = cleaned.indexOf('[');
        int end = cleaned.lastIndexOf(']');
        if (start < 0 || end <= start) throw new IllegalArgumentException("Quiz JSON array was not found.");
        JSONArray array = new JSONArray(cleaned.substring(start, end + 1));
        List<QuizQuestion> result = new ArrayList<>();
        for (int i = 0; i < array.length() && result.size() < maximumQuestions; i++) {
            JSONObject item = array.getJSONObject(i);
            QuizQuestion.Type type;
            try {
                type = QuizQuestion.Type.valueOf(item.optString("type", "MULTIPLE_CHOICE")
                        .toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException error) {
                type = QuizQuestion.Type.MULTIPLE_CHOICE;
            }
            List<String> options = new ArrayList<>();
            JSONArray optionsArray = item.optJSONArray("options");
            if (optionsArray != null) {
                for (int j = 0; j < optionsArray.length(); j++) options.add(optionsArray.optString(j));
            }
            result.add(new QuizQuestion(
                    type,
                    item.optString("prompt"),
                    options,
                    item.optString("correctAnswer"),
                    item.optString("explanation")
            ));
        }
        return result;
    }
}
