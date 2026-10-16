package com.brightpath.science.domain.service;

import com.brightpath.science.domain.model.ScienceAnswer;
import com.brightpath.science.domain.model.ScienceQuestion;

public interface ScienceAnswerService {
        /*
 * Responsibility: Performs the AI/service operation `answer` using the configured Science request/context.
 * Purpose: Keeps networking/fallback concerns inside the service layer instead of the UI.
 */
void answer(ScienceQuestion question, ResultCallback<ScienceAnswer> callback);
}
