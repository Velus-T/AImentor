package com.example.mathmodule.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.mentor.data.ModuleDatabaseHelper;

import com.example.mathmodule.util.MathQuizDifficulty;
import com.example.mathmodule.util.MathUserSession;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/*
 * Role: Local data-layer class for Math, responsible for SQLite and data read/write operations.
 * Purpose: Separating the database from the UI centralizes queries, improves maintainability, and reduces direct SQL dependencies in Activities and Fragments.
 */
public class SqliteDbHelper extends ModuleDatabaseHelper {

    private static final String DATABASE_NAME = "MathMentor.db";
    private static final int DATABASE_VERSION = 9;

    public static final String TABLE_HISTORY = "math_history";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_QUESTION = "question";
    public static final String COLUMN_ANSWER = "answer";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public static final String TABLE_NOTES = "math_notes";
    public static final String COLUMN_NOTE_ID = "id";
    public static final String COLUMN_NOTE_TITLE = "title";
    public static final String COLUMN_NOTE_FORMULA = "formula";
    public static final String COLUMN_NOTE_CATEGORY = "category";
    public static final String COLUMN_NOTE_PINNED = "is_pinned";

    public static final String TABLE_QUIZ_SETS = "math_quiz_sets";
    public static final String COLUMN_SET_ID = "id";
    public static final String COLUMN_SET_TITLE = "title";
    public static final String COLUMN_SET_CATEGORY = "category";
    public static final String COLUMN_SET_DIFFICULTY = "difficulty";
    public static final String COLUMN_SET_EDUCATION_LEVEL = "education_level";

    public static final String TABLE_QUESTIONS = "math_quiz_questions";
    public static final String COLUMN_Q_ID = "id";
    public static final String COLUMN_Q_SET_ID = "set_id";
    public static final String COLUMN_Q_TEXT = "question_text";
    public static final String COLUMN_Q_OPT_A = "option_a";
    public static final String COLUMN_Q_OPT_B = "option_b";
    public static final String COLUMN_Q_OPT_C = "option_c";
    public static final String COLUMN_Q_OPT_D = "option_d";
    public static final String COLUMN_Q_CORRECT = "correct_answer";
    public static final String COLUMN_Q_EXPLANATION = "explanation";
    public static final String COLUMN_Q_DIFFICULTY = "difficulty";

    public static final String TABLE_USER = "math_user_profile";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USER_EXP = "exp";

    public static final String TABLE_QUIZ_ATTEMPTS = "math_quiz_attempts";
    public static final String COLUMN_ATTEMPT_SET_ID = "set_id";
    public static final String COLUMN_ATTEMPT_CORRECT = "correct_count";
    public static final String COLUMN_ATTEMPT_TOTAL = "total_count";
    public static final String COLUMN_ATTEMPT_EXP = "exp_earned";

    private final Context appContext;

    /*
     * Responsibility: Initializes the `SqliteDbHelper` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public SqliteDbHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        appContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createCoreTables(db);
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USER
                + " (" + COLUMN_USER_ID + "," + COLUMN_USER_EXP + ") VALUES (0,0)");
    }

    /*
     * Responsibility: Creates the object or data structure for `create core tables` using the class's consistent creation rules.
     * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
     */
    private void createCoreTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_QUESTION + " TEXT NOT NULL DEFAULT '', "
                + COLUMN_ANSWER + " TEXT NOT NULL DEFAULT '', "
                + COLUMN_TIMESTAMP + " INTEGER DEFAULT (CAST(strftime('%s','now') AS INTEGER)*1000))");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTES + " ("
                + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_NOTE_TITLE + " TEXT NOT NULL DEFAULT '', "
                + COLUMN_NOTE_FORMULA + " TEXT NOT NULL DEFAULT '', "
                + COLUMN_NOTE_CATEGORY + " TEXT NOT NULL DEFAULT 'Đại số', "
                + COLUMN_NOTE_PINNED + " INTEGER NOT NULL DEFAULT 0)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USER + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_USER_EXP + " INTEGER NOT NULL DEFAULT 0, "
                + "UNIQUE(" + COLUMN_USER_ID + ") ON CONFLICT IGNORE)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_QUIZ_SETS + " ("
                + COLUMN_SET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "owner_id INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_SET_TITLE + " TEXT NOT NULL DEFAULT 'Bộ đề Toán', "
                + COLUMN_SET_CATEGORY + " TEXT NOT NULL DEFAULT 'Khác', "
                + COLUMN_SET_DIFFICULTY + " TEXT NOT NULL DEFAULT 'Trung bình', "
                + COLUMN_SET_EDUCATION_LEVEL + " TEXT NOT NULL DEFAULT 'Middle School')");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_QUESTIONS + " ("
                + COLUMN_Q_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_Q_SET_ID + " INTEGER NOT NULL, "
                + COLUMN_Q_TEXT + " TEXT NOT NULL DEFAULT '', "
                + COLUMN_Q_OPT_A + " TEXT NOT NULL DEFAULT '', "
                + COLUMN_Q_OPT_B + " TEXT NOT NULL DEFAULT '', "
                + COLUMN_Q_OPT_C + " TEXT NOT NULL DEFAULT '', "
                + COLUMN_Q_OPT_D + " TEXT NOT NULL DEFAULT '', "
                + COLUMN_Q_CORRECT + " TEXT NOT NULL DEFAULT 'A', "
                + COLUMN_Q_EXPLANATION + " TEXT NOT NULL DEFAULT '', "
                + COLUMN_Q_DIFFICULTY + " TEXT NOT NULL DEFAULT 'MEDIUM')");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_QUIZ_ATTEMPTS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_ATTEMPT_SET_ID + " INTEGER NOT NULL DEFAULT -1, "
                + COLUMN_ATTEMPT_CORRECT + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_ATTEMPT_TOTAL + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_ATTEMPT_EXP + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_TIMESTAMP + " INTEGER DEFAULT (CAST(strftime('%s','now') AS INTEGER)*1000))");

        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_math_user_profile_user ON "
                + TABLE_USER + "(" + COLUMN_USER_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_math_question_set ON "
                + TABLE_QUESTIONS + "(" + COLUMN_Q_SET_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_math_attempt_user ON "
                + TABLE_QUIZ_ATTEMPTS + "(" + COLUMN_USER_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_math_history_user ON "
                + TABLE_HISTORY + "(" + COLUMN_USER_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_math_notes_user ON "
                + TABLE_NOTES + "(" + COLUMN_USER_ID + ")");
    }

    /*
     * Responsibility: Handles the Android callback or event `onUpgrade` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createLegacyTablesIfMissing(db);
        if(!columnExists(db,TABLE_QUIZ_SETS,"owner_id"))db.execSQL("ALTER TABLE "+TABLE_QUIZ_SETS+" ADD COLUMN owner_id INTEGER NOT NULL DEFAULT 0");

        if (!columnExists(db, TABLE_USER, COLUMN_USER_ID)) {
            db.execSQL("ALTER TABLE " + TABLE_USER + " ADD COLUMN "
                    + COLUMN_USER_ID + " INTEGER NOT NULL DEFAULT 0");
        }
        if (!columnExists(db, TABLE_QUIZ_SETS, COLUMN_SET_DIFFICULTY)) {
            db.execSQL("ALTER TABLE " + TABLE_QUIZ_SETS + " ADD COLUMN "
                    + COLUMN_SET_DIFFICULTY + " TEXT NOT NULL DEFAULT 'Trung bình'");
        }
        if (!columnExists(db, TABLE_QUIZ_SETS, COLUMN_SET_EDUCATION_LEVEL)) {
            db.execSQL("ALTER TABLE " + TABLE_QUIZ_SETS + " ADD COLUMN "
                    + COLUMN_SET_EDUCATION_LEVEL + " TEXT NOT NULL DEFAULT 'Middle School'");
        }
        if (!columnExists(db, TABLE_QUESTIONS, COLUMN_Q_DIFFICULTY)) {
            db.execSQL("ALTER TABLE " + TABLE_QUESTIONS + " ADD COLUMN "
                    + COLUMN_Q_DIFFICULTY + " TEXT NOT NULL DEFAULT 'MEDIUM'");
            db.execSQL("UPDATE " + TABLE_QUESTIONS + " SET " + COLUMN_Q_DIFFICULTY
                    + "=COALESCE((SELECT CASE "
                    + "WHEN lower(s." + COLUMN_SET_DIFFICULTY + ") LIKE '%khó%' THEN 'HARD' "
                    + "WHEN lower(s." + COLUMN_SET_DIFFICULTY + ") LIKE '%dễ%' THEN 'EASY' "
                    + "ELSE 'MEDIUM' END FROM " + TABLE_QUIZ_SETS + " s WHERE s."
                    + COLUMN_SET_ID + "=" + TABLE_QUESTIONS + "." + COLUMN_Q_SET_ID
                    + "),'MEDIUM')");
        }
        if (!columnExists(db, TABLE_HISTORY, COLUMN_USER_ID)) {
            db.execSQL("ALTER TABLE " + TABLE_HISTORY + " ADD COLUMN "
                    + COLUMN_USER_ID + " INTEGER NOT NULL DEFAULT 0");
        }
        if (!columnExists(db, TABLE_NOTES, COLUMN_USER_ID)) {
            db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN "
                    + COLUMN_USER_ID + " INTEGER NOT NULL DEFAULT 0");
        }

        // Older schemas could contain more than one profile row before user_id
        // became unique. Preserve the highest EXP and remove duplicates before
        // creating the unique user index.
        db.execSQL("UPDATE " + TABLE_USER + " SET " + COLUMN_USER_EXP
                + "=(SELECT MAX(other." + COLUMN_USER_EXP + ") FROM " + TABLE_USER
                + " other WHERE other." + COLUMN_USER_ID + "=" + TABLE_USER + "."
                + COLUMN_USER_ID + ")");
        db.execSQL("DELETE FROM " + TABLE_USER + " WHERE " + COLUMN_ID
                + " NOT IN (SELECT MIN(" + COLUMN_ID + ") FROM " + TABLE_USER
                + " GROUP BY " + COLUMN_USER_ID + ")");

        createCoreTables(db);
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USER
                + " (" + COLUMN_USER_ID + "," + COLUMN_USER_EXP + ") VALUES (0,0)");
    }

    /*
     * Responsibility: Creates the object or data structure for `create legacy tables if missing` using the class's consistent creation rules.
     * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
     */
    private void createLegacyTablesIfMissing(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_QUESTION + " TEXT NOT NULL DEFAULT '', "
                + COLUMN_ANSWER + " TEXT NOT NULL DEFAULT '', "
                + COLUMN_TIMESTAMP + " INTEGER DEFAULT (CAST(strftime('%s','now') AS INTEGER)*1000))");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTES + " ("
                + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NOTE_TITLE + " TEXT NOT NULL DEFAULT '', "
                + COLUMN_NOTE_FORMULA + " TEXT NOT NULL DEFAULT '', "
                + COLUMN_NOTE_CATEGORY + " TEXT NOT NULL DEFAULT 'Đại số', "
                + COLUMN_NOTE_PINNED + " INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USER + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_EXP + " INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_QUIZ_SETS + " ("
                + COLUMN_SET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SET_TITLE + " TEXT, "
                + COLUMN_SET_CATEGORY + " TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_QUESTIONS + " ("
                + COLUMN_Q_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_Q_SET_ID + " INTEGER, "
                + COLUMN_Q_TEXT + " TEXT, "
                + COLUMN_Q_OPT_A + " TEXT, " + COLUMN_Q_OPT_B + " TEXT, "
                + COLUMN_Q_OPT_C + " TEXT, " + COLUMN_Q_OPT_D + " TEXT, "
                + COLUMN_Q_CORRECT + " TEXT, " + COLUMN_Q_EXPLANATION + " TEXT)");
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
     * Responsibility: Saves or adds data for `insert history` to the appropriate storage or collection.
     * Purpose: Centralizing write operations helps maintain data consistency and handle storage errors.
     */
    public long insertHistory(String question, String answer) {
        return insertHistoryForUser(currentUserId(), question, answer);
    }

    /** Used by the demo seeder without changing the active module session. */
    /*
     * Responsibility: Saves or adds data for `insert history for user` to the appropriate storage or collection.
     * Purpose: Centralizing write operations helps maintain data consistency and handle storage errors.
     */
    public long insertHistoryForUser(int userId, String question, String answer) {
        int safeUserId = Math.max(0, userId);
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, safeUserId);
        values.put(COLUMN_QUESTION, safe(question));
        values.put(COLUMN_ANSWER, safe(answer));
        return getWritableDatabase().insert(TABLE_HISTORY, null, values);
    }

    /*
     * Responsibility: Reads or loads data for `get all history` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public List<MathHistoryItem> getAllHistory() { return getAllHistory(currentUserId()); }

    public List<MathHistoryItem> getAllHistory(int userId) {
        SQLiteDatabase db = getWritableDatabase();

        List<MathHistoryItem> historyList = new ArrayList<>();
        try (Cursor cursor = db.query(
                TABLE_HISTORY,
                new String[]{COLUMN_ID, COLUMN_QUESTION, COLUMN_ANSWER, COLUMN_TIMESTAMP},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                COLUMN_TIMESTAMP + " DESC, " + COLUMN_ID + " DESC")) {
            while (cursor.moveToNext()) {
                MathHistoryItem item = new MathHistoryItem();
                item.setId(cursor.getInt(0));
                item.setQuestion(cursor.getString(1));
                item.setAnswer(cursor.getString(2));
                item.setTimestamp(cursor.getString(3));
                historyList.add(item);
            }
        }
        return historyList;
    }

    /*
     * Responsibility: Saves or adds data for `insert note` to the appropriate storage or collection.
     * Purpose: Centralizing write operations helps maintain data consistency and handle storage errors.
     */
    public long insertNote(String title, String formula, String category) {
        ContentValues values = noteValues(title, formula, category);
        values.put(COLUMN_USER_ID, currentUserId());
        values.put(COLUMN_NOTE_PINNED, 0);
        return getWritableDatabase().insert(TABLE_NOTES, null, values);
    }

    /*
     * Responsibility: Updates or synchronizes `update note` with the latest data or state.
     * Purpose: Keeps the UI and data synchronized after user actions.
     */
    public boolean updateNote(int noteId, String title, String formula, String category) {
        if (noteId <= 0) return false;
        int userId = currentUserId();
        int rows = getWritableDatabase().update(
                TABLE_NOTES,
                noteValues(title, formula, category),
                COLUMN_NOTE_ID + "=? AND " + COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(noteId), String.valueOf(userId)}
        );
        return rows > 0;
    }

    /*
     * Responsibility: Clears or resets the data and state related to `delete note`.
     * Purpose: Keeping deletion logic separate helps ensure related UI and data updates are not missed.
     */
    public boolean deleteNote(int noteId) {
        if (noteId <= 0) return false;
        int userId = currentUserId();
        return getWritableDatabase().delete(
                TABLE_NOTES,
                COLUMN_NOTE_ID + "=? AND " + COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(noteId), String.valueOf(userId)}
        ) > 0;
    }

    /*
     * Responsibility: Performs the `note values` responsibility of `SqliteDbHelper` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private ContentValues noteValues(String title, String formula, String category) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_TITLE, safe(title));
        values.put(COLUMN_NOTE_FORMULA, safe(formula));
        values.put(COLUMN_NOTE_CATEGORY, com.example.mathmodule.util.MathCategory.canonical(safe(category).isEmpty() ? "algebra" : safe(category)));
        return values;
    }

    /*
     * Responsibility: Updates or synchronizes `update pin status` with the latest data or state.
     * Purpose: Keeps the UI and data synchronized after user actions.
     */
    public void updatePinStatus(int noteId, int isPinned) {
        if (noteId <= 0) return;
        int userId = currentUserId();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_PINNED, isPinned == 1 ? 1 : 0);
        getWritableDatabase().update(
                TABLE_NOTES,
                values,
                COLUMN_NOTE_ID + "=? AND " + COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(noteId), String.valueOf(userId)}
        );
    }

    /*
     * Responsibility: Reads or loads data for `get all notes` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public List<MathNoteItem> getAllNotes() {
        int userId = currentUserId();
        SQLiteDatabase db = getWritableDatabase();

        List<MathNoteItem> noteList = new ArrayList<>();
        try (Cursor cursor = db.query(
                TABLE_NOTES,
                new String[]{COLUMN_NOTE_ID, COLUMN_NOTE_TITLE, COLUMN_NOTE_FORMULA,
                        COLUMN_NOTE_CATEGORY, COLUMN_NOTE_PINNED},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                COLUMN_NOTE_PINNED + " DESC, " + COLUMN_NOTE_ID + " DESC")) {
            while (cursor.moveToNext()) {
                noteList.add(new MathNoteItem(
                        cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), cursor.getInt(4)
                ));
            }
        }
        return noteList;
    }

    /*
     * Responsibility: Clears or resets the data and state related to `delete user history`.
     * Purpose: Keeping deletion logic separate helps ensure related UI and data updates are not missed.
     */
    public int deleteUserHistory(int userId) {
        return getWritableDatabase().delete(
                TABLE_HISTORY,
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(Math.max(0, userId))}
        );
    }

    /*
     * Responsibility: Clears or resets the data and state related to `delete user notes`.
     * Purpose: Keeping deletion logic separate helps ensure related UI and data updates are not missed.
     */
    public int deleteUserNotes(int userId) {
        return getWritableDatabase().delete(
                TABLE_NOTES,
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(Math.max(0, userId))}
        );
    }

    /*
     * Responsibility: Performs the `current user id` responsibility of `SqliteDbHelper` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private int currentUserId() {
        return Math.max(0, MathUserSession.getUserId(appContext));
    }

    // Legacy ownerless rows stay stored with user_id=0. Reading another account must
    // never silently claim them; an explicit, verified migration can handle them later.

    /*
     * Responsibility: Reads or loads data for `get quiz sets by category` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public List<QuizSet> getQuizSetsByCategory(String category) {
        List<QuizSet> list = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_QUIZ_SETS,
                quizSetColumns(),
                COLUMN_SET_CATEGORY + " IN (?,?,?) AND (owner_id=0 OR owner_id=" + currentUserId() + ")",
                com.example.mathmodule.util.MathCategory.aliases(category), null, null,
                COLUMN_SET_ID + " DESC")) {
            readQuizSets(cursor, list);
        }
        return list;
    }

    /*
     * Responsibility: Reads or loads data for `get all quiz sets` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public List<QuizSet> getAllQuizSets() {
        List<QuizSet> list = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_QUIZ_SETS, quizSetColumns(),
                "owner_id=0 OR owner_id=?", new String[]{String.valueOf(currentUserId())}, null, null, COLUMN_SET_ID + " DESC")) {
            readQuizSets(cursor, list);
        }
        return list;
    }

    /*
     * Responsibility: Performs the `quiz set columns` responsibility of `SqliteDbHelper` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private String[] quizSetColumns() {
        return new String[]{COLUMN_SET_ID, COLUMN_SET_TITLE, COLUMN_SET_CATEGORY,
                COLUMN_SET_DIFFICULTY, COLUMN_SET_EDUCATION_LEVEL};
    }

    /*
     * Responsibility: Reads or loads data for `read quiz sets` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    private void readQuizSets(Cursor cursor, List<QuizSet> output) {
        while (cursor.moveToNext()) {
            output.add(new QuizSet(
                    cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4)
            ));
        }
    }

    /*
     * Responsibility: Reads or loads data for `get questions by set id` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public List<QuizQuestion> getQuestionsBySetId(int setId) {
        List<QuizQuestion> list = new ArrayList<>();
        String[] columns = {
                COLUMN_Q_ID, COLUMN_Q_SET_ID, COLUMN_Q_TEXT,
                COLUMN_Q_OPT_A, COLUMN_Q_OPT_B, COLUMN_Q_OPT_C, COLUMN_Q_OPT_D,
                COLUMN_Q_CORRECT, COLUMN_Q_EXPLANATION, COLUMN_Q_DIFFICULTY
        };
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_QUESTIONS, columns,
                COLUMN_Q_SET_ID + "=? AND set_id IN (SELECT id FROM math_quiz_sets WHERE owner_id=0 OR owner_id=?)", new String[]{String.valueOf(setId),String.valueOf(currentUserId())},
                null, null, COLUMN_Q_ID + " ASC")) {
            while (cursor.moveToNext()) {
                list.add(new QuizQuestion(
                        cursor.getInt(0), cursor.getInt(1), cursor.getString(2),
                        cursor.getString(3), cursor.getString(4), cursor.getString(5),
                        cursor.getString(6), cursor.getString(7), cursor.getString(8),
                        cursor.getString(9)
                ));
            }
        }
        return list;
    }

    /*
     * Responsibility: Saves or adds data for `insert full quiz set` to the appropriate storage or collection.
     * Purpose: Centralizing write operations helps maintain data consistency and handle storage errors.
     */
    public long insertFullQuizSet(String title, String category, String difficulty,
                                  List<QuizQuestion> questions) {
        return insertFullQuizSet(title, category, difficulty, "Middle School", questions);
    }

    /*
     * Responsibility: Saves or adds data for `insert full quiz set` to the appropriate storage or collection.
     * Purpose: Centralizing write operations helps maintain data consistency and handle storage errors.
     */
    public long insertFullQuizSet(String title, String category, String difficulty,
                                  String educationLevel, List<QuizQuestion> questions) {
        if (questions == null || questions.isEmpty()) return -1;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        long setId = -1;
        try {
            ContentValues setValues = new ContentValues();
            setValues.put("owner_id", "AI Generated".equals(category) || category.startsWith("BANK:") ? currentUserId() : 0);
            setValues.put(COLUMN_SET_TITLE, safe(title));
            setValues.put(COLUMN_SET_CATEGORY, safe(category));
            setValues.put(COLUMN_SET_DIFFICULTY, safe(difficulty));
            setValues.put(COLUMN_SET_EDUCATION_LEVEL,
                    safe(educationLevel).isEmpty() ? "Middle School" : safe(educationLevel));
            setId = db.insert(TABLE_QUIZ_SETS, null, setValues);

            if (setId > 0) {
                for (QuizQuestion q : questions) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_Q_SET_ID, setId);
                    values.put(COLUMN_Q_TEXT, q.getQuestionText());
                    values.put(COLUMN_Q_OPT_A, q.getOptionA());
                    values.put(COLUMN_Q_OPT_B, q.getOptionB());
                    values.put(COLUMN_Q_OPT_C, q.getOptionC());
                    values.put(COLUMN_Q_OPT_D, q.getOptionD());
                    values.put(COLUMN_Q_CORRECT, q.getCorrectAnswer());
                    values.put(COLUMN_Q_EXPLANATION, q.getExplanation());
                    values.put(COLUMN_Q_DIFFICULTY, q.getDifficulty().name());
                    if (db.insert(TABLE_QUESTIONS, null, values) < 0) {
                        throw new IllegalStateException("Không thể lưu câu hỏi Quiz Toán.");
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return setId;
    }

    /*
     * Responsibility: Reads or loads data for `get user exp` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public int getUserExp() { return getUserExp(MathUserSession.getUserId(appContext)); }

    public int getUserExp(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        ensureUserProfile(db, userId);
        try (Cursor cursor = db.query(
                TABLE_USER, new String[]{COLUMN_USER_EXP},
                COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null, "1")) {
            return cursor.moveToFirst() ? Math.max(0, cursor.getInt(0)) : 0;
        }
    }

    /*
     * Responsibility: Saves or adds data for `add exp` to the appropriate storage or collection.
     * Purpose: Centralizing write operations helps maintain data consistency and handle storage errors.
     */
    public void addExp(int amount) {
        if (amount <= 0) return;
        SQLiteDatabase db = getWritableDatabase();
        int userId = MathUserSession.getUserId(appContext);
        ensureUserProfile(db, userId);
        db.execSQL("UPDATE " + TABLE_USER + " SET " + COLUMN_USER_EXP + "="
                        + COLUMN_USER_EXP + "+? WHERE " + COLUMN_USER_ID + "=?",
                new Object[]{amount, userId});
    }

    /*
     * Responsibility: Saves or adds data for `save quiz attempt and add exp` to the appropriate storage or collection.
     * Purpose: Centralizing write operations helps maintain data consistency and handle storage errors.
     */
    public boolean saveQuizAttemptAndAddExp(int setId, int correctCount,
                                            int totalCount, int expEarned) {
        SQLiteDatabase db = getWritableDatabase();
        int userId = MathUserSession.getUserId(appContext);
        int safeCorrect = Math.max(0, Math.min(correctCount, Math.max(0, totalCount)));
        int safeTotal = Math.max(0, totalCount);
        int safeExp = Math.max(0, expEarned);

        db.beginTransaction();
        try {
            ensureUserProfile(db, userId);
            ContentValues attempt = new ContentValues();
            attempt.put(COLUMN_USER_ID, userId);
            attempt.put(COLUMN_ATTEMPT_SET_ID, setId);
            attempt.put(COLUMN_ATTEMPT_CORRECT, safeCorrect);
            attempt.put(COLUMN_ATTEMPT_TOTAL, safeTotal);
            attempt.put(COLUMN_ATTEMPT_EXP, safeExp);
            if (db.insert(TABLE_QUIZ_ATTEMPTS, null, attempt) < 0) return false;

            if (safeExp > 0) {
                db.execSQL("UPDATE " + TABLE_USER + " SET " + COLUMN_USER_EXP + "="
                                + COLUMN_USER_EXP + "+? WHERE " + COLUMN_USER_ID + "=?",
                        new Object[]{safeExp, userId});
            }
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    /*
     * Responsibility: Performs the `ensure user profile` responsibility of `SqliteDbHelper` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void ensureUserProfile(SQLiteDatabase db, int userId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, Math.max(0, userId));
        values.put(COLUMN_USER_EXP, 0);
        db.insertWithOnConflict(TABLE_USER, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /*
     * Responsibility: Performs the `seed quiz data if needed` responsibility of `SqliteDbHelper` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    public void seedQuizDataIfNeeded(Context context) {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_QUIZ_SETS, null)) {
            if (!cursor.moveToFirst() || cursor.getInt(0) > 0) return;
        }

        try (InputStream input = context.getAssets().open("quiz_data.json")) {
            byte[] buffer = new byte[input.available()];
            int read = input.read(buffer);
            if (read <= 0) return;
            JSONObject root = new JSONObject(new String(buffer, 0, read, StandardCharsets.UTF_8));
            JSONArray sets = root.getJSONArray("math_quiz_sets");

            for (int i = 0; i < sets.length(); i++) {
                JSONObject set = sets.getJSONObject(i);
                String setDifficulty = set.optString("difficulty", "Trung bình");
                String educationLevel = set.optString("education_level", "Middle School");
                JSONArray items = set.getJSONArray("questions");
                List<QuizQuestion> questions = new ArrayList<>();

                for (int j = 0; j < items.length(); j++) {
                    JSONObject q = items.getJSONObject(j);
                    String questionDifficulty = q.optString("difficulty", setDifficulty);
                    questions.add(new QuizQuestion(
                            0, 0,
                            q.getString("question_text"),
                            q.getString("option_a"), q.getString("option_b"),
                            q.getString("option_c"), q.getString("option_d"),
                            q.getString("correct_answer"), q.getString("explanation"),
                            MathQuizDifficulty.fromApiValue(questionDifficulty)
                    ));
                }

                insertFullQuizSet(
                        set.getString("set_title"),
                        set.getString("category"),
                        setDifficulty,
                        educationLevel,
                        questions
                );
            }
        } catch (Exception ignored) {
            // Bundled data is optional; AI-created quizzes remain available.
        }
    }

    /*
     * Responsibility: Performs the `safe` responsibility of `SqliteDbHelper` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
