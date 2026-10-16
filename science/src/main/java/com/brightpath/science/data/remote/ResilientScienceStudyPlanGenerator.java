package com.brightpath.science.data.remote;

import com.brightpath.science.domain.model.StudyPlan;
import com.brightpath.science.domain.model.StudyPlanRequest;
import com.brightpath.science.domain.service.ResultCallback;
import com.brightpath.science.domain.service.ScienceStudyPlanGenerator;

/** Uses AI first and falls back to a safe local plan instead of crashing. */
public final class ResilientScienceStudyPlanGenerator implements ScienceStudyPlanGenerator {
    private final ScienceStudyPlanGenerator primary;
    private final ScienceStudyPlanGenerator fallback;

        /*
 * Responsibility: Initializes ResilientScienceStudyPlanGenerator and receives or prepares the dependencies/state required by this Science Module component.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ResilientScienceStudyPlanGenerator(ScienceStudyPlanGenerator primary,
                                              ScienceStudyPlanGenerator fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

        /*
 * Responsibility: Generates the Science result handled by `generate` using the configured generator/service and request context.
 * Purpose: Keeps content generation behind a focused method so the presentation layer stays independent from AI/fallback details.
 */
@Override
    public void generate(StudyPlanRequest request, ResultCallback<StudyPlan> callback) {
        primary.generate(request, new ResultCallback<StudyPlan>() {
                        /*
 * Responsibility: Performs the `onSuccess` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onSuccess(StudyPlan result) {
                callback.onSuccess(result);
            }

                        /*
 * Responsibility: Performs the `onError` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onError(String message, Throwable error) {
                fallback.generate(request, callback);
            }
        });
    }
}
