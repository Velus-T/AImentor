package com.example.asm_final.dashboard;
import com.example.asm_final.utils.RecentActivity;
import java.util.*;
public final class SubjectProgress {
    public final int experience;
    public final List<RecentActivity> activities;
    public SubjectProgress(int experience, List<RecentActivity> activities) {
        this.experience = Math.max(0, experience);
        this.activities = Collections.unmodifiableList(new ArrayList<>(activities));
    }
}
