package com.brightpath.science.domain.repository;

import com.brightpath.science.domain.model.ScienceHistory;
import com.brightpath.science.domain.model.ScienceQuestion;
import com.brightpath.science.domain.model.ScienceSubject;

import java.util.List;

public interface ScienceHistoryRepository {
        /*
 * Responsibility: Persists the data supplied to `save` through the appropriate Science storage/repository.
 * Purpose: Keeps important learner state available after navigation or application restart.
 */
long save(ScienceQuestion question, String answer);
        /*
 * Responsibility: Retrieves the data/state required by `findCached` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
ScienceHistory findCached(int userId, ScienceSubject subject, String educationLevel, String normalizedQuestion);
        /*
 * Responsibility: Retrieves the data/state required by `findById` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
ScienceHistory findById(int userId, long historyId);
        /*
 * Responsibility: Retrieves the data/state required by `findConversation` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
List<ScienceHistory> findConversation(int userId, String conversationId);
        /*
 * Responsibility: Performs the repository operation `search` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
List<ScienceHistory> search(int userId, ScienceSubject subject, String keyword);
        /*
 * Responsibility: Adds the data, listener, duration, key, or UI element represented by `addReviewDuration`.
 * Purpose: Keeps the incremental update explicit and isolated from unrelated responsibilities.
 */
void addReviewDuration(long historyId, int seconds);
        /*
 * Responsibility: Performs the repository operation `countByUser` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
int countByUser(int userId);
        /*
 * Responsibility: Converts input data in `totalReviewSeconds` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
int totalReviewSeconds(int userId);
        /*
 * Responsibility: Performs the repository operation `mostFrequentSubject` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
ScienceSubject mostFrequentSubject(int userId);
}
