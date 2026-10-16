package com.example.asm_final.dashboard;
import android.content.Context;
import android.database.Cursor;
import com.example.asm_final.R;
import com.example.asm_final.utils.RecentActivity;
import com.example.asm_final.utils.RecentActivity.Subject;
import com.example.asm_final.utils.RecentActivity.Type;
import java.util.*;
/** Composition root for local subject adapters. Replace individual sources when a backend is introduced. */
public final class LocalSubjectSources {
    private LocalSubjectSources() {}
    private interface Reader { SubjectProgress read(int userId); }
    private static SubjectProgressSource source(Subject subject, Reader reader) {
        return new SubjectProgressSource() {
            public Subject subject() { return subject; }
            public SubjectProgress load(int userId) { return reader.read(userId); }
        };
    }
    public static DashboardRepository create(Context context) {
        Context c = context.getApplicationContext();
        return new CompositeDashboardRepository(Arrays.asList(
            source(Subject.MATH, id -> math(c,id)), source(Subject.SCIENCE, id -> science(c,id)),
            source(Subject.HISTORY, id -> history(c,id)), source(Subject.PROGRAMMING, id -> programming(c,id))));
    }
    private static SubjectProgress math(Context c, int id) {
        try (com.example.mathmodule.databases.SqliteDbHelper db = new com.example.mathmodule.databases.SqliteDbHelper(c)) {
            int exp = db.getUserExp(id);
            List<RecentActivity> events = new ArrayList<>();
            List<com.example.mathmodule.databases.MathHistoryItem> history = db.getAllHistory(id);
            for (int i=0; i<Math.min(3,history.size()); i++) {
                com.example.mathmodule.databases.MathHistoryItem item=history.get(i);
                events.add(new RecentActivity(Subject.MATH,Type.CHAT,c.getString(R.string.activity_solve_math),item.getQuestion(),ActivityTimestamp.parse(item.getTimestamp())));
            }
            return new SubjectProgress(exp, exp*10L, events);
        }
    }
    private static SubjectProgress programming(Context c, int id) {
        try (com.example.modulelaptrinh.database.ProgrammingDbHelper db = new com.example.modulelaptrinh.database.ProgrammingDbHelper(c)) {
            int exp=db.getTotalExpAllSubjects(id);
            List<RecentActivity> events=new ArrayList<>();
            List<com.example.modulelaptrinh.database.SessionItem> sessions=db.getAllSessions(id);
            for (int i=0; i<Math.min(3,sessions.size()); i++) {
                com.example.modulelaptrinh.database.SessionItem item=sessions.get(i);
                events.add(new RecentActivity(Subject.PROGRAMMING,Type.CHAT,c.getString(R.string.activity_chat_prog),item.getName(),ActivityTimestamp.parse(item.getTimestamp())));
            }
            return new SubjectProgress(exp,exp*10L,events);
        }
    }
    private static SubjectProgress history(Context c, int id) {
        try (com.example.historymodule.databases.SqliteDbHelper db=new com.example.historymodule.databases.SqliteDbHelper(c)) {
            int exp=db.getTotalQuizExp(id);
            List<RecentActivity> events=new ArrayList<>();
            for (com.example.historymodule.databases.HistoryHistoryItem item:db.loadQuizAttempts(id,3))
                events.add(new RecentActivity(Subject.HISTORY,Type.QUIZ,c.getString(R.string.activity_quiz_history),item.getTitle(),ActivityTimestamp.parse(item.getTimestamp())));
            for (com.example.historymodule.databases.HistoryHistoryItem item:db.loadConversationSummaries(id,0,0,3))
                events.add(new RecentActivity(Subject.HISTORY,Type.CHAT,c.getString(R.string.activity_chat_history),item.getTitle(),ActivityTimestamp.parse(item.getTimestamp())));
            return new SubjectProgress(exp,exp*10L,events);
        }
    }
    private static SubjectProgress science(Context c, int id) {
        try (com.brightpath.science.data.local.ScienceDbHelper db=new com.brightpath.science.data.local.ScienceDbHelper(c)) {
            int exp=db.getTotalExp(id);long reviewed=0;
            List<RecentActivity> events=new ArrayList<>();
            try (Cursor cursor=db.getReadableDatabase().rawQuery("SELECT SUM(review_seconds) FROM science_history WHERE user_id=?",new String[]{String.valueOf(id)})) {
                if(cursor.moveToFirst()) reviewed=cursor.getLong(0);
            }
            try (Cursor cursor=db.getReadableDatabase().query("science_history",new String[]{"question","created_at"},"user_id=?",new String[]{String.valueOf(id)},null,null,"created_at DESC","3")) {
                while(cursor.moveToNext()) events.add(new RecentActivity(Subject.SCIENCE,Type.CHAT,c.getString(R.string.activity_ask_science),cursor.getString(0),cursor.getLong(1)));
            }
            try (Cursor cursor=db.getReadableDatabase().query("science_quiz_attempts",new String[]{"topic","created_at"},"user_id=?",new String[]{String.valueOf(id)},null,null,"created_at DESC","2")) {
                while(cursor.moveToNext()) events.add(new RecentActivity(Subject.SCIENCE,Type.QUIZ,c.getString(R.string.activity_quiz_science),cursor.getString(0),cursor.getLong(1)));
            }
            return new SubjectProgress(exp,reviewed+exp*5L,events);
        }
    }
}
