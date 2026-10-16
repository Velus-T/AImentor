package com.brightpath.science.domain.usecase;

import com.brightpath.science.domain.model.ScienceBookmark;
import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.domain.repository.ScienceBookmarkRepository;

public final class SaveBookmarkUseCase {
    private final ScienceBookmarkRepository repository;

        /*
 * Responsibility: Initializes SaveBookmarkUseCase and receives or prepares the dependencies/state required by this application/business use-case.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public SaveBookmarkUseCase(ScienceBookmarkRepository repository) {
        this.repository = repository;
    }

        /*
 * Responsibility: Checks the condition represented by `isBookmarked` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean isBookmarked(int userId, long historyId) {
        return historyId > 0 && repository.existsForHistory(userId, historyId);
    }

        /*
 * Responsibility: Converts input data in `toggle` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
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
