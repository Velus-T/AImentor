package com.brightpath.science.data.remote;

import android.os.Handler;
import android.os.Looper;

import com.brightpath.science.domain.model.QuizQuestion;
import com.brightpath.science.domain.model.QuizDifficulty;
import com.brightpath.science.domain.model.QuizRequest;
import com.brightpath.science.domain.model.ScienceAnswer;
import com.brightpath.science.domain.model.ScienceQuestion;
import com.brightpath.science.domain.service.ResultCallback;
import com.brightpath.science.domain.service.ScienceAnswerService;
import com.brightpath.science.domain.service.SciencePromptFactory;
import com.brightpath.science.domain.service.ScienceQuizGenerator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class GeminiScienceService implements ScienceAnswerService, ScienceQuizGenerator {
    private static final int ANSWER_OUTPUT_TOKENS = 6000;
    private static final int CONTINUATION_OUTPUT_TOKENS = 3000;
    private static final int MAX_CONTINUATIONS = 2;
    private android.content.Context bankContext;
    private final SciencePromptFactory promptFactory;
    private final GeminiApiClient aiClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

        /*
 * Responsibility: Initializes GeminiScienceService and receives or prepares the dependencies/state required by this AI/network or fallback service.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public GeminiScienceService(android.content.Context context, SciencePromptFactory promptFactory, GeminiApiClient aiClient) {
        this(promptFactory, aiClient); bankContext = context.getApplicationContext();
    }

public GeminiScienceService(SciencePromptFactory promptFactory,
                                GeminiApiClient aiClient) {
        this.promptFactory = promptFactory;
        this.aiClient = aiClient;
    }

        /*
 * Responsibility: Checks the condition represented by `isConfigured` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean isConfigured() {
        return aiClient.isConfigured();
    }

        /*
 * Responsibility: Builds the Science prompt and sends the text/image request to the Gemini client, then converts the remote result into ScienceAnswer.
 * Purpose: Provides the remote AI implementation behind the ScienceAnswerService abstraction.
 */
@Override
    public void answer(ScienceQuestion question, ResultCallback<ScienceAnswer> callback) {
        executor.execute(() -> {
            try {
                GeminiApiClient.Result result = generateCompleteAnswer(question);

                ScienceAnswer answer = new ScienceAnswer(
                        result.text,
                        question.getSubject().getDisplayName(),
                        question.getEducationLevel().getDisplayName(),
                        false,
                        0,
                        result.promptTokens,
                        result.candidateTokens,
                        result.totalTokens
                );
                mainHandler.post(() -> callback.onSuccess(answer));
            } catch (Exception error) {
                mainHandler.post(() -> callback.onError(userMessage(error, "Unable to contact the AI service."), error));
            }
        });
    }


    /**
     * Generates a complete chat answer. Gemini may return finishReason=MAX_TOKENS
     * while still returning HTTP 200. In that case, request a continuation and
     * merge it into the visible answer instead of showing a cut-off response.
     */
        /*
 * Responsibility: Runs the Gemini request and continuation logic needed to obtain a complete Science explanation.
 * Purpose: Improves reliability when one model response is incomplete while keeping networking details inside the service layer.
 */
private GeminiApiClient.Result generateCompleteAnswer(ScienceQuestion question) throws Exception {
        GeminiApiClient.Result current = aiClient.generateText(
                promptFactory.buildAnswerPrompt(question),
                question.hasImage() ? question.getImageBytes() : null,
                question.hasImage() ? question.getImageMimeType() : null,
                0.55,
                ANSWER_OUTPUT_TOKENS
        );

        StringBuilder completeText = new StringBuilder(current.text.trim());
        int promptTokens = current.promptTokens;
        int candidateTokens = current.candidateTokens;
        int totalTokens = current.totalTokens;
        String status = current.status;
        String finishReason = current.finishReason;

        for (int continuation = 0;
             continuation < MAX_CONTINUATIONS && current.wasTruncated();
             continuation++) {

            String continuationPrompt = buildContinuationPrompt(
                    question,
                    completeText.toString(),
                    continuation + 1
            );

            current = aiClient.generateText(
                    continuationPrompt,
                    null,
                    null,
                    0.35,
                    CONTINUATION_OUTPUT_TOKENS
            );

            appendWithoutRepeating(completeText, current.text);
            promptTokens += current.promptTokens;
            candidateTokens += current.candidateTokens;
            totalTokens += current.totalTokens;
            status = current.status;
            finishReason = current.finishReason;
        }

        return new GeminiApiClient.Result(
                completeText.toString().trim(),
                promptTokens,
                candidateTokens,
                totalTokens,
                status,
                finishReason
        );
    }

        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `buildContinuationPrompt`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
private String buildContinuationPrompt(ScienceQuestion question,
                                           String partialAnswer,
                                           int continuationNumber) {
        String tail = partialAnswer == null ? "" : partialAnswer.trim();
        if (tail.length() > 7000) {
            tail = tail.substring(tail.length() - 7000);
        }

        String context = question.getConversationContext();
        if (context.length() > 3500) {
            context = context.substring(context.length() - 3500);
        }

        return "The previous Science tutor answer was cut off by the output limit.\n"
                + "Continue in the same language as the student and complete the SAME answer.\n"
                + "Do not switch topic, restart, repeat headings, repeat calculations, or repeat text already written.\n"
                + "Begin exactly from the unfinished idea or step. Finish all remaining steps, "
                + "state the final conclusion clearly, and end with at most one short follow-up question.\n"
                + "Selected subject: " + question.getSubject().getDisplayName() + "\n"
                + "Latest student message being answered: " + question.getQuestionText() + "\n"
                + (context.isEmpty() ? "" : "Recent conversation context:\n" + context + "\n")
                + "Continuation number: " + continuationNumber + "\n\n"
                + "END OF THE EXISTING PARTIAL ANSWER:\n"
                + tail;
    }

    /**
     * Removes an exact repeated boundary between the existing response and the
     * continuation. Gemini sometimes repeats the final sentence before continuing.
     */
        /*
 * Responsibility: Performs the AI/service operation `appendWithoutRepeating` using the configured Science request/context.
 * Purpose: Keeps networking/fallback concerns inside the service layer instead of the UI.
 */
private void appendWithoutRepeating(StringBuilder destination, String continuation) {
        if (continuation == null || continuation.trim().isEmpty()) return;

        String next = continuation.trim();
        String existing = destination.toString();
        int maximumOverlap = Math.min(1200, Math.min(existing.length(), next.length()));
        int overlap = 0;

        for (int length = maximumOverlap; length >= 24; length--) {
            if (existing.regionMatches(
                    existing.length() - length,
                    next,
                    0,
                    length
            )) {
                overlap = length;
                break;
            }
        }

        String uniquePart = next.substring(overlap).trim();
        if (uniquePart.isEmpty()) return;

        if (destination.length() > 0) destination.append('\n');
        destination.append(uniquePart);
    }

        /*
 * Responsibility: Sends a Science quiz prompt to Gemini and parses the raw model output into QuizQuestion objects.
 * Purpose: Converts unstructured AI output into domain data that the quiz UI and scoring logic can safely use.
 */
@Override
    public void generateQuiz(QuizRequest request, ResultCallback<List<QuizQuestion>> callback) {
        executor.execute(() -> {
            try {
                GeminiApiClient.Result result = aiClient.generateText(
                        promptFactory.buildQuizPrompt(request), null, null,
                        0.9, 8192);
                
                List<QuizQuestion> questions = parseQuiz(result.text, request.getNumberOfQuestions());
                if (questions.size() < request.getNumberOfQuestions()) throw new IllegalStateException("AI returned too few quiz questions.");
                if (bankContext != null) try (com.example.mentor.data.QuizBank bank = new com.example.mentor.data.QuizBank(bankContext)) {
                    bank.save(request.getUserId(), "science", request.getSubject().getCode(), request.getLevel().name(),
                            request.getTopic(), "GEMINI", com.brightpath.science.util.QuizCards.from(questions));
                } catch (RuntimeException storageError) { throw new com.example.mentor.data.QuizBank.SaveException(storageError); }
                mainHandler.post(() -> callback.onSuccess(questions));
            } catch (Exception error) {
                mainHandler.post(() -> callback.onError(userMessage(error, "Unable to generate an AI quiz."), error));
            }
        });
    }


        /*
 * Responsibility: Performs the AI/service operation `userMessage` using the configured Science request/context.
 * Purpose: Keeps networking/fallback concerns inside the service layer instead of the UI.
 */
public static String userMessage(Throwable error, String fallback) {
        if (error instanceof GeminiApiException) {
            GeminiApiException apiError = (GeminiApiException) error;
            int status = apiError.getHttpStatus();
            String googleMessage = apiError.getMessage();
            String detail = googleMessage == null || googleMessage.trim().isEmpty()
                    ? "No error detail returned by Google."
                    : googleMessage.trim();
            
            if (status == 503) {
                return "Gemini is currently experiencing high demand (HTTP 503). Retrying with a fallback model...";
            }
            if (status == 429) {
                return "Rate limit exceeded (HTTP 429). Please wait a moment before trying again.";
            }

            return "Gemini HTTP " + status
                    + (apiError.getApiStatus().isEmpty() ? "" : " " + apiError.getApiStatus())
                    + ": " + detail;
        }

        if (error instanceof java.net.UnknownHostException) {
            return "Cannot resolve the Gemini server. Check Wi-Fi/mobile data and DNS.";
        }
        if (error instanceof java.net.SocketTimeoutException) {
            return "Gemini request timed out. Try again with a smaller image or shorter question.";
        }
        if (error instanceof javax.net.ssl.SSLException) {
            return "Secure connection to Gemini failed: " + safeDetail(error);
        }

        String detail = safeDetail(error);
        return fallback + (detail.isEmpty() ? "" : " " + detail);
    }

        /*
 * Responsibility: Performs the AI/service operation `safeDetail` using the configured Science request/context.
 * Purpose: Keeps networking/fallback concerns inside the service layer instead of the UI.
 */
private static String safeDetail(Throwable error) {
        if (error == null) return "";
        String message = error.getMessage();
        String type = error.getClass().getSimpleName();
        if (message == null || message.trim().isEmpty()) return "(" + type + ")";
        return "(" + type + ": " + message.trim() + ")";
    }


        /*
 * Responsibility: Converts input data in `parseQuiz` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
private List<QuizQuestion> parseQuiz(String rawText, int maximumQuestions) throws Exception {
        String cleaned = rawText.replace("```json", "").replace("```", "").trim();
        int start = cleaned.indexOf('[');
        int end = cleaned.lastIndexOf(']');
        if (start < 0 || end <= start) throw new IllegalArgumentException("Quiz JSON array was not found.");
        JSONArray array = new JSONArray(cleaned.substring(start, end + 1));
        List<QuizQuestion> result = new ArrayList<>();
        if (array.length() != maximumQuestions) throw new IllegalArgumentException("AI returned an unexpected question count.");
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            QuizQuestion.Type type;
            try {
                type = QuizQuestion.Type.valueOf(item.optString("type", "MULTIPLE_CHOICE")
                        .toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException error) {
                throw new IllegalArgumentException("AI returned an unknown question type.", error);
            }
            List<String> options = new ArrayList<>();
            JSONArray optionsArray = item.optJSONArray("options");
            if (optionsArray != null) {
                for (int j = 0; j < optionsArray.length(); j++) options.add(optionsArray.optString(j));
            }
            // The player maps the first true/false row to TRUE, independently of AI ordering.
            if (type == QuizQuestion.Type.TRUE_FALSE) options = java.util.Arrays.asList("True", "False");
            String correctAnswer = item.optString("correctAnswer").trim();
            if (type == QuizQuestion.Type.MULTIPLE_CHOICE) {
                for (int j = 0; j < options.size(); j++) {
                    String body = options.get(j).replaceFirst("(?i)^[A-D][.)]\\s*", "").trim();
                    String given = correctAnswer.replaceFirst("(?i)^[A-D][.)]\\s*", "").trim();
                    if (body.equalsIgnoreCase(given)) {
                        correctAnswer = String.valueOf((char) ('A' + j));
                        break;
                    }
                }
            }
            result.add(new QuizQuestion(
                    type,
                    item.optString("prompt"),
                    options,
                    correctAnswer,
                    item.optString("explanation"),
                    QuizDifficulty.fromApiValue(item.optString("difficulty", "MEDIUM"))
            ));
        }
        com.brightpath.science.domain.model.ScienceQuizValidator.validate(result, maximumQuestions);
        return result;
    }
}
