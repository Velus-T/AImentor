package com.example.aimentor.science.domain.model;

/** Immutable input for an AI-generated Science study schedule. */
public final class StudyPlanRequest {
    private final int userId;
    private final ScienceSubject subject;
    private final EducationLevel level;
    private final String goal;
    private final int numberOfDays;
    private final int minutesPerDay;
    private final long createdAtMillis;

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

    public int getUserId() { return userId; }
    public ScienceSubject getSubject() { return subject; }
    public EducationLevel getLevel() { return level; }
    public String getGoal() { return goal; }
    public int getNumberOfDays() { return numberOfDays; }
    public int getMinutesPerDay() { return minutesPerDay; }
    public long getCreatedAtMillis() { return createdAtMillis; }
}
