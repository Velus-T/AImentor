package com.example.asm_final.dashboard;
import com.example.asm_final.utils.RecentActivity;
import java.util.*;
public final class SubjectProgress {
    public final int experience;
    public final long estimatedSeconds;
    public final List<RecentActivity> activities;
    public SubjectProgress(int experience, long estimatedSeconds, List<RecentActivity> activities) {
        this.experience = Math.max(0, experience);
        this.estimatedSeconds = Math.max(0, estimatedSeconds);
        this.activities = Collections.unmodifiableList(new ArrayList<>(activities));
    }
}
