package com.example.mentor.data;
import java.util.*;
import org.junit.Test;

public class QuizContentValidatorTest {
  @Test(expected=IllegalArgumentException.class) public void optionPrefixesDoNotHideDuplicateAnswers() {
    QuizContentValidator.generated(Collections.singletonList(new QuizCard("Question",
        Arrays.asList("A. Same","B. Same","C. Three","D. Four"), QuizCard.letters(4),"A","Reason","EASY",false)));
  }
  @Test(expected=IllegalArgumentException.class) public void missingExplanationCannotBePublishedAsGeneratedQuiz() {
    QuizContentValidator.generated(Collections.singletonList(new QuizCard("Question",
        Arrays.asList("One","Two","Three","Four"), QuizCard.letters(4),"A","","EASY",false)));
  }
}
