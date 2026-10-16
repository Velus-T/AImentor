package com.brightpath.science.domain.service;

import com.brightpath.science.domain.model.ScienceSubject;

/** Subject-specific teaching strategy for one Science AI tutor. */
public interface ScienceTutorPromptStrategy {
        /*
 * Responsibility: Retrieves the data/state required by `getSubject` from the appropriate subject-specific prompt builder/strategy.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
ScienceSubject getSubject();
        /*
 * Responsibility: Retrieves the data/state required by `getTutorName` from the appropriate subject-specific prompt builder/strategy.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
String getTutorName();
        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `buildSubjectInstructions`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
String buildSubjectInstructions();
}
