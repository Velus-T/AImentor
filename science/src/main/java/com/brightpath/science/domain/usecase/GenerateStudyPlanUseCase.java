package com.brightpath.science.domain.usecase;

import com.brightpath.science.domain.model.StudyPlan;
import com.brightpath.science.domain.model.StudyPlanRequest;
import com.brightpath.science.domain.repository.StudyPlanRepository;
import com.brightpath.science.domain.service.ResultCallback;
import com.brightpath.science.domain.service.ScienceStudyPlanGenerator;

public final class GenerateStudyPlanUseCase {
    private final ScienceStudyPlanGenerator generator;
    private final StudyPlanRepository repository;

        /*
 * Responsibility: Initializes GenerateStudyPlanUseCase and receives or prepares the dependencies/state required by this application/business use-case.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public GenerateStudyPlanUseCase(ScienceStudyPlanGenerator generator,
                                    StudyPlanRepository repository) {
        this.generator = generator;
        this.repository = repository;
    }

        /*
 * Responsibility: Requests a Science study plan from the configured generator and saves the successful plan through the repository.
 * Purpose: Coordinates generation and persistence so the learner can reopen the plan instead of regenerating it every time.
 */
public void execute(StudyPlanRequest request, ResultCallback<StudyPlan> callback) {
        if (request == null) {
            callback.onError("Study plan information is missing.", null);
            return;
        }
        generator.generate(request, new ResultCallback<StudyPlan>() {
                        /*
 * Responsibility: Performs the `onSuccess` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onSuccess(StudyPlan result) {
                if (result == null || result.getSessions().isEmpty()) {
                    callback.onError("The generated study plan was empty.", null);
                    return;
                }
                try {
                    repository.save(request.getUserId(), result);
                } catch (RuntimeException error) {
                    callback.onError("Unable to save the study plan. Please try again.", error);
                    return;
                }
                callback.onSuccess(result);
            }

                        /*
 * Responsibility: Performs the `onError` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onError(String message, Throwable error) {
                callback.onError(message, error);
            }
        });
    }
}
