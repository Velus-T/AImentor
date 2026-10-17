package com.brightpath.science.data.repository;

import android.content.ContentValues;
import android.database.Cursor;

import com.brightpath.science.data.local.ScienceDbContract;
import com.brightpath.science.data.local.ScienceDbHelper;
import com.brightpath.science.domain.model.ScienceNote;
import com.brightpath.science.domain.repository.ScienceNoteRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite implementation of {@link ScienceNoteRepository}.
 *
 * <p>review FLOW:
 * UI -> ScienceNoteRepository -> this class -> science_notes table in science_ai.db.</p>
 *
 * <p>Every UPDATE, DELETE and SELECT is scoped by {@code user_id}. This prevents one
 * account from reading or changing another account's notes on the same device.</p>
 */
public final class SQLiteScienceNoteRepository implements ScienceNoteRepository {
    private final ScienceDbHelper helper;

        /*
 * Responsibility: Initializes SQLiteScienceNoteRepository and receives or prepares the dependencies/state required by this data repository.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public SQLiteScienceNoteRepository(ScienceDbHelper helper) {
        this.helper = helper;
    }

        /*
 * Responsibility: Inserts a new Science note into science_notes with the active user ID, title, content, and subject.
 * Purpose: Persists learner-written notes locally so they can be reviewed offline and kept separate between users.
 */
@Override
    public long insert(int userId, String title, String content, String subject) {
        long now = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(ScienceDbContract.Note.USER_ID, userId);
        values.put(ScienceDbContract.Note.TITLE, clean(title));
        values.put(ScienceDbContract.Note.CONTENT, clean(content));
        values.put(ScienceDbContract.Note.SUBJECT, com.brightpath.science.util.ScienceNoteSubject.normalize(subject));
        values.put(ScienceDbContract.Note.CREATED_AT, now);
        values.put(ScienceDbContract.Note.UPDATED_AT, now);

        return helper.getWritableDatabase().insert(
                ScienceDbContract.Note.TABLE,
                null,
                values
        );
    }

        /*
 * Responsibility: Updates an existing Science note only when both the note ID and user ID match.
 * Purpose: Prevents one user from modifying another user's note and supports safe note editing.
 */
@Override
    public boolean update(long id,
                          int userId,
                          String title,
                          String content,
                          String subject) {
        ContentValues values = new ContentValues();
        values.put(ScienceDbContract.Note.TITLE, clean(title));
        values.put(ScienceDbContract.Note.CONTENT, clean(content));
        values.put(ScienceDbContract.Note.SUBJECT, com.brightpath.science.util.ScienceNoteSubject.normalize(subject));
        values.put(ScienceDbContract.Note.UPDATED_AT, System.currentTimeMillis());

        int rows = helper.getWritableDatabase().update(
                ScienceDbContract.Note.TABLE,
                values,
                ScienceDbContract.Note.ID + "=? AND " + ScienceDbContract.Note.USER_ID + "=?",
                new String[]{String.valueOf(id), String.valueOf(userId)}
        );
        return rows > 0;
    }

        /*
 * Responsibility: Deletes a Science note using both note ID and user ID as ownership conditions.
 * Purpose: Provides safe CRUD deletion while preserving account-level data isolation.
 */
@Override
    public boolean delete(long id, int userId) {
        int rows = helper.getWritableDatabase().delete(
                ScienceDbContract.Note.TABLE,
                ScienceDbContract.Note.ID + "=? AND " + ScienceDbContract.Note.USER_ID + "=?",
                new String[]{String.valueOf(id), String.valueOf(userId)}
        );
        return rows > 0;
    }

        /*
 * Responsibility: Queries all Science notes owned by the active user and maps SQLite rows into ScienceNote objects.
 * Purpose: Supplies the Notes UI with persistent user-scoped data without exposing raw Cursor/database logic to the screen.
 */
@Override
    public List<ScienceNote> getAll(int userId) {
        List<ScienceNote> notes = new ArrayList<>();
        String sql = "SELECT "
                + ScienceDbContract.Note.ID + ","
                + ScienceDbContract.Note.TITLE + ","
                + ScienceDbContract.Note.CONTENT + ","
                + ScienceDbContract.Note.SUBJECT + ","
                + ScienceDbContract.Note.UPDATED_AT
                + " FROM " + ScienceDbContract.Note.TABLE
                + " WHERE " + ScienceDbContract.Note.USER_ID + "=?"
                + " ORDER BY " + ScienceDbContract.Note.UPDATED_AT + " DESC";

        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                sql,
                new String[]{String.valueOf(userId)}
        )) {
            while (cursor.moveToNext()) {
                notes.add(new ScienceNote(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        com.brightpath.science.util.ScienceNoteSubject.normalize(cursor.getString(3)),
                        cursor.getLong(4)
                ));
            }
        }
        return notes;
    }

        /*
 * Responsibility: Normalizes or formats the value handled by `clean` into a stable representation.
 * Purpose: Improves consistency for display, comparison, caching, storage, or AI prompt construction.
 */
private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
