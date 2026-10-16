package com.brightpath.science.domain.prompt;

import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.domain.service.ScienceTutorPromptStrategy;

/** Chemistry-specific AI tutoring rules. */
public final class ChemistryTutorPromptStrategy implements ScienceTutorPromptStrategy {
        /*
 * Responsibility: Retrieves the data/state required by `getSubject` from the appropriate subject-specific prompt builder/strategy.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public ScienceSubject getSubject() {
        return ScienceSubject.CHEMISTRY;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getTutorName` from the appropriate subject-specific prompt builder/strategy.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public String getTutorName() {
        return "Chemistry AI Tutor";
    }

        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `buildSubjectInstructions`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
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
