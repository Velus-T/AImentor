package com.example.aimentor.auth;

import android.database.sqlite.SQLiteDatabase;

/** Release builds do not create demo accounts. */
public final class DemoAccounts {
    private DemoAccounts() { }
    public static void seed(SQLiteDatabase db) { }
}
