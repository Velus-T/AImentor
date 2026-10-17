package com.brightpath.science.data.repository;

import android.content.ContentValues;
import android.database.Cursor;

import com.brightpath.science.data.local.ScienceDbContract;
import com.brightpath.science.data.local.ScienceDbHelper;
import com.brightpath.science.domain.model.EducationLevel;
import com.brightpath.science.domain.model.QuizChallenge;
import com.brightpath.science.domain.model.QuizPerformance;
import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.domain.repository.QuizAttemptRepository;

public final class SQLiteQuizAttemptRepository implements QuizAttemptRepository {
    private final ScienceDbHelper dbHelper;

        /*
 * Responsibility: Initializes SQLiteQuizAttemptRepository and receives or prepares the dependencies/state required by this data repository.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public SQLiteQuizAttemptRepository(ScienceDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

        /*
 * Responsibility: Persists the data supplied to `saveAttempt` through the appropriate Science storage/repository.
 * Purpose: Keeps important learner state available after navigation or application restart.
 */
@Override
    public long saveAttempt(int userId, ScienceSubject subject, EducationLevel level,
                            String topic, QuizChallenge challenge,
                            int totalQuestions, int correctAnswers, int expEarned) {
        ContentValues values = new ContentValues();
        values.put(ScienceDbContract.QuizAttempt.USER_ID, userId);
        values.put(ScienceDbContract.QuizAttempt.SUBJECT, subject.getCode());
        values.put(ScienceDbContract.QuizAttempt.EDUCATION_LEVEL, level.name());
        values.put(ScienceDbContract.QuizAttempt.TOPIC, topic == null ? "" : topic.trim());
        values.put(ScienceDbContract.QuizAttempt.CHALLENGE, challenge.name());
        values.put(ScienceDbContract.QuizAttempt.TOTAL, Math.max(0, totalQuestions));
        values.put(ScienceDbContract.QuizAttempt.CORRECT, Math.max(0, correctAnswers));
        values.put(ScienceDbContract.QuizAttempt.EXP_EARNED, Math.max(0, expEarned));
        values.put(ScienceDbContract.QuizAttempt.CREATED_AT, System.currentTimeMillis());
        return totalQuestions > 0
                ? com.example.mentor.data.LearningDays.insert(dbHelper.getWritableDatabase(),
                    ScienceDbContract.QuizAttempt.TABLE, values, userId,
                    values.getAsLong(ScienceDbContract.QuizAttempt.CREATED_AT))
                : dbHelper.getWritableDatabase().insert(
                    ScienceDbContract.QuizAttempt.TABLE, null, values);
    }

        /*
 * Responsibility: Retrieves the data/state required by `getRecentPerformance` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
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

        /*
 * Responsibility: Retrieves the data/state required by `getSubjectPerformance` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public QuizPerformance getSubjectPerformance(int userId, ScienceSubject subject) {
        if (subject == null) {
            return QuizPerformance.empty();
        }

        String sql = "SELECT COUNT(*), "
                + "COALESCE(SUM(" + ScienceDbContract.QuizAttempt.TOTAL + "),0), "
                + "COALESCE(SUM(" + ScienceDbContract.QuizAttempt.CORRECT + "),0) FROM "
                + ScienceDbContract.QuizAttempt.TABLE + " WHERE "
                + ScienceDbContract.QuizAttempt.USER_ID + "=? AND "
                + ScienceDbContract.QuizAttempt.SUBJECT + "=?";

        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                sql,
                new String[]{String.valueOf(userId), subject.getCode()})) {
            if (!cursor.moveToFirst()) {
                return QuizPerformance.empty();
            }
            return new QuizPerformance(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2)
            );
        }
    }

        /*
 * Responsibility: Performs the repository operation `countAttempts` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
@Override
    public int countAttempts(int userId) {
        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + ScienceDbContract.QuizAttempt.TABLE + " WHERE "
                        + ScienceDbContract.QuizAttempt.USER_ID + "=?",
                new String[]{String.valueOf(userId)})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

        /*
 * Responsibility: Calculates the value represented by `calculateAccuracy` from the supplied Science learning data.
 * Purpose: Keeps the calculation rule deterministic and reusable outside the UI.
 */
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

        /*
 * Responsibility: Retrieves the data/state required by `getTotalExp` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public int getTotalExp(int userId) {
        String sql = "SELECT COALESCE(SUM(" + ScienceDbContract.QuizAttempt.EXP_EARNED
                + "),0) FROM " + ScienceDbContract.QuizAttempt.TABLE + " WHERE "
                + ScienceDbContract.QuizAttempt.USER_ID + "=?";
        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                sql, new String[]{String.valueOf(userId)})) {
            return cursor.moveToFirst() ? Math.max(0, cursor.getInt(0)) : 0;
        }
    }
}
