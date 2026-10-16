package com.brightpath.science.data.local;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLiteOpenHelper for the Science-owned database {@code science_ai.db}.
 *
 * <p>review NOTE:</p>
 * <ul>
 *   <li>{@code onCreate()} creates History, Bookmark, Note and QuizAttempt tables.</li>
 *   <li>{@code onUpgrade()} performs non-destructive schema migration by version.</li>
 *   <li>Indexes start with the active {@code user_id} where ownership filtering matters.</li>
 * </ul>
 *
 * <p>Keeping this database separate prevents Science schema changes from affecting other modules.</p>
 */
public final class ScienceDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "science_ai.db";
    private static final int DATABASE_VERSION = 5;

        /*
 * Responsibility: Initializes ScienceDbHelper and receives or prepares the dependencies/state required by this SQLite database infrastructure.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceDbHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    // region Database lifecycle

        /*
 * Responsibility: Creates the Science SQLite tables and indexes when science_ai.db is created for the first time.
 * Purpose: Defines the persistent structure used by Science history, bookmarks, notes, quiz attempts, progress, and planning data.
 */
@Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ScienceDbContract.createHistoryTable());
        db.execSQL(ScienceDbContract.createBookmarkTable());
        db.execSQL(ScienceDbContract.createQuizAttemptTable());
        db.execSQL(ScienceDbContract.createNoteTable());
        createIndexes(db);
    }

        /*
 * Responsibility: Applies version-based, non-destructive database migrations from an older Science schema to the current schema.
 * Purpose: Preserves existing learner data while allowing new columns/tables/indexes to be introduced safely.
 */
@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + ScienceDbContract.QuizAttempt.TABLE
                    + " ADD COLUMN " + ScienceDbContract.QuizAttempt.EDUCATION_LEVEL
                    + " TEXT NOT NULL DEFAULT 'MIDDLE_SCHOOL'");
            db.execSQL("ALTER TABLE " + ScienceDbContract.QuizAttempt.TABLE
                    + " ADD COLUMN " + ScienceDbContract.QuizAttempt.TOPIC + " TEXT");
            db.execSQL("ALTER TABLE " + ScienceDbContract.QuizAttempt.TABLE
                    + " ADD COLUMN " + ScienceDbContract.QuizAttempt.CHALLENGE
                    + " TEXT NOT NULL DEFAULT 'ON_LEVEL'");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + ScienceDbContract.QuizAttempt.TABLE
                    + " ADD COLUMN " + ScienceDbContract.QuizAttempt.EXP_EARNED
                    + " INTEGER NOT NULL DEFAULT 0");
        }
        if (oldVersion < 4) {
            db.execSQL(ScienceDbContract.createNoteTable());
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + ScienceDbContract.History.TABLE
                    + " ADD COLUMN " + ScienceDbContract.History.CONVERSATION_ID
                    + " TEXT NOT NULL DEFAULT ''");
            db.execSQL("UPDATE " + ScienceDbContract.History.TABLE + " SET "
                    + ScienceDbContract.History.CONVERSATION_ID + " = 'legacy-' || "
                    + ScienceDbContract.History.ID + " WHERE "
                    + ScienceDbContract.History.CONVERSATION_ID + " = ''");
        }
        createIndexes(db);
    }

        /*
 * Responsibility: Performs the `onDowngrade` operation for ScienceDbHelper within the Science Module.
 * Purpose: Encapsulates this responsibility inside the SQLite database infrastructure so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Refuse an unsupported downgrade rather than silently deleting a user's learning data.
        throw new android.database.sqlite.SQLiteException("Unsupported Science database downgrade: " + oldVersion + " -> " + newVersion);
    }

    // endregion

    // region Indexes and read helpers

        /*
 * Responsibility: Creates SQLite indexes used by frequent Science queries such as user-, subject-, conversation-, or history-based lookups.
 * Purpose: Improves lookup performance and supports efficient user-scoped retrieval.
 */
private void createIndexes(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_science_history_user_subject_time ON "
                + ScienceDbContract.History.TABLE + "("
                + ScienceDbContract.History.USER_ID + ","
                + ScienceDbContract.History.SUBJECT + ","
                + ScienceDbContract.History.CREATED_AT + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_science_history_conversation_time ON "
                + ScienceDbContract.History.TABLE + "("
                + ScienceDbContract.History.USER_ID + ","
                + ScienceDbContract.History.CONVERSATION_ID + ","
                + ScienceDbContract.History.CREATED_AT + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_science_notes_user_time ON "
                + ScienceDbContract.Note.TABLE + "("
                + ScienceDbContract.Note.USER_ID + ","
                + ScienceDbContract.Note.UPDATED_AT + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_science_quiz_adaptive ON "
                + ScienceDbContract.QuizAttempt.TABLE + "("
                + ScienceDbContract.QuizAttempt.USER_ID + ","
                + ScienceDbContract.QuizAttempt.SUBJECT + ","
                + ScienceDbContract.QuizAttempt.EDUCATION_LEVEL + ","
                + ScienceDbContract.QuizAttempt.CREATED_AT + ")");
    }

        /*
 * Responsibility: Retrieves the data/state required by `getTotalExp` from the appropriate SQLite database infrastructure.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getTotalExp(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT SUM(" + ScienceDbContract.QuizAttempt.EXP_EARNED + ") FROM "
                + ScienceDbContract.QuizAttempt.TABLE + " WHERE "
                + ScienceDbContract.QuizAttempt.USER_ID + "=?";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    // endregion
}
