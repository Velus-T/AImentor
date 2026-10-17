package com.brightpath.science.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class QuizQuestion {
    public enum Type { MULTIPLE_CHOICE, TRUE_FALSE, FILL_BLANK, MATCHING }

    private boolean local;
    public boolean isLocal() { return local; }
    public QuizQuestion asLocal() {
        QuizQuestion copy = new QuizQuestion(type,prompt,options,correctAnswer,explanation,difficulty);
        copy.local = true;
        return copy;
    }
    private final Type type;
    private final String prompt;
    private final List<String> options;
    private final String correctAnswer;
    private final String explanation;
    private final QuizDifficulty difficulty;

    /** Backward-compatible constructor for local questions created before EXP support. */
        /*
 * Responsibility: Initializes QuizQuestion and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public QuizQuestion(Type type, String prompt, List<String> options,
                        String correctAnswer, String explanation) {
        this(type, prompt, options, correctAnswer, explanation,
                QuizDifficulty.inferFromType(type));
    }

        /*
 * Responsibility: Initializes QuizQuestion and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public QuizQuestion(Type type, String prompt, List<String> options,
                        String correctAnswer, String explanation,
                        QuizDifficulty difficulty) {
        this.type = type == null ? Type.MULTIPLE_CHOICE : type;
        this.prompt = prompt == null ? "" : prompt;
        this.options = options == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(options));
        this.correctAnswer = correctAnswer == null ? "" : correctAnswer;
        this.explanation = explanation == null ? "" : explanation;
        this.difficulty = difficulty == null
                ? QuizDifficulty.inferFromType(this.type)
                : difficulty;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getType` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public Type getType() { return type; }
        /*
 * Responsibility: Retrieves the data/state required by `getPrompt` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getPrompt() { return prompt; }
        /*
 * Responsibility: Retrieves the data/state required by `getOptions` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public List<String> getOptions() { return options; }
        /*
 * Responsibility: Retrieves the data/state required by `getCorrectAnswer` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getCorrectAnswer() { return correctAnswer; }
        /*
 * Responsibility: Retrieves the data/state required by `getExplanation` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getExplanation() { return explanation; }
        /*
 * Responsibility: Retrieves the data/state required by `getDifficulty` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public QuizDifficulty getDifficulty() { return difficulty; }
        /*
 * Responsibility: Retrieves the data/state required by `getExpReward` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getExpReward() { return difficulty.getExpReward(); }
}
