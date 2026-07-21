package com.example.aimentor.science.domain.usecase;

import com.example.aimentor.science.domain.model.ScienceBookmark;
import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.repository.ScienceBookmarkRepository;

public final class SaveBookmarkUseCase {
    private final ScienceBookmarkRepository repository;

    public SaveBookmarkUseCase(ScienceBookmarkRepository repository) {
        this.repository = repository;
    }

    public boolean isBookmarked(int userId, long historyId) {
        return historyId > 0 && repository.existsForHistory(userId, historyId);
    }

    public boolean toggle(int userId, long historyId, ScienceSubject subject, String title, String content) {
        if (historyId <= 0 || content == null || content.trim().isEmpty()) return false;
        if (repository.existsForHistory(userId, historyId)) {
            repository.deleteForHistory(userId, historyId);
            return false;
        }
        repository.save(new ScienceBookmark(
                0,
                userId,
                historyId,
                subject,
                title == null || title.trim().isEmpty() ? "Saved science answer" : title.trim(),
                content.trim(),
                System.currentTimeMillis()
        ));
        return true;
    }
}
