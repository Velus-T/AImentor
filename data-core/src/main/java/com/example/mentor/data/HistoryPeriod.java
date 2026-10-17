package com.example.mentor.data;

import java.util.Calendar;
import java.util.TimeZone;

/** Calendar-based local date filters, also valid on daylight-saving transitions. */
public final class HistoryPeriod {
  public static final int ALL = 0, TODAY = 1, SEVEN_DAYS = 2, THIS_MONTH = 3, LAST_MONTH = 4;

  private HistoryPeriod() {}

  public static long[] range(int period, long now, TimeZone zone) {
    if (period == ALL) return new long[] {0, 0};
    Calendar start = Calendar.getInstance(zone);
    start.setTimeInMillis(now);
    start.set(Calendar.HOUR_OF_DAY, 0);
    start.set(Calendar.MINUTE, 0);
    start.set(Calendar.SECOND, 0);
    start.set(Calendar.MILLISECOND, 0);
    Calendar end = (Calendar) start.clone();
    end.add(Calendar.DAY_OF_YEAR, 1);
    if (period == SEVEN_DAYS) start.add(Calendar.DAY_OF_YEAR, -6);
    if (period == THIS_MONTH || period == LAST_MONTH) {
      start.set(Calendar.DAY_OF_MONTH, 1);
      if (period == LAST_MONTH) {
        end = (Calendar) start.clone();
        start.add(Calendar.MONTH, -1);
      }
    }
    return new long[] {start.getTimeInMillis(), end.getTimeInMillis()};
  }

  public static boolean contains(int period, long timestamp, long now, TimeZone zone) {
    if (period == ALL) return true;
    long[] range = range(period, now, zone);
    return timestamp > 0 && timestamp >= range[0] && timestamp < range[1];
  }
}
