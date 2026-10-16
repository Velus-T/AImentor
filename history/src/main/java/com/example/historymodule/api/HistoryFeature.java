package com.example.historymodule.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.historymodule.HistoryDashboardActivity;
import com.example.historymodule.util.HistoryUserSession;

/** Public entry point used by the host app to open the History module. */
/*
 * Role: AI/API class for History, responsible for preparing requests or prompts, calling the service, and converting response data.
 * Purpose: Separating AI communication from the UI makes error handling, model or endpoint changes, and logic testing easier.
 */
public final class HistoryFeature {
    private HistoryFeature() {
    }

    public static void open(@NonNull Context context) {
        open(context, 0);
    }

    /*
     * Responsibility: Displays or binds data from `open` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    public static void open(@NonNull Context context, int userId) {
        HistoryUserSession.setUserId(context, Math.max(0, userId));
        Intent intent = new Intent(context, HistoryDashboardActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
