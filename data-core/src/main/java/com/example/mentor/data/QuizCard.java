package com.example.mentor.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Presentation-independent question; adapters retain each subject's existing source and storage.
 */
public final class QuizCard implements Serializable {
  private static final long serialVersionUID = 1L;
  public final String prompt, answer, explanation, difficulty;
  public final ArrayList<String> options;
  public final ArrayList<String> optionValues;
  public final boolean math;
  private final String answerType;

  public QuizCard(
      String prompt,
      List<String> options,
      List<String> values,
      String answer,
      String explanation,
      String difficulty,
      boolean math) {
    this(
        prompt,
        options,
        values,
        answer,
        explanation,
        difficulty,
        math,
        options.isEmpty() ? "TEXT" : "CHOICE");
  }

  public QuizCard(
      String prompt,
      List<String> options,
      List<String> values,
      String answer,
      String explanation,
      String difficulty,
      boolean math,
      String answerType) {
    this.answerType = answerType;
    this.prompt = prompt == null ? "" : prompt;
    this.options = new ArrayList<>(options);
    this.optionValues = new ArrayList<>(values);
    if (this.options.size() != this.optionValues.size())
      throw new IllegalArgumentException("Option values");
    this.answer = answer == null ? "" : answer;
    this.explanation = explanation == null ? "" : explanation;
    this.difficulty = difficulty == null ? "MEDIUM" : difficulty;
    this.math = math;
  }

  public String answerType() {
    return answerType == null ? (options.isEmpty() ? "TEXT" : "CHOICE") : answerType;
  }

  public boolean correct(String actual) {
    if (actual == null || actual.trim().isEmpty()) return false;
    if ("MATCHING".equals(answerType())) {
      java.util.Map<String, String> expected = pairs(answer), given = pairs(actual);
      if (expected != null) return expected.equals(given);
    }
    String expectedText = normalize(answer), actualText = normalize(actual);
    String number = "[+-]?(?:[0-9]+(?:[.,][0-9]+)?|[.,][0-9]+)(?:[eE][+-]?[0-9]+)?";
    if (options.isEmpty() && expectedText.matches(number) && actualText.matches(number)) {
      try {
        return new java.math.BigDecimal(expectedText.replace(',', '.'))
                .compareTo(new java.math.BigDecimal(actualText.replace(',', '.')))
            == 0;
      } catch (NumberFormatException invalidNumber) {
        return false;
      }
    }
    return expectedText.equals(actualText);
  }

  private static java.util.Map<String, String> pairs(String value) {
    String token = "([A-Za-z0-9]+)\\s*[-:=]\\s*([A-Za-z0-9]+)";
    if (!value.trim().matches("(?i)(?:" + token + "\\s*[,;]?\\s*)+")) return null;
    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(token).matcher(value);
    java.util.Map<String, String> result = new java.util.TreeMap<>();
    while (matcher.find())
      if (result.put(
              matcher.group(1).toUpperCase(Locale.ROOT), matcher.group(2).toUpperCase(Locale.ROOT))
          != null) return null;
    return result;
  }

  private static String normalize(String value) {
    // Keep signs and punctuation: -2 must never equal 2, and 1.5 must never equal 15.
    String raw = value.trim().toLowerCase(Locale.ROOT);
    String subs = "₀₁₂₃₄₅₆₇₈₉", supers = "⁰¹²³⁴⁵⁶⁷⁸⁹";
    for (int i = 0; i < 10; i++)
      raw =
          raw.replace(subs.charAt(i), (char) ('0' + i)).replace(supers.charAt(i), (char) ('0' + i));
    return raw.replaceAll("\\s+", " ");
  }

  public static ArrayList<String> letters(int count) {
    ArrayList<String> result = new ArrayList<>();
    for (int i = 0; i < count; i++) result.add(String.valueOf((char) ('A' + i)));
    return result;
  }
}
