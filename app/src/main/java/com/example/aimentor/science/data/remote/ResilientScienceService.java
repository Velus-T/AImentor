package com.example.aimentor.science.data.remote;

import com.example.aimentor.science.domain.model.QuizQuestion;
import com.example.aimentor.science.domain.model.QuizRequest;
import com.example.aimentor.science.domain.model.ScienceAnswer;
import com.example.aimentor.science.domain.model.ScienceQuestion;
import com.example.aimentor.science.domain.service.ResultCallback;
import com.example.aimentor.science.domain.service.ScienceAnswerService;
import com.example.aimentor.science.domain.service.ScienceQuizGenerator;

import java.util.List;

public final class ResilientScienceService implements ScienceAnswerService, ScienceQuizGenerator {
    private final GeminiScienceService remoteService;
    private final LocalScienceService localService;

    public ResilientScienceService(GeminiScienceService remoteService, LocalScienceService localService) {
        this.remoteService = remoteService;
        this.localService = localService;
    }

    @Override
    public void answer(ScienceQuestion question, ResultCallback<ScienceAnswer> callback) {
        if (!remoteService.isConfigured()) {
            localService.answer(question, callback);
            return;
        }
        remoteService.answer(question, new ResultCallback<ScienceAnswer>() {
            @Override
            public void onSuccess(ScienceAnswer result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message, Throwable error) {
                localService.answer(question, new ResultCallback<ScienceAnswer>() {
                    @Override
                    public void onSuccess(ScienceAnswer result) {
                        callback.onSuccess(new ScienceAnswer(
                                result.getContent() + "\n\nNote: Offline fallback was used because the AI service was unavailable.",
                                result.getDetectedSubject(),
                                result.getDifficulty(),
                                false,
                                0
                        ));
                    }

                    @Override
                    public void onError(String fallbackMessage, Throwable fallbackError) {
                        callback.onError(message, error);
                    }
                });
            }
        });
    }

    @Override
    public void generateQuiz(QuizRequest request, ResultCallback<List<QuizQuestion>> callback) {
        if (!remoteService.isConfigured()) {
            localService.generateQuiz(request, callback);
            return;
        }
        remoteService.generateQuiz(request, new ResultCallback<List<QuizQuestion>>() {
            @Override
            public void onSuccess(List<QuizQuestion> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message, Throwable error) {
                localService.generateQuiz(request, callback);
            }
        });
    }
}
