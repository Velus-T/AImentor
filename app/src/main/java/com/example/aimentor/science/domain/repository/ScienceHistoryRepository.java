package com.example.aimentor.science.domain.repository;

import com.example.aimentor.science.domain.model.ScienceHistory;
import com.example.aimentor.science.domain.model.ScienceQuestion;
import com.example.aimentor.science.domain.model.ScienceSubject;

import java.util.List;

public interface ScienceHistoryRepository {
    long save(ScienceQuestion question, String answer);
    ScienceHistory findCached(int userId, ScienceSubject subject, String educationLevel, String normalizedQuestion);
    List<ScienceHistory> search(int userId, ScienceSubject subject, String keyword);
    void addReviewDuration(long historyId, int seconds);
    int countByUser(int userId);
    int totalReviewSeconds(int userId);
    ScienceSubject mostFrequentSubject(int userId);
}
