package com.example.asm_final.utils;
import android.content.Context;
import com.example.asm_final.activities.database.DatabaseHelper;

/** Debug-only accounts use the canonical app's existing bcrypt account API. */
public final class DebugAccounts {
    private DebugAccounts() { }
    public static void initialize(Context context) {
        Thread worker = new Thread(() -> seed(context.getApplicationContext()), "debug-account-seed");
        worker.start();
    }
    public static synchronized void seed(Context context) {
        try (DatabaseHelper db = new DatabaseHelper(context)) {
            for (int i = 1; i <= 3; i++) {
                String username = "hocvien0" + i;
                if (db.getUserId(username) <= 0) {
                    db.addUser(username, "Mentor0" + i + "!2026", username + "@gmail.com", "090000000" + i);
                }
            }
        }
    }
}
