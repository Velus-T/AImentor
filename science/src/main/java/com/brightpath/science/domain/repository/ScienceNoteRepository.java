package com.brightpath.science.domain.repository;

import com.brightpath.science.domain.model.ScienceNote;

import java.util.List;

/**
 * Domain abstraction for personal Science notes.
 *
 * <p>Presentation code depends on this interface, not on SQLite directly. This keeps
 * the UI independent from the storage technology and demonstrates Dependency Inversion.</p>
 */
public interface ScienceNoteRepository {
        /*
 * Responsibility: Persists the data supplied to `insert` through the appropriate Science storage/repository.
 * Purpose: Keeps important learner state available after navigation or application restart.
 */
long insert(int userId, String title, String content, String subject);

        /*
 * Responsibility: Updates the state or value handled by `update` for ScienceNoteRepository.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
boolean update(long id, int userId, String title, String content, String subject);

        /*
 * Responsibility: Removes or resets the data/state handled by `delete`.
 * Purpose: Prevents obsolete state from affecting the next Science operation and supports controlled CRUD behavior.
 */
boolean delete(long id, int userId);

        /*
 * Responsibility: Retrieves the data/state required by `getAll` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
List<ScienceNote> getAll(int userId);
}
