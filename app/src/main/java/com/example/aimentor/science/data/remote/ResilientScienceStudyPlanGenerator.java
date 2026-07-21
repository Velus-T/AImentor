package com.example.aimentor.science.data.remote;

import com.example.aimentor.science.domain.model.StudyPlan;
import com.example.aimentor.science.domain.model.StudyPlanRequest;
import com.example.aimentor.science.domain.service.ResultCallback;
import com.example.aimentor.science.domain.service.ScienceStudyPlanGenerator;

/** Uses AI first and falls back to a safe local plan instead of crashing. */
public final class ResilientScienceStudyPlanGenerator implements ScienceStudyPlanGenerator {
    private final ScienceStudyPlanGenerator primary;
    private final ScienceStudyPlanGenerator fallback;

    public ResilientScienceStudyPlanGenerator(ScienceStudyPlanGenerator primary,
                                              ScienceStudyPlanGenerator fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public void generate(StudyPlanRequest request, ResultCallback<StudyPlan> callback) {
        primary.generate(request, new ResultCallback<StudyPlan>() {
            @Override
            public void onSuccess(StudyPlan result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message, Throwable error) {
                fallback.generate(request, callback);
            }
        });
    }
}
