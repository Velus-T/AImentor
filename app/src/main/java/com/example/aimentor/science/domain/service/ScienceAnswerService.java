package com.example.aimentor.science.domain.service;

import com.example.aimentor.science.domain.model.ScienceAnswer;
import com.example.aimentor.science.domain.model.ScienceQuestion;

public interface ScienceAnswerService {
    void answer(ScienceQuestion question, ResultCallback<ScienceAnswer> callback);
}
