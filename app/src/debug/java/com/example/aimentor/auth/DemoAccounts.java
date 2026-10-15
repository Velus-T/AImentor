package com.example.aimentor.auth;

import android.database.sqlite.SQLiteDatabase;

/** Test accounts exist only in debug builds; existing accounts are never changed. */
public final class DemoAccounts {
    private DemoAccounts() { }

    public static void seed(SQLiteDatabase db) {
        insertIfMissing(db, "hocvien01", "Mentor01!2026");
        insertIfMissing(db, "hocvien02", "Mentor02!2026");
        insertIfMissing(db, "hocvien03", "Mentor03!2026");
    }

    private static void insertIfMissing(SQLiteDatabase db, String username, String password) {
        db.execSQL("INSERT INTO users (username, password, email, phone, role, createdAt) "
                + "SELECT ?, ?, ?, '', 1, datetime('now') "
                + "WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = ?)",
                new Object[]{username, password, username + "@example.invalid", username});
    }
}
