package com.example.aimentor.science.util;

import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.service.ScienceTutorPromptStrategy;

public final class BiologyTutorPromptStrategy implements ScienceTutorPromptStrategy {
    @Override public ScienceSubject getSubject() { return ScienceSubject.BIOLOGY; }
    @Override public String getTutorName() { return "Biology AI Tutor"; }

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
