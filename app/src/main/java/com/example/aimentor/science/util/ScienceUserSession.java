package com.example.aimentor.science.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class ScienceUserSession {
    private final SharedPreferences preferences;

    public ScienceUserSession(Context context) {
        preferences = context.getSharedPreferences("USER_INFO", Context.MODE_PRIVATE);
    }

    public int getUserId() {
        return Math.max(0, preferences.getInt("ID_USER", 0));
    }
}
