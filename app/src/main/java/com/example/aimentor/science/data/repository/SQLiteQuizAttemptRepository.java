package com.example.aimentor.science.data.repository;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.aimentor.science.data.local.ScienceDbContract;
import com.example.aimentor.science.data.local.ScienceDbHelper;
import com.example.aimentor.science.domain.model.EducationLevel;
import com.example.aimentor.science.domain.model.QuizChallenge;
import com.example.aimentor.science.domain.model.QuizPerformance;
import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.repository.QuizAttemptRepository;

public final class SQLiteQuizAttemptRepository implements QuizAttemptRepository {
    private final ScienceDbHelper dbHelper;

    public SQLiteQuizAttemptRepository(ScienceDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public long saveAttempt(int userId, ScienceSubject subject, EducationLevel level,
                            String topic, QuizChallenge challenge,
                            int totalQuestions, int correctAnswers) {
        ContentValues values = new ContentValues();
        values.put(ScienceDbContract.QuizAttempt.USER_ID, userId);
        values.put(ScienceDbContract.QuizAttempt.SUBJECT, subject.getCode());
        values.put(ScienceDbContract.QuizAttempt.EDUCATION_LEVEL, level.name());
        values.put(ScienceDbContract.QuizAttempt.TOPIC, topic == null ? "" : topic.trim());
        values.put(ScienceDbContract.QuizAttempt.CHALLENGE, challenge.name());
        values.put(ScienceDbContract.QuizAttempt.TOTAL, totalQuestions);
        values.put(ScienceDbContract.QuizAttempt.CORRECT, correctAnswers);
        values.put(ScienceDbContract.QuizAttempt.CREATED_AT, System.currentTimeMillis());
        return dbHelper.getWritableDatabase().insert(
                ScienceDbContract.QuizAttempt.TABLE, null, values);
    }

    @Override
    public QuizPerformance getRecentPerformance(int userId, ScienceSubject subject,
                                                EducationLevel level, int attemptLimit) {
        String sql = "SELECT " + ScienceDbContract.QuizAttempt.TOTAL + ", "
                + ScienceDbContract.QuizAttempt.CORRECT + " FROM "
                + ScienceDbContract.QuizAttempt.TABLE + " WHERE "
                + ScienceDbContract.QuizAttempt.USER_ID + "=? AND "
                + ScienceDbContract.QuizAttempt.SUBJECT + "=? AND "
                + ScienceDbContract.QuizAttempt.EDUCATION_LEVEL + "=? ORDER BY "
                + ScienceDbContract.QuizAttempt.CREATED_AT + " DESC LIMIT ?";

        int attempts = 0;
        int total = 0;
        int correct = 0;
        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql,
                new String[]{
                        String.valueOf(userId),
                        subject.getCode(),
                        level.name(),
                        String.valueOf(Math.max(1, attemptLimit))
                })) {
            while (cursor.moveToNext()) {
                attempts++;
                total += cursor.getInt(0);
                correct += cursor.getInt(1);
            }
        }
        return new QuizPerformance(attempts, total, correct);
    }

    @Override
    public int countAttempts(int userId) {
        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + ScienceDbContract.QuizAttempt.TABLE + " WHERE "
                        + ScienceDbContract.QuizAttempt.USER_ID + "=?",
                new String[]{String.valueOf(userId)})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    @Override
    public double calculateAccuracy(int userId) {
        String sql = "SELECT COALESCE(SUM(" + ScienceDbContract.QuizAttempt.CORRECT + "),0), "
                + "COALESCE(SUM(" + ScienceDbContract.QuizAttempt.TOTAL + "),0) FROM "
                + ScienceDbContract.QuizAttempt.TABLE + " WHERE "
                + ScienceDbContract.QuizAttempt.USER_ID + "=?";
        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(userId)})) {
            if (!cursor.moveToFirst()) return 0;
            int correct = cursor.getInt(0);
            int total = cursor.getInt(1);
            return total == 0 ? 0 : (correct * 100.0) / total;
        }
    }
}
