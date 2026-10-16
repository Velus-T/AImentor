package com.example.asm_final;
import com.example.asm_final.dashboard.*;
import com.example.asm_final.utils.RecentActivity;
import com.example.asm_final.utils.RecentActivity.Subject;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class DashboardRegressionTest {
    private SubjectProgressSource source(Subject subject, int exp, long time) {
        return new SubjectProgressSource() {
            public Subject subject() { return subject; }
            public SubjectProgress load(int id) {
                assertEquals(42,id);
                return new SubjectProgress(exp,Arrays.asList(new RecentActivity(subject,RecentActivity.Type.CHAT,"","",time)));
            }
        };
    }
    @Test public void usesStoredUtcTimestampRatherThanCurrentTime() {
        assertEquals(1704067200000L,ActivityTimestamp.parse("2024-01-01 00:00:00"));
        assertEquals(1704067200000L,ActivityTimestamp.parse("1704067200"));
        assertEquals(1704067200000L,ActivityTimestamp.parse("1704067200000"));
    }
    @Test public void invalidTimestampDoesNotBecomeRecent() {
        assertEquals(0,ActivityTimestamp.parse("2024-02-30 00:00:00"));
        assertEquals(0,ActivityTimestamp.parse("2024-01-01 00:00:00 trailing"));
        assertEquals(0,ActivityTimestamp.parse(null));
    }
    @Test public void ordersAcrossSubjectsByActualDateAndKeepsUserId() {
        DashboardSnapshot value=new CompositeDashboardRepository(Arrays.asList(source(Subject.MATH,10,100),source(Subject.PROGRAMMING,20,200))).load(42);
        assertEquals(Subject.PROGRAMMING,value.activities.get(0).getSubject());
        assertEquals(10,value.progress(Subject.MATH).experience);
    }
    @Test public void oneUnavailableSubjectDoesNotHideOtherSubjects() {
        SubjectProgressSource broken=new SubjectProgressSource() {
            public Subject subject() { return Subject.HISTORY; }
            public SubjectProgress load(int id) { throw new IllegalStateException("database unavailable"); }
        };
        DashboardSnapshot result=new CompositeDashboardRepository(Arrays.asList(broken,source(Subject.MATH,5,100))).load(42);
        assertEquals(5,result.progress(Subject.MATH).experience);
        assertTrue(result.unavailable.contains(Subject.HISTORY));
    }
    @Test public void missingAccountNeverReadsAnonymousData() {
        SubjectProgressSource forbidden=new SubjectProgressSource() {
            public Subject subject() { return Subject.MATH; }
            public SubjectProgress load(int id) { fail("Anonymous source must not be read");return null; }
        };
        assertTrue(new CompositeDashboardRepository(Arrays.asList(forbidden)).load(0).activities.isEmpty());
    }
}
