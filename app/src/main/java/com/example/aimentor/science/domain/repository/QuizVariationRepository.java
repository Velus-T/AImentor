package com.example.aimentor.science.domain.repository;

import com.example.aimentor.science.domain.model.EducationLevel;
import com.example.aimentor.science.domain.model.ScienceSubject;

import java.util.List;

public interface QuizVariationRepository {
    List<String> getRecentPrompts(int userId, ScienceSubject subject,
                                  EducationLevel level, int limit);

    void rememberPrompts(int userId, ScienceSubject subject,
                         EducationLevel level, List<String> prompts);
}
