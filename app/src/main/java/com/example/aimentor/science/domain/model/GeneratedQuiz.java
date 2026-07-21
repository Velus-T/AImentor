package com.example.aimentor.science.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Quiz questions plus the adaptive request that produced them. */
public final class GeneratedQuiz {
    private final QuizRequest request;
    private final List<QuizQuestion> questions;

    public GeneratedQuiz(QuizRequest request, List<QuizQuestion> questions) {
        this.request = request;
        this.questions = questions == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(questions));
    }

    public QuizRequest getRequest() { return request; }
    public List<QuizQuestion> getQuestions() { return questions; }
}
