package com.brightpath.science.domain.model;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Reject unusable AI questions before they reach either the player or offline bank. */
public final class ScienceQuizValidator {
  private ScienceQuizValidator() {}

  public static void validate(List<QuizQuestion> questions, int count) {
    if (questions == null || questions.size() != count)
      throw new IllegalArgumentException("AI returned an unexpected question count.");
    Set<String> prompts = new HashSet<>();
    for (QuizQuestion q : questions) {
      if (q == null || q.getPrompt().trim().isEmpty() || q.getCorrectAnswer().trim().isEmpty()
          || q.getExplanation().trim().isEmpty())
        throw new IllegalArgumentException("AI returned an incomplete question.");
      if (!prompts.add(q.getPrompt().trim().toLowerCase(Locale.ROOT)))
        throw new IllegalArgumentException("AI returned duplicate questions.");
      Set<String> options = new HashSet<>();
      for (String option : q.getOptions()) {
        if (option != null && q.getType() == QuizQuestion.Type.MULTIPLE_CHOICE)
          option = option.replaceFirst("(?i)^[A-D][.)]\\s*", "");
        if (option == null || option.trim().isEmpty()
            || !options.add(option.trim().toLowerCase(Locale.ROOT)))
          throw new IllegalArgumentException("AI returned invalid options.");
      }
      String answer = q.getCorrectAnswer().trim().toUpperCase(Locale.ROOT);
      switch (q.getType()) {
        case MULTIPLE_CHOICE:
          if (options.size() != 4 || !answer.matches("[A-D]"))
            throw new IllegalArgumentException("AI returned an invalid choice answer.");
          break;
        case TRUE_FALSE:
          if (q.getOptions().size() != 2 || !answer.matches("TRUE|FALSE"))
            throw new IllegalArgumentException("AI returned an invalid true/false question.");
          break;
        case MATCHING:
          if (options.size() < 4 || !answer.matches("(?:[A-Z0-9]+\\s*[-:=]\\s*[A-Z0-9]+\\s*[,;]?\\s*)+"))
            throw new IllegalArgumentException("AI returned invalid matching pairs.");
          java.util.regex.Matcher pairs = java.util.regex.Pattern.compile("([A-Z0-9]+)\\s*[-:=]\\s*([A-Z0-9]+)").matcher(answer);
          Set<String> left = new HashSet<>(), right = new HashSet<>(), labels = new HashSet<>();
          for (String option : q.getOptions()) labels.add(option.trim().split("[.)\\s]", 2)[0].toUpperCase(Locale.ROOT));
          while (pairs.find()) {
            if (!left.add(pairs.group(1)) || !right.add(pairs.group(2))
                || !labels.contains(pairs.group(1)) || !labels.contains(pairs.group(2)))
              throw new IllegalArgumentException("AI returned ambiguous matching pairs.");
          }
          if (left.size() * 2 != options.size())
            throw new IllegalArgumentException("AI returned incomplete matching pairs.");
          break;
        case FILL_BLANK:
          break;
      }
    }
  }
}
