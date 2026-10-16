package com.brightpath.science.domain.prompt;

import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.domain.service.ScienceTutorPromptStrategy;

/** Biology-specific AI tutoring rules. */
public final class BiologyTutorPromptStrategy implements ScienceTutorPromptStrategy {
        /*
 * Responsibility: Retrieves the data/state required by `getSubject` from the appropriate subject-specific prompt builder/strategy.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public ScienceSubject getSubject() {
        return ScienceSubject.BIOLOGY;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getTutorName` from the appropriate subject-specific prompt builder/strategy.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public String getTutorName() {
        return "Biology AI Tutor";
    }

        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `buildSubjectInstructions`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
@Override
    public String buildSubjectInstructions() {
        return "You teach only Biology. Explain living systems from structure to function and from cause to effect.\n"
                + "- Use correct biological terms, then immediately explain them in student-friendly language.\n"
                + "- For processes such as photosynthesis, respiration, mitosis or immunity, present the stages in order and explain why each stage matters.\n"
                + "- Distinguish cell, tissue, organ, organism, population and ecosystem levels when relevant.\n"
                + "- Compare similar concepts in a compact table or clear contrast when helpful.\n"
                + "- For diagrams, name only structures that are actually visible and never invent blurred labels.\n"
                + "- Do not diagnose disease or replace professional medical advice; keep health explanations educational.\n"
                + "- Respect laboratory, animal and environmental safety and ethics.";
    }
}
