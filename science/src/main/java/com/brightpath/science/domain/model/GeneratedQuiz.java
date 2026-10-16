package com.brightpath.science.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Quiz questions plus the adaptive request that produced them. */
public final class GeneratedQuiz {
    private final QuizRequest request;
    private final List<QuizQuestion> questions;

        /*
 * Responsibility: Initializes GeneratedQuiz and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public GeneratedQuiz(QuizRequest request, List<QuizQuestion> questions) {
        this.request = request;
        this.questions = questions == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(questions));
    }

        /*
 * Responsibility: Retrieves the data/state required by `getRequest` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public QuizRequest getRequest() { return request; }
        /*
 * Responsibility: Retrieves the data/state required by `getQuestions` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public List<QuizQuestion> getQuestions() { return questions; }
}
