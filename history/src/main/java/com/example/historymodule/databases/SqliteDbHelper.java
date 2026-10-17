package com.example.historymodule.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.historymodule.HistoryMessage;
import com.example.mentor.data.ModuleDatabaseHelper;
import java.util.ArrayList;
import java.util.List;

/** SQLite storage for History conversations, notes, Quiz attempts and EXP. */
/*
 * Role: Local data-layer class for History, responsible for SQLite and data read/write operations.
 * Purpose: Separating the database from the UI centralizes queries, improves maintainability, and reduces direct SQL dependencies in Activities and Fragments.
 */
public final class SqliteDbHelper extends ModuleDatabaseHelper {
  private static final String DATABASE_NAME = "history_db";
  private static final int DATABASE_VERSION = 8;

  public SqliteDbHelper(Context context) {
    super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
  }

  /*
   * Responsibility: Initializes the Android component, inflates and binds the layout, and prepares initial data and listeners.
   * Purpose: This is the screen entry point, so the initial state must be prepared before user interaction begins.
   */
  @Override
  public void onCreate(SQLiteDatabase db) {
    createLegacyTables(db);
    createConversationTable(db);
    createQuizAttemptTable(db);
    ensureIndexes(db);
  }

  /*
   * Responsibility: Handles the Android callback or event `onUpgrade` and updates the related state.
   * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
   */
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // Non-destructive migration: preserve conversations, notes and Quiz history.
    createLegacyTables(db);
    createConversationTable(db);
    createQuizAttemptTable(db);
    ensureColumns(db);
    ensureIndexes(db);
  }

  /*
   * Responsibility: Handles the Android callback or event `onOpen` and updates the related state.
   * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
   */
  @Override
  public void onOpen(SQLiteDatabase db) {
    super.onOpen(db);
    if (!db.isReadOnly()) {
      ensureColumns(db);
      ensureIndexes(db);
    }
  }

  /*
   * Responsibility: Creates the object or data structure for `create legacy tables` using the class's consistent creation rules.
   * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
   */
  private void createLegacyTables(SQLiteDatabase db) {
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS history_legacy_items ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "title TEXT NOT NULL DEFAULT '', "
            + "content TEXT NOT NULL DEFAULT '', "
            + "timestamp TEXT NOT NULL DEFAULT '', "
            + "type TEXT NOT NULL DEFAULT 'search')");

    db.execSQL(
        "CREATE TABLE IF NOT EXISTS history_notes ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "title TEXT NOT NULL DEFAULT '', "
            + "content TEXT NOT NULL DEFAULT '', "
            + "category TEXT NOT NULL DEFAULT 'Khác', "
            + "user_id INTEGER NOT NULL DEFAULT 0, "
            + "pinned INTEGER NOT NULL DEFAULT 0, "
            + "created_at INTEGER NOT NULL DEFAULT 0, "
            + "updated_at INTEGER NOT NULL DEFAULT 0)");

    // Retained only so old installations can migrate without losing data.
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS history_quiz_sets ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, category TEXT)");
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS history_quiz_questions ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, set_id INTEGER, question TEXT, "
            + "option_a TEXT, option_b TEXT, option_c TEXT, option_d TEXT, correct_option TEXT)");
  }

  /*
   * Responsibility: Creates the object or data structure for `create conversation table` using the class's consistent creation rules.
   * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
   */
  private void createConversationTable(SQLiteDatabase db) {
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS history_messages ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "user_id INTEGER NOT NULL DEFAULT 0, "
            + "conversation_id TEXT NOT NULL, "
            + "role TEXT NOT NULL, "
            + "content TEXT NOT NULL, "
            + "image_attached INTEGER NOT NULL DEFAULT 0, "
            + "created_at INTEGER NOT NULL)");
  }

  /*
   * Responsibility: Creates the object or data structure for `create quiz attempt table` using the class's consistent creation rules.
   * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
   */
  private void createQuizAttemptTable(SQLiteDatabase db) {
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS history_quiz_attempts ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "user_id INTEGER NOT NULL DEFAULT 0, "
            + "topic TEXT NOT NULL, "
            + "education_level TEXT NOT NULL DEFAULT 'Trung học cơ sở', "
            + "correct_count INTEGER NOT NULL, "
            + "total_count INTEGER NOT NULL, "
            + "exp_earned INTEGER NOT NULL DEFAULT 0, "
            + "created_at INTEGER NOT NULL)");
  }

  /*
   * Responsibility: Performs the `ensure columns` responsibility of `SqliteDbHelper` and returns or updates the result for the caller.
   * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
   */
  private void ensureColumns(SQLiteDatabase db) {
    addColumnIfMissing(db, "history_notes", "user_id", "INTEGER NOT NULL DEFAULT 0");
    addColumnIfMissing(db, "history_notes", "pinned", "INTEGER NOT NULL DEFAULT 0");
    addColumnIfMissing(db, "history_notes", "created_at", "INTEGER NOT NULL DEFAULT 0");
    addColumnIfMissing(db, "history_notes", "updated_at", "INTEGER NOT NULL DEFAULT 0");

    addColumnIfMissing(db, "history_messages", "user_id", "INTEGER NOT NULL DEFAULT 0");

    addColumnIfMissing(db, "history_quiz_attempts", "user_id", "INTEGER NOT NULL DEFAULT 0");
    addColumnIfMissing(
        db, "history_quiz_attempts", "education_level", "TEXT NOT NULL DEFAULT 'Trung học cơ sở'");
    addColumnIfMissing(db, "history_quiz_attempts", "exp_earned", "INTEGER NOT NULL DEFAULT 0");

    // Missing historical dates remain unknown; never fabricate a recent date during reads.
  }

  /*
   * Responsibility: Saves or adds data for `add column if missing` to the appropriate storage or collection.
   * Purpose: Centralizing write operations helps maintain data consistency and handle storage errors.
   */
  private void addColumnIfMissing(
      SQLiteDatabase db, String table, String column, String definition) {
    if (!columnExists(db, table, column)) {
      db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
    }
  }

  /*
   * Responsibility: Performs the `column exists` responsibility of `SqliteDbHelper` and returns or updates the result for the caller.
   * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
   */
  private boolean columnExists(SQLiteDatabase db, String table, String column) {
    try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null)) {
      int nameIndex = cursor.getColumnIndex("name");
      while (cursor.moveToNext()) {
        if (column.equalsIgnoreCase(cursor.getString(nameIndex))) return true;
      }
    }
    return false;
  }

  /*
   * Responsibility: Performs the `ensure indexes` responsibility of `SqliteDbHelper` and returns or updates the result for the caller.
   * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
   */
  private void ensureIndexes(SQLiteDatabase db) {
    db.execSQL(
        "CREATE INDEX IF NOT EXISTS idx_history_chat_user_conversation_time "
            + "ON history_messages(user_id, conversation_id, created_at)");
    db.execSQL(
        "CREATE INDEX IF NOT EXISTS idx_history_notes_user_pin_time "
            + "ON history_notes(user_id, pinned, updated_at)");
    db.execSQL(
        "CREATE INDEX IF NOT EXISTS idx_history_quiz_user_time "
            + "ON history_quiz_attempts(user_id, created_at)");
  }

  /*
   * Responsibility: Saves or adds data for `save chat message` to the appropriate storage or collection.
   * Purpose: Centralizing write operations helps maintain data consistency and handle storage errors.
   */
  public long saveChatMessage(
      int userId, String conversationId, String role, String content, boolean imageAttached) {
    ContentValues values = new ContentValues();
    values.put("user_id", Math.max(0, userId));
    values.put("conversation_id", normalizeConversationId(conversationId));
    values.put("role", role == null ? HistoryMessage.SENT_BY_BOT : role);
    values.put("content", content == null ? "" : content.trim());
    values.put("image_attached", imageAttached ? 1 : 0);
    values.put("created_at", System.currentTimeMillis());
    return getWritableDatabase().insert("history_messages", null, values);
  }

  /*
   * Responsibility: Reads or loads data for `load chat messages` from the appropriate data source.
   * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
   */
  public List<HistoryMessage> loadChatMessages(int userId, String conversationId) {
    List<HistoryMessage> result = new ArrayList<>();
    try (Cursor cursor =
        getReadableDatabase()
            .query(
                "history_messages",
                new String[] {"role", "content", "image_attached"},
                "user_id = ? AND conversation_id = ?",
                new String[] {
                  String.valueOf(Math.max(0, userId)), normalizeConversationId(conversationId)
                },
                null,
                null,
                "created_at ASC, id ASC")) {
      while (cursor.moveToNext()) {
        result.add(
            new HistoryMessage(
                cursor.getString(1), cursor.getString(0), false, cursor.getInt(2) == 1));
      }
    }
    return result;
  }

  /*
   * Responsibility: Reads or loads data for `load conversation summaries` from the appropriate data source.
   * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
   */
  public List<HistoryHistoryItem> loadConversationSummaries(
      int userId, long startInclusive, long endExclusive, int limit) {
    List<HistoryHistoryItem> result = new ArrayList<>();
    StringBuilder sql =
        new StringBuilder()
            .append("SELECT c.conversation_id, MAX(c.created_at) AS last_time, ")
            .append("(SELECT first.content FROM history_messages first ")
            .append(
                " WHERE first.user_id = c.user_id AND first.conversation_id = c.conversation_id ")
            .append(
                " AND first.role = ? ORDER BY first.created_at ASC, first.id ASC LIMIT 1) AS"
                    + " first_user, ")
            .append("(SELECT last.content FROM history_messages last ")
            .append(" WHERE last.user_id = c.user_id AND last.conversation_id = c.conversation_id ")
            .append(" ORDER BY last.created_at DESC, last.id DESC LIMIT 1) AS last_content ")
            .append("FROM history_messages c WHERE c.user_id = ? ")
            .append("GROUP BY c.conversation_id ");

    List<String> args = new ArrayList<>();
    args.add(HistoryMessage.SENT_BY_ME);
    args.add(String.valueOf(Math.max(0, userId)));
    if (startInclusive > 0L) {
      sql.append("HAVING MAX(c.created_at) >= ? ");
      args.add(String.valueOf(startInclusive));
      if (endExclusive > 0L) {
        sql.append("AND MAX(c.created_at) < ? ");
        args.add(String.valueOf(endExclusive));
      }
    } else if (endExclusive > 0L) {
      sql.append("HAVING MAX(c.created_at) < ? ");
      args.add(String.valueOf(endExclusive));
    }
    sql.append("ORDER BY last_time DESC ");
    if (limit > 0) sql.append("LIMIT ").append(Math.min(100, limit));

    try (Cursor cursor =
        getReadableDatabase().rawQuery(sql.toString(), args.toArray(new String[0]))) {
      long rowId = 1;
      while (cursor.moveToNext()) {
        String conversationId = cursor.getString(0);
        long lastTime = cursor.getLong(1);
        String firstUser = cursor.isNull(2) ? "Cuộc trò chuyện Lịch sử" : cursor.getString(2);
        String lastContent = cursor.isNull(3) ? "" : cursor.getString(3);
        result.add(
            new HistoryHistoryItem(
                rowId++,
                conversationId,
                shorten(cleanImageMarker(firstUser), 56),
                shorten(cleanImageMarker(lastContent), 120),
                String.valueOf(lastTime)));
      }
    }
    return result;
  }

  /*
   * Responsibility: Clears or resets the data and state related to `delete conversation`.
   * Purpose: Keeping deletion logic separate helps ensure related UI and data updates are not missed.
   */
  public int deleteConversation(int userId, String conversationId) {
    return getWritableDatabase()
        .delete(
            "history_messages",
            "user_id = ? AND conversation_id = ?",
            new String[] {
              String.valueOf(Math.max(0, userId)), normalizeConversationId(conversationId)
            });
  }

  /*
   * Responsibility: Saves or adds data for `insert note` to the appropriate storage or collection.
   * Purpose: Centralizing write operations helps maintain data consistency and handle storage errors.
   */
  public long insertNote(int userId, String title, String content) {
    long now = System.currentTimeMillis();
    ContentValues values = new ContentValues();
    values.put("user_id", Math.max(0, userId));
    values.put("title", safeText(title));
    values.put("content", safeText(content));
    values.put("category", "Ghi chú");
    values.put("pinned", 0);
    values.put("created_at", now);
    values.put("updated_at", now);
    return getWritableDatabase().insert("history_notes", null, values);
  }

  /*
   * Responsibility: Updates or synchronizes `update note` with the latest data or state.
   * Purpose: Keeps the UI and data synchronized after user actions.
   */
  public int updateNote(int userId, long noteId, String title, String content) {
    ContentValues values = new ContentValues();
    values.put("title", safeText(title));
    values.put("content", safeText(content));
    values.put("updated_at", System.currentTimeMillis());
    return getWritableDatabase()
        .update(
            "history_notes",
            values,
            "id = ? AND user_id = ?",
            new String[] {String.valueOf(noteId), String.valueOf(Math.max(0, userId))});
  }

  /*
   * Responsibility: Clears or resets the data and state related to `delete note`.
   * Purpose: Keeping deletion logic separate helps ensure related UI and data updates are not missed.
   */
  public int deleteNote(int userId, long noteId) {
    return getWritableDatabase()
        .delete(
            "history_notes",
            "id = ? AND user_id = ?",
            new String[] {String.valueOf(noteId), String.valueOf(Math.max(0, userId))});
  }

  /*
   * Responsibility: Applies or updates `set note pinned` in the current state or UI.
   * Purpose: A dedicated method ensures the same update behavior is used consistently by every caller.
   */
  public int setNotePinned(int userId, long noteId, boolean pinned) {
    ContentValues values = new ContentValues();
    values.put("pinned", pinned ? 1 : 0);
    values.put("updated_at", System.currentTimeMillis());
    return getWritableDatabase()
        .update(
            "history_notes",
            values,
            "id = ? AND user_id = ?",
            new String[] {String.valueOf(noteId), String.valueOf(Math.max(0, userId))});
  }

  /*
   * Responsibility: Reads or loads data for `load notes` from the appropriate data source.
   * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
   */
  public List<HistoryNoteItem> loadNotes(int userId) {
    List<HistoryNoteItem> result = new ArrayList<>();
    try (Cursor cursor =
        getReadableDatabase()
            .query(
                "history_notes",
                new String[] {"id", "title", "content", "pinned", "updated_at"},
                "user_id = ?",
                new String[] {String.valueOf(Math.max(0, userId))},
                null,
                null,
                "pinned DESC, updated_at DESC, id DESC")) {
      while (cursor.moveToNext()) {
        result.add(
            new HistoryNoteItem(
                cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getInt(3) == 1,
                cursor.getLong(4)));
      }
    }
    return result;
  }

  /*
   * Responsibility: Saves or adds data for `save quiz attempt` to the appropriate storage or collection.
   * Purpose: Centralizing write operations helps maintain data consistency and handle storage errors.
   */
  public long saveQuizAttempt(
      int userId, String topic, String educationLevel, int correct, int total, int expEarned) {
    ContentValues values = new ContentValues();
    values.put("user_id", Math.max(0, userId));
    values.put("topic", safeText(topic).isEmpty() ? "Tổng hợp" : safeText(topic));
    values.put(
        "education_level",
        safeText(educationLevel).isEmpty() ? "Trung học cơ sở" : safeText(educationLevel));
    values.put("correct_count", Math.max(0, correct));
    values.put("total_count", Math.max(0, total));
    values.put("exp_earned", Math.max(0, expEarned));
    values.put("created_at", System.currentTimeMillis());
    return total > 0
        ? com.example.mentor.data.LearningDays.insert(
            getWritableDatabase(),
            "history_quiz_attempts",
            values,
            userId,
            values.getAsLong("created_at"))
        : getWritableDatabase().insert("history_quiz_attempts", null, values);
  }

  /*
   * Responsibility: Reads or loads data for `get total quiz exp` from the appropriate data source.
   * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
   */
  public int getTotalQuizExp(int userId) {
    try (Cursor cursor =
        getReadableDatabase()
            .rawQuery(
                "SELECT COALESCE(SUM(exp_earned), 0) FROM history_quiz_attempts WHERE user_id = ?",
                new String[] {String.valueOf(Math.max(0, userId))})) {
      return cursor.moveToFirst() ? Math.max(0, cursor.getInt(0)) : 0;
    }
  }

  /*
   * Responsibility: Reads or loads data for `load quiz attempts` from the appropriate data source.
   * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
   */
  public List<HistoryHistoryItem> loadQuizAttempts(int userId, int limit) {
    List<HistoryHistoryItem> result = new ArrayList<>();
    try (Cursor cursor =
        getReadableDatabase()
            .query(
                "history_quiz_attempts",
                new String[] {
                  "id",
                  "topic",
                  "education_level",
                  "correct_count",
                  "total_count",
                  "exp_earned",
                  "created_at"
                },
                "user_id = ?",
                new String[] {String.valueOf(Math.max(0, userId))},
                null,
                null,
                "created_at DESC, id DESC",
                limit > 0 ? String.valueOf(Math.min(100, limit)) : null)) {
      while (cursor.moveToNext()) {
        int correct = cursor.getInt(3);
        int total = cursor.getInt(4);
        int percent = total <= 0 ? 0 : Math.round(correct * 100f / total);
        result.add(
            new HistoryHistoryItem(
                cursor.getLong(0),
                cursor.getString(1) + " • " + cursor.getString(2),
                correct
                    + "/"
                    + total
                    + " câu đúng • "
                    + percent
                    + "% chính xác • +"
                    + cursor.getInt(5)
                    + " EXP",
                String.valueOf(cursor.getLong(6))));
      }
    }
    return result;
  }

  /*
   * Responsibility: Converts or normalizes data in `normalize conversation id` into the required format.
   * Purpose: A centralized conversion point keeps formatting consistent and makes invalid data easier to handle.
   */
  private String normalizeConversationId(String value) {
    return value == null || value.trim().isEmpty() ? "default" : value.trim();
  }

  private String safeText(String value) {
    return value == null ? "" : value.trim();
  }

  /*
   * Responsibility: Performs the `clean image marker` responsibility of `SqliteDbHelper` and returns or updates the result for the caller.
   * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
   */
  private String cleanImageMarker(String value) {
    return safeText(value).replace("[Đã gửi kèm ảnh]", "").trim();
  }

  private String shorten(String value, int maxLength) {
    String safe = safeText(value).replaceAll("\\s+", " ");
    return safe.length() <= maxLength ? safe : safe.substring(0, Math.max(1, maxLength - 1)) + "…";
  }

  public com.example.mentor.data.ConversationSearchIndex loadConversationSearchIndex(int userId) {
    com.example.mentor.data.ConversationSearchIndex result =
        new com.example.mentor.data.ConversationSearchIndex();
    try (Cursor c =
        getReadableDatabase()
            .rawQuery(
                "SELECT conversation_id,content,role FROM history_messages WHERE user_id=? ORDER BY"
                    + " created_at,id",
                new String[] {String.valueOf(userId)})) {
      while (c.moveToNext())
        result.add(
            c.getString(0), c.getString(1), HistoryMessage.SENT_BY_ME.equals(c.getString(2)));
    }
    return result;
  }
}
