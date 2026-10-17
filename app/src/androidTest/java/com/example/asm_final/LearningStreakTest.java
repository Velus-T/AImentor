package com.example.asm_final;

import static org.junit.Assert.*;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.asm_final.dashboard.LocalLearningStreakRepository;
import com.example.mentor.data.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LearningStreakTest {
  private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
  private int owner, previous;
  private com.example.historymodule.databases.SqliteDbHelper history;
  private LocalLearningStreakRepository repository;
  private SQLiteDatabase db;

  @Before
  public void prepare() {
    previous = ActiveUser.get(context);
    owner =
        (int)
            ((AiMentorApplication) context.getApplicationContext())
                .services()
                .registration()
                .register(
                    "streak_qa_" + System.nanoTime(),
                    "Mentor01!2026",
                    "qa@gmail.com",
                    "0901234567");
    assertTrue(owner > 0);
    ActiveUser.set(context, owner);
    repository = new LocalLearningStreakRepository(context);
    assertEquals(0, repository.load(owner).current);
    history = new com.example.historymodule.databases.SqliteDbHelper(context);
    db = history.getWritableDatabase();
  }

  @After
  public void cleanup() {
    if (db != null && db.isOpen()) {
      db.execSQL("DROP TRIGGER IF EXISTS qa_streak_fail");
      for (String table :
          new String[] {
            "math_history",
            "math_quiz_attempts",
            "history_quiz_attempts",
            "history_messages",
            "science_quiz_attempts",
            "programming_quiz_attempts",
            "programming_user_progress"
          }) {
        try {
          db.delete(table, "user_id=?", new String[] {String.valueOf(owner)});
        } catch (android.database.SQLException ignored) {
        }
      }
      db.delete("users", "id=?", new String[] {String.valueOf(owner)});
    }
    if (history != null) history.close();
    ActiveUser.set(context, previous);
  }

  private long count(String table) {
    try (Cursor c =
        db.rawQuery(
            "SELECT COUNT(*) FROM " + table + " WHERE user_id=?",
            new String[] {String.valueOf(owner)})) {
      c.moveToFirst();
      return c.getLong(0);
    }
  }

  private void clearDays() {
    db.delete("mentor_learning_days", "user_id=?", new String[] {String.valueOf(owner)});
  }

  @Test
  public void chatsAndEmptyAttemptsDoNotStartStreak() {
    history.saveChatMessage(owner, "qa", "me", "Question", false);
    history.saveChatMessage(owner, "qa", "bot", "Answer", false);
    try (com.example.mathmodule.databases.SqliteDbHelper math =
        new com.example.mathmodule.databases.SqliteDbHelper(context)) {
      assertTrue(math.insertHistoryForUser(owner, "Question", "Answer") > 0);
    }
    assertTrue(history.saveQuizAttempt(owner, "Topic", "High", 0, 0, 0) > 0);
    assertEquals(0, repository.load(owner).current);
    assertEquals(0, count("mentor_learning_days"));
  }

  @Test
  public void everySubjectQuizStartsStreakAndSameDayCountsOnce() {
    assertTrue(history.saveQuizAttempt(owner, "Topic", "High", 0, 5, 0) > 0);
    assertTrue(repository.load(owner).completedToday);
    clearDays();
    try (com.example.mathmodule.databases.SqliteDbHelper math =
        new com.example.mathmodule.databases.SqliteDbHelper(context)) {
      assertTrue(math.saveQuizAttemptAndAddExp(0, 1, 5, 5));
    }
    assertTrue(repository.load(owner).completedToday);
    clearDays();
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper programming =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      assertTrue(programming.saveQuizResult(owner, "Programming", "Basic", "Topic", 5, 1, 5) > 0);
    }
    assertTrue(repository.load(owner).completedToday);
    clearDays();
    try (com.brightpath.science.data.local.ScienceDbHelper science =
        new com.brightpath.science.data.local.ScienceDbHelper(context)) {
      assertTrue(
          new com.brightpath.science.data.repository.SQLiteQuizAttemptRepository(science)
                  .saveAttempt(
                      owner,
                      com.brightpath.science.domain.model.ScienceSubject.PHYSICS,
                      com.brightpath.science.domain.model.EducationLevel.MIDDLE_SCHOOL,
                      "Topic",
                      com.brightpath.science.domain.model.QuizChallenge.ON_LEVEL,
                      5,
                      1,
                      5)
              > 0);
    }
    assertTrue(repository.load(owner).completedToday);
    assertTrue(history.saveQuizAttempt(owner, "Topic", "High", 2, 5, 10) > 0);
    assertEquals(1, repository.load(owner).current);
    assertEquals(1, count("mentor_learning_days"));
    com.example.asm_final.dashboard.WeeklyQuizProgress weekly =
        new com.example.asm_final.dashboard.LocalWeeklyQuizRepository(context).load(owner);
    assertEquals(25, weekly.experience);
    assertEquals(5, weekly.completedQuizzes);
  }

  @Test
  public void legacyImportOnlyUsesQuizzesAndOnlyRunsOnce() {
    db.delete("mentor_streak_imports", "user_id=?", new String[] {String.valueOf(owner)});
    history.saveChatMessage(owner, "qa", "me", "Question", false);
    history.saveChatMessage(owner, "qa", "bot", "Answer", false);
    assertEquals(0, repository.load(owner).current);
    db.delete("mentor_streak_imports", "user_id=?", new String[] {String.valueOf(owner)});
    history.saveQuizAttempt(owner, "Topic", "High", 2, 5, 10);
    clearDays();
    assertEquals(1, repository.load(owner).current);
    db.delete("history_quiz_attempts", "user_id=?", new String[] {String.valueOf(owner)});
    assertEquals(1, repository.load(owner).current);
  }

  @Test
  public void failedLedgerWriteRollsBackQuiz() {
    db.execSQL(
        "CREATE TRIGGER qa_streak_fail BEFORE INSERT ON mentor_learning_days BEGIN SELECT"
            + " RAISE(ABORT,'qa'); END");
    try {
      history.saveQuizAttempt(owner, "Topic", "High", 1, 5, 5);
      fail("Expected storage error");
    } catch (android.database.SQLException expected) {
    }
    assertEquals(0, count("history_quiz_attempts"));
    assertEquals(0, count("mentor_learning_days"));
  }

  @Test
  public void accountIsolationAndDeletionKeepCorrectDays() {
    history.saveQuizAttempt(owner, "Topic", "High", 1, 5, 5);
    assertEquals(0, repository.load(0).current);
    db.delete("users", "id=?", new String[] {String.valueOf(owner)});
    assertEquals(0, count("mentor_learning_days"));
    assertEquals(0, count("mentor_streak_imports"));
  }

  @Test
  public void quizOnYesterdayCanBeContinuedTodayButMissedDayResets() {
    TimeZone zone = TimeZone.getDefault();
    Calendar c = Calendar.getInstance(zone);
    c.add(Calendar.DAY_OF_MONTH, -1);
    LearningDays.record(db, owner, c.getTimeInMillis(), zone);
    assertEquals(1, repository.load(owner).current);
    assertFalse(repository.load(owner).completedToday);
    LocalLearningStreakRepository tomorrow =
        new LocalLearningStreakRepository(
            context, () -> System.currentTimeMillis() + 2 * 86400000L, () -> zone);
    assertEquals(0, tomorrow.load(owner).current);
    assertEquals(1, tomorrow.load(owner).best);
  }

  @Test
  public void weeklyXpExcludesChatEmptyOldFutureAndOtherAccounts() {
    history.saveQuizAttempt(owner, "Topic", "High", 1, 5, 10);
    history.saveQuizAttempt(owner, "Topic", "High", 1, 5, 20);
    history.saveQuizAttempt(owner, "Empty", "High", 0, 0, 999);
    db.execSQL(
        "UPDATE history_quiz_attempts SET created_at=? WHERE user_id=? AND exp_earned=20",
        new Object[] {System.currentTimeMillis() + 8 * 86400000L, owner});
    com.example.asm_final.dashboard.WeeklyQuizProgress progress =
        new com.example.asm_final.dashboard.LocalWeeklyQuizRepository(context).load(owner);
    assertEquals(10, progress.experience);
    assertEquals(1, progress.completedQuizzes);
    assertEquals(
        0,
        new com.example.asm_final.dashboard.LocalWeeklyQuizRepository(context).load(0).experience);
  }
}
