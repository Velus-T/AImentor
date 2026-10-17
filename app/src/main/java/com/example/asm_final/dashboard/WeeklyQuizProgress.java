package com.example.asm_final.dashboard;

public final class WeeklyQuizProgress {
  public final long experience, completedQuizzes, startsAt, endsAt;

  public WeeklyQuizProgress(long experience, long completedQuizzes, long startsAt, long endsAt) {
    this.experience = experience;
    this.completedQuizzes = completedQuizzes;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
  }
}
