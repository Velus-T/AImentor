package com.example.mentor.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.TimeZone;

/**
 * Persistent day ledger, independent of deletable chat/history. Writes join the learning
 * transaction.
 */
public final class LearningDays {
  private LearningDays() {}

  public static void ensure(SQLiteDatabase db) {
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS mentor_learning_days(user_id INTEGER NOT NULL,day_id INTEGER"
            + " NOT NULL,completed_at INTEGER NOT NULL,PRIMARY KEY(user_id,day_id))");
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS mentor_streak_imports(user_id INTEGER PRIMARY"
            + " KEY,initialized_at INTEGER NOT NULL)");
    try (Cursor c =
        db.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name='users'", null)) {
      if (c.moveToFirst())
        db.execSQL(
            "CREATE TRIGGER IF NOT EXISTS mentor_streak_account_delete AFTER DELETE ON users BEGIN"
                + " DELETE FROM mentor_learning_days WHERE user_id=OLD.id; DELETE FROM"
                + " mentor_streak_imports WHERE user_id=OLD.id; END");
    }
  }

  public static void record(SQLiteDatabase db, int userId, long completedAt) {
    record(db, userId, completedAt, TimeZone.getDefault());
  }

  public static void record(SQLiteDatabase db, int userId, long completedAt, TimeZone zone) {
    if (userId <= 0 || completedAt <= 0) return;
    ensure(db);
    db.execSQL(
        "INSERT OR IGNORE INTO mentor_learning_days(user_id,day_id,completed_at) VALUES(?,?,?)",
        new Object[] {userId, StreakPolicy.dayId(completedAt, zone), completedAt});
  }

  public static long insert(
      SQLiteDatabase db, String table, ContentValues values, int userId, long completedAt) {
    boolean ownTransaction = !db.inTransaction();
    if (ownTransaction) db.beginTransaction();
    try {
      long id = db.insert(table, null, values);
      if (id > 0) record(db, userId, completedAt);
      if (ownTransaction) db.setTransactionSuccessful();
      return id;
    } finally {
      if (ownTransaction) db.endTransaction();
    }
  }
}
