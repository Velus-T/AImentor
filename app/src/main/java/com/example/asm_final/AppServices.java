package com.example.asm_final;

import android.content.Context;
import com.example.asm_final.accounts.*;
import com.example.asm_final.dashboard.*;
import java.util.concurrent.*;

/** Application composition root. Screens consume focused contracts rather than database helpers. */
public final class AppServices {
  private final AccountRepository accounts;
  private final DashboardRepository dashboard;
  private final LearningStreakRepository streaks;
  private final WeeklyQuizRepository weeklyQuizzes;
  private final UserProfileRepository profiles;
  private final ExecutorService io = Executors.newFixedThreadPool(2);

  public AppServices(Context context) {
    accounts = new LocalAccountRepository(context);
    profiles = new LocalUserProfileRepository(context);
    dashboard = LocalSubjectSources.create(context);
    streaks = new LocalLearningStreakRepository(context);
    weeklyQuizzes = new LocalWeeklyQuizRepository(context);
  }

  public UserAuthenticator authenticator() {
    return accounts;
  }

  public UserRegistration registration() {
    return accounts;
  }

  public AccountLookup accountLookup() {
    return accounts;
  }

  public UserProfileRepository profiles() {
    return profiles;
  }

  public LearningStreakRepository streaks() {
    return streaks;
  }

  public WeeklyQuizRepository weeklyQuizzes() {
    return weeklyQuizzes;
  }

  public DashboardRepository dashboard() {
    return dashboard;
  }

  public ExecutorService io() {
    return io;
  }
}
