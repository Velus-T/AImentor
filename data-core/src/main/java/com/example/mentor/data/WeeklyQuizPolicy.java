package com.example.mentor.data;

import java.util.Calendar;
import java.util.TimeZone;

/** AIMentor local weekly preview: Monday 00:00 to next Monday, end exclusive. */
public final class WeeklyQuizPolicy {
  private WeeklyQuizPolicy() {}

  public static long[] week(long now, TimeZone zone) {
    Calendar c = Calendar.getInstance(zone);
    c.setTimeInMillis(now);
    int elapsed = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    c.add(Calendar.DAY_OF_MONTH, -elapsed);
    long start = c.getTimeInMillis();
    c.add(Calendar.DAY_OF_MONTH, 7);
    return new long[] {start, c.getTimeInMillis()};
  }
}
