package com.example.aimentor.science.data.local;

public final class ScienceDbContract {
    private ScienceDbContract() { }

    public static final class History {
        public static final String TABLE = "science_history";
        public static final String ID = "id";
        public static final String USER_ID = "user_id";
        public static final String SUBJECT = "subject";
        public static final String EDUCATION_LEVEL = "education_level";
        public static final String EXPLANATION_STYLE = "explanation_style";
        public static final String NORMALIZED_QUESTION = "normalized_question";
        public static final String QUESTION = "question";
        public static final String IMAGE_URI = "image_uri";
        public static final String ANSWER = "answer";
        public static final String CREATED_AT = "created_at";
        public static final String REVIEW_SECONDS = "review_seconds";
    }

    public static final class Bookmark {
        public static final String TABLE = "science_bookmarks";
        public static final String ID = "id";
        public static final String USER_ID = "user_id";
        public static final String HISTORY_ID = "history_id";
        public static final String SUBJECT = "subject";
        public static final String TITLE = "title";
        public static final String CONTENT = "content";
        public static final String CREATED_AT = "created_at";
    }

    public static final class QuizAttempt {
        public static final String TABLE = "science_quiz_attempts";
        public static final String ID = "id";
        public static final String USER_ID = "user_id";
        public static final String SUBJECT = "subject";
        public static final String EDUCATION_LEVEL = "education_level";
        public static final String TOPIC = "topic";
        public static final String CHALLENGE = "challenge";
        public static final String TOTAL = "total_questions";
        public static final String CORRECT = "correct_answers";
        public static final String CREATED_AT = "created_at";
    }

    public static String createHistoryTable() {
        return "CREATE TABLE IF NOT EXISTS " + History.TABLE + " ("
                + History.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + History.USER_ID + " INTEGER NOT NULL, "
                + History.SUBJECT + " TEXT NOT NULL, "
                + History.EDUCATION_LEVEL + " TEXT NOT NULL, "
                + History.EXPLANATION_STYLE + " TEXT NOT NULL, "
                + History.NORMALIZED_QUESTION + " TEXT, "
                + History.QUESTION + " TEXT NOT NULL, "
                + History.IMAGE_URI + " TEXT, "
                + History.ANSWER + " TEXT NOT NULL, "
                + History.CREATED_AT + " INTEGER NOT NULL, "
                + History.REVIEW_SECONDS + " INTEGER NOT NULL DEFAULT 0)";
    }

    public static String createBookmarkTable() {
        return "CREATE TABLE IF NOT EXISTS " + Bookmark.TABLE + " ("
                + Bookmark.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Bookmark.USER_ID + " INTEGER NOT NULL, "
                + Bookmark.HISTORY_ID + " INTEGER NOT NULL, "
                + Bookmark.SUBJECT + " TEXT NOT NULL, "
                + Bookmark.TITLE + " TEXT NOT NULL, "
                + Bookmark.CONTENT + " TEXT NOT NULL, "
                + Bookmark.CREATED_AT + " INTEGER NOT NULL, "
                + "UNIQUE(" + Bookmark.USER_ID + ", " + Bookmark.HISTORY_ID + ") ON CONFLICT REPLACE)";
    }

    public static String createQuizAttemptTable() {
        return "CREATE TABLE IF NOT EXISTS " + QuizAttempt.TABLE + " ("
                + QuizAttempt.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + QuizAttempt.USER_ID + " INTEGER NOT NULL, "
                + QuizAttempt.SUBJECT + " TEXT NOT NULL, "
                + QuizAttempt.EDUCATION_LEVEL + " TEXT NOT NULL, "
                + QuizAttempt.TOPIC + " TEXT, "
                + QuizAttempt.CHALLENGE + " TEXT NOT NULL, "
                + QuizAttempt.TOTAL + " INTEGER NOT NULL, "
                + QuizAttempt.CORRECT + " INTEGER NOT NULL, "
                + QuizAttempt.CREATED_AT + " INTEGER NOT NULL)";
    }
}
