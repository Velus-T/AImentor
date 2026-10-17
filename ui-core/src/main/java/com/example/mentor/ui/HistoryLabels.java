package com.example.mentor.ui;

import android.content.Context;
import com.example.mentor.data.Timestamps;
import java.util.*;

/** Shared chat-style grouping; actual saved questions remain separate records. */
public final class HistoryLabels {
  private HistoryLabels() {}

  public static String group(Context context, String raw) {
    long time = Timestamps.parse(raw);
    if (time <= 0) return context.getString(R.string.mentor_time_unknown);
    Calendar today = Calendar.getInstance();
    today.set(Calendar.HOUR_OF_DAY, 0);
    today.set(Calendar.MINUTE, 0);
    today.set(Calendar.SECOND, 0);
    today.set(Calendar.MILLISECOND, 0);
    Calendar tomorrow = (Calendar) today.clone();
    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
    if (time >= today.getTimeInMillis() && time < tomorrow.getTimeInMillis())
      return context.getString(R.string.mentor_history_today);
    today.add(Calendar.DAY_OF_YEAR, -1);
    if (time >= today.getTimeInMillis() && time < tomorrow.getTimeInMillis())
      return context.getString(R.string.mentor_history_yesterday);
    today.add(Calendar.DAY_OF_YEAR, -6);
    if (time >= today.getTimeInMillis() && time < tomorrow.getTimeInMillis())
      return context.getString(R.string.mentor_history_previous_seven);
    today.add(Calendar.DAY_OF_YEAR, -23);
    if (time >= today.getTimeInMillis() && time < tomorrow.getTimeInMillis())
      return context.getString(R.string.mentor_history_previous_thirty);
    Locale locale = context.getResources().getConfiguration().getLocales().get(0);
    return new java.text.SimpleDateFormat(
            android.text.format.DateFormat.getBestDateTimePattern(locale, "MMMM yyyy"), locale)
        .format(new Date(time));
  }

  public static String[] periods(Context c) {
    return new String[] {
      c.getString(R.string.mentor_history_all),
      c.getString(R.string.mentor_history_today),
      c.getString(R.string.mentor_history_seven),
      c.getString(R.string.mentor_history_month),
      c.getString(R.string.mentor_history_last_month)
    };
  }
}
