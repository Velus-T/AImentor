package com.example.aimentor.science.domain.usecase;

import com.example.aimentor.science.domain.model.StudyPlan;
import com.example.aimentor.science.domain.model.StudyPlanRequest;
import com.example.aimentor.science.domain.repository.StudyPlanRepository;
import com.example.aimentor.science.domain.service.ResultCallback;
import com.example.aimentor.science.domain.service.ScienceStudyPlanGenerator;

public final class GenerateStudyPlanUseCase {
    private final ScienceStudyPlanGenerator generator;
    private final StudyPlanRepository repository;

    public GenerateStudyPlanUseCase(ScienceStudyPlanGenerator generator,
                                    StudyPlanRepository repository) {
        this.generator = generator;
        this.repository = repository;
    }

    public void execute(StudyPlanRequest request, ResultCallback<StudyPlan> callback) {
        if (request == null) {
            callback.onError("Study plan information is missing.", null);
            return;
        }
        generator.generate(request, new ResultCallback<StudyPlan>() {
            @Override
            public void onSuccess(StudyPlan result) {
                if (result == null || result.getSessions().isEmpty()) {
                    callback.onError("The generated study plan was empty.", null);
                    return;
                }
                repository.save(request.getUserId(), result);
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message, Throwable error) {
                callback.onError(message, error);
            }
        });
    }
}
