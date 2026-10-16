package com.brightpath.science.domain.model;

import com.brightpath.science.R;

public enum ScienceSubject {
    PHYSICS("PHYSICS", R.string.science_subj_physics),
    CHEMISTRY("CHEMISTRY", R.string.science_subj_chemistry),
    BIOLOGY("BIOLOGY", R.string.science_subj_biology);

    private final String code;
    private final int displayNameResId;

        /*
 * Responsibility: Initializes ScienceSubject and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
ScienceSubject(String code, int displayNameResId) {
        this.code = code;
        this.displayNameResId = displayNameResId;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getCode` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getCode() { return code; }
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
        if (this == CHEMISTRY) return "Chemistry";
        if (this == BIOLOGY) return "Biology";
        return "Physics";
    }

        /*
 * Responsibility: Converts input data in `fromCode` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
public static ScienceSubject fromCode(String value) {
        if (value == null) return PHYSICS;
        for (ScienceSubject subject : values()) {
            if (subject.code.equalsIgnoreCase(value)) {
                return subject;
            }
        }
        return PHYSICS;
    }
}
