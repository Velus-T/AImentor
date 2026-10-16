package com.brightpath.science.domain.model;

/** Immutable input for an AI-generated Science study schedule. */
public final class StudyPlanRequest {
    private final int userId;
    private final ScienceSubject subject;
    private final EducationLevel level;
    private final String goal;
    private final int numberOfDays;
    private final int minutesPerDay;
    private final long createdAtMillis;

        /*
 * Responsibility: Initializes StudyPlanRequest and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public StudyPlanRequest(int userId,
                            ScienceSubject subject,
                            EducationLevel level,
                            String goal,
                            int numberOfDays,
                            int minutesPerDay,
                            long createdAtMillis) {
        this.userId = userId;
        this.subject = subject == null ? ScienceSubject.PHYSICS : subject;
        this.level = level == null ? EducationLevel.MIDDLE_SCHOOL : level;
        this.goal = goal == null || goal.trim().isEmpty()
                ? "Improve understanding and practice consistently" : goal.trim();
        this.numberOfDays = Math.max(1, Math.min(numberOfDays, 30));
        this.minutesPerDay = Math.max(20, Math.min(minutesPerDay, 240));
        this.createdAtMillis = Math.max(0L, createdAtMillis);
    }

        /*
 * Responsibility: Retrieves the data/state required by `getUserId` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getUserId() { return userId; }
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
 * Responsibility: Retrieves the data/state required by `getNumberOfDays` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getNumberOfDays() { return numberOfDays; }
        /*
 * Responsibility: Retrieves the data/state required by `getMinutesPerDay` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getMinutesPerDay() { return minutesPerDay; }
        /*
 * Responsibility: Retrieves the data/state required by `getCreatedAtMillis` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public long getCreatedAtMillis() { return createdAtMillis; }
}
