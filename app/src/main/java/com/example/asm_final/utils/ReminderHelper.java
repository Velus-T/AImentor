package com.example.asm_final.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

/*
 * Role: Utility class for App Shell, grouping reusable logic such as user sessions, levels, formatting, or data conversion.
 * Purpose: Moving shared logic into helpers reduces duplication and keeps Activities and Fragments focused on screen coordination.
 */
public final class ReminderHelper {
    private static final String PREFS_NAME = "settings";
    private static final String KEY_REMINDERS_ENABLED = "reminders_enabled";
    private static final int REQUEST_CODE = 1001;

    /*
     * Responsibility: Initializes the `ReminderHelper` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    private ReminderHelper() {}

    public static boolean isReminderEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_REMINDERS_ENABLED, false)
                && androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /*
     * Responsibility: Applies or updates `set reminder enabled` in the current state or UI.
     * Purpose: A dedicated method ensures the same update behavior is used consistently by every caller.
     */
    public static void setReminderEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_REMINDERS_ENABLED, enabled)
                .apply();

        if (enabled) {
            scheduleDailyReminder(context);
        } else {
            cancelReminder(context);
        }
    }

    /*
     * Responsibility: Performs the `schedule daily reminder` responsibility of `ReminderHelper` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    public static void scheduleDailyReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 20); // 8:00 PM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If time is in the past, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent);
    }

    /*
     * Responsibility: Validates the conditions or rules handled by `cancel reminder` and returns the result to the caller.
     * Purpose: A dedicated validation method keeps rules consistent and easier to test.
     */
    private static void cancelReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);
    }
}
