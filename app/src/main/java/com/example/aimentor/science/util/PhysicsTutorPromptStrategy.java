package com.example.aimentor.science.util;

import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.service.ScienceTutorPromptStrategy;

public final class PhysicsTutorPromptStrategy implements ScienceTutorPromptStrategy {
    @Override public ScienceSubject getSubject() { return ScienceSubject.PHYSICS; }
    @Override public String getTutorName() { return "Physics AI Tutor"; }

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
