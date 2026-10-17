package com.example.historymodule.util;
import com.example.historymodule.model.HistoryQuizQuestion;
import com.example.mentor.data.QuizCard;
import java.util.*;
/** Adapts the subject model without changing its generator or database format. */
public final class QuizCards {
 private QuizCards() {}
 public static ArrayList<QuizCard> from(List<HistoryQuizQuestion> questions) {
  ArrayList<QuizCard> result=new ArrayList<>();
  for(HistoryQuizQuestion q:questions) {
   List<String> options=q.getOptions();
   result.add(new QuizCard(q.getQuestion(),options,com.example.mentor.data.QuizCard.letters(q.getOptions().size()),String.valueOf((char)('A'+q.getCorrectIndex())),q.getExplanation(),q.getDifficulty(),false));
  }
  return result;
 }
}
