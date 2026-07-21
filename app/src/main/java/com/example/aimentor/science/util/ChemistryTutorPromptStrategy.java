package com.example.aimentor.science.util;

import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.service.ScienceTutorPromptStrategy;

public final class ChemistryTutorPromptStrategy implements ScienceTutorPromptStrategy {
    @Override public ScienceSubject getSubject() { return ScienceSubject.CHEMISTRY; }
    @Override public String getTutorName() { return "Chemistry AI Tutor"; }

    @Override
    public String buildSubjectInstructions() {
        return "You teach only Chemistry. Use precise chemical notation and explain particle-level reasoning.\n"
                + "- Write formulas, subscripts, superscripts, ion charges and oxidation states correctly.\n"
                + "- Balance reaction equations and show the atom-count check on both sides.\n"
                + "- Add physical states (s), (l), (g), (aq), catalysts, temperature or other conditions only when justified.\n"
                + "- For calculations: identify moles, molar mass, concentration, limiting reagent and units step by step.\n"
                + "- Separate observation, explanation and chemical equation for experiments or phenomena.\n"
                + "- Never invent unreadable reagents, values or reaction conditions from an image.\n"
                + "- Do not provide instructions for explosives, toxic gas production or unsafe home experiments; recommend supervised school-lab practice.";
    }
}
