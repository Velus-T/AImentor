package com.example.aimentor.science.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.example.aimentor.science.data.local.ScienceDbHelper;
import com.example.aimentor.science.data.local.ScienceDbContract;
import com.example.aimentor.science.data.local.TextCipher;
import com.example.aimentor.science.domain.model.ScienceBookmark;
import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.repository.ScienceBookmarkRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SQLiteScienceBookmarkRepository implements ScienceBookmarkRepository {
    private final ScienceDbHelper dbHelper;
    private final TextCipher textCipher;

    public SQLiteScienceBookmarkRepository(ScienceDbHelper dbHelper, TextCipher textCipher) {
        this.dbHelper = dbHelper;
        this.textCipher = textCipher;
    }

    @Override
    public long save(ScienceBookmark bookmark) {
        ContentValues values = new ContentValues();
        values.put(ScienceDbContract.Bookmark.USER_ID, bookmark.getUserId());
        values.put(ScienceDbContract.Bookmark.HISTORY_ID, bookmark.getHistoryId());
        values.put(ScienceDbContract.Bookmark.SUBJECT, bookmark.getSubject().getCode());
        values.put(ScienceDbContract.Bookmark.TITLE, textCipher.encrypt(bookmark.getTitle()));
        values.put(ScienceDbContract.Bookmark.CONTENT, textCipher.encrypt(bookmark.getContent()));
        values.put(ScienceDbContract.Bookmark.CREATED_AT, bookmark.getCreatedAt());
        return dbHelper.getWritableDatabase().insertWithOnConflict(
                ScienceDbContract.Bookmark.TABLE,
                null,
                values,
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    @Override
    public boolean existsForHistory(int userId, long historyId) {
        String selection = ScienceDbContract.Bookmark.USER_ID + "=? AND "
                + ScienceDbContract.Bookmark.HISTORY_ID + "=?";
        try (Cursor cursor = dbHelper.getReadableDatabase().query(
                ScienceDbContract.Bookmark.TABLE,
                new String[]{ScienceDbContract.Bookmark.ID},
                selection,
                new String[]{String.valueOf(userId), String.valueOf(historyId)},
                null,
                null,
                null,
                "1")) {
            return cursor.moveToFirst();
        }
    }

    @Override
    public void deleteForHistory(int userId, long historyId) {
        dbHelper.getWritableDatabase().delete(
                ScienceDbContract.Bookmark.TABLE,
                ScienceDbContract.Bookmark.USER_ID + "=? AND " + ScienceDbContract.Bookmark.HISTORY_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(historyId)}
        );
    }

    @Override
    public List<ScienceBookmark> search(int userId, ScienceSubject subject, String keyword) {
        List<String> clauses = new ArrayList<>();
        List<String> args = new ArrayList<>();
        clauses.add(ScienceDbContract.Bookmark.USER_ID + "=?");
        args.add(String.valueOf(userId));
        if (subject != null) {
            clauses.add(ScienceDbContract.Bookmark.SUBJECT + "=?");
            args.add(subject.getCode());
        }

        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<ScienceBookmark> items = new ArrayList<>();
        try (Cursor cursor = dbHelper.getReadableDatabase().query(
                ScienceDbContract.Bookmark.TABLE,
                null,
                TextUtils.join(" AND ", clauses),
                args.toArray(new String[0]),
                null,
                null,
                ScienceDbContract.Bookmark.CREATED_AT + " DESC")) {
            while (cursor.moveToNext()) {
                ScienceBookmark item = mapBookmark(cursor);
                if (normalizedKeyword.isEmpty()
                        || item.getTitle().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || item.getContent().toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    @Override
    public int countByUser(int userId) {
        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + ScienceDbContract.Bookmark.TABLE + " WHERE "
                        + ScienceDbContract.Bookmark.USER_ID + "=?",
                new String[]{String.valueOf(userId)})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    private ScienceBookmark mapBookmark(Cursor cursor) {
        return new ScienceBookmark(
                cursor.getLong(cursor.getColumnIndexOrThrow(ScienceDbContract.Bookmark.ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(ScienceDbContract.Bookmark.USER_ID)),
                cursor.getLong(cursor.getColumnIndexOrThrow(ScienceDbContract.Bookmark.HISTORY_ID)),
                ScienceSubject.fromCode(cursor.getString(cursor.getColumnIndexOrThrow(ScienceDbContract.Bookmark.SUBJECT))),
                textCipher.decrypt(cursor.getString(cursor.getColumnIndexOrThrow(ScienceDbContract.Bookmark.TITLE))),
                textCipher.decrypt(cursor.getString(cursor.getColumnIndexOrThrow(ScienceDbContract.Bookmark.CONTENT))),
                cursor.getLong(cursor.getColumnIndexOrThrow(ScienceDbContract.Bookmark.CREATED_AT))
        );
    }
}
