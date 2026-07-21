package com.example.aimentor.science.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.aimentor.science.data.local.ScienceDbHelper;
import com.example.aimentor.science.data.local.ScienceDbContract;
import com.example.aimentor.science.data.local.TextCipher;
import com.example.aimentor.science.domain.model.ScienceHistory;
import com.example.aimentor.science.domain.model.ScienceQuestion;
import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.repository.ScienceHistoryRepository;
import com.example.aimentor.science.util.QuestionFingerprint;
import com.example.aimentor.science.util.QuestionNormalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SQLiteScienceHistoryRepository implements ScienceHistoryRepository {
    private final ScienceDbHelper dbHelper;
    private final QuestionNormalizer normalizer;
    private final QuestionFingerprint fingerprint;
    private final TextCipher textCipher;

    public SQLiteScienceHistoryRepository(ScienceDbHelper dbHelper,
                                          QuestionNormalizer normalizer,
                                          QuestionFingerprint fingerprint,
                                          TextCipher textCipher) {
        this.dbHelper = dbHelper;
        this.normalizer = normalizer;
        this.fingerprint = fingerprint;
        this.textCipher = textCipher;
    }

    @Override
    public long save(ScienceQuestion question, String answer) {
        String rawQuestion = question.getQuestionText().isEmpty()
                ? "Image-based science question" : question.getQuestionText();
        ContentValues values = new ContentValues();
        values.put(ScienceDbContract.History.USER_ID, question.getUserId());
        values.put(ScienceDbContract.History.SUBJECT, question.getSubject().getCode());
        values.put(ScienceDbContract.History.EDUCATION_LEVEL, question.getEducationLevel().name());
        values.put(ScienceDbContract.History.EXPLANATION_STYLE, question.getExplanationStyle().name());
        values.put(ScienceDbContract.History.NORMALIZED_QUESTION,
                fingerprint.create(normalizer.normalize(question.getQuestionText())));
        values.put(ScienceDbContract.History.QUESTION, textCipher.encrypt(rawQuestion));
        values.put(ScienceDbContract.History.IMAGE_URI, question.getImageUri());
        values.put(ScienceDbContract.History.ANSWER, textCipher.encrypt(answer));
        values.put(ScienceDbContract.History.CREATED_AT, System.currentTimeMillis());
        return dbHelper.getWritableDatabase().insert(ScienceDbContract.History.TABLE, null, values);
    }

    @Override
    public ScienceHistory findCached(int userId, ScienceSubject subject, String educationLevel,
                                     String normalizedQuestion) {
        String selection = ScienceDbContract.History.USER_ID + "=? AND "
                + ScienceDbContract.History.SUBJECT + "=? AND "
                + ScienceDbContract.History.EDUCATION_LEVEL + "=? AND "
                + ScienceDbContract.History.NORMALIZED_QUESTION + "=?";
        String[] args = {
                String.valueOf(userId), subject.getCode(), educationLevel,
                fingerprint.create(normalizedQuestion)
        };
        try (Cursor cursor = dbHelper.getReadableDatabase().query(
                ScienceDbContract.History.TABLE,
                null,
                selection,
                args,
                null,
                null,
                ScienceDbContract.History.CREATED_AT + " DESC",
                "1")) {
            return cursor.moveToFirst() ? mapHistory(cursor) : null;
        }
    }

    @Override
    public List<ScienceHistory> search(int userId, ScienceSubject subject, String keyword) {
        List<String> clauses = new ArrayList<>();
        List<String> args = new ArrayList<>();
        clauses.add(ScienceDbContract.History.USER_ID + "=?");
        args.add(String.valueOf(userId));
        if (subject != null) {
            clauses.add(ScienceDbContract.History.SUBJECT + "=?");
            args.add(subject.getCode());
        }

        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<ScienceHistory> items = new ArrayList<>();
        try (Cursor cursor = dbHelper.getReadableDatabase().query(
                ScienceDbContract.History.TABLE,
                null,
                TextUtils.join(" AND ", clauses),
                args.toArray(new String[0]),
                null,
                null,
                ScienceDbContract.History.CREATED_AT + " DESC")) {
            while (cursor.moveToNext()) {
                ScienceHistory item = mapHistory(cursor);
                if (normalizedKeyword.isEmpty()
                        || item.getQuestion().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || item.getAnswer().toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    @Override
    public void addReviewDuration(long historyId, int seconds) {
        int safeSeconds = Math.max(1, seconds);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + ScienceDbContract.History.TABLE + " SET "
                        + ScienceDbContract.History.REVIEW_SECONDS + " = "
                        + ScienceDbContract.History.REVIEW_SECONDS + " + ? WHERE "
                        + ScienceDbContract.History.ID + " = ?",
                new Object[]{safeSeconds, historyId});
    }

    @Override
    public int countByUser(int userId) {
        return intQuery("SELECT COUNT(*) FROM " + ScienceDbContract.History.TABLE
                + " WHERE " + ScienceDbContract.History.USER_ID + "=?", userId);
    }

    @Override
    public int totalReviewSeconds(int userId) {
        return intQuery("SELECT COALESCE(SUM(" + ScienceDbContract.History.REVIEW_SECONDS + "),0) FROM "
                + ScienceDbContract.History.TABLE + " WHERE "
                + ScienceDbContract.History.USER_ID + "=?", userId);
    }

    @Override
    public ScienceSubject mostFrequentSubject(int userId) {
        String sql = "SELECT " + ScienceDbContract.History.SUBJECT + ", COUNT(*) AS amount FROM "
                + ScienceDbContract.History.TABLE + " WHERE "
                + ScienceDbContract.History.USER_ID + "=? GROUP BY "
                + ScienceDbContract.History.SUBJECT + " ORDER BY amount DESC LIMIT 1";
        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(userId)})) {
            return cursor.moveToFirst()
                    ? ScienceSubject.fromCode(cursor.getString(0))
                    : ScienceSubject.PHYSICS;
        }
    }

    private int intQuery(String sql, int userId) {
        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(userId)})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    private ScienceHistory mapHistory(Cursor cursor) {
        return new ScienceHistory(
                cursor.getLong(cursor.getColumnIndexOrThrow(ScienceDbContract.History.ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(ScienceDbContract.History.USER_ID)),
                ScienceSubject.fromCode(cursor.getString(cursor.getColumnIndexOrThrow(ScienceDbContract.History.SUBJECT))),
                cursor.getString(cursor.getColumnIndexOrThrow(ScienceDbContract.History.EDUCATION_LEVEL)),
                cursor.getString(cursor.getColumnIndexOrThrow(ScienceDbContract.History.EXPLANATION_STYLE)),
                textCipher.decrypt(cursor.getString(cursor.getColumnIndexOrThrow(ScienceDbContract.History.QUESTION))),
                cursor.getString(cursor.getColumnIndexOrThrow(ScienceDbContract.History.IMAGE_URI)),
                textCipher.decrypt(cursor.getString(cursor.getColumnIndexOrThrow(ScienceDbContract.History.ANSWER))),
                cursor.getLong(cursor.getColumnIndexOrThrow(ScienceDbContract.History.CREATED_AT)),
                cursor.getInt(cursor.getColumnIndexOrThrow(ScienceDbContract.History.REVIEW_SECONDS))
        );
    }
}
