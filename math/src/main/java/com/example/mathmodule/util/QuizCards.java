package com.example.mathmodule.util;
import com.example.mathmodule.databases.QuizQuestion;
import com.example.mentor.data.QuizCard;
import java.util.*;
/** Adapts the subject model without changing its generator or database format. */
public final class QuizCards {
 private QuizCards() {}
 public static ArrayList<QuizCard> from(List<QuizQuestion> questions) {
  ArrayList<QuizCard> result=new ArrayList<>();
  for(QuizQuestion q:questions) {
   List<String> options=java.util.Arrays.asList(q.getOptionA(),q.getOptionB(),q.getOptionC(),q.getOptionD());
   result.add(new QuizCard(q.getQuestionText(),options,com.example.mentor.data.QuizCard.letters(4),q.getCorrectAnswer(),q.getExplanation(),q.getDifficulty().name(),true));
  }
  return result;
 }
}
