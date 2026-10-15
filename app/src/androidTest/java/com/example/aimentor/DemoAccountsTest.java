package com.example.aimentor;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.aimentor.auth.DemoAccounts;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DemoAccountsTest {
    @Test public void seedingIsRepeatableAndPreservesAnExistingPassword() {
        try (SQLiteDatabase db = SQLiteDatabase.create(null)) {
            db.execSQL("CREATE TABLE users (username TEXT, password TEXT, email TEXT, "
                    + "phone TEXT, role INTEGER, createdAt TEXT)");
            db.execSQL("INSERT INTO users (username, password) VALUES ('hocvien01', 'existing-password')");
            DemoAccounts.seed(db);
            DemoAccounts.seed(db);
            try (Cursor count = db.rawQuery("SELECT COUNT(*) FROM users", null)) {
                count.moveToFirst();
                assertEquals(3, count.getInt(0));
            }
            try (Cursor user = db.rawQuery("SELECT password FROM users WHERE username = 'hocvien01'", null)) {
                user.moveToFirst();
                assertEquals("existing-password", user.getString(0));
            }
            try (Cursor user = db.rawQuery("SELECT password, role FROM users WHERE username = 'hocvien02'", null)) {
                user.moveToFirst();
                assertEquals("Mentor02!2026", user.getString(0));
                assertEquals(1, user.getInt(1));
            }
        }
    }
}
