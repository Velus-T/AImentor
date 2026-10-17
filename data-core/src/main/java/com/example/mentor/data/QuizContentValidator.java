package com.example.mentor.data;

import java.util.*;

/** Structural validation only; factual correctness still needs editorial/AI review. */
public final class QuizContentValidator {
  private QuizContentValidator() {}
  public static void generated(List<QuizCard> cards) {
    if (cards == null || cards.isEmpty()) throw new IllegalArgumentException("Empty quiz");
    Set<String> prompts = new HashSet<>();
    for (QuizCard q : cards) {
      if (q == null || q.prompt.trim().isEmpty() || q.answer.trim().isEmpty()
          || q.explanation.trim().isEmpty()) throw new IllegalArgumentException("Incomplete question");
      if (!prompts.add(q.prompt.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT)))
        throw new IllegalArgumentException("Repeated question");
      if (!q.options.isEmpty()) {
        if (!q.optionValues.contains(q.answer)) throw new IllegalArgumentException("Invalid answer");
        Set<String> options = new HashSet<>();
        for (String raw : q.options) {
          String option = raw == null ? "" : raw.replaceFirst("(?i)^[A-D][.)]\\s*", "").trim().toLowerCase(Locale.ROOT);
          if (option.isEmpty() || !options.add(option)) throw new IllegalArgumentException("Invalid options");
        }
      }
    }
  }
}
