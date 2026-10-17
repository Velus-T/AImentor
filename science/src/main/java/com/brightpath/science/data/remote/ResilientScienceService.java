package com.brightpath.science.data.remote;

import com.brightpath.science.domain.model.QuizQuestion;
import com.brightpath.science.domain.model.QuizRequest;
import com.brightpath.science.domain.model.ScienceAnswer;
import com.brightpath.science.domain.model.ScienceQuestion;
import com.brightpath.science.domain.service.ResultCallback;
import com.brightpath.science.domain.service.ScienceAnswerService;
import com.brightpath.science.domain.service.ScienceQuizGenerator;

import java.util.List;
import java.util.Locale;

public final class ResilientScienceService implements ScienceAnswerService, ScienceQuizGenerator {
    private final GeminiScienceService remoteService;
    private final LocalScienceService localService;

        /*
 * Responsibility: Initializes ResilientScienceService and receives or prepares the dependencies/state required by this AI/network or fallback service.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ResilientScienceService(GeminiScienceService remoteService, LocalScienceService localService) {
        this.remoteService = remoteService;
        this.localService = localService;
    }

        /*
 * Responsibility: Tries the remote Gemini Science service first and falls back to LocalScienceService for suitable network/quota/server failures.
 * Purpose: Keeps the Science learning flow usable during temporary AI outages without hiding credential or permission errors.
 */
@Override
    public void answer(ScienceQuestion question, ResultCallback<ScienceAnswer> callback) {
        if (!remoteService.isConfigured()) {
            localService.answer(question, callback);
            return;
        }

        remoteService.answer(question, new ResultCallback<ScienceAnswer>() {
                        /*
 * Responsibility: Performs the `onSuccess` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onSuccess(ScienceAnswer result) {
                callback.onSuccess(result);
            }

                        /*
 * Responsibility: Performs the `onError` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onError(String message, Throwable error) {
                if (error instanceof com.example.mentor.data.QuizBank.SaveException || isCredentialOrPermissionError(error, message)) {
                    callback.onError(message, error);
                    return;
                }
                // Network, quota, timeout and temporary server failures should not
                // make the Science screen unusable. Continue with the local tutor.
                localService.answer(question, callback);
            }
        });
    }

        /*
 * Responsibility: Attempts AI quiz generation and switches to the local quiz fallback when the failure policy allows it.
 * Purpose: Provides resilient practice even when Gemini is unavailable or rate-limited.
 */
@Override
    public void generateQuiz(QuizRequest request, ResultCallback<List<QuizQuestion>> callback) {
        if (!remoteService.isConfigured()) {
            localService.generateQuiz(request, callback);
            return;
        }
        remoteService.generateQuiz(request, new ResultCallback<List<QuizQuestion>>() {
                        /*
 * Responsibility: Performs the `onSuccess` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onSuccess(List<QuizQuestion> result) {
                callback.onSuccess(result);
            }

                        /*
 * Responsibility: Performs the `onError` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onError(String message, Throwable error) {
                if (error instanceof com.example.mentor.data.QuizBank.SaveException || isCredentialOrPermissionError(error, message)) {
                    callback.onError(message, error);
                    return;
                }
                localService.generateQuiz(request, callback);
            }
        });
    }

        /*
 * Responsibility: Checks the condition represented by `isCredentialOrPermissionError` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
private boolean isCredentialOrPermissionError(Throwable error, String callbackMessage) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof GeminiApiException) {
                GeminiApiException apiError = (GeminiApiException) current;
                int status = apiError.getHttpStatus();
                String apiStatus = apiError.getApiStatus().toUpperCase(Locale.ROOT);
                if (status == 401 || status == 403
                        || apiStatus.contains("UNAUTHENTICATED")
                        || apiStatus.contains("PERMISSION_DENIED")) {
                    return true;
                }
            }
            if (containsCredentialMessage(current.getMessage())) return true;
            current = current.getCause();
        }
        return containsCredentialMessage(callbackMessage);
    }

        /*
 * Responsibility: Checks the condition represented by `containsCredentialMessage` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
private boolean containsCredentialMessage(String message) {
        if (message == null) return false;
        String normalized = message.toLowerCase(Locale.ROOT);
        return (normalized.contains("api key") || normalized.contains("credential"))
                && (normalized.contains("invalid")
                || normalized.contains("permission")
                || normalized.contains("unauthorized")
                || normalized.contains("forbidden"));
    }
}
