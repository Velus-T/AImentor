package com.example.asm_final.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.example.asm_final.activities.database.DatabaseHelper;

/*
 * Role: Utility class for App Shell, grouping reusable logic such as user sessions, levels, formatting, or data conversion.
 * Purpose: Moving shared logic into helpers reduces duplication and keeps Activities and Fragments focused on screen coordination.
 */
public class DemoDataSeeder {

    public static final String DEMO_USER = "admin_demo";
    public static final String DEMO_PASS = "Admin@123";

    /*
     * Responsibility: Performs the `seed` responsibility of `DemoDataSeeder` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    public static void seed(Context context) {
        DatabaseHelper mainDb = new DatabaseHelper(context);
        SQLiteDatabase db = mainDb.getWritableDatabase();

        // Reuse the existing demo account id so data in every feature module can
        // be refreshed without leaving orphaned records under an older user id.
        int existingDemoId = mainDb.getUserId(DEMO_USER);
        String hashedPass = BCrypt.withDefaults().hashToString(12, DEMO_PASS.toCharArray());
        ContentValues userValues = new ContentValues();
        userValues.put("username", DEMO_USER);
        userValues.put("password", hashedPass);
        userValues.put("full_name", "Demo Master");
        userValues.put("address", "Hà Nội, Việt Nam");
        userValues.put("phone", "0987654321");
        userValues.put("email", "admin_demo@gmail.com");

        long userId;
        if (existingDemoId > 0) {
            int updated = db.update(
                    "users",
                    userValues,
                    "id = ?",
                    new String[]{String.valueOf(existingDemoId)}
            );
            userId = updated > 0 ? existingDemoId : -1;
        } else {
            userId = db.insert("users", null, userValues);
        }
        db.close();

        if (userId <= 0) return;
        int uid = (int) userId;

        // 3. Seed Math Data (MathMentor.db)
        try (com.example.mathmodule.databases.SqliteDbHelper mathDb = new com.example.mathmodule.databases.SqliteDbHelper(context)) {
            SQLiteDatabase mdb = mathDb.getWritableDatabase();
            mdb.delete("user_profile", "user_id = ?", new String[]{String.valueOf(uid)});
            mdb.delete("math_quiz_attempts", "user_id = ?", new String[]{String.valueOf(uid)});
            mathDb.deleteUserHistory(uid);
            mathDb.deleteUserNotes(uid);

            ContentValues mathProfile = new ContentValues();
            mathProfile.put("user_id", uid);
            mathProfile.put("exp", 520);
            mdb.insert("user_profile", null, mathProfile);

            for (int i = 0; i < 5; i++) {
                ContentValues attempt = new ContentValues();
                attempt.put("user_id", uid);
                attempt.put("set_id", i + 1);
                attempt.put("correct_count", 8);
                attempt.put("total_count", 10);
                attempt.put("exp_earned", 104);
                mdb.insert("math_quiz_attempts", null, attempt);
            }
            
            mathDb.insertHistoryForUser(uid, "Giải phương trình bậc 2", "x = 1, x = 2");
            mathDb.insertHistoryForUser(uid, "Tính diện tích hình tròn r=5", "S = 78.5");
        } catch (Exception ignored) {}

        // 4. Seed History Data (history_db)
        try (com.example.historymodule.databases.SqliteDbHelper histDb = new com.example.historymodule.databases.SqliteDbHelper(context)) {
            SQLiteDatabase hdb = histDb.getWritableDatabase();
            hdb.delete("quiz_attempts", "user_id = ?", new String[]{String.valueOf(uid)});
            hdb.delete("chat_messages", "user_id = ?", new String[]{String.valueOf(uid)});

            histDb.saveQuizAttempt(uid, "Thời kỳ Hùng Vương", "Trung học cơ sở", 9, 10, 150);
            histDb.saveQuizAttempt(uid, "Cách mạng Tháng 8", "Trung học phổ thông", 8, 10, 120);
            histDb.saveQuizAttempt(uid, "Chiến thắng Điện Biên Phủ", "Đại học", 10, 10, 200);

            histDb.saveChatMessage(uid, "conv_1", "user", "Kể cho tôi về vua Quang Trung", false);
            histDb.saveChatMessage(uid, "conv_1", "bot", "Quang Trung - Nguyễn Huệ là vị anh hùng dân tộc...", false);
        } catch (Exception ignored) {}

        // 5. Seed Science Data (science_ai.db)
        try (com.brightpath.science.data.local.ScienceDbHelper sciDb = new com.brightpath.science.data.local.ScienceDbHelper(context)) {
            SQLiteDatabase sdb = sciDb.getWritableDatabase();
            sdb.delete("science_quiz_attempts", "user_id = ?", new String[]{String.valueOf(uid)});
            sdb.delete("science_history", "user_id = ?", new String[]{String.valueOf(uid)});

            ContentValues sciQuiz = new ContentValues();
            sciQuiz.put("user_id", uid);
            sciQuiz.put("subject", "Vật lý");
            sciQuiz.put("education_level", "UNIVERSITY");
            sciQuiz.put("topic", "Cơ học lượng tử");
            sciQuiz.put("challenge", "ON_LEVEL");
            sciQuiz.put("total_questions", 10);
            sciQuiz.put("correct_answers", 9);
            sciQuiz.put("exp_earned", 450);
            sciQuiz.put("created_at", System.currentTimeMillis());
            sdb.insert("science_quiz_attempts", null, sciQuiz);

            ContentValues sciHistory = new ContentValues();
            sciHistory.put("user_id", uid);
            sciHistory.put("subject", "Hóa học");
            sciHistory.put("education_level", "MIDDLE_SCHOOL");
            sciHistory.put("explanation_style", "SIMPLE");
            sciHistory.put("question", "Phản ứng nhiệt nhôm là gì?");
            sciHistory.put("answer", "Là phản ứng hóa học giữa nhôm và oxit kim loại...");
            sciHistory.put("created_at", System.currentTimeMillis());
            sciHistory.put("review_seconds", 3600); // 1 hour of review
            sdb.insert("science_history", null, sciHistory);
        } catch (Exception ignored) {}

        // 6. Seed Programming Data (ProgrammingMentor.db)
        try (com.example.modulelaptrinh.database.ProgrammingDbHelper progDb = new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
            SQLiteDatabase pdb = progDb.getWritableDatabase();
            pdb.delete("user_progress", "user_id = ?", new String[]{String.valueOf(uid)});
            pdb.delete("programming_quiz_attempts", "user_id = ?", new String[]{String.valueOf(uid)});
            pdb.delete("sessions", "user_id = ?", new String[]{String.valueOf(uid)});

            progDb.saveQuizResult(uid, "Java", "Cơ bản", "Vòng lặp", 10, 10, 200);
            progDb.saveQuizResult(uid, "Python", "Nâng cao", "AI/ML", 10, 8, 250);
            progDb.saveQuizResult(uid, "Android", "Trung bình", "Layout", 10, 9, 150);

            long sid = progDb.createNewSession(uid);
            progDb.updateSessionName(sid, "Tìm hiểu về Spring Boot");
            progDb.insertMessage(sid, "Giải thích về Dependency Injection", true);
            progDb.insertMessage(sid, "DI là một design pattern giúp giảm sự phụ thuộc...", false);
        } catch (Exception ignored) {}
    }
}
