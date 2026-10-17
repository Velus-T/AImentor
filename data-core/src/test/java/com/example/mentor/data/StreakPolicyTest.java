package com.example.mentor.data;

import static org.junit.Assert.*;

import java.util.*;
import org.junit.Test;

public class StreakPolicyTest {
  private final TimeZone zone = TimeZone.getTimeZone("Asia/Bangkok");

  private long time(TimeZone z, int year, int month, int day, int hour) {
    Calendar c = Calendar.getInstance(z);
    c.clear();
    c.set(year, month - 1, day, hour, 0);
    return c.getTimeInMillis();
  }

  private final long now = time(zone, 2026, 10, 17, 12);
  private final long today = StreakPolicy.dayId(now, zone);

  private StreakSnapshot calc(Long... days) {
    return StreakPolicy.calculate(Arrays.asList(days), now, zone);
  }

  @Test
  public void emptyHasNoStreak() {
    assertEquals(0, calc().current);
    assertEquals(0, calc().best);
    assertFalse(calc().completedToday);
  }

  @Test
  public void todayCountsOnlyOnce() {
    StreakSnapshot s = calc(today, today, today);
    assertEquals(1, s.current);
    assertEquals(1, s.best);
    assertTrue(s.completedToday);
  }

  @Test
  public void yesterdayRemainsOpenForToday() {
    StreakSnapshot s = calc(today - 2, today - 1);
    assertEquals(2, s.current);
    assertFalse(s.completedToday);
  }

  @Test
  public void missedDayResetsCurrentButKeepsBest() {
    StreakSnapshot s = calc(today - 5, today - 4, today - 3);
    assertEquals(0, s.current);
    assertEquals(3, s.best);
  }

  @Test
  public void restartKeepsLongestRun() {
    StreakSnapshot s = calc(today - 6, today - 5, today - 4, today);
    assertEquals(1, s.current);
    assertEquals(3, s.best);
  }

  @Test
  public void futureAndInvalidDaysCannotGiveStreak() {
    StreakSnapshot s = calc(today + 1, -1L, null);
    assertEquals(0, s.best);
  }

  @Test
  public void localMidnightStartsANewDay() {
    assertEquals(
        1,
        StreakPolicy.dayId(time(zone, 2026, 10, 18, 0), zone)
            - StreakPolicy.dayId(time(zone, 2026, 10, 17, 23), zone));
  }

  @Test
  public void leapDayAndYearBoundaryAreConsecutive() {
    assertEquals(
        1,
        StreakPolicy.dayId(time(zone, 2024, 3, 1, 0), zone)
            - StreakPolicy.dayId(time(zone, 2024, 2, 29, 23), zone));
    assertEquals(
        1,
        StreakPolicy.dayId(time(zone, 2027, 1, 1, 0), zone)
            - StreakPolicy.dayId(time(zone, 2026, 12, 31, 23), zone));
  }

  @Test
  public void daylightSavingDoesNotBreakStreak() {
    TimeZone z = TimeZone.getTimeZone("America/New_York");
    long a = time(z, 2026, 3, 7, 12), b = time(z, 2026, 3, 8, 12);
    assertEquals(23 * 3600000L, b - a);
    assertEquals(
        2,
        StreakPolicy.calculate(
                Arrays.asList(StreakPolicy.dayId(a, z), StreakPolicy.dayId(b, z)), b, z)
            .current);
  }

  @Test
  public void timezoneUsesLocalCalendarDate() {
    assertEquals(
        1,
        StreakPolicy.dayId(time(zone, 2026, 10, 17, 0), zone)
            - StreakPolicy.dayId(time(zone, 2026, 10, 17, 0), TimeZone.getTimeZone("UTC")));
  }
}
