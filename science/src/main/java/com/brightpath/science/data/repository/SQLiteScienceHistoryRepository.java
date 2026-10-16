package com.brightpath.science.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.brightpath.science.data.local.ScienceDbHelper;
import com.brightpath.science.data.local.ScienceDbContract;
import com.brightpath.science.data.local.TextCipher;
import com.brightpath.science.domain.model.ScienceHistory;
import com.brightpath.science.domain.model.ScienceQuestion;
import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.domain.repository.ScienceHistoryRepository;
import com.brightpath.science.util.QuestionFingerprint;
import com.brightpath.science.util.QuestionNormalizer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class SQLiteScienceHistoryRepository implements ScienceHistoryRepository {
    private final ScienceDbHelper dbHelper;
    private final QuestionNormalizer normalizer;
    private final QuestionFingerprint fingerprint;
    private final TextCipher textCipher;

        /*
 * Responsibility: Initializes SQLiteScienceHistoryRepository and receives or prepares the dependencies/state required by this data repository.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public SQLiteScienceHistoryRepository(ScienceDbHelper dbHelper,
                                          QuestionNormalizer normalizer,
                                          QuestionFingerprint fingerprint,
                                          TextCipher textCipher) {
        this.dbHelper = dbHelper;
        this.normalizer = normalizer;
        this.fingerprint = fingerprint;
        this.textCipher = textCipher;
    }

        /*
 * Responsibility: Normalizes/encrypts the required Science data and inserts the question-answer record for the correct user, subject, and conversation.
 * Purpose: Creates persistent, user-scoped learning history that can be reopened offline.
 */
@Override
    public long save(ScienceQuestion question, String answer) {
        String rawQuestion = question.getQuestionText().isEmpty()
                ? "Image-based science question" : question.getQuestionText();
        ContentValues values = new ContentValues();
        values.put(ScienceDbContract.History.USER_ID, question.getUserId());
        String conversationId = question.getConversationId();
        if (conversationId == null || conversationId.trim().isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }
        values.put(ScienceDbContract.History.CONVERSATION_ID, conversationId);
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

        /*
 * Responsibility: Looks up a previous compatible Science answer using the current user, subject, education level, and normalized question.
 * Purpose: Supports the exact-question cache while preventing cross-user or cross-subject answer reuse.
 */
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

        /*
 * Responsibility: Retrieves the data/state required by `findById` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public ScienceHistory findById(int userId, long historyId) {
        if (historyId <= 0) return null;
        String selection = ScienceDbContract.History.USER_ID + "=? AND "
                + ScienceDbContract.History.ID + "=?";
        try (Cursor cursor = dbHelper.getReadableDatabase().query(
                ScienceDbContract.History.TABLE,
                null,
                selection,
                new String[]{String.valueOf(userId), String.valueOf(historyId)},
                null,
                null,
                null,
                "1")) {
            return cursor.moveToFirst() ? mapHistory(cursor) : null;
        }
    }

        /*
 * Responsibility: Loads all history rows belonging to one conversation and one user in chronological order.
 * Purpose: Reconstructs a complete saved Science chat without exposing another user's records.
 */
@Override
    public List<ScienceHistory> findConversation(int userId, String conversationId) {
        List<ScienceHistory> items = new ArrayList<>();
        if (conversationId == null || conversationId.trim().isEmpty()) return items;
        String selection = ScienceDbContract.History.USER_ID + "=? AND "
                + ScienceDbContract.History.CONVERSATION_ID + "=?";
        try (Cursor cursor = dbHelper.getReadableDatabase().query(
                ScienceDbContract.History.TABLE,
                null,
                selection,
                new String[]{String.valueOf(userId), conversationId.trim()},
                null,
                null,
                ScienceDbContract.History.CREATED_AT + " ASC, "
                        + ScienceDbContract.History.ID + " ASC")) {
            while (cursor.moveToNext()) items.add(mapHistory(cursor));
        }
        return items;
    }

        /*
 * Responsibility: Searches saved Science history using user ownership, optional subject filtering, and the requested keyword.
 * Purpose: Provides fast History-screen review while keeping the query rules inside the repository layer.
 */
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
        Set<String> seenConversations = new LinkedHashSet<>();
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
                boolean matches = normalizedKeyword.isEmpty()
                        || item.getQuestion().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || item.getAnswer().toLowerCase(Locale.ROOT).contains(normalizedKeyword);
                if (matches && seenConversations.add(item.getConversationId())) {
                    items.add(item);
                }
            }
        }
        return items;
    }

        /*
 * Responsibility: Adds the data, listener, duration, key, or UI element represented by `addReviewDuration`.
 * Purpose: Keeps the incremental update explicit and isolated from unrelated responsibilities.
 */
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

        /*
 * Responsibility: Performs the repository operation `countByUser` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
@Override
    public int countByUser(int userId) {
        return intQuery("SELECT COUNT(*) FROM " + ScienceDbContract.History.TABLE
                + " WHERE " + ScienceDbContract.History.USER_ID + "=?", userId);
    }

        /*
 * Responsibility: Converts input data in `totalReviewSeconds` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
@Override
    public int totalReviewSeconds(int userId) {
        return intQuery("SELECT COALESCE(SUM(" + ScienceDbContract.History.REVIEW_SECONDS + "),0) FROM "
                + ScienceDbContract.History.TABLE + " WHERE "
                + ScienceDbContract.History.USER_ID + "=?", userId);
    }

        /*
 * Responsibility: Performs the repository operation `mostFrequentSubject` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
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

        /*
 * Responsibility: Performs the repository operation `intQuery` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
private int intQuery(String sql, int userId) {
        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(userId)})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

        /*
 * Responsibility: Converts input data in `mapHistory` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
private ScienceHistory mapHistory(Cursor cursor) {
        return new ScienceHistory(
                cursor.getLong(cursor.getColumnIndexOrThrow(ScienceDbContract.History.ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(ScienceDbContract.History.USER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(ScienceDbContract.History.CONVERSATION_ID)),
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
