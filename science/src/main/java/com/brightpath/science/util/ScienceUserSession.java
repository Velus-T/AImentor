package com.brightpath.science.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Module-owned user session. The host passes its user id through ScienceFeature.open(...). */
public final class ScienceUserSession {
    private static final String FILE_NAME = "SCIENCE_HOST_SESSION";
    private static final String KEY_USER_ID = "science_user_id";

    private final SharedPreferences preferences;

        /*
 * Responsibility: Initializes ScienceUserSession and receives or prepares the dependencies/state required by this Science Module component.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceUserSession(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

        /*
 * Responsibility: Retrieves the data/state required by `getUserId` from the appropriate Science Module component.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getUserId() {
        return Math.max(0, preferences.getInt(KEY_USER_ID, 0));
    }

        /*
 * Responsibility: Updates the state or value handled by `setUserId` for ScienceUserSession.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
public static void setUserId(Context context, int userId) {
        context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_USER_ID, Math.max(0, userId))
                .apply();
    }
}
