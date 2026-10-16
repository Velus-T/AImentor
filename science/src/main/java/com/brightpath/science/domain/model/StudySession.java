package com.brightpath.science.domain.model;

import org.json.JSONObject;

public final class StudySession {
    private final int dayNumber;
    private final long startAtMillis;
    private final int durationMinutes;
    private final String title;
    private final String activities;
    private boolean completed;

        /*
 * Responsibility: Initializes StudySession and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public StudySession(int dayNumber,
                        long startAtMillis,
                        int durationMinutes,
                        String title,
                        String activities,
                        boolean completed) {
        this.dayNumber = Math.max(1, dayNumber);
        this.startAtMillis = Math.max(0L, startAtMillis);
        this.durationMinutes = Math.max(10, Math.min(durationMinutes, 240));
        this.title = title == null || title.trim().isEmpty() ? "Science study session" : title.trim();
        this.activities = activities == null ? "Review, practise and check understanding." : activities.trim();
        this.completed = completed;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getDayNumber` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getDayNumber() { return dayNumber; }
        /*
 * Responsibility: Retrieves the data/state required by `getStartAtMillis` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public long getStartAtMillis() { return startAtMillis; }
        /*
 * Responsibility: Retrieves the data/state required by `getDurationMinutes` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getDurationMinutes() { return durationMinutes; }
        /*
 * Responsibility: Retrieves the data/state required by `getTitle` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getTitle() { return title; }
        /*
 * Responsibility: Retrieves the data/state required by `getActivities` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getActivities() { return activities; }
        /*
 * Responsibility: Checks the condition represented by `isCompleted` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean isCompleted() { return completed; }
        /*
 * Responsibility: Updates the state or value handled by `setCompleted` for StudySession.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
public void setCompleted(boolean completed) { this.completed = completed; }

        /*
 * Responsibility: Converts input data in `toJson` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("dayNumber", dayNumber);
            json.put("startAtMillis", startAtMillis);
            json.put("durationMinutes", durationMinutes);
            json.put("title", title);
            json.put("activities", activities);
            json.put("completed", completed);
        } catch (Exception ignored) { }
        return json;
    }

        /*
 * Responsibility: Converts input data in `fromJson` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
public static StudySession fromJson(JSONObject json) {
        if (json == null) return null;
        return new StudySession(
                json.optInt("dayNumber", 1),
                json.optLong("startAtMillis", 0L),
                json.optInt("durationMinutes", 30),
                json.optString("title", "Science study session"),
                json.optString("activities", "Review and practise."),
                json.optBoolean("completed", false)
        );
    }
}
