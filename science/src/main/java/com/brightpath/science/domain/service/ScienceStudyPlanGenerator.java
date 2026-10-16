package com.brightpath.science.domain.service;

import com.brightpath.science.domain.model.StudyPlan;
import com.brightpath.science.domain.model.StudyPlanRequest;

public interface ScienceStudyPlanGenerator {
        /*
 * Responsibility: Generates the Science result handled by `generate` using the configured generator/service and request context.
 * Purpose: Keeps content generation behind a focused method so the presentation layer stays independent from AI/fallback details.
 */
void generate(StudyPlanRequest request, ResultCallback<StudyPlan> callback);
}
