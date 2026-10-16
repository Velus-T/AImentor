package com.example.mentor.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import java.io.File;
import java.util.*;

/** One physical database, isolated table namespaces and independently versioned module schemas. */
public abstract class ModuleDatabaseHelper implements AutoCloseable {
  public static final String DATABASE_NAME = "AImentor.db";
  private static final Object MIGRATION_LOCK = new Object();
  private final Context context;
  private final String legacyName;
  private final int version;
  private SQLiteDatabase database;

  protected ModuleDatabaseHelper(
      Context context, String legacyName, SQLiteDatabase.CursorFactory ignored, int version) {
    this.context = context.getApplicationContext();
    this.legacyName = legacyName;
    this.version = version;
  }

  protected Map<String, String> legacyTables() {
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    String[] tables;
    switch (legacyName) {
      case "user_db":
        tables = new String[] {"users"};
        break;
      case "MathMentor.db":
        tables =
            new String[] {
              "math_history",
              "Notes:math_notes",
              "quiz_sets:math_quiz_sets",
              "quiz_questions:math_quiz_questions",
              "user_profile:math_user_profile",
              "math_quiz_attempts"
            };
        break;
      case "history_db":
        tables =
            new String[] {
              "history_items:history_legacy_items",
              "note_items:history_notes",
              "quiz_sets:history_quiz_sets",
              "quiz_questions:history_quiz_questions",
              "chat_messages:history_messages",
              "quiz_attempts:history_quiz_attempts"
            };
        break;
      case "ProgrammingMentor.db":
        tables =
            new String[] {
              "sessions:programming_sessions",
              "messages:programming_messages",
              "chat_history:programming_chat_history",
              "notes:programming_notes",
              "programming_quiz_attempts",
              "user_progress:programming_user_progress"
            };
        break;
      case "science_ai.db":
        tables =
            new String[] {
              "science_history", "science_bookmarks", "science_quiz_attempts", "science_notes"
            };
        break;
      default:
        throw new IllegalArgumentException("No migration plan for " + legacyName);
    }
    for (String table : tables) {
      String[] parts = table.split(":");
      map.put(parts[0], parts.length == 1 ? parts[0] : parts[1]);
    }
    return map;
  }

  public final synchronized SQLiteDatabase getWritableDatabase() {
    if (database != null && database.isOpen()) return database;
    synchronized (MIGRATION_LOCK) {
      File target = context.getDatabasePath(DATABASE_NAME);
      if (!target.getParentFile().exists() && !target.getParentFile().mkdirs())
        throw new SQLiteException("Cannot create database directory");
      SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(target, null);
      boolean attached = false;
      try {
        onConfigure(db);
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS mentor_schema_versions(module TEXT PRIMARY KEY,version"
                + " INTEGER NOT NULL)");
        int current = 0;
        try (Cursor c =
            db.rawQuery(
                "SELECT version FROM mentor_schema_versions WHERE module=?",
                new String[] {legacyName})) {
          if (c.moveToFirst()) current = c.getInt(0);
        }
        if (current > version) {
          onDowngrade(db, current, version);
          throw new SQLiteException("Unsupported module downgrade");
        }
        if (current < version) {
          File legacy = context.getDatabasePath(legacyName);
          if (current == 0 && legacy.exists()) {
            db.execSQL(
                "ATTACH DATABASE ? AS legacy_import", new Object[] {legacy.getAbsolutePath()});
            attached = true;
          }
          int oldVersion = 0;
          if (attached)
            try (Cursor c = db.rawQuery("PRAGMA legacy_import.user_version", null)) {
              if (c.moveToFirst()) oldVersion = c.getInt(0);
            }
          if (oldVersion > version) {
            onDowngrade(db, oldVersion, version);
            throw new SQLiteException("Legacy schema is newer than app");
          }
          db.beginTransaction();
          try {
            db.execSQL("PRAGMA defer_foreign_keys=ON");
            if (current == 0) {
              onCreate(db);
              if (attached)
                for (Map.Entry<String, String> table : legacyTables().entrySet())
                  copyTable(db, table.getKey(), table.getValue());
              if (attached && oldVersion > 0 && oldVersion < version)
                onUpgrade(db, oldVersion, version);
              for (String table : legacyTables().values()) normalizeDates(db, table);
            } else onUpgrade(db, current, version);
            for (String table : legacyTables().values()) {
              try (Cursor c = db.rawQuery("PRAGMA foreign_key_check(" + quote(table) + ")", null)) {
                if (c.moveToFirst())
                  throw new SQLiteException("Foreign-key check failed: " + table);
              }
            }
            db.execSQL(
                "INSERT OR REPLACE INTO mentor_schema_versions(module,version) VALUES(?,?)",
                new Object[] {legacyName, version});
            db.setTransactionSuccessful();
          } finally {
            db.endTransaction();
          }
        }
        if (attached) {
          db.execSQL("DETACH DATABASE legacy_import");
          attached = false;
        }
        onOpen(db);
        database = db;
        return db;
      } catch (RuntimeException error) {
        db.close();
        throw error;
      }
    }
  }

  private static String quote(String value) {
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }

  private static List<String> columns(SQLiteDatabase db, String schema, String table) {
    List<String> result = new ArrayList<>();
    try (Cursor c = db.rawQuery("PRAGMA " + schema + ".table_info(" + quote(table) + ")", null)) {
      while (c.moveToNext()) result.add(c.getString(c.getColumnIndexOrThrow("name")));
    }
    return result;
  }

  private static void copyTable(SQLiteDatabase db, String source, String target) {
    List<String> old = columns(db, "legacy_import", source);
    if (old.isEmpty()) return;
    List<String> common = columns(db, "main", target);
    common.retainAll(old);
    if (common.isEmpty()) throw new SQLiteException("No compatible columns: " + source);
    List<String> quoted = new ArrayList<>(), compare = new ArrayList<>();
    for (String column : common) {
      quoted.add(quote(column));
      compare.add("t." + quote(column) + " IS s." + quote(column));
    }
    String names = String.join(",", quoted),
        from = "legacy_import." + quote(source),
        to = "main." + quote(target);
    db.execSQL(
        "INSERT OR REPLACE INTO " + to + " (" + names + ") SELECT " + names + " FROM " + from);
    try (Cursor c =
        db.rawQuery(
            "SELECT COUNT(*) FROM "
                + from
                + " s WHERE NOT EXISTS(SELECT 1 FROM "
                + to
                + " t WHERE "
                + String.join(" AND ", compare)
                + ")",
            null)) {
      if (c.moveToFirst() && c.getLong(0) != 0)
        throw new SQLiteException("Migration verification failed: " + source);
    }
  }

  private static void normalizeDates(SQLiteDatabase db, String table) {
    for (String column : columns(db, "main", table)) {
      if (!column.equals("timestamp")
          && !column.equals("created_at")
          && !column.equals("updated_at")) continue;
      String name = quote(column);
      List<Long> rowIds = new ArrayList<>(), times = new ArrayList<>();
      try (Cursor c = db.rawQuery("SELECT rowid," + name + " FROM " + quote(table), null)) {
        while (c.moveToNext()) {
          rowIds.add(c.getLong(0));
          times.add(Timestamps.parse(c.getString(1)));
        }
      }
      for (int i = 0; i < rowIds.size(); i++)
        db.execSQL(
            "UPDATE " + quote(table) + " SET " + name + "=? WHERE rowid=?",
            new Object[] {times.get(i), rowIds.get(i)});
    }
  }

  public final SQLiteDatabase getReadableDatabase() {
    return getWritableDatabase();
  }

  public final String getDatabaseName() {
    return DATABASE_NAME;
  }

  public abstract void onCreate(SQLiteDatabase db);

  public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

  public void onOpen(SQLiteDatabase db) {}

  public void onConfigure(SQLiteDatabase db) {
    db.setForeignKeyConstraintsEnabled(true);
  }

  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    throw new SQLiteException("Unsupported schema downgrade");
  }

  @Override
  public synchronized void close() {
    if (database != null) {
      database.close();
      database = null;
    }
  }
}
