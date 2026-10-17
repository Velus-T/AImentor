package com.example.mentor.data;

import static org.junit.Assert.*;

import java.util.*;
import org.junit.Test;

public class WeeklyQuizPolicyTest {
  private long time(TimeZone z, int year, int month, int day, int hour) {
    Calendar c = Calendar.getInstance(z);
    c.clear();
    c.set(year, month - 1, day, hour, 0);
    return c.getTimeInMillis();
  }

  @Test
  public void mondayBeginsWeekAndSundayStaysInIt() {
    TimeZone z = TimeZone.getTimeZone("Asia/Bangkok");
    long[] week = WeeklyQuizPolicy.week(time(z, 2026, 10, 18, 23), z);
    assertEquals(time(z, 2026, 10, 12, 0), week[0]);
    assertEquals(time(z, 2026, 10, 19, 0), week[1]);
    assertEquals(week[1], WeeklyQuizPolicy.week(week[1], z)[0]);
  }

  @Test
  public void yearBoundaryKeepsSevenCalendarDays() {
    TimeZone z = TimeZone.getTimeZone("Asia/Bangkok");
    long[] week = WeeklyQuizPolicy.week(time(z, 2027, 1, 1, 12), z);
    assertEquals(time(z, 2026, 12, 28, 0), week[0]);
    assertEquals(time(z, 2027, 1, 4, 0), week[1]);
  }

  @Test
  public void daylightSavingUsesCalendarWeek() {
    TimeZone z = TimeZone.getTimeZone("America/New_York");
    long[] week = WeeklyQuizPolicy.week(time(z, 2026, 3, 8, 12), z);
    assertEquals(167 * 3600000L, week[1] - week[0]);
  }
}
