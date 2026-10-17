package com.example.asm_final.dashboard;

import com.example.mentor.data.StreakSnapshot;

public interface LearningStreakRepository {
  StreakSnapshot load(int userId);
}
