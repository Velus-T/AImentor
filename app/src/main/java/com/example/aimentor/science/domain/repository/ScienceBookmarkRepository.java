package com.example.aimentor.science.domain.repository;

import com.example.aimentor.science.domain.model.ScienceBookmark;
import com.example.aimentor.science.domain.model.ScienceSubject;

import java.util.List;

public interface ScienceBookmarkRepository {
    long save(ScienceBookmark bookmark);
    boolean existsForHistory(int userId, long historyId);
    void deleteForHistory(int userId, long historyId);
    List<ScienceBookmark> search(int userId, ScienceSubject subject, String keyword);
    int countByUser(int userId);
}
