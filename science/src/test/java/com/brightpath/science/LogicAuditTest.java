package com.brightpath.science;

import static org.junit.Assert.*;
import com.brightpath.science.domain.model.*;
import com.brightpath.science.domain.repository.*;
import com.brightpath.science.domain.service.*;
import com.brightpath.science.domain.usecase.*;
import com.brightpath.science.util.ScienceNoteSubject;
import java.util.*;
import org.junit.Test;

public class LogicAuditTest {
  private QuizQuestion choice(String prompt, String answer, String explanation) {
    return new QuizQuestion(QuizQuestion.Type.MULTIPLE_CHOICE, prompt,
        Arrays.asList("One", "Two", "Three", "Four"), answer, explanation);
  }
  @Test public void validMixedQuestionTypesAreAccepted() {
    ScienceQuizValidator.validate(Arrays.asList(choice("Choice?", "B", "Reason"),
        new QuizQuestion(QuizQuestion.Type.TRUE_FALSE, "True?", Arrays.asList("True", "False"), "False", "Reason"),
        new QuizQuestion(QuizQuestion.Type.FILL_BLANK, "Value?", Collections.emptyList(), "-1.5", "Reason"),
        new QuizQuestion(QuizQuestion.Type.MATCHING, "Match?", Arrays.asList("A. X", "B. Y", "1. Y", "2. X"), "A-2, B-1", "Reason")), 4);
  }
  @Test(expected=IllegalArgumentException.class) public void incompleteChoiceCannotEnterBank() {
    ScienceQuizValidator.validate(Collections.singletonList(new QuizQuestion(QuizQuestion.Type.MULTIPLE_CHOICE,
        "Question", Collections.emptyList(), "A", "Reason")), 1);
  }
  @Test(expected=IllegalArgumentException.class) public void answerOutsideOptionsIsRejected() {
    ScienceQuizValidator.validate(Collections.singletonList(choice("Question", "E", "Reason")), 1);
  }
  @Test(expected=IllegalArgumentException.class) public void repeatedPromptsAreRejected() {
    ScienceQuizValidator.validate(Arrays.asList(choice("Question", "A", "Reason"), choice(" question ", "B", "Reason")), 2);
  }
  @Test(expected=IllegalArgumentException.class) public void missingExplanationIsRejected() {
    ScienceQuizValidator.validate(Collections.singletonList(choice("Question", "A", "")), 1);
  }
  @Test public void legacyNoteSubjectsKeepClassificationAcrossLanguages() {
    assertEquals("chemistry", ScienceNoteSubject.normalize("Hóa học"));
    assertEquals("chemistry", ScienceNoteSubject.normalize("Chemistry"));
    assertEquals(2, ScienceNoteSubject.index("Sinh học"));
    assertEquals(0, ScienceNoteSubject.index("Vật lý"));
  }
  @Test(expected=IllegalArgumentException.class) public void duplicateMatchingAssignmentsAreRejected() {
    ScienceQuizValidator.validate(Collections.singletonList(new QuizQuestion(QuizQuestion.Type.MATCHING,
        "Match?",Arrays.asList("A. X","B. Y","1. X","2. Y"),"A-1, A-2","Reason")),1);
  }
  private static final class Bookmarks implements ScienceBookmarkRepository {
    boolean exists; long insertResult = -1;
    public long save(ScienceBookmark b) { return insertResult; }
    public boolean existsForHistory(int u,long h) { return exists; }
    public void deleteForHistory(int u,long h) {}
    public List<ScienceBookmark> search(int u,ScienceSubject s,String k) { return Collections.emptyList(); }
    public int countByUser(int u) { return 0; }
  }
  @Test(expected=IllegalStateException.class) public void failedBookmarkInsertDoesNotReportSuccess() {
    new SaveBookmarkUseCase(new Bookmarks()).toggle(1, 7, ScienceSubject.PHYSICS, "Title", "Content");
  }
  @Test(expected=IllegalStateException.class) public void silentBookmarkDeleteDoesNotReportRemoved() {
    Bookmarks repo = new Bookmarks(); repo.exists = true;
    new SaveBookmarkUseCase(repo).toggle(1, 7, ScienceSubject.PHYSICS, "Title", "Content");
  }
  @Test public void studyPlanPersistenceFailureReachesErrorCallback() {
    StudyPlan plan = new StudyPlan(1, ScienceSubject.PHYSICS, EducationLevel.MIDDLE_SCHOOL, "Goal", false,
        Collections.singletonList(new StudySession(1, 1, 30, "Title", "Read", false)));
    boolean[] success = {false}, error = {false};
    StudyPlanRepository repo = new StudyPlanRepository() {
      public StudyPlan getLatest(int u) { return null; }
      public void save(int u,StudyPlan p) { throw new IllegalStateException("disk"); }
      public void clear(int u) {}
    };
    new GenerateStudyPlanUseCase((request, callback) -> callback.onSuccess(plan), repo).execute(
        new StudyPlanRequest(1, ScienceSubject.PHYSICS, EducationLevel.MIDDLE_SCHOOL, "Goal", 7, 30, 1),
        new ResultCallback<StudyPlan>() {
          public void onSuccess(StudyPlan p) { success[0] = true; }
          public void onError(String m,Throwable t) { error[0] = true; }
        });
    assertFalse(success[0]); assertTrue(error[0]);
  }
}
