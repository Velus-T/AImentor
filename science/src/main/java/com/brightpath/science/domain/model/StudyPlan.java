package com.brightpath.science.domain.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StudyPlan {
    private final long createdAtMillis;
    private final ScienceSubject subject;
    private final EducationLevel level;
    private final String goal;
    private final boolean generatedByAi;
    private final List<StudySession> sessions;

        /*
 * Responsibility: Initializes StudyPlan and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public StudyPlan(long createdAtMillis,
                     ScienceSubject subject,
                     EducationLevel level,
                     String goal,
                     boolean generatedByAi,
                     List<StudySession> sessions) {
        this.createdAtMillis = Math.max(0L, createdAtMillis);
        this.subject = subject == null ? ScienceSubject.PHYSICS : subject;
        this.level = level == null ? EducationLevel.MIDDLE_SCHOOL : level;
        this.goal = goal == null ? "" : goal;
        this.generatedByAi = generatedByAi;
        this.sessions = sessions == null ? new ArrayList<>() : new ArrayList<>(sessions);
    }

        /*
 * Responsibility: Retrieves the data/state required by `getCreatedAtMillis` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public long getCreatedAtMillis() { return createdAtMillis; }
        /*
 * Responsibility: Retrieves the data/state required by `getSubject` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public ScienceSubject getSubject() { return subject; }
        /*
 * Responsibility: Retrieves the data/state required by `getLevel` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public EducationLevel getLevel() { return level; }
        /*
 * Responsibility: Retrieves the data/state required by `getGoal` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getGoal() { return goal; }
        /*
 * Responsibility: Checks the condition represented by `isGeneratedByAi` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean isGeneratedByAi() { return generatedByAi; }
        /*
 * Responsibility: Retrieves the data/state required by `getSessions` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public List<StudySession> getSessions() { return Collections.unmodifiableList(sessions); }

        /*
 * Responsibility: Converts input data in `toJson` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("createdAtMillis", createdAtMillis);
            json.put("subject", subject.name());
            json.put("level", level.name());
            json.put("goal", goal);
            json.put("generatedByAi", generatedByAi);
            JSONArray array = new JSONArray();
            for (StudySession session : sessions) array.put(session.toJson());
            json.put("sessions", array);
        } catch (Exception ignored) { }
        return json;
    }

        /*
 * Responsibility: Converts input data in `fromJson` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
public static StudyPlan fromJson(JSONObject json) {
        if (json == null) return null;
        try {
            ScienceSubject subject = ScienceSubject.valueOf(
                    json.optString("subject", ScienceSubject.PHYSICS.name()));
            EducationLevel level = EducationLevel.valueOf(
                    json.optString("level", EducationLevel.MIDDLE_SCHOOL.name()));
            List<StudySession> sessions = new ArrayList<>();
            JSONArray array = json.optJSONArray("sessions");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    StudySession session = StudySession.fromJson(array.optJSONObject(i));
                    if (session != null) sessions.add(session);
                }
            }
            return new StudyPlan(
                    json.optLong("createdAtMillis", 0L),
                    subject,
                    level,
                    json.optString("goal", ""),
                    json.optBoolean("generatedByAi", false),
                    sessions
            );
        } catch (Exception ignored) {
            return null;
        }
    }
}
