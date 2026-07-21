package com.example.aimentor.science;

import com.example.aimentor.science.data.remote.LocalScienceStudyPlanGenerator;
import com.example.aimentor.science.domain.model.EducationLevel;
import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.model.StudyPlan;
import com.example.aimentor.science.domain.model.StudyPlanRequest;
import com.example.aimentor.science.domain.service.ResultCallback;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class StudyPlanGeneratorTest {
    @Test
    public void startsFromCreationMomentAndKeepsRequestedLevel() {
        long createdAt = 1_700_000_000_000L;
        StudyPlanRequest request = new StudyPlanRequest(
                3, ScienceSubject.BIOLOGY, EducationLevel.HIGH_SCHOOL,
                "Review genetics", 7, 60, createdAt);
        AtomicReference<StudyPlan> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        new LocalScienceStudyPlanGenerator().generate(request, new ResultCallback<StudyPlan>() {
            @Override public void onSuccess(StudyPlan plan) { result.set(plan); }
            @Override public void onError(String message, Throwable throwable) { error.set(message); }
        });

        assertEquals(null, error.get());
        assertNotNull(result.get());
        assertEquals(7, result.get().getSessions().size());
        assertEquals(EducationLevel.HIGH_SCHOOL, result.get().getLevel());
        assertEquals(createdAt,
                result.get().getSessions().get(0).getStartAtMillis());
        assertTrue(result.get().getSessions().get(0).getActivities().contains("Review genetics"));
    }
}
