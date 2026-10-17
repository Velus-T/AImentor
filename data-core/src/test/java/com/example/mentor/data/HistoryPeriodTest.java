package com.example.mentor.data;

import static org.junit.Assert.*;

import java.util.*;
import org.junit.Test;

public class HistoryPeriodTest {
  private long local(TimeZone zone, int year, int month, int day, int hour) {
    Calendar c = Calendar.getInstance(zone);
    c.clear();
    c.set(year, month - 1, day, hour, 0);
    return c.getTimeInMillis();
  }

  @Test
  public void todayAndSevenDaysRespectLocalMidnight() {
    TimeZone zone = TimeZone.getTimeZone("Asia/Bangkok");
    long now = local(zone, 2026, 10, 17, 12);
    assertTrue(
        HistoryPeriod.contains(HistoryPeriod.TODAY, local(zone, 2026, 10, 17, 0), now, zone));
    assertFalse(
        HistoryPeriod.contains(HistoryPeriod.TODAY, local(zone, 2026, 10, 16, 23), now, zone));
    assertFalse(
        HistoryPeriod.contains(HistoryPeriod.TODAY, local(zone, 2026, 10, 18, 0), now, zone));
    assertTrue(
        HistoryPeriod.contains(HistoryPeriod.SEVEN_DAYS, local(zone, 2026, 10, 11, 0), now, zone));
    assertFalse(
        HistoryPeriod.contains(HistoryPeriod.SEVEN_DAYS, local(zone, 2026, 10, 10, 23), now, zone));
  }

  @Test
  public void previousMonthWorksAcrossYearBoundary() {
    TimeZone zone = TimeZone.getTimeZone("Asia/Bangkok");
    long now = local(zone, 2026, 1, 8, 12);
    assertArrayEquals(
        new long[] {local(zone, 2025, 12, 1, 0), local(zone, 2026, 1, 1, 0)},
        HistoryPeriod.range(HistoryPeriod.LAST_MONTH, now, zone));
    assertTrue(HistoryPeriod.contains(HistoryPeriod.ALL, 0, now, zone));
    assertFalse(HistoryPeriod.contains(HistoryPeriod.TODAY, 0, now, zone));
  }

  @Test
  public void daylightSavingDayIsNotForcedToTwentyFourHours() {
    TimeZone zone = TimeZone.getTimeZone("America/New_York");
    long[] range = HistoryPeriod.range(HistoryPeriod.TODAY, local(zone, 2026, 3, 8, 12), zone);
    assertEquals(23 * 3600000L, range[1] - range[0]);
  }

  @Test
  public void legacyHumanDatesAndEpochDatesRemainReadable() {
    TimeZone previous = TimeZone.getDefault();
    TimeZone zone = TimeZone.getTimeZone("Asia/Bangkok");
    try {
      TimeZone.setDefault(zone);
      long expected = local(zone, 2026, 10, 17, 12);
      assertEquals(expected, Timestamps.parse("17/10/2026 12:00"));
      assertEquals(expected, Timestamps.parse(String.valueOf(expected)));
      assertEquals(expected, Timestamps.parse(String.valueOf(expected / 1000)));
      assertEquals(0, Timestamps.parse("31/02/2026"));
    } finally {
      TimeZone.setDefault(previous);
    }
  }
}
