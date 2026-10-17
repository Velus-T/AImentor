package com.brightpath.science.util;

import com.brightpath.science.domain.model.QuizQuestion;
import com.example.mentor.data.QuizCard;
import java.util.*;

/** Adapts the subject model without changing its generator or database format. */
public final class QuizCards {
  private QuizCards() {}

  public static ArrayList<QuizCard> from(List<QuizQuestion> questions) {
    ArrayList<QuizCard> result = new ArrayList<>();
    for (QuizQuestion q : questions) {
      List<String> options =
          q.getType() == QuizQuestion.Type.MULTIPLE_CHOICE
                  || q.getType() == QuizQuestion.Type.TRUE_FALSE
              ? q.getOptions()
              : java.util.Collections.emptyList();
      result.add(
          new QuizCard(
              q.getPrompt()
                  + (q.getType() == QuizQuestion.Type.MATCHING
                      ? "\n" + String.join("\n", q.getOptions())
                      : ""),
              options,
              q.getType() == QuizQuestion.Type.TRUE_FALSE
                  ? java.util.Arrays.asList("TRUE", "FALSE")
                  : com.example.mentor.data.QuizCard.letters(options.size()),
              options.isEmpty() ? q.getCorrectAnswer() : q.getCorrectAnswer().trim().toUpperCase(Locale.ROOT),
              q.getExplanation(),
              q.getDifficulty().name(),
              false,
              q.getType().name()));
    }
    return result;
  }
}
