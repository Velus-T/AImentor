package com.example.mentor.data;

import static org.junit.Assert.*;
import java.util.Collections;
import org.junit.Test;

public class NumericAnswerSafetyTest {
  @Test public void hugeExponentIsAnIncorrectAnswerInsteadOfCrashingSubmission() {
    QuizCard q = new QuizCard("Value?", Collections.emptyList(), Collections.emptyList(), "1.5", "Reason", "EASY", false);
    assertFalse(q.correct("1e999999999999999"));
    assertTrue(q.correct("1,500"));
    assertFalse(q.correct("-1.5"));
  }
}
