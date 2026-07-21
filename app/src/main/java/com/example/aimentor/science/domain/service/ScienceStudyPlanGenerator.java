package com.example.aimentor.science.domain.service;

import com.example.aimentor.science.domain.model.StudyPlan;
import com.example.aimentor.science.domain.model.StudyPlanRequest;

public interface ScienceStudyPlanGenerator {
    void generate(StudyPlanRequest request, ResultCallback<StudyPlan> callback);
}
