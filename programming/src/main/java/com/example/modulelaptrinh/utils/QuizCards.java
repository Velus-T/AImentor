package com.example.modulelaptrinh.utils;
import com.example.modulelaptrinh.models.QuizQuestion;
import com.example.mentor.data.QuizCard;
import java.util.*;
/** Adapts the subject model without changing its generator or database format. */
public final class QuizCards {
 private QuizCards() {}
 public static ArrayList<QuizCard> from(List<QuizQuestion> questions) {
  ArrayList<QuizCard> result=new ArrayList<>();
  for(QuizQuestion q:questions) {
   List<String> options=q.getType()==QuizQuestion.Type.FILL_BLANK ? java.util.Collections.emptyList() : q.getOptions();
   result.add(new QuizCard(q.getPrompt(),options,q.getType()==QuizQuestion.Type.TRUE_FALSE ? java.util.Arrays.asList("TRUE","FALSE") : com.example.mentor.data.QuizCard.letters(options.size()),q.getCorrectAnswer(),q.getExplanation(),q.getDifficulty().name(),false));
  }
  return result;
 }
}
