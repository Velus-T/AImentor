package com.example.asm_final.dashboard;

/** Personal device progress only; the server owns competitive ranks and league membership. */
public interface WeeklyQuizRepository {
  WeeklyQuizProgress load(int userId);
}
