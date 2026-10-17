package com.example.mentor.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.json.*;

/** Account-scoped, atomic offline question bank in the existing AImentor.db. */
public final class QuizBank extends ModuleDatabaseHelper {
  public static final class SaveException extends RuntimeException {
    public SaveException(Throwable cause) {
      super("Quiz bank write failed", cause);
    }
  }

  public static final class Set {
    public final long id;
    public final String subject, scope, level, topic, source;
    public final long createdAt;
    public final int count;

    private Set(Cursor c) {
      id = c.getLong(0);
      subject = c.getString(1);
      scope = c.getString(2);
      level = c.getString(3);
      topic = c.getString(4);
      source = c.getString(5);
      createdAt = c.getLong(6);
      count = c.getInt(7);
    }
  }

  public QuizBank(Context context) {
    super(context, "mentor_quiz_bank_schema", null, 2);
  }

  @Override
  protected Map<String, String> legacyTables() {
    return Collections.emptyMap();
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS mentor_quiz_bank_sets(id INTEGER PRIMARY KEY"
            + " AUTOINCREMENT,user_id INTEGER NOT NULL,subject TEXT NOT NULL,scope TEXT NOT"
            + " NULL,level TEXT NOT NULL,topic TEXT NOT NULL,source TEXT NOT NULL,created_at"
            + " INTEGER NOT NULL,fingerprint TEXT NOT NULL,UNIQUE(user_id,subject,fingerprint))");
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS mentor_quiz_bank_questions(id INTEGER PRIMARY KEY"
            + " AUTOINCREMENT,set_id INTEGER NOT NULL REFERENCES mentor_quiz_bank_sets(id) ON"
            + " DELETE CASCADE,position INTEGER NOT NULL,prompt TEXT NOT NULL,options TEXT NOT"
            + " NULL,option_values TEXT NOT NULL,answer TEXT NOT NULL,explanation TEXT NOT"
            + " NULL,difficulty TEXT NOT NULL,math INTEGER NOT NULL,answer_type TEXT NOT NULL"
            + " DEFAULT 'TEXT',UNIQUE(set_id,position))");
    db.execSQL(
        "CREATE INDEX IF NOT EXISTS mentor_bank_filter ON"
            + " mentor_quiz_bank_sets(user_id,subject,scope,level,created_at)");
    db.execSQL(
        "CREATE INDEX IF NOT EXISTS mentor_bank_difficulty ON"
            + " mentor_quiz_bank_questions(set_id,difficulty)");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onCreate(db);
    boolean exists = false;
    try (Cursor c = db.rawQuery("PRAGMA table_info(mentor_quiz_bank_questions)", null)) {
      while (c.moveToNext()) if ("answer_type".equals(c.getString(1))) exists = true;
    }
    if (!exists) {
      db.execSQL(
          "ALTER TABLE mentor_quiz_bank_questions ADD COLUMN answer_type TEXT NOT NULL DEFAULT"
              + " 'TEXT'");
      db.execSQL("UPDATE mentor_quiz_bank_questions SET answer_type='CHOICE' WHERE options!='[]'");
    }
  }

  public long save(
      int userId,
      String subject,
      String scope,
      String level,
      String topic,
      String source,
      List<QuizCard> cards) {
    if (userId <= 0 || cards == null || cards.isEmpty())
      throw new IllegalArgumentException("Missing owner or questions");
    StringBuilder content = new StringBuilder();
    content.append(new JSONArray(Arrays.asList(scope, level, topic, source)).toString());
    for (QuizCard q : cards) {
      if (q.prompt.trim().isEmpty() || q.answer.trim().isEmpty())
        throw new IllegalArgumentException("Invalid question");
      if (!q.optionValues.isEmpty() && !q.optionValues.contains(q.answer))
        throw new IllegalArgumentException("Invalid answer");
      content.append(
          new JSONArray(
                  Arrays.asList(
                      q.prompt,
                      new JSONArray(q.options),
                      new JSONArray(q.optionValues),
                      q.answer,
                      q.explanation,
                      q.difficulty,
                      q.math))
              .toString());
    }
    String fingerprint = hash(content.toString());
    SQLiteDatabase db = getWritableDatabase();
    db.beginTransaction();
    try {
      long existing = -1;
      try (Cursor c =
          db.rawQuery(
              "SELECT id FROM mentor_quiz_bank_sets WHERE user_id=? AND subject=? AND"
                  + " fingerprint=?",
              new String[] {String.valueOf(userId), subject, fingerprint})) {
        if (c.moveToFirst()) existing = c.getLong(0);
      }
      if (existing > 0) {
        for (int i = 0; i < cards.size(); i++) {
          ContentValues kind = new ContentValues();
          kind.put("answer_type", cards.get(i).answerType());
          db.update(
              "mentor_quiz_bank_questions",
              kind,
              "set_id=? AND position=?",
              new String[] {String.valueOf(existing), String.valueOf(i)});
        }
        db.setTransactionSuccessful();
        return existing;
      }
      ContentValues set = new ContentValues();
      set.put("user_id", userId);
      set.put("subject", subject);
      set.put("scope", scope);
      set.put("level", level);
      set.put("topic", topic);
      set.put("source", source);
      set.put("created_at", System.currentTimeMillis());
      set.put("fingerprint", fingerprint);
      long id = db.insertOrThrow("mentor_quiz_bank_sets", null, set);
      int position = 0;
      for (QuizCard q : cards) {
        ContentValues row = new ContentValues();
        row.put("set_id", id);
        row.put("position", position++);
        row.put("prompt", q.prompt);
        row.put("options", new JSONArray(q.options).toString());
        row.put("option_values", new JSONArray(q.optionValues).toString());
        row.put("answer", q.answer);
        row.put("explanation", q.explanation);
        row.put("difficulty", q.difficulty);
        row.put("math", q.math ? 1 : 0);
        row.put("answer_type", q.answerType());
        db.insertOrThrow("mentor_quiz_bank_questions", null, row);
      }
      db.setTransactionSuccessful();
      return id;
    } finally {
      db.endTransaction();
    }
  }

  public List<Set> list(int userId, String subject) {
    List<Set> result = new ArrayList<>();
    try (Cursor c =
        getReadableDatabase()
            .rawQuery(
                "SELECT s.id,s.subject,s.scope,s.level,s.topic,s.source,s.created_at,COUNT(q.id)"
                    + " FROM mentor_quiz_bank_sets s JOIN mentor_quiz_bank_questions q ON"
                    + " q.set_id=s.id WHERE s.user_id=? AND s.subject=? GROUP BY s.id ORDER BY"
                    + " s.created_at DESC,s.id DESC",
                new String[] {String.valueOf(userId), subject})) {
      while (c.moveToNext()) result.add(new Set(c));
    }
    return result;
  }

  public ArrayList<QuizCard> questions(int userId, long setId) {
    ArrayList<QuizCard> result = new ArrayList<>();
    try (Cursor c =
        getReadableDatabase()
            .rawQuery(
                "SELECT"
                    + " q.prompt,q.options,q.option_values,q.answer,q.explanation,q.difficulty,q.math,q.answer_type"
                    + " FROM mentor_quiz_bank_questions q JOIN mentor_quiz_bank_sets s ON"
                    + " s.id=q.set_id WHERE s.user_id=? AND s.id=? ORDER BY q.position",
                new String[] {String.valueOf(userId), String.valueOf(setId)})) {
      while (c.moveToNext())
        result.add(
            new QuizCard(
                c.getString(0),
                strings(c.getString(1)),
                strings(c.getString(2)),
                c.getString(3),
                c.getString(4),
                c.getString(5),
                c.getInt(6) != 0,
                c.getString(7)));
    }
    return result;
  }

  private static ArrayList<String> strings(String raw) {
    try {
      JSONArray array = new JSONArray(raw);
      ArrayList<String> result = new ArrayList<>();
      for (int i = 0; i < array.length(); i++) result.add(array.getString(i));
      return result;
    } catch (JSONException error) {
      throw new IllegalStateException("Invalid bank data", error);
    }
  }

  private static String hash(String value) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder result = new StringBuilder();
      for (byte b : digest) result.append(String.format(Locale.ROOT, "%02x", b & 255));
      return result.toString();
    } catch (java.security.NoSuchAlgorithmException error) {
      throw new IllegalStateException(error);
    }
  }
}
