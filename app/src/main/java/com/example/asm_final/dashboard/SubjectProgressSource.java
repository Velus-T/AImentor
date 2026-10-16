package com.example.asm_final.dashboard;
import com.example.asm_final.utils.RecentActivity.Subject;
public interface SubjectProgressSource {
    Subject subject();
    SubjectProgress load(int userId);
}
