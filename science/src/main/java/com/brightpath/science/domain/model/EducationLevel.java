package com.brightpath.science.domain.model;

import com.brightpath.science.R;

public enum EducationLevel {
    MIDDLE_SCHOOL(R.string.science_edu_middle),
    HIGH_SCHOOL(R.string.science_edu_high),
    UNIVERSITY(R.string.science_edu_uni);

    private final int displayNameResId;

        /*
 * Responsibility: Initializes EducationLevel and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
EducationLevel(int displayNameResId) { this.displayNameResId = displayNameResId; }
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
        if (this == HIGH_SCHOOL) return "High school";
        if (this == UNIVERSITY) return "University";
        return "Middle school";
    }

        /*
 * Responsibility: Converts input data in `fromPosition` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
public static EducationLevel fromPosition(int position) {
        EducationLevel[] values = values();
        return position >= 0 && position < values.length ? values[position] : MIDDLE_SCHOOL;
    }
}
