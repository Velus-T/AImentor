package com.example.historymodule.api;

import android.os.Handler;
import android.os.Looper;

import com.example.historymodule.model.HistoryQuizQuestion;
import com.example.historymodule.util.HistoryPromptFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLException;

/** AI-backed History tutor and quiz generator. */
/*
 * Role: AI/API class for History, responsible for preparing requests or prompts, calling the service, and converting response data.
 * Purpose: Separating AI communication from the UI makes error handling, model or endpoint changes, and logic testing easier.
 */
public final class HistoryAiService {
    private static final int ANSWER_OUTPUT_TOKENS = 6000;
    private static final int CONTINUATION_OUTPUT_TOKENS = 3000;
    private static final int MAX_CONTINUATIONS = 2;

    private final HistoryGeminiApiClient client;
    private final HistoryPromptFactory promptFactory;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /*
     * Responsibility: Initializes the `HistoryAiService` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public HistoryAiService() {
        this(new HistoryGeminiApiClient(), new HistoryPromptFactory());
    }

    public HistoryAiService(HistoryGeminiApiClient client,
                            HistoryPromptFactory promptFactory) {
        this.client = client;
        this.promptFactory = promptFactory;
    }

    /*
     * Responsibility: Validates the conditions or rules handled by `is configured` and returns the result to the caller.
     * Purpose: A dedicated validation method keeps rules consistent and easier to test.
     */
    public boolean isConfigured() {
        return client.isConfigured();
    }

    public String getModelName() {
        return client.getModelName();
    }

    /*
     * Responsibility: Performs the `answer` responsibility of `HistoryAiService` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    public void answer(String latestMessage,
                       String conversationContext,
                       byte[] imageBytes,
                       String imageMimeType,
                       Callback<String> callback) {
        executor.execute(() -> {
            try {
                HistoryGeminiApiClient.Result result = generateCompleteAnswer(
                        latestMessage,
                        conversationContext,
                        imageBytes,
                        imageMimeType
                );
                mainHandler.post(() -> callback.onSuccess(result.getText()));
            } catch (Exception error) {
                mainHandler.post(() -> callback.onError(userMessage(error), error));
            }
        });
    }

    /*
     * Responsibility: Generates data or content for `generate quiz` based on the input and module rules.
     * Purpose: Keeping content generation separate makes the strategy easier to change without affecting presentation logic.
     */
    public void generateQuiz(String topic,
                             String educationLevel,
                             int questionCount,
                             Callback<List<HistoryQuizQuestion>> callback) {
        executor.execute(() -> {
            try {
                String prompt = promptFactory.buildQuizPrompt(topic, educationLevel, questionCount);
                HistoryGeminiApiClient.Result current = client.generateText(
                        prompt,
                        null,
                        null,
                        0.75,
                        7000
                );

                StringBuilder complete = new StringBuilder(current.getText());
                for (int i = 0; i < MAX_CONTINUATIONS && current.wasTruncated(); i++) {
                    current = client.generateText(
                            promptFactory.buildContinuationPrompt(
                                    prompt,
                                    "",
                                    complete.toString(),
                                    i + 1
                            ),
                            null,
                            null,
                            0.75,
                            CONTINUATION_OUTPUT_TOKENS
                    );
                    appendWithoutRepeating(complete, current.getText());
                }

                List<HistoryQuizQuestion> questions = parseQuiz(complete.toString(), questionCount);
                if (questions.size() < Math.max(5, questionCount)) {
                    throw new IllegalStateException("AI trả về chưa đủ số câu Quiz.");
                }
                mainHandler.post(() -> callback.onSuccess(questions));
            } catch (Exception error) {
                mainHandler.post(() -> callback.onError(userMessage(error), error));
            }
        });
    }

    /*
     * Responsibility: Performs the `shutdown` responsibility of `HistoryAiService` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    public void shutdown() {
        executor.shutdownNow();
    }

    private HistoryGeminiApiClient.Result generateCompleteAnswer(String latestMessage,
                                                            String conversationContext,
                                                            byte[] imageBytes,
                                                            String imageMimeType) throws Exception {
        HistoryGeminiApiClient.Result current = client.generateText(
                promptFactory.buildAnswerPrompt(
                        latestMessage,
                        conversationContext,
                        imageBytes != null && imageBytes.length > 0
                ),
                imageBytes,
                imageMimeType,
                0.45,
                ANSWER_OUTPUT_TOKENS
        );

        StringBuilder complete = new StringBuilder(current.getText());
        int promptTokens = current.getPromptTokens();
        int candidateTokens = current.getCandidateTokens();
        int totalTokens = current.getTotalTokens();
        String finishReason = current.getFinishReason();

        for (int continuation = 0;
             continuation < MAX_CONTINUATIONS && current.wasTruncated();
             continuation++) {
            current = client.generateText(
                    promptFactory.buildContinuationPrompt(
                            latestMessage,
                            conversationContext,
                            complete.toString(),
                            continuation + 1
                    ),
                    null,
                    null,
                    0.30,
                    CONTINUATION_OUTPUT_TOKENS
            );

            appendWithoutRepeating(complete, current.getText());
            promptTokens += current.getPromptTokens();
            candidateTokens += current.getCandidateTokens();
            totalTokens += current.getTotalTokens();
            finishReason = current.getFinishReason();
        }

        return new HistoryGeminiApiClient.Result(
                complete.toString().trim(),
                finishReason,
                promptTokens,
                candidateTokens,
                totalTokens
        );
    }

    /*
     * Responsibility: Performs the `append without repeating` responsibility of `HistoryAiService` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void appendWithoutRepeating(StringBuilder destination,
                                        String continuation) {
        if (continuation == null || continuation.trim().isEmpty()) return;

        String next = continuation.trim();
        String existing = destination.toString();
        int maximumOverlap = Math.min(1200, Math.min(existing.length(), next.length()));
        int overlap = 0;

        for (int length = maximumOverlap; length >= 24; length--) {
            if (existing.regionMatches(existing.length() - length, next, 0, length)) {
                overlap = length;
                break;
            }
        }

        String unique = next.substring(overlap).trim();
        if (unique.isEmpty()) return;
        if (destination.length() > 0) destination.append('\n');
        destination.append(unique);
    }

    /*
     * Responsibility: Converts or normalizes data in `parse quiz` into the required format.
     * Purpose: A centralized conversion point keeps formatting consistent and makes invalid data easier to handle.
     */
    private List<HistoryQuizQuestion> parseQuiz(String rawText,
                                                 int expectedCount) throws Exception {
        String json = extractJsonArray(rawText);
        JSONArray array = new JSONArray(json);
        List<HistoryQuizQuestion> result = new ArrayList<>();
        int limit = Math.min(array.length(), Math.max(5, Math.min(10, expectedCount)));

        for (int i = 0; i < limit; i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) continue;

            String question = firstNonBlank(
                    item.optString("question", ""),
                    item.optString("prompt", "")
            );
            String difficulty = item.optString("difficulty", "MEDIUM");
            String explanation = item.optString("explanation", "");
            JSONArray optionArray = item.optJSONArray("options");
            List<String> options = new ArrayList<>();
            if (optionArray != null) {
                for (int optionIndex = 0; optionIndex < optionArray.length(); optionIndex++) {
                    String option = optionArray.optString(optionIndex, "").trim();
                    if (!option.isEmpty()) options.add(stripOptionPrefix(option));
                }
            }

            int correctIndex = item.optInt("correctIndex", -1);
            if (correctIndex < 0) {
                String correctAnswer = item.optString("correctAnswer", "").trim();
                correctIndex = resolveCorrectIndex(correctAnswer, options);
            }

            if (!question.isEmpty() && options.size() == 4
                    && correctIndex >= 0 && correctIndex < 4) {
                result.add(new HistoryQuizQuestion(
                        difficulty,
                        question,
                        options,
                        correctIndex,
                        explanation
                ));
            }
        }
        return result;
    }

    /*
     * Responsibility: Performs the `extract json array` responsibility of `HistoryAiService` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private String extractJsonArray(String rawText) {
        if (rawText == null) return "[]";
        String text = rawText.trim();
        if (text.startsWith("```")) {
            int firstLine = text.indexOf('\n');
            if (firstLine >= 0) text = text.substring(firstLine + 1);
            int lastFence = text.lastIndexOf("```");
            if (lastFence >= 0) text = text.substring(0, lastFence);
        }
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        if (start >= 0) {
            int lastBrace = text.lastIndexOf('}');
            if (lastBrace > start) {
                return text.substring(start, lastBrace + 1) + "]";
            }
        }
        return text;
    }

    /*
     * Responsibility: Performs the `strip option prefix` responsibility of `HistoryAiService` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private String stripOptionPrefix(String value) {
        return value.replaceFirst("^[A-Da-d][\\.\\)\\:\\-]\\s*", "").trim();
    }

    private int resolveCorrectIndex(String value, List<String> options) {
        if (value == null) return -1;
        String normalized = value.trim();
        if (normalized.matches("[0-3]")) {
            return Integer.parseInt(normalized);
        }
        if (normalized.matches("[1-4]")) {
            return Integer.parseInt(normalized) - 1;
        }
        if (normalized.matches("(?i)[A-D]")) {
            return Character.toUpperCase(normalized.charAt(0)) - 'A';
        }
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).equalsIgnoreCase(stripOptionPrefix(normalized))) {
                return i;
            }
        }
        return -1;
    }

    /*
     * Responsibility: Performs the `first non blank` responsibility of `HistoryAiService` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) return first.trim();
        return second == null ? "" : second.trim();
    }

    /*
     * Responsibility: Performs the `user message` responsibility of `HistoryAiService` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private String userMessage(Throwable error) {
        if (error instanceof HistoryGeminiApiException) {
            HistoryGeminiApiException apiError = (HistoryGeminiApiException) error;
            int status = apiError.getHttpStatus();
            if (status == 401 || status == 403) {
                return "Gemini từ chối API key. Hãy kiểm tra HISTORY_GEMINI_API_KEY.";
            }
            if (status == 400) {
                return "Yêu cầu gửi tới Gemini không hợp lệ. Hãy thử lại với câu hỏi hoặc ảnh khác.";
            }
            if (status == 404) {
                return "Không tìm thấy model Gemini đã cấu hình. Hãy kiểm tra HISTORY_GEMINI_MODEL.";
            }
            if (status == 429) {
                return "Gemini đang giới hạn lượt gọi. Hãy đợi một lúc rồi thử lại.";
            }
            if (status >= 500) {
                return "Gemini đang bận hoặc tạm thời gián đoạn. Hãy thử lại sau.";
            }
            String detail = apiError.getMessage();
            return "Gemini HTTP " + status
                    + (detail == null || detail.trim().isEmpty() ? "" : ": " + detail.trim());
        }
        if (error instanceof UnknownHostException) {
            return "Không thể kết nối máy chủ Gemini. Hãy kiểm tra Wi-Fi hoặc dữ liệu di động.";
        }
        if (error instanceof SocketTimeoutException) {
            return "Gemini phản hồi quá lâu. Hãy thử lại với ảnh nhỏ hơn hoặc câu hỏi ngắn hơn.";
        }
        if (error instanceof SSLException) {
            return "Kết nối bảo mật tới Gemini bị lỗi.";
        }
        String detail = error == null ? "" : error.getMessage();
        return detail == null || detail.trim().isEmpty()
                ? "Không thể nhận câu trả lời từ Gemini."
                : detail.trim();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message, Throwable error);
    }
}
