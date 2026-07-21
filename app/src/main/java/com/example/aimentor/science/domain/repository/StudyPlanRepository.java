package com.example.aimentor.science.domain.repository;

import com.example.aimentor.science.domain.model.StudyPlan;

public interface StudyPlanRepository {
    StudyPlan getLatest(int userId);
    void save(int userId, StudyPlan plan);
    void clear(int userId);
}
