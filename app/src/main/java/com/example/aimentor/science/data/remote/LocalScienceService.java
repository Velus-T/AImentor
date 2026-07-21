package com.example.aimentor.science.data.remote;

import com.example.aimentor.science.domain.model.QuizQuestion;
import com.example.aimentor.science.domain.model.QuizRequest;
import com.example.aimentor.science.domain.model.ScienceAnswer;
import com.example.aimentor.science.domain.model.ScienceQuestion;
import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.service.ResultCallback;
import com.example.aimentor.science.domain.service.ScienceAnswerService;
import com.example.aimentor.science.domain.service.ScienceQuizGenerator;

import java.util.List;
import java.util.Locale;

public final class LocalScienceService implements ScienceAnswerService, ScienceQuizGenerator {
    @Override
    public void answer(ScienceQuestion question, ResultCallback<ScienceAnswer> callback) {
        String content = createLocalExplanation(question);
        callback.onSuccess(new ScienceAnswer(
                content,
                question.getSubject().getDisplayName(),
                question.getEducationLevel().getDisplayName(),
                false,
                0
        ));
    }

    @Override
    public void generateQuiz(QuizRequest request, ResultCallback<List<QuizQuestion>> callback) {
        callback.onSuccess(new LocalAdaptiveQuizBank().generate(request));
    }

    private String createLocalExplanation(ScienceQuestion question) {
        String q = question.getQuestionText().toLowerCase(Locale.ROOT);
        String shortAnswer;
        String principle;
        String example;
        if (question.getSubject() == ScienceSubject.PHYSICS) {
            if (q.contains("energy") || q.contains("năng lượng")) {
                shortAnswer = "Energy cannot be created or destroyed; it changes from one form to another.";
                principle = "Conservation of energy: total energy before a process equals total energy after it, when the system is isolated.";
                example = "A falling ball changes gravitational potential energy into kinetic energy.";
            } else {
                shortAnswer = "Physics explains the question by relating motion, forces, energy and measurable quantities.";
                principle = "Start by listing known values, choosing the relevant law, substituting values with units, and checking the final unit.";
                example = "For constant speed, distance = speed × time.";
            }
        } else if (question.getSubject() == ScienceSubject.CHEMISTRY) {
            if (q.contains("water") || q.contains("nước") || q.contains("h2o")) {
                shortAnswer = "Water is a compound made from two hydrogen atoms and one oxygen atom: H2O.";
                principle = "Chemical formulas show the elements and atom ratios in a substance. A balanced example is 2H2 + O2 → 2H2O.";
                example = "Electrolysis can split water into hydrogen and oxygen under controlled laboratory conditions.";
            } else {
                shortAnswer = "Chemistry explains matter through particles, bonding and chemical reactions.";
                principle = "A chemical equation must conserve the number of atoms of every element on both sides.";
                example = "NaCl is formed from sodium and chlorine ions in a 1:1 ratio.";
            }
        } else {
            if (q.contains("photosynthesis") || q.contains("quang hợp")) {
                shortAnswer = "Photosynthesis allows plants to use light energy to make glucose from carbon dioxide and water.";
                principle = "6CO2 + 6H2O → C6H12O6 + 6O2, with light and chlorophyll.";
                example = "Leaves exposed to light produce stored chemical energy for plant growth.";
            } else {
                shortAnswer = "Biology explains living systems by connecting structures with their functions.";
                principle = "Identify the biological level involved: cell, organ, organism, population or ecosystem.";
                example = "Red blood cells have a shape that helps them carry oxygen efficiently.";
            }
        }

        String imageNote = question.hasImage()
                ? "\n\nIMAGE ANALYSIS\nAn image was attached. Offline mode cannot reliably read its details; use a configured Gemini key for image analysis."
                : "";
        return "TOPIC\n" + question.getSubject().getDisplayName() + "\n\n"
                + "DIFFICULTY\n" + question.getEducationLevel().getDisplayName() + "\n\n"
                + "SHORT ANSWER\n" + shortAnswer + "\n\n"
                + "DETAILED EXPLANATION\n" + principle + "\n\n"
                + "FORMULA OR PRINCIPLE\n" + principle + "\n\n"
                + "REAL-LIFE EXAMPLE\n" + example + "\n\n"
                + "COMMON MISTAKES\nDo not skip units, do not confuse observations with explanations, and do not invent data that are not provided.\n\n"
                + "RELATED SUGGESTIONS\nTry drawing a labelled diagram, changing one variable at a time, and explaining the result in your own words."
                + imageNote;
    }

}
