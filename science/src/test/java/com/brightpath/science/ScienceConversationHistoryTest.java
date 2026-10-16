package com.brightpath.science;

import com.brightpath.science.domain.model.EducationLevel;
import com.brightpath.science.domain.model.ExplanationStyle;
import com.brightpath.science.domain.model.ScienceAnswer;
import com.brightpath.science.domain.model.ScienceHistory;
import com.brightpath.science.domain.model.ScienceQuestion;
import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.domain.repository.ScienceHistoryRepository;
import com.brightpath.science.domain.service.ResultCallback;
import com.brightpath.science.domain.service.ScienceAnswerService;
import com.brightpath.science.domain.usecase.AskScienceQuestionUseCase;
import com.brightpath.science.util.QuestionNormalizer;
import com.brightpath.science.util.ScienceQuestionValidator;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScienceConversationHistoryTest {
    @Test
    public void modelsKeepConversationIdentifier() {
        ScienceQuestion question = question("chat-new");
        ScienceHistory history = new ScienceHistory(
                8L, 4, "chat-new", ScienceSubject.BIOLOGY,
                EducationLevel.HIGH_SCHOOL.name(), ExplanationStyle.DETAILED.name(),
                "Explain photosynthesis", null, "Plants convert light energy.", 10L, 0);

        assertEquals("chat-new", question.getConversationId());
        assertEquals("chat-new", history.getConversationId());
    }

    @Test
    public void cachedAnswerIsSavedIntoCurrentConversation() {
        FakeHistoryRepository repository = new FakeHistoryRepository();
        ScienceAnswerService service = (question, callback) ->
                callback.onError("Remote service should not run for a cache hit", null);
        AskScienceQuestionUseCase useCase = new AskScienceQuestionUseCase(
                service, repository, new ScienceQuestionValidator(), new QuestionNormalizer());
        AtomicReference<ScienceAnswer> result = new AtomicReference<>();

        useCase.execute(question("chat-new"), new ResultCallback<ScienceAnswer>() {
            @Override public void onSuccess(ScienceAnswer answer) { result.set(answer); }
            @Override public void onError(String message, Throwable error) {
                throw new AssertionError(message);
            }
        });

        assertEquals("chat-new", repository.savedConversationId);
        assertEquals(22L, result.get().getHistoryId());
        assertTrue(result.get().isFromCache());
    }

    private ScienceQuestion question(String conversationId) {
        return new ScienceQuestion(
                4,
                ScienceSubject.BIOLOGY,
                EducationLevel.HIGH_SCHOOL,
                ExplanationStyle.DETAILED,
                "Explain the photosynthesis process clearly",
                "",
                conversationId,
                null,
                null,
                null
        );
    }

    private static final class FakeHistoryRepository implements ScienceHistoryRepository {
        private String savedConversationId = "";

        @Override public long save(ScienceQuestion question, String answer) {
            savedConversationId = question.getConversationId();
            return 22L;
        }

        @Override public ScienceHistory findCached(int userId, ScienceSubject subject,
                                                   String educationLevel, String normalizedQuestion) {
            return new ScienceHistory(
                    10L, userId, "chat-old", subject, educationLevel,
                    ExplanationStyle.DETAILED.name(), "Old question", null,
                    "Cached answer", 1L, 0);
        }

        @Override public ScienceHistory findById(int userId, long historyId) { return null; }
        @Override public List<ScienceHistory> findConversation(int userId, String conversationId) {
            return Collections.emptyList();
        }
        @Override public List<ScienceHistory> search(int userId, ScienceSubject subject, String keyword) {
            return Collections.emptyList();
        }
        @Override public void addReviewDuration(long historyId, int seconds) { }
        @Override public int countByUser(int userId) { return 0; }
        @Override public int totalReviewSeconds(int userId) { return 0; }
        @Override public ScienceSubject mostFrequentSubject(int userId) { return ScienceSubject.BIOLOGY; }
    }
}
