package com.brightpath.science.data.remote;

import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.domain.model.StudyPlan;
import com.brightpath.science.domain.model.StudyPlanRequest;
import com.brightpath.science.domain.model.StudySession;
import com.brightpath.science.domain.service.ResultCallback;
import com.brightpath.science.domain.service.ScienceStudyPlanGenerator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** Deterministic offline study planner used when no AI connection is available. */
public final class LocalScienceStudyPlanGenerator implements ScienceStudyPlanGenerator {
    private static final String[] PHYSICS_TOPICS = {
            "Core concepts and units", "Worked examples", "Formula practice",
            "Real-world application", "Mixed review", "Practice quiz"
    };
    private static final String[] CHEMISTRY_TOPICS = {
            "Key terms and particles", "Chemical formulas", "Reaction reasoning",
            "Balanced equations", "Real-world chemistry", "Mixed review"
    };
    private static final String[] BIOLOGY_TOPICS = {
            "Core structures", "Processes and functions", "Diagram review",
            "Cause-and-effect reasoning", "Real-world biology", "Mixed review"
    };

        /*
 * Responsibility: Generates the Science result handled by `generate` using the configured generator/service and request context.
 * Purpose: Keeps content generation behind a focused method so the presentation layer stays independent from AI/fallback details.
 */
@Override
    public void generate(StudyPlanRequest request, ResultCallback<StudyPlan> callback) {
        try {
            List<StudySession> sessions = new ArrayList<>();
            Calendar first = Calendar.getInstance();
            first.setTimeInMillis(request.getCreatedAtMillis());

            String[] topics = topics(request.getSubject());
            for (int i = 0; i < request.getNumberOfDays(); i++) {
                Calendar start = (Calendar) first.clone();
                start.add(Calendar.DAY_OF_YEAR, i);
                String topic = topics[i % topics.length];
                sessions.add(new StudySession(
                        i + 1,
                        start.getTimeInMillis(),
                        request.getMinutesPerDay(),
                        request.getSubject().getDisplayName() + ": " + topic,
                        buildActivities(request.getMinutesPerDay(), topic, request.getGoal()),
                        false
                ));
            }
            callback.onSuccess(new StudyPlan(
                    request.getCreatedAtMillis(),
                    request.getSubject(),
                    request.getLevel(),
                    request.getGoal(),
                    false,
                    sessions
            ));
        } catch (Exception error) {
            callback.onError("Unable to create an offline study plan.", error);
        }
    }

        /*
 * Responsibility: Converts input data in `topics` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
private String[] topics(ScienceSubject subject) {
        if (subject == ScienceSubject.CHEMISTRY) return CHEMISTRY_TOPICS;
        if (subject == ScienceSubject.BIOLOGY) return BIOLOGY_TOPICS;
        return PHYSICS_TOPICS;
    }

        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `buildActivities`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
private String buildActivities(int totalMinutes, String topic, String goal) {
        int review = Math.max(5, Math.round(totalMinutes * 0.25f));
        int learn = Math.max(8, Math.round(totalMinutes * 0.35f));
        int practice = Math.max(7, totalMinutes - review - learn);
        return review + " min: review previous knowledge.\n"
                + learn + " min: learn " + topic.toLowerCase() + " with examples.\n"
                + practice + " min: practise and check mistakes.\n"
                + "Goal focus: " + goal + ".";
    }
}
