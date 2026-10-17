package com.example.mentor.data;

import static org.junit.Assert.*;

import java.util.Collections;
import org.junit.Test;

public class QuizPolicyTest {
  @Test
  public void everyLevelBoundaryAndProgressUsesOneCurve() {
    assertEquals(1, QuizPolicy.level(-1));
    assertEquals(0, QuizPolicy.progress(-1));
    for (int level = 2; level <= QuizPolicy.MAX_LEVEL; level++) {
      int threshold = QuizPolicy.threshold(level);
      assertEquals(level - 1, QuizPolicy.level(threshold - 1));
      assertEquals(level, QuizPolicy.level(threshold));
      assertEquals(QuizPolicy.required(level - 1), threshold - QuizPolicy.threshold(level - 1));
    }
    assertEquals(100, QuizPolicy.progress(Integer.MAX_VALUE));
    assertEquals(0, QuizPolicy.remaining(Integer.MAX_VALUE));
  }

  @Test
  public void rewardDoesNotDependOnSubject() {
    assertArrayEquals(new int[] {5, 10, 15}, QuizPolicy.questionCounts());
    assertEquals(5, QuizPolicy.reward("EASY"));
    assertEquals(10, QuizPolicy.reward("MEDIUM"));
    assertEquals(20, QuizPolicy.reward("HARD"));
    assertEquals(10, QuizPolicy.reward(null));
  }

  private QuizCard text(String answer) {
    return new QuizCard(
        "Question",
        Collections.emptyList(),
        Collections.emptyList(),
        answer,
        "Explanation",
        "MEDIUM",
        false);
  }

  @Test
  public void signedAndDecimalAnswersAreNotSilentlyChanged() {
    assertFalse(text("-2").correct("2"));
    assertFalse(text("1.5").correct("15"));
    assertTrue(text("1.5").correct("1,50"));
    assertTrue(text("-2").correct("-2.0"));
    assertFalse(text("A+B").correct("AB"));
    assertTrue(text("H₂O").correct("h2o"));
    assertTrue(text("hello world").correct("  Hello   world "));
    assertFalse(text("").correct(""));
  }

  @Test
  public void matchingPairsAllowReorderingButRejectMissingOrRepeatedKeys() {
    QuizCard question =
        new QuizCard(
            "Match the pairs",
            Collections.emptyList(),
            Collections.emptyList(),
            "A-2, B-1",
            "Explanation",
            "HARD",
            false,
            "MATCHING");
    assertTrue(question.correct("b:1; a:2"));
    assertTrue(question.correct("A-2,B-1"));
    assertFalse(question.correct("A-2"));
    assertFalse(question.correct("A-2, A-1"));
    assertFalse(question.correct("A-1, B-2"));
  }
}
