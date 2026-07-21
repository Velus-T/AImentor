package com.example.aimentor.science.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database owned only by the Science module.
 * Keeping it separate prevents Science schema changes from affecting the existing app database.
 */
public final class ScienceDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "science_ai.db";
    private static final int DATABASE_VERSION = 2;

    public ScienceDbHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ScienceDbContract.createHistoryTable());
        db.execSQL(ScienceDbContract.createBookmarkTable());
        db.execSQL(ScienceDbContract.createQuizAttemptTable());
        createIndexes(db);
    }

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
        createIndexes(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop all tables and recreate them if downgraded.
        db.execSQL("DROP TABLE IF EXISTS " + ScienceDbContract.History.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ScienceDbContract.Bookmark.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ScienceDbContract.QuizAttempt.TABLE);
        onCreate(db);
    }

    private void createIndexes(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_science_history_user_subject_time ON "
                + ScienceDbContract.History.TABLE + "("
                + ScienceDbContract.History.USER_ID + ","
                + ScienceDbContract.History.SUBJECT + ","
                + ScienceDbContract.History.CREATED_AT + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_science_quiz_adaptive ON "
                + ScienceDbContract.QuizAttempt.TABLE + "("
                + ScienceDbContract.QuizAttempt.USER_ID + ","
                + ScienceDbContract.QuizAttempt.SUBJECT + ","
                + ScienceDbContract.QuizAttempt.EDUCATION_LEVEL + ","
                + ScienceDbContract.QuizAttempt.CREATED_AT + ")");
    }
}
