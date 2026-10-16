package com.brightpath.science.domain.repository;

import com.brightpath.science.domain.model.StudyPlan;

public interface StudyPlanRepository {
        /*
 * Responsibility: Retrieves the data/state required by `getLatest` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
StudyPlan getLatest(int userId);
        /*
 * Responsibility: Persists the data supplied to `save` through the appropriate Science storage/repository.
 * Purpose: Keeps important learner state available after navigation or application restart.
 */
void save(int userId, StudyPlan plan);
        /*
 * Responsibility: Removes or resets the data/state handled by `clear`.
 * Purpose: Prevents obsolete state from affecting the next Science operation and supports controlled CRUD behavior.
 */
void clear(int userId);
}
