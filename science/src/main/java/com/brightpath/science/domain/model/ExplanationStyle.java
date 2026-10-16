package com.brightpath.science.domain.model;

import com.brightpath.science.R;

public enum ExplanationStyle {
    SHORT(R.string.science_style_short),
    DETAILED(R.string.science_style_detailed),
    STEP_BY_STEP(R.string.science_style_step_by_step);

    private final int displayNameResId;

        /*
 * Responsibility: Initializes ExplanationStyle and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
ExplanationStyle(int displayNameResId) { this.displayNameResId = displayNameResId; }
        /*
 * Responsibility: Retrieves the data/state required by `getDisplayNameResId` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getDisplayNameResId() { return displayNameResId; }

        /*
 * Responsibility: Retrieves the data/state required by `getDisplayName` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getDisplayName() {
        if (this == SHORT) return "Short";
        if (this == DETAILED) return "Detailed";
        return "Step-by-step";
    }

        /*
 * Responsibility: Converts input data in `fromPosition` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
public static ExplanationStyle fromPosition(int position) {
        ExplanationStyle[] values = values();
        return position >= 0 && position < values.length ? values[position] : STEP_BY_STEP;
    }
}
