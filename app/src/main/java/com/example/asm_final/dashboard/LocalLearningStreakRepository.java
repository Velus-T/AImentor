package com.example.asm_final.dashboard;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.mentor.data.*;
import java.util.*;
import java.util.function.*;

/** Imports completed quizzes once, then reads the independent daily ledger. */
public final class LocalLearningStreakRepository implements LearningStreakRepository {
  private final Context context;
  private final LongSupplier clock;
  private final Supplier<TimeZone> zone;

  public LocalLearningStreakRepository(Context context) {
    this(context, System::currentTimeMillis, TimeZone::getDefault);
  }

  public LocalLearningStreakRepository(
      Context context, LongSupplier clock, Supplier<TimeZone> zone) {
    this.context = context.getApplicationContext();
    this.clock = clock;
    this.zone = zone;
  }

  @Override
  public StreakSnapshot load(int userId) {
    long now = clock.getAsLong();
    TimeZone timeZone = zone.get();
    if (userId <= 0) return StreakPolicy.calculate(Collections.emptyList(), now, timeZone);
    QuizModuleStorage.prepare(context);
    try (com.example.historymodule.databases.SqliteDbHelper helper =
        new com.example.historymodule.databases.SqliteDbHelper(context)) {
      SQLiteDatabase db = helper.getWritableDatabase();
      db.beginTransaction();
      try {
        LearningDays.ensure(db);
        boolean initialized;
        try (Cursor c =
            db.rawQuery(
                "SELECT 1 FROM mentor_streak_imports WHERE user_id=?",
                new String[] {String.valueOf(userId)})) {
          initialized = c.moveToFirst();
        }
        if (!initialized) {
          String[] queries = {
            "SELECT timestamp FROM math_quiz_attempts WHERE user_id=? AND total_count>0",
            "SELECT created_at FROM history_quiz_attempts WHERE user_id=? AND total_count>0",
            "SELECT created_at FROM science_quiz_attempts WHERE user_id=? AND total_questions>0",
            "SELECT created_at FROM programming_quiz_attempts WHERE user_id=? AND total_questions>0"
          };
          for (String sql : queries)
            try (Cursor c = db.rawQuery(sql, new String[] {String.valueOf(userId)})) {
              while (c.moveToNext()) {
                long time = Timestamps.parse(c.getString(0));
                if (time > 0 && time <= now) LearningDays.record(db, userId, time, timeZone);
              }
            }
          db.execSQL(
              "INSERT INTO mentor_streak_imports(user_id,initialized_at) VALUES(?,?)",
              new Object[] {userId, now});
        }
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
      List<Long> days = new ArrayList<>();
      try (Cursor c =
          db.rawQuery(
              "SELECT day_id FROM mentor_learning_days WHERE user_id=? ORDER BY day_id",
              new String[] {String.valueOf(userId)})) {
        while (c.moveToNext()) days.add(c.getLong(0));
      }
      return StreakPolicy.calculate(days, now, timeZone);
    }
  }
}
