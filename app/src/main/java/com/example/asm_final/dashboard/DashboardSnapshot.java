package com.example.asm_final.dashboard;
import com.example.asm_final.utils.RecentActivity;
import com.example.asm_final.utils.RecentActivity.Subject;
import java.util.*;
public final class DashboardSnapshot {
    private final Map<Subject, SubjectProgress> progress;
    public final List<RecentActivity> activities;
    public final Set<Subject> unavailable;
    public DashboardSnapshot(Map<Subject, SubjectProgress> progress, List<RecentActivity> activities, Set<Subject> unavailable) {
        this.progress = Collections.unmodifiableMap(new EnumMap<>(progress));
        this.activities = Collections.unmodifiableList(new ArrayList<>(activities));
        this.unavailable = Collections.unmodifiableSet(new HashSet<>(unavailable));
    }
    public SubjectProgress progress(Subject subject) {
        SubjectProgress value = progress.get(subject);
        return value == null ? new SubjectProgress(0, 0, Collections.emptyList()) : value;
    }
}
