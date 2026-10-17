package com.example.mentor.data;

import java.util.*;

/** Immutable daily learning summary; future days never affect the displayed streak. */
public final class StreakSnapshot {
  public final int current, best;
  public final boolean completedToday;
  public final long today;
  public final Set<Long> days;

  StreakSnapshot(int current, int best, long today, Set<Long> days) {
    this.current = current;
    this.best = best;
    this.today = today;
    this.days = Collections.unmodifiableSet(new HashSet<>(days));
    completedToday = days.contains(today);
  }
}
