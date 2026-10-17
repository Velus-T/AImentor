package com.example.asm_final.dashboard;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.mentor.data.Timestamps;
import com.example.mentor.data.WeeklyQuizPolicy;
import java.util.TimeZone;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/** Weekly XP across subjects, never lifetime EXP or a rank fabricated from local accounts. */
public final class LocalWeeklyQuizRepository implements WeeklyQuizRepository {
  private final Context context;
  private final LongSupplier clock;
  private final Supplier<TimeZone> zone;

  public LocalWeeklyQuizRepository(Context context) {
    this(context, System::currentTimeMillis, TimeZone::getDefault);
  }

  public LocalWeeklyQuizRepository(Context context, LongSupplier clock, Supplier<TimeZone> zone) {
    this.context = context.getApplicationContext();
    this.clock = clock;
    this.zone = zone;
  }

  @Override
  public WeeklyQuizProgress load(int userId) {
    long now = clock.getAsLong();
    long[] week = WeeklyQuizPolicy.week(now, zone.get());
    long xp = 0, attempts = 0;
    if (userId <= 0) return new WeeklyQuizProgress(0, 0, week[0], week[1]);
    QuizModuleStorage.prepare(context);
    try (com.example.historymodule.databases.SqliteDbHelper helper =
        new com.example.historymodule.databases.SqliteDbHelper(context)) {
      SQLiteDatabase db = helper.getReadableDatabase();
      String[] queries = {
        "SELECT timestamp,exp_earned FROM math_quiz_attempts WHERE user_id=? AND total_count>0",
        "SELECT created_at,exp_earned FROM history_quiz_attempts WHERE user_id=? AND total_count>0",
        "SELECT created_at,exp_earned FROM science_quiz_attempts WHERE user_id=? AND"
            + " total_questions>0",
        "SELECT created_at,exp_earned FROM programming_quiz_attempts WHERE user_id=? AND"
            + " total_questions>0"
      };
      for (String sql : queries)
        try (Cursor c = db.rawQuery(sql, new String[] {String.valueOf(userId)})) {
          while (c.moveToNext()) {
            long time = Timestamps.parse(c.getString(0));
            if (time >= week[0] && time < week[1] && time <= now) {
              attempts++;
              xp += Math.max(0, c.getLong(1));
            }
          }
        }
    }
    return new WeeklyQuizProgress(xp, attempts, week[0], week[1]);
  }
}
