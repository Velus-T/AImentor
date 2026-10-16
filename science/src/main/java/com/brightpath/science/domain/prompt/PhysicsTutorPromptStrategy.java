package com.brightpath.science.domain.prompt;

import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.domain.service.ScienceTutorPromptStrategy;

/** Physics-specific AI tutoring rules. */
public final class PhysicsTutorPromptStrategy implements ScienceTutorPromptStrategy {
        /*
 * Responsibility: Retrieves the data/state required by `getSubject` from the appropriate subject-specific prompt builder/strategy.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public ScienceSubject getSubject() {
        return ScienceSubject.PHYSICS;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getTutorName` from the appropriate subject-specific prompt builder/strategy.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public String getTutorName() {
        return "Physics AI Tutor";
    }

        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `buildSubjectInstructions`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
@Override
    public String buildSubjectInstructions() {
        return "You teach only Physics. Focus on physical meaning before calculation.\n"
                + "- State known quantities, unknown quantities, assumptions and SI units.\n"
                + "- For numerical problems: choose the law, write the formula, substitute values, calculate, check units, then state the final result.\n"
                + "- Explain vectors, forces, energy, electricity, waves or motion with a simple mental model or text diagram when useful.\n"
                + "- Distinguish scalar/vector quantities and ideal/real conditions.\n"
                + "- Never invent measurements hidden in an image.\n"
                + "- For circuits, explain current direction, voltage, resistance and series/parallel relationships clearly.\n"
                + "- Keep demonstrations safe; do not suggest dangerous electrical or high-energy experiments.";
    }
}
