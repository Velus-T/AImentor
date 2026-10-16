package com.example.asm_final.dashboard;
import com.example.asm_final.utils.RecentActivity;
import com.example.asm_final.utils.RecentActivity.Subject;
import java.util.*;
/** Aggregates interchangeable subject sources; it has no SQLite or Android dependency. */
public final class CompositeDashboardRepository implements DashboardRepository {
    private final List<SubjectProgressSource> sources;
    public CompositeDashboardRepository(List<SubjectProgressSource> sources) { this.sources = new ArrayList<>(sources); }
    @Override public DashboardSnapshot load(int userId) {
        Map<Subject, SubjectProgress> values = new EnumMap<>(Subject.class);
        List<RecentActivity> recent = new ArrayList<>();
        Set<Subject> unavailable = EnumSet.noneOf(Subject.class);
        if (userId <= 0) return new DashboardSnapshot(values, recent, unavailable);
        for (SubjectProgressSource source : sources) {
            try {
                SubjectProgress value = source.load(userId);
                values.put(source.subject(), value);
                recent.addAll(value.activities);
            } catch (RuntimeException error) { unavailable.add(source.subject()); }
        }
        recent.sort((a,b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return new DashboardSnapshot(values, recent.subList(0, Math.min(10, recent.size())), unavailable);
    }
}
