package com.brightpath.science.domain.repository;

import com.brightpath.science.domain.model.ScienceBookmark;
import com.brightpath.science.domain.model.ScienceSubject;

import java.util.List;

public interface ScienceBookmarkRepository {
        /*
 * Responsibility: Persists the data supplied to `save` through the appropriate Science storage/repository.
 * Purpose: Keeps important learner state available after navigation or application restart.
 */
long save(ScienceBookmark bookmark);
        /*
 * Responsibility: Performs the repository operation `existsForHistory` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
boolean existsForHistory(int userId, long historyId);
        /*
 * Responsibility: Removes or resets the data/state handled by `deleteForHistory`.
 * Purpose: Prevents obsolete state from affecting the next Science operation and supports controlled CRUD behavior.
 */
void deleteForHistory(int userId, long historyId);
        /*
 * Responsibility: Performs the repository operation `search` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
List<ScienceBookmark> search(int userId, ScienceSubject subject, String keyword);
        /*
 * Responsibility: Performs the repository operation `countByUser` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
int countByUser(int userId);
}
