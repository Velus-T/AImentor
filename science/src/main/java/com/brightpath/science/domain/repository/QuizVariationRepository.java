package com.brightpath.science.domain.repository;

import com.brightpath.science.domain.model.EducationLevel;
import com.brightpath.science.domain.model.ScienceSubject;

import java.util.List;

public interface QuizVariationRepository {
        /*
 * Responsibility: Retrieves the data/state required by `getRecentPrompts` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
List<String> getRecentPrompts(int userId, ScienceSubject subject,
                                  EducationLevel level, int limit);

        /*
 * Responsibility: Performs the repository operation `rememberPrompts` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
void rememberPrompts(int userId, ScienceSubject subject,
                         EducationLevel level, List<String> prompts);
}
