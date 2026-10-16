package com.example.modulelaptrinh.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.mentor.data.ModuleDatabaseHelper;
import android.util.Log;

import com.example.modulelaptrinh.ProgrammingChatMessage;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * DATABASE TRUNG TÂM CỦA PROGRAMMING MODULE.
 *
 * <p>SQLite file: {@code ProgrammingMentor.db}.</p>
 * <p>Các bảng đang dùng:</p>
 * <ul>
 *   <li>{@code sessions}: một cuộc hội thoại.</li>
 *   <li>{@code messages}: từng tin nhắn User/AI, có thể chứa ảnh Base64.</li>
 *   <li>{@code notes}: ghi chú theo user.</li>
 *   <li>{@code programming_quiz_attempts}: kết quả từng lần làm Quiz.</li>
 *   <li>{@code user_progress}: tổng EXP.</li>
 * </ul>
 *
 * <p>Cần phân biệt:</p>
 * <ul>
 *   <li>{@code getWritableDatabase()} mở/tạo file DB khi cần.</li>
 *   <li>{@code onCreate()} chạy CREATE TABLE khi DB được tạo lần đầu.</li>
 *   <li>{@code db.insert/update/query/delete} mới là thao tác dữ liệu CRUD.</li>
 * </ul>
 */
public class ProgrammingDbHelper extends ModuleDatabaseHelper {
    private static final String TAG = "ProgrammingDbHelper";
    // Tính/đọc và lưu giá trị `DATABASE_NAME` (database name) để sử dụng ở các bước phía sau.
    private static final String DATABASE_NAME = "ProgrammingMentor.db";
    // Tính/đọc và lưu giá trị `DATABASE_VERSION` (database version) để sử dụng ở các bước phía sau.
    private static final int DATABASE_VERSION = 7;

    // Bảng sessions
    public static final String TABLE_SESSIONS = "programming_sessions";
    public static final String COLUMN_SESSION_ID = "id";
    public static final String COLUMN_SESSION_NAME = "name";
    public static final String COLUMN_SESSION_USER_ID = "user_id";
    public static final String COLUMN_SESSION_TIMESTAMP = "timestamp";

    // Bảng messages
    public static final String TABLE_MESSAGES = "programming_messages";
    public static final String COLUMN_MESSAGE_ID = "id";
    public static final String COLUMN_MESSAGE_SESSION_ID = "session_id";
    public static final String COLUMN_MESSAGE_CONTENT = "content";
    public static final String COLUMN_MESSAGE_IS_USER = "is_user";
    public static final String COLUMN_MESSAGE_IMAGES = "images";
    public static final String COLUMN_MESSAGE_TIMESTAMP = "timestamp";

    // Bảng chat history cũ
    public static final String TABLE_CHAT_HISTORY = "programming_chat_history";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_QUESTION = "question";
    public static final String COLUMN_ANSWER = "answer";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // Bảng notes
    public static final String TABLE_NOTES = "programming_notes";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_NOTE_USER_ID = "user_id";

    // ===== CÁC BẢNG DỮ LIỆU QUIZ =====
    public static final String TABLE_QUIZ_ATTEMPTS = "programming_quiz_attempts";
    public static final String COLUMN_QUIZ_ID = "id";
    public static final String COLUMN_QUIZ_USER_ID = "user_id";
    public static final String COLUMN_QUIZ_SUBJECT = "subject";
    public static final String COLUMN_QUIZ_LEVEL = "level";
    public static final String COLUMN_QUIZ_TOPIC = "topic";
    public static final String COLUMN_QUIZ_TOTAL = "total_questions";
    public static final String COLUMN_QUIZ_CORRECT = "correct_answers";
    public static final String COLUMN_QUIZ_EXP_EARNED = "exp_earned";
    public static final String COLUMN_QUIZ_TIMESTAMP = "created_at";

    public static final String TABLE_USER_PROGRESS = "programming_user_progress";
    public static final String COLUMN_PROGRESS_USER_ID = "user_id";
    public static final String COLUMN_PROGRESS_TOTAL_EXP = "total_exp";
    public static final String COLUMN_PROGRESS_SUBJECT = "subject";
    public static final String COLUMN_PROGRESS_UPDATED_AT = "updated_at";

    // ===== CÁC CÂU LỆNH TẠO BẢNG =====
    private static final String CREATE_SESSIONS_TABLE =
            "CREATE TABLE " + TABLE_SESSIONS + " (" +
                    COLUMN_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SESSION_USER_ID + " INTEGER NOT NULL DEFAULT 0, " +
                    COLUMN_SESSION_NAME + " TEXT, " +
                    COLUMN_SESSION_TIMESTAMP + " INTEGER DEFAULT (CAST(strftime('%s','now') AS INTEGER)*1000)" +
                    ");";

    private static final String CREATE_MESSAGES_TABLE =
            "CREATE TABLE " + TABLE_MESSAGES + " (" +
                    COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MESSAGE_SESSION_ID + " INTEGER NOT NULL, " +
                    COLUMN_MESSAGE_CONTENT + " TEXT, " +
                    COLUMN_MESSAGE_IS_USER + " INTEGER, " +
                    COLUMN_MESSAGE_IMAGES + " TEXT, " +
                    COLUMN_MESSAGE_TIMESTAMP + " INTEGER DEFAULT (CAST(strftime('%s','now') AS INTEGER)*1000), " +
                    "FOREIGN KEY(" + COLUMN_MESSAGE_SESSION_ID + ") REFERENCES " +
                    TABLE_SESSIONS + "(" + COLUMN_SESSION_ID + ") ON DELETE CASCADE" +
                    ");";

    private static final String CREATE_CHAT_TABLE =
            "CREATE TABLE " + TABLE_CHAT_HISTORY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_QUESTION + " TEXT, " +
                    COLUMN_ANSWER + " TEXT, " +
                    COLUMN_TIMESTAMP + " INTEGER DEFAULT (CAST(strftime('%s','now') AS INTEGER)*1000)" +
                    ");";

    private static final String CREATE_NOTES_TABLE =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOTE_USER_ID + " INTEGER NOT NULL DEFAULT 0, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_TIMESTAMP + " INTEGER DEFAULT (CAST(strftime('%s','now') AS INTEGER)*1000)" +
                    ");";

    private static final String CREATE_QUIZ_ATTEMPTS_TABLE =
            "CREATE TABLE " + TABLE_QUIZ_ATTEMPTS + " (" +
                    COLUMN_QUIZ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_QUIZ_USER_ID + " INTEGER NOT NULL, " +
                    COLUMN_QUIZ_SUBJECT + " TEXT NOT NULL, " +
                    COLUMN_QUIZ_LEVEL + " TEXT NOT NULL, " +
                    COLUMN_QUIZ_TOPIC + " TEXT, " +
                    COLUMN_QUIZ_TOTAL + " INTEGER NOT NULL, " +
                    COLUMN_QUIZ_CORRECT + " INTEGER NOT NULL, " +
                    COLUMN_QUIZ_EXP_EARNED + " INTEGER NOT NULL DEFAULT 0, " +
                    COLUMN_QUIZ_TIMESTAMP + " INTEGER NOT NULL" +
                    ");";

    private static final String CREATE_USER_PROGRESS_TABLE =
            "CREATE TABLE " + TABLE_USER_PROGRESS + " (" +
                    COLUMN_PROGRESS_USER_ID + " INTEGER NOT NULL, " +
                    COLUMN_PROGRESS_TOTAL_EXP + " INTEGER NOT NULL DEFAULT 0, " +
                    COLUMN_PROGRESS_SUBJECT + " TEXT NOT NULL, " +
                    COLUMN_PROGRESS_UPDATED_AT + " INTEGER NOT NULL, " +
                    "PRIMARY KEY (" + COLUMN_PROGRESS_USER_ID + ", " + COLUMN_PROGRESS_SUBJECT + ")" +
                    ");";

    /**
     * Trigger duy trì tính toàn vẹn dữ liệu Chat.
     * Nếu một session bị xóa trực tiếp (kể cả từ code seed), mọi message thuộc session đó cũng bị xóa.
     */
    private static final String CREATE_SESSION_DELETE_TRIGGER =
            "CREATE TRIGGER IF NOT EXISTS trg_delete_session_messages " +
                    "AFTER DELETE ON " + TABLE_SESSIONS + " " +
                    "BEGIN DELETE FROM " + TABLE_MESSAGES +
                    " WHERE " + COLUMN_MESSAGE_SESSION_ID + " = OLD." + COLUMN_SESSION_ID + "; END;";

    /**
     * Constructor giao tên/version DB cho SQLiteOpenHelper quản lý.
     * File .db thực tế được Android mở/tạo khi getWritableDatabase()/getReadableDatabase() được gọi.
     */
    public ProgrammingDbHelper(Context context) {
        // Gọi hàm `super()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Bật Foreign Key của SQLite để ON DELETE CASCADE hoạt động đúng.
     */
    @Override
    // Hàm `onConfigure()`: Cấu hình SQLite connection trước khi database được mở.
    public void onConfigure(SQLiteDatabase db) {
        // Gọi implementation `onConfigure()` của lớp cha để giữ đúng vòng đời/hành vi mặc định.
        super.onConfigure(db);
        // Bật kiểm tra khóa ngoại để quan hệ và ON DELETE CASCADE hoạt động đúng.
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Android tự gọi hàm này khi ProgrammingMentor.db được tạo lần đầu.
     * db.execSQL(CREATE_...) thực thi từng câu SQL CREATE TABLE.
     */
    @Override
    // Hàm `onCreate()`: Khởi tạo Activity và chuẩn bị các thành phần cần thiết khi màn hình được tạo.
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng sessions: id, user_id, name, timestamp.
        db.execSQL(CREATE_SESSIONS_TABLE);
        // Tạo bảng messages: session_id, content, is_user, images, timestamp.
        db.execSQL(CREATE_MESSAGES_TABLE);
        // Thực thi trực tiếp câu lệnh SQL không trả về tập kết quả, thường dùng cho CREATE/ALTER/DELETE/UPDATE schema.
        db.execSQL(CREATE_CHAT_TABLE);
        // Tạo bảng notes: user_id, title, content, timestamp.
        db.execSQL(CREATE_NOTES_TABLE);
        // Tạo bảng lưu kết quả từng lần làm Quiz.
        db.execSQL(CREATE_QUIZ_ATTEMPTS_TABLE);
        // Tạo bảng lưu tổng EXP/progress của user.
        db.execSQL(CREATE_USER_PROGRESS_TABLE);
        // Bảo đảm xóa session thì messages không bị mồ côi.
        db.execSQL(CREATE_SESSION_DELETE_TRIGGER);
    }

    /** Migration schema khi DATABASE_VERSION tăng, tránh xóa dữ liệu cũ. */
    @Override
    // Hàm `onUpgrade()`: Thực hiện migration schema khi version database tăng.
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Gọi `d()` trên `Log` để thực hiện thao tác tương ứng.
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (oldVersion < 2) {
            // Thực thi trực tiếp câu lệnh SQL không trả về tập kết quả, thường dùng cho CREATE/ALTER/DELETE/UPDATE schema.
            db.execSQL(CREATE_SESSIONS_TABLE);
            // Thực thi trực tiếp câu lệnh SQL không trả về tập kết quả, thường dùng cho CREATE/ALTER/DELETE/UPDATE schema.
            db.execSQL(CREATE_MESSAGES_TABLE);
        }
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (oldVersion < 3) {
            // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
            try {
                // Thực thi trực tiếp câu lệnh SQL không trả về tập kết quả, thường dùng cho CREATE/ALTER/DELETE/UPDATE schema.
                db.execSQL("ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COLUMN_MESSAGE_IMAGES + " TEXT");
            // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
            } catch (Exception e) {
                // Gọi `w()` trên `Log` để thực hiện thao tác tương ứng.
                Log.w(TAG, "Column already exists or migration error: " + e.getMessage());
            }
        }
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (oldVersion < 4) {
            // Nâng cấp lên version 4
        }
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (oldVersion < 5) {
            // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
            try {
                // Thực thi trực tiếp câu lệnh SQL không trả về tập kết quả, thường dùng cho CREATE/ALTER/DELETE/UPDATE schema.
                db.execSQL(CREATE_QUIZ_ATTEMPTS_TABLE);
                // Thực thi trực tiếp câu lệnh SQL không trả về tập kết quả, thường dùng cho CREATE/ALTER/DELETE/UPDATE schema.
                db.execSQL(CREATE_USER_PROGRESS_TABLE);
                // Gọi `d()` trên `Log` để thực hiện thao tác tương ứng.
                Log.d(TAG, "Quiz tables created successfully");
            // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
            } catch (Exception e) {
                // Gọi `e()` trên `Log` để thực hiện thao tác tương ứng.
                Log.e(TAG, "Error creating quiz tables: " + e.getMessage());
            }
        }
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (oldVersion < 6) {
            // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
            try {
                // Thực thi trực tiếp câu lệnh SQL không trả về tập kết quả, thường dùng cho CREATE/ALTER/DELETE/UPDATE schema.
                db.execSQL("ALTER TABLE " + TABLE_SESSIONS
                        + " ADD COLUMN " + COLUMN_SESSION_USER_ID
                        + " INTEGER NOT NULL DEFAULT 0");
            // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
            } catch (Exception e) {
                // Gọi `w()` trên `Log` để thực hiện thao tác tương ứng.
                Log.w(TAG, "Session user migration skipped: " + e.getMessage());
            }
            // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
            try {
                // Thực thi trực tiếp câu lệnh SQL không trả về tập kết quả, thường dùng cho CREATE/ALTER/DELETE/UPDATE schema.
                db.execSQL("ALTER TABLE " + TABLE_NOTES
                        + " ADD COLUMN " + COLUMN_NOTE_USER_ID
                        + " INTEGER NOT NULL DEFAULT 0");
            // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
            } catch (Exception e) {
                // Gọi `w()` trên `Log` để thực hiện thao tác tương ứng.
                Log.w(TAG, "Note user migration skipped: " + e.getMessage());
            }
        }
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (oldVersion < 7) {
            // Gọi hàm `migrateUserProgressToCompositeKey()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            migrateUserProgressToCompositeKey(db);
            // Trigger này cũng xử lý các đoạn code xóa session trực tiếp thay vì gọi deleteSession().
            db.execSQL(CREATE_SESSION_DELETE_TRIGGER);
            // Xóa message mồ côi đã tồn tại từ các bản cũ trước khi có trigger/FK.
            db.execSQL("DELETE FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_MESSAGE_SESSION_ID
                    + " NOT IN (SELECT " + COLUMN_SESSION_ID + " FROM " + TABLE_SESSIONS + ")");
        }
    }

    /**
     * Sửa khóa của user_progress từ chỉ user_id thành (user_id, subject).
     * Nhờ vậy cùng một user có thể lưu EXP riêng cho Java/Python/Android/Lập trình mà không ghi đè nhau.
     */
    private void migrateUserProgressToCompositeKey(SQLiteDatabase db) {
        // Tính/đọc và lưu giá trị `tempTable` (temp table) để sử dụng ở các bước phía sau.
        final String tempTable = TABLE_USER_PROGRESS + "_v7";
        // Bắt đầu transaction để nhóm nhiều thao tác database thành một khối nguyên tử.
        db.beginTransaction();
        // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
        try {
            // Thực thi trực tiếp câu lệnh SQL không trả về tập kết quả, thường dùng cho CREATE/ALTER/DELETE/UPDATE schema.
            db.execSQL("DROP TABLE IF EXISTS " + tempTable);
            // Thực thi trực tiếp câu lệnh SQL không trả về tập kết quả, thường dùng cho CREATE/ALTER/DELETE/UPDATE schema.
            db.execSQL("CREATE TABLE " + tempTable + " (" +
                    COLUMN_PROGRESS_USER_ID + " INTEGER NOT NULL, " +
                    COLUMN_PROGRESS_TOTAL_EXP + " INTEGER NOT NULL DEFAULT 0, " +
                    COLUMN_PROGRESS_SUBJECT + " TEXT NOT NULL, " +
                    COLUMN_PROGRESS_UPDATED_AT + " INTEGER NOT NULL, " +
                    "PRIMARY KEY (" + COLUMN_PROGRESS_USER_ID + ", " + COLUMN_PROGRESS_SUBJECT + ")" +
                    ")");

            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (tableExists(db, TABLE_USER_PROGRESS)) {
                // Thực thi trực tiếp câu lệnh SQL không trả về tập kết quả, thường dùng cho CREATE/ALTER/DELETE/UPDATE schema.
                db.execSQL("INSERT OR REPLACE INTO " + tempTable + " (" +
                        COLUMN_PROGRESS_USER_ID + ", " +
                        COLUMN_PROGRESS_TOTAL_EXP + ", " +
                        COLUMN_PROGRESS_SUBJECT + ", " +
                        COLUMN_PROGRESS_UPDATED_AT + ") SELECT " +
                        COLUMN_PROGRESS_USER_ID + ", " +
                        COLUMN_PROGRESS_TOTAL_EXP + ", " +
                        "COALESCE(NULLIF(" + COLUMN_PROGRESS_SUBJECT + ", ''), 'Lập trình'), " +
                        COLUMN_PROGRESS_UPDATED_AT + " FROM " + TABLE_USER_PROGRESS);
                // Thực thi trực tiếp câu lệnh SQL không trả về tập kết quả, thường dùng cho CREATE/ALTER/DELETE/UPDATE schema.
                db.execSQL("DROP TABLE " + TABLE_USER_PROGRESS);
            }

            // Thực thi trực tiếp câu lệnh SQL không trả về tập kết quả, thường dùng cho CREATE/ALTER/DELETE/UPDATE schema.
            db.execSQL("ALTER TABLE " + tempTable + " RENAME TO " + TABLE_USER_PROGRESS);
            // Đánh dấu transaction thành công để các thay đổi được commit.
            db.setTransactionSuccessful();
        // Khối finally luôn chạy để giải phóng/hoàn tất tài nguyên cần thiết.
        } finally {
            // Kết thúc transaction; nếu chưa đánh dấu thành công thì SQLite sẽ rollback.
            db.endTransaction();
        }
    }

    // Hàm `tableExists()`: Thực hiện chức năng `tableExists` của class này.
    private boolean tableExists(SQLiteDatabase db, String tableName) {
        // Chạy câu SELECT dạng raw SQL và nhận kết quả thông qua Cursor.
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name=? LIMIT 1",
                new String[]{tableName}
        );
        // Di chuyển Cursor tới record đầu tiên và đồng thời kiểm tra truy vấn có dữ liệu hay không.
        boolean exists = cursor.moveToFirst();
        // Đóng Cursor sau khi đọc xong để giải phóng tài nguyên truy vấn.
        cursor.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return exists;
    }

    // ===== CÁC HÀM XỬ LÝ PHIÊN CHAT =====
    /** INSERT một chat session mới cho user và trả về sessionId. */
    public long createNewSession(int userId) {
        // Mở SQLite ở chế độ ghi; SQLiteOpenHelper sẽ tạo/mở database nếu cần.
        SQLiteDatabase db = this.getWritableDatabase();
        // Tạo ContentValues để đóng gói dữ liệu theo dạng tên-cột → giá-trị trước khi INSERT/UPDATE.
        ContentValues values = new ContentValues();
        // Gán dữ liệu cho COLUMN_SESSION_USER_ID trong ContentValues.
        values.put(COLUMN_SESSION_USER_ID, Math.max(0, userId));
        // Gán dữ liệu cho COLUMN_SESSION_NAME trong ContentValues.
        values.put(COLUMN_SESSION_NAME, "Cuộc trò chuyện mới");
        // Đây là dòng ghi record thật xuống bảng sessions.
        long id = db.insert(TABLE_SESSIONS, null, values);
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return id;
    }

    /** Đổi tên session sau câu hỏi đầu để History dễ nhận biết. */
    public void updateSessionName(long sessionId, String name) {
        // Mở SQLite ở chế độ ghi; SQLiteOpenHelper sẽ tạo/mở database nếu cần.
        SQLiteDatabase db = this.getWritableDatabase();
        // Tạo ContentValues để đóng gói dữ liệu theo dạng tên-cột → giá-trị trước khi INSERT/UPDATE.
        ContentValues values = new ContentValues();
        // Gán dữ liệu cho COLUMN_SESSION_NAME trong ContentValues.
        values.put(COLUMN_SESSION_NAME, name);
        // Thực hiện UPDATE để thay đổi record đã tồn tại trong SQLite theo điều kiện chỉ định.
        db.update(TABLE_SESSIONS, values, COLUMN_SESSION_ID + " = ?", new String[]{String.valueOf(sessionId)});
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
    }

    /** Kiểm tra session_id có thuộc user_id hiện tại hay không. */
    public boolean isSessionOwnedByUser(long sessionId, int userId) {
        // Mở SQLite ở chế độ đọc để thực hiện truy vấn dữ liệu.
        SQLiteDatabase db = this.getReadableDatabase();
        // Dùng API query của SQLite để SELECT dữ liệu và nhận về Cursor.
        Cursor cursor = db.query(
                TABLE_SESSIONS,
                new String[]{COLUMN_SESSION_ID},
                COLUMN_SESSION_ID + "=? AND " + COLUMN_SESSION_USER_ID + "=?",
                new String[]{String.valueOf(sessionId), String.valueOf(Math.max(0, userId))},
                null, null, null
        );
        // Di chuyển Cursor tới record đầu tiên và đồng thời kiểm tra truy vấn có dữ liệu hay không.
        boolean owned = cursor.moveToFirst();
        // Đóng Cursor sau khi đọc xong để giải phóng tài nguyên truy vấn.
        cursor.close();
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return owned;
    }

    /** SELECT lịch sử chat theo user_id. */
    public List<SessionItem> getAllSessions(int userId) {
        // Khởi tạo danh sách phiên chat phục vụ cho bước xử lý tiếp theo.
        List<SessionItem> sessionList = new ArrayList<>();
        // Mở SQLite ở chế độ đọc để thực hiện truy vấn dữ liệu.
        SQLiteDatabase db = this.getReadableDatabase();
        // Tính/đọc và lưu giá trị `query` (query) để sử dụng ở các bước phía sau.
        String query = "SELECT * FROM " + TABLE_SESSIONS
                + " WHERE " + COLUMN_SESSION_USER_ID + "=?"
                + " ORDER BY " + COLUMN_SESSION_TIMESTAMP + " DESC";
        // Chạy câu SELECT dạng raw SQL và nhận kết quả thông qua Cursor.
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(Math.max(0, userId))});

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (cursor.moveToFirst()) {
            // Bắt đầu vòng lặp do-while để xử lý ít nhất một phần tử.
            do {
                // Khởi tạo giá trị `session` (session) phục vụ cho bước xử lý tiếp theo.
                SessionItem session = new SessionItem();
                // Đọc giá trị của cột hiện tại từ Cursor.
                session.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SESSION_ID)));
                // Đọc giá trị của cột hiện tại từ Cursor.
                session.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_NAME)));
                // Đọc giá trị của cột hiện tại từ Cursor.
                session.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_TIMESTAMP)));
                // Thêm phần tử vừa tạo/xử lý vào danh sách `sessionList`.
                sessionList.add(session);
            // Di chuyển Cursor sang record tiếp theo trong tập kết quả.
            } while (cursor.moveToNext());
        }
        // Đóng Cursor sau khi đọc xong để giải phóng tài nguyên truy vấn.
        cursor.close();
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return sessionList;
    }

    /**
     * Xóa session của đúng user.
     * Message con được xóa tự động bởi FOREIGN KEY ON DELETE CASCADE / trigger của database.
     */
    public boolean deleteSession(long sessionId, int userId) {
        // Mở SQLite ở chế độ ghi; SQLiteOpenHelper sẽ tạo/mở database nếu cần.
        SQLiteDatabase db = this.getWritableDatabase();
        // Thực hiện DELETE để xóa record trong SQLite theo điều kiện chỉ định.
        int deleted = db.delete(TABLE_SESSIONS,
                COLUMN_SESSION_ID + "=? AND " + COLUMN_SESSION_USER_ID + "=?",
                new String[]{String.valueOf(sessionId), String.valueOf(Math.max(0, userId))});
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        return deleted > 0;
    }

    /** Xóa toàn bộ session của user; database tự cascade xóa messages tương ứng. */
    public boolean deleteAllSessions(int userId) {
        // Mở SQLite ở chế độ ghi; SQLiteOpenHelper sẽ tạo/mở database nếu cần.
        SQLiteDatabase db = this.getWritableDatabase();
        // Thực hiện DELETE để xóa record trong SQLite theo điều kiện chỉ định.
        int deleted = db.delete(TABLE_SESSIONS, COLUMN_SESSION_USER_ID + "=?",
                new String[]{String.valueOf(Math.max(0, userId))});
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        return deleted > 0;
    }

    // ===== CÁC HÀM XỬ LÝ TIN NHẮN =====
    // Hàm `insertMessage()`: Lưu một message của User/AI vào SQLite, kèm danh sách ảnh Base64 nếu có.
    public long insertMessage(long sessionId, String content, boolean isUser) {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return insertMessage(sessionId, content, isUser, null);
    }

    /**
     * Persist một message. isUser=true => 1, false => 0.
     * Danh sách ảnh được chuyển thành JSON string rồi lưu ở cột images TEXT.
     */
    public long insertMessage(long sessionId, String content, boolean isUser, List<String> imageBase64List) {
        // Mở SQLite ở chế độ ghi; SQLiteOpenHelper sẽ tạo/mở database nếu cần.
        SQLiteDatabase db = this.getWritableDatabase();
        // Tạo ContentValues để đóng gói dữ liệu theo dạng tên-cột → giá-trị trước khi INSERT/UPDATE.
        ContentValues values = new ContentValues();
        // Gán dữ liệu cho COLUMN_MESSAGE_SESSION_ID trong ContentValues.
        values.put(COLUMN_MESSAGE_SESSION_ID, sessionId);
        // Gán dữ liệu cho COLUMN_MESSAGE_CONTENT trong ContentValues.
        values.put(COLUMN_MESSAGE_CONTENT, content);
        // Gán dữ liệu cho COLUMN_MESSAGE_IS_USER trong ContentValues.
        values.put(COLUMN_MESSAGE_IS_USER, isUser ? 1 : 0);

        // Tính/đọc và lưu chuỗi JSON lưu danh sách ảnh Base64 để sử dụng ở các bước phía sau.
        String imagesJson = null;
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (imageBase64List != null && !imageBase64List.isEmpty()) {
            // Tạo/parse JSONArray để làm việc với danh sách dữ liệu JSON.
            JSONArray jsonArray = new JSONArray();
            // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
            for (String image : imageBase64List) {
                // Gọi `put()` trên `jsonArray` để thực hiện thao tác tương ứng.
                jsonArray.put(image);
            }
            // Cập nhật chuỗi JSON lưu danh sách ảnh Base64 theo trạng thái/kết quả vừa tính được.
            imagesJson = jsonArray.toString();
        }
        // Gán dữ liệu cho COLUMN_MESSAGE_IMAGES trong ContentValues.
        values.put(COLUMN_MESSAGE_IMAGES, imagesJson);

        // Đây là dòng INSERT thật vào bảng messages.
        long id = db.insert(TABLE_MESSAGES, null, values);
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (id != -1) {
            // Mỗi message mới làm session trở thành cuộc chat hoạt động gần nhất.
            db.execSQL("UPDATE " + TABLE_SESSIONS + " SET " + COLUMN_SESSION_TIMESTAMP
                    + " = CURRENT_TIMESTAMP WHERE " + COLUMN_SESSION_ID + " = ?",
                    new Object[]{sessionId});
        }
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return id;
    }

    /** Khôi phục toàn bộ message của một session để mở lại Chat History. */
    public List<ProgrammingChatMessage> getMessagesBySession(long sessionId) {
        // Khởi tạo danh sách tin nhắn phục vụ cho bước xử lý tiếp theo.
        List<ProgrammingChatMessage> messageList = new ArrayList<>();
        // Mở SQLite ở chế độ đọc để thực hiện truy vấn dữ liệu.
        SQLiteDatabase db = this.getReadableDatabase();
        // Tính/đọc và lưu giá trị `query` (query) để sử dụng ở các bước phía sau.
        String query = "SELECT * FROM " + TABLE_MESSAGES +
                " WHERE " + COLUMN_MESSAGE_SESSION_ID + " = ?" +
                " ORDER BY " + COLUMN_MESSAGE_TIMESTAMP + " ASC";
        // Chạy câu SELECT dạng raw SQL và nhận kết quả thông qua Cursor.
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(sessionId)});

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (cursor.moveToFirst()) {
            // Bắt đầu vòng lặp do-while để xử lý ít nhất một phần tử.
            do {
                // Đọc giá trị của cột hiện tại từ Cursor.
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_CONTENT));
                // Đọc giá trị của cột hiện tại từ Cursor.
                boolean isUser = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_IS_USER)) == 1;
                // Đọc giá trị của cột hiện tại từ Cursor.
                String imagesJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_IMAGES));

                // Khởi tạo danh sách ảnh đã mã hóa Base64 phục vụ cho bước xử lý tiếp theo.
                List<String> imageBase64List = new ArrayList<>();
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (imagesJson != null && !imagesJson.isEmpty()) {
                    // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
                    try {
                        // Tạo/parse JSONArray để làm việc với danh sách dữ liệu JSON.
                        JSONArray jsonArray = new JSONArray(imagesJson);
                        // Lặp qua các phần tử/chỉ số cần xử lý trong khối lệnh bên dưới.
                        for (int i = 0; i < jsonArray.length(); i++) {
                            // Đọc chuỗi tại vị trí/key tương ứng từ JSON.
                            imageBase64List.add(jsonArray.getString(i));
                        }
                    // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
                    } catch (JSONException e) {
                        // Gọi `printStackTrace()` trên `e` để thực hiện thao tác tương ứng.
                        e.printStackTrace();
                    }
                }

                // Khởi tạo đối tượng/nội dung tin nhắn phục vụ cho bước xử lý tiếp theo.
                ProgrammingChatMessage message = new ProgrammingChatMessage(content, isUser, imageBase64List);
                // Thêm phần tử vừa tạo/xử lý vào danh sách `messageList`.
                messageList.add(message);
            // Di chuyển Cursor sang record tiếp theo trong tập kết quả.
            } while (cursor.moveToNext());
        }
        // Đóng Cursor sau khi đọc xong để giải phóng tài nguyên truy vấn.
        cursor.close();
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return messageList;
    }

    // ===== CÁC HÀM LỊCH SỬ CHAT CŨ (LEGACY) =====
    // Cơ chế cũ; UI hiện tại dùng sessions + messages.
    // Hàm `insertChatHistory()`: Thực hiện chức năng `insertChatHistory` của class này.
    public long insertChatHistory(String question, String answer) {
        // Mở SQLite ở chế độ ghi; SQLiteOpenHelper sẽ tạo/mở database nếu cần.
        SQLiteDatabase db = this.getWritableDatabase();
        // Tạo ContentValues để đóng gói dữ liệu theo dạng tên-cột → giá-trị trước khi INSERT/UPDATE.
        ContentValues values = new ContentValues();
        // Gán dữ liệu cho COLUMN_QUESTION trong ContentValues.
        values.put(COLUMN_QUESTION, question);
        // Gán dữ liệu cho COLUMN_ANSWER trong ContentValues.
        values.put(COLUMN_ANSWER, answer);
        // Thực hiện INSERT: đây là thao tác ghi một record mới thật sự xuống bảng SQLite.
        long id = db.insert(TABLE_CHAT_HISTORY, null, values);
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return id;
    }

    // Hàm `getAllChatHistory()`: Trả về giá trị AllChatHistory hiện đang được đối tượng lưu giữ.
    public List<ProgrammingChatHistoryItem> getAllChatHistory() {
        // Khởi tạo giá trị `historyList` (history list) phục vụ cho bước xử lý tiếp theo.
        List<ProgrammingChatHistoryItem> historyList = new ArrayList<>();
        // Mở SQLite ở chế độ đọc để thực hiện truy vấn dữ liệu.
        SQLiteDatabase db = this.getReadableDatabase();
        // Tính/đọc và lưu giá trị `query` (query) để sử dụng ở các bước phía sau.
        String query = "SELECT * FROM " + TABLE_CHAT_HISTORY + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";
        // Chạy câu SELECT dạng raw SQL và nhận kết quả thông qua Cursor.
        Cursor cursor = db.rawQuery(query, null);

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (cursor.moveToFirst()) {
            // Bắt đầu vòng lặp do-while để xử lý ít nhất một phần tử.
            do {
                // Khởi tạo giá trị `item` (item) phục vụ cho bước xử lý tiếp theo.
                ProgrammingChatHistoryItem item = new ProgrammingChatHistoryItem();
                // Đọc giá trị của cột hiện tại từ Cursor.
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                // Đọc giá trị của cột hiện tại từ Cursor.
                item.setQuestion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUESTION)));
                // Đọc giá trị của cột hiện tại từ Cursor.
                item.setAnswer(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANSWER)));
                // Đọc giá trị của cột hiện tại từ Cursor.
                item.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                // Thêm phần tử vừa tạo/xử lý vào danh sách `historyList`.
                historyList.add(item);
            // Di chuyển Cursor sang record tiếp theo trong tập kết quả.
            } while (cursor.moveToNext());
        }
        // Đóng Cursor sau khi đọc xong để giải phóng tài nguyên truy vấn.
        cursor.close();
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return historyList;
    }

    // Hàm `deleteChatHistory()`: Xóa dữ liệu tương ứng và giải phóng các quan hệ liên quan nếu có.
    public void deleteChatHistory(int id) {
        // Mở SQLite ở chế độ ghi; SQLiteOpenHelper sẽ tạo/mở database nếu cần.
        SQLiteDatabase db = this.getWritableDatabase();
        // Thực hiện DELETE để xóa record trong SQLite theo điều kiện chỉ định.
        db.delete(TABLE_CHAT_HISTORY, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
    }

    // Hàm `deleteAllChatHistory()`: Xóa dữ liệu tương ứng và giải phóng các quan hệ liên quan nếu có.
    public void deleteAllChatHistory() {
        // Mở SQLite ở chế độ ghi; SQLiteOpenHelper sẽ tạo/mở database nếu cần.
        SQLiteDatabase db = this.getWritableDatabase();
        // Thực hiện DELETE để xóa record trong SQLite theo điều kiện chỉ định.
        db.delete(TABLE_CHAT_HISTORY, null, null);
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
    }

    // ===== CÁC HÀM XỬ LÝ GHI CHÚ =====
    /** INSERT note mới và gắn user_id. */
    public long insertNote(int userId, String title, String content) {
        // Mở SQLite ở chế độ ghi; SQLiteOpenHelper sẽ tạo/mở database nếu cần.
        SQLiteDatabase db = this.getWritableDatabase();
        // Tạo ContentValues để đóng gói dữ liệu theo dạng tên-cột → giá-trị trước khi INSERT/UPDATE.
        ContentValues values = new ContentValues();
        // Gán dữ liệu cho COLUMN_NOTE_USER_ID trong ContentValues.
        values.put(COLUMN_NOTE_USER_ID, Math.max(0, userId));
        // Gán dữ liệu cho COLUMN_TITLE trong ContentValues.
        values.put(COLUMN_TITLE, title);
        // Gán dữ liệu cho COLUMN_CONTENT trong ContentValues.
        values.put(COLUMN_CONTENT, content);
        // Thực hiện INSERT: đây là thao tác ghi một record mới thật sự xuống bảng SQLite.
        long id = db.insert(TABLE_NOTES, null, values);
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return id;
    }

    /** SELECT toàn bộ notes của đúng user. */
    public List<ProgrammingNote> getAllNotes(int userId) {
        // Khởi tạo danh sách ghi chú phục vụ cho bước xử lý tiếp theo.
        List<ProgrammingNote> noteList = new ArrayList<>();
        // Mở SQLite ở chế độ đọc để thực hiện truy vấn dữ liệu.
        SQLiteDatabase db = this.getReadableDatabase();
        // Tính/đọc và lưu giá trị `query` (query) để sử dụng ở các bước phía sau.
        String query = "SELECT * FROM " + TABLE_NOTES
                + " WHERE " + COLUMN_NOTE_USER_ID + "=?"
                + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";
        // Chạy câu SELECT dạng raw SQL và nhận kết quả thông qua Cursor.
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(Math.max(0, userId))});

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (cursor.moveToFirst()) {
            // Bắt đầu vòng lặp do-while để xử lý ít nhất một phần tử.
            do {
                // Khởi tạo giá trị `note` (note) phục vụ cho bước xử lý tiếp theo.
                ProgrammingNote note = new ProgrammingNote();
                // Đọc giá trị của cột hiện tại từ Cursor.
                note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                // Đọc giá trị của cột hiện tại từ Cursor.
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                // Đọc giá trị của cột hiện tại từ Cursor.
                note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                // Đọc giá trị của cột hiện tại từ Cursor.
                note.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                // Thêm phần tử vừa tạo/xử lý vào danh sách `noteList`.
                noteList.add(note);
            // Di chuyển Cursor sang record tiếp theo trong tập kết quả.
            } while (cursor.moveToNext());
        }
        // Đóng Cursor sau khi đọc xong để giải phóng tài nguyên truy vấn.
        cursor.close();
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return noteList;
    }

    /** Search title/content nhưng luôn có điều kiện user_id. */
    public List<ProgrammingNote> searchNotes(int userId, String keyword) {
        // Khởi tạo danh sách ghi chú phục vụ cho bước xử lý tiếp theo.
        List<ProgrammingNote> noteList = new ArrayList<>();
        // Mở SQLite ở chế độ đọc để thực hiện truy vấn dữ liệu.
        SQLiteDatabase db = this.getReadableDatabase();
        // Tính/đọc và lưu giá trị `query` (query) để sử dụng ở các bước phía sau.
        String query = "SELECT * FROM " + TABLE_NOTES
                + " WHERE " + COLUMN_NOTE_USER_ID + "=? AND ("
                + COLUMN_TITLE + " LIKE ? OR " + COLUMN_CONTENT + " LIKE ?)"
                + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";
        // Tính/đọc và lưu Bundle chứa tham số truyền cho Fragment để sử dụng ở các bước phía sau.
        String[] args = {String.valueOf(Math.max(0, userId)),
                "%" + keyword + "%", "%" + keyword + "%"};
        // Chạy câu SELECT dạng raw SQL và nhận kết quả thông qua Cursor.
        Cursor cursor = db.rawQuery(query, args);

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (cursor.moveToFirst()) {
            // Bắt đầu vòng lặp do-while để xử lý ít nhất một phần tử.
            do {
                // Khởi tạo giá trị `note` (note) phục vụ cho bước xử lý tiếp theo.
                ProgrammingNote note = new ProgrammingNote();
                // Đọc giá trị của cột hiện tại từ Cursor.
                note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                // Đọc giá trị của cột hiện tại từ Cursor.
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                // Đọc giá trị của cột hiện tại từ Cursor.
                note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                // Đọc giá trị của cột hiện tại từ Cursor.
                note.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                // Thêm phần tử vừa tạo/xử lý vào danh sách `noteList`.
                noteList.add(note);
            // Di chuyển Cursor sang record tiếp theo trong tập kết quả.
            } while (cursor.moveToNext());
        }
        // Đóng Cursor sau khi đọc xong để giải phóng tài nguyên truy vấn.
        cursor.close();
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return noteList;
    }

    /** Xóa note theo cả id và user_id. */
    public boolean deleteNote(int userId, int id) {
        // Mở SQLite ở chế độ ghi; SQLiteOpenHelper sẽ tạo/mở database nếu cần.
        SQLiteDatabase db = this.getWritableDatabase();
        // Thực hiện DELETE để xóa record trong SQLite theo điều kiện chỉ định.
        int deleted = db.delete(TABLE_NOTES,
                COLUMN_ID + "=? AND " + COLUMN_NOTE_USER_ID + "=?",
                new String[]{String.valueOf(id), String.valueOf(Math.max(0, userId))});
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        return deleted > 0;
    }

    /** Update title/content của note đúng user. */
    public void updateNote(int userId, int id, String title, String content) {
        // Mở SQLite ở chế độ ghi; SQLiteOpenHelper sẽ tạo/mở database nếu cần.
        SQLiteDatabase db = this.getWritableDatabase();
        // Tạo ContentValues để đóng gói dữ liệu theo dạng tên-cột → giá-trị trước khi INSERT/UPDATE.
        ContentValues values = new ContentValues();
        // Gán dữ liệu cho COLUMN_TITLE trong ContentValues.
        values.put(COLUMN_TITLE, title);
        // Gán dữ liệu cho COLUMN_CONTENT trong ContentValues.
        values.put(COLUMN_CONTENT, content);
        // Thực hiện UPDATE để thay đổi record đã tồn tại trong SQLite theo điều kiện chỉ định.
        db.update(TABLE_NOTES, values,
                COLUMN_ID + "=? AND " + COLUMN_NOTE_USER_ID + "=?",
                new String[]{String.valueOf(id), String.valueOf(Math.max(0, userId))});
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
    }

    // ===== CÁC HÀM XỬ LÝ QUIZ VÀ EXP =====

    /**
     * Persist kết quả một Quiz vào programming_quiz_attempts.
     * Nếu INSERT thành công thì gọi updateUserExp() để cộng tổng EXP.
     */
    public long saveQuizResult(int userId, String subject, String level, String topic,
                               int totalQuestions, int correctAnswers, int expEarned) {
        // Mở SQLite ở chế độ ghi; SQLiteOpenHelper sẽ tạo/mở database nếu cần.
        SQLiteDatabase db = this.getWritableDatabase();
        // Tạo ContentValues để đóng gói dữ liệu theo dạng tên-cột → giá-trị trước khi INSERT/UPDATE.
        ContentValues values = new ContentValues();
        // Gán dữ liệu cho COLUMN_QUIZ_USER_ID trong ContentValues.
        values.put(COLUMN_QUIZ_USER_ID, userId);
        // Gán dữ liệu cho COLUMN_QUIZ_SUBJECT trong ContentValues.
        values.put(COLUMN_QUIZ_SUBJECT, subject != null ? subject : "Lập trình");
        // Gán dữ liệu cho COLUMN_QUIZ_LEVEL trong ContentValues.
        values.put(COLUMN_QUIZ_LEVEL, level != null ? level : "Cơ bản");
        // Gán dữ liệu cho COLUMN_QUIZ_TOPIC trong ContentValues.
        values.put(COLUMN_QUIZ_TOPIC, topic != null ? topic : "Kiến thức lập trình");
        // Gán dữ liệu cho COLUMN_QUIZ_TOTAL trong ContentValues.
        values.put(COLUMN_QUIZ_TOTAL, Math.max(0, totalQuestions));
        // Gán dữ liệu cho COLUMN_QUIZ_CORRECT trong ContentValues.
        values.put(COLUMN_QUIZ_CORRECT, Math.max(0, correctAnswers));
        // Gán dữ liệu cho COLUMN_QUIZ_EXP_EARNED trong ContentValues.
        values.put(COLUMN_QUIZ_EXP_EARNED, Math.max(0, expEarned));
        // Gán dữ liệu cho COLUMN_QUIZ_TIMESTAMP trong ContentValues.
        values.put(COLUMN_QUIZ_TIMESTAMP, System.currentTimeMillis());

        // Lưu một attempt: tổng câu, số đúng, EXP kiếm được.
        long id = db.insert(TABLE_QUIZ_ATTEMPTS, null, values);
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (id != -1) {
            // Gọi hàm `updateUserExp()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            updateUserExp(userId, subject, expEarned);
        }

        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return id;
    }

    /**
     * Có progress theo (user_id, subject) thì UPDATE; chưa có thì INSERT.
     * Khóa ghép giúp nhiều subject của cùng user tồn tại song song mà không ghi đè EXP.
     */
    private void updateUserExp(int userId, String subject, int expEarned) {
        // Mở SQLite ở chế độ ghi; SQLiteOpenHelper sẽ tạo/mở database nếu cần.
        SQLiteDatabase db = this.getWritableDatabase();
        // Tính/đọc và lưu giá trị `safeSubject` (safe subject) để sử dụng ở các bước phía sau.
        String safeSubject = subject != null ? subject : "Lập trình";

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (expEarned <= 0) {
            // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
            db.close();
            // Dừng hàm tại đây vì không cần tiếp tục các bước phía sau.
            return;
        }

        // Tính/đọc và lưu con trỏ đọc kết quả truy vấn SQLite để sử dụng ở các bước phía sau.
        Cursor cursor = null;
        // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
        try {
            // Dùng API query của SQLite để SELECT dữ liệu và nhận về Cursor.
            cursor = db.query(
                    TABLE_USER_PROGRESS,
                    new String[]{COLUMN_PROGRESS_TOTAL_EXP},
                    COLUMN_PROGRESS_USER_ID + "=? AND " + COLUMN_PROGRESS_SUBJECT + "=?",
                    new String[]{String.valueOf(userId), safeSubject},
                    null, null, null
            );

            // Tạo ContentValues để đóng gói dữ liệu theo dạng tên-cột → giá-trị trước khi INSERT/UPDATE.
            ContentValues values = new ContentValues();
            // Gán dữ liệu cho COLUMN_PROGRESS_UPDATED_AT trong ContentValues.
            values.put(COLUMN_PROGRESS_UPDATED_AT, System.currentTimeMillis());

            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (cursor != null && cursor.moveToFirst()) {
                // Đọc giá trị của cột hiện tại từ Cursor.
                int currentExp = cursor.getInt(0);
                // Gán dữ liệu cho COLUMN_PROGRESS_TOTAL_EXP trong ContentValues.
                values.put(COLUMN_PROGRESS_TOTAL_EXP, currentExp + expEarned);
                // UPDATE thay vì INSERT
                db.update(TABLE_USER_PROGRESS, values,
                        COLUMN_PROGRESS_USER_ID + "=? AND " + COLUMN_PROGRESS_SUBJECT + "=?",
                        new String[]{String.valueOf(userId), safeSubject});
                // Gọi `d()` trên `Log` để thực hiện thao tác tương ứng.
                Log.d(TAG, "Updated EXP for user " + userId + ": " + currentExp + " + " + expEarned + " = " + (currentExp + expEarned));
            // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
            } else {
                // Gán dữ liệu cho COLUMN_PROGRESS_USER_ID trong ContentValues.
                values.put(COLUMN_PROGRESS_USER_ID, userId);
                // Gán dữ liệu cho COLUMN_PROGRESS_SUBJECT trong ContentValues.
                values.put(COLUMN_PROGRESS_SUBJECT, safeSubject);
                // Gán dữ liệu cho COLUMN_PROGRESS_TOTAL_EXP trong ContentValues.
                values.put(COLUMN_PROGRESS_TOTAL_EXP, expEarned);
                // INSERT record EXP đầu tiên của subject này.
                db.insertWithOnConflict(TABLE_USER_PROGRESS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                // Gọi `d()` trên `Log` để thực hiện thao tác tương ứng.
                Log.d(TAG, "Inserted new EXP for user " + userId + ": " + expEarned);
            }
        // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
        } catch (Exception e) {
            // Gọi `e()` trên `Log` để thực hiện thao tác tương ứng.
            Log.e(TAG, "Lỗi update EXP: " + e.getMessage());
        // Khối finally luôn chạy để giải phóng/hoàn tất tài nguyên cần thiết.
        } finally {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (cursor != null) {
                // Đóng Cursor sau khi đọc xong để giải phóng tài nguyên truy vấn.
                cursor.close();
            }
            // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
            db.close();
        }
    }

    /** Lấy total_exp của user theo subject. */
    public int getTotalExp(int userId, String subject) {
        // Mở SQLite ở chế độ đọc để thực hiện truy vấn dữ liệu.
        SQLiteDatabase db = this.getReadableDatabase();
        // Tính/đọc và lưu giá trị `safeSubject` (safe subject) để sử dụng ở các bước phía sau.
        String safeSubject = subject != null ? subject : "Lập trình";

        // Dùng API query của SQLite để SELECT dữ liệu và nhận về Cursor.
        Cursor cursor = db.query(
                TABLE_USER_PROGRESS,
                new String[]{COLUMN_PROGRESS_TOTAL_EXP},
                COLUMN_PROGRESS_USER_ID + "=? AND " + COLUMN_PROGRESS_SUBJECT + "=?",
                new String[]{String.valueOf(userId), safeSubject},
                null, null, null
        );
        // Tính/đọc và lưu giá trị `exp` (exp) để sử dụng ở các bước phía sau.
        int exp = 0;
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (cursor.moveToFirst()) {
            // Đọc giá trị của cột hiện tại từ Cursor.
            exp = cursor.getInt(0);
        }
        // Đóng Cursor sau khi đọc xong để giải phóng tài nguyên truy vấn.
        cursor.close();
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return exp;
    }

    /** Accuracy = SUM(correct_answers) / SUM(total_questions) * 100. */
    public double getAverageAccuracy(int userId, String subject) {
        // Mở SQLite ở chế độ đọc để thực hiện truy vấn dữ liệu.
        SQLiteDatabase db = this.getReadableDatabase();
        // Tính/đọc và lưu giá trị `safeSubject` (safe subject) để sử dụng ở các bước phía sau.
        String safeSubject = subject != null ? subject : "Lập trình";

        // Tính/đọc và lưu giá trị `sql` (sql) để sử dụng ở các bước phía sau.
        String sql = "SELECT COALESCE(SUM(" + COLUMN_QUIZ_CORRECT + "),0), "
                + "COALESCE(SUM(" + COLUMN_QUIZ_TOTAL + "),0) FROM "
                + TABLE_QUIZ_ATTEMPTS + " WHERE "
                + COLUMN_QUIZ_USER_ID + "=? AND "
                + COLUMN_QUIZ_SUBJECT + "=?";

        // Chạy câu SELECT dạng raw SQL và nhận kết quả thông qua Cursor.
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId), safeSubject});
        // Tính/đọc và lưu tỷ lệ trả lời đúng để sử dụng ở các bước phía sau.
        double accuracy = 0;
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (cursor.moveToFirst()) {
            // Đọc giá trị của cột hiện tại từ Cursor.
            int correct = cursor.getInt(0);
            // Đọc giá trị của cột hiện tại từ Cursor.
            int total = cursor.getInt(1);
            // Cập nhật tỷ lệ trả lời đúng theo trạng thái/kết quả vừa tính được.
            accuracy = total == 0 ? 0 : (correct * 100.0) / total;
        }
        // Đóng Cursor sau khi đọc xong để giải phóng tài nguyên truy vấn.
        cursor.close();
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return accuracy;
    }

    /**
     * Accuracy tổng của Programming = tổng câu đúng / tổng câu đã làm, không phụ thuộc tên subject.
     * Dùng cho Home/Progress để dữ liệu seed Java/Python/Android và Quiz mới đều được tính đúng.
     */
    public double getAverageAccuracyAllSubjects(int userId) {
        // Mở SQLite ở chế độ đọc để thực hiện truy vấn dữ liệu.
        SQLiteDatabase db = this.getReadableDatabase();
        // Tính/đọc và lưu giá trị `sql` (sql) để sử dụng ở các bước phía sau.
        String sql = "SELECT COALESCE(SUM(" + COLUMN_QUIZ_CORRECT + "),0), "
                + "COALESCE(SUM(" + COLUMN_QUIZ_TOTAL + "),0) FROM "
                + TABLE_QUIZ_ATTEMPTS + " WHERE " + COLUMN_QUIZ_USER_ID + "=?";

        // Chạy câu SELECT dạng raw SQL và nhận kết quả thông qua Cursor.
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        // Tính/đọc và lưu tỷ lệ trả lời đúng để sử dụng ở các bước phía sau.
        double accuracy = 0;
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (cursor.moveToFirst()) {
            // Đọc giá trị của cột hiện tại từ Cursor.
            int correct = cursor.getInt(0);
            // Đọc giá trị của cột hiện tại từ Cursor.
            int total = cursor.getInt(1);
            // Cập nhật tỷ lệ trả lời đúng theo trạng thái/kết quả vừa tính được.
            accuracy = total == 0 ? 0 : (correct * 100.0) / total;
        }
        // Đóng Cursor sau khi đọc xong để giải phóng tài nguyên truy vấn.
        cursor.close();
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return accuracy;
    }

    /** COUNT số Quiz attempt của user. */
    public int getQuizAttemptCount(int userId, String subject) {
        // Mở SQLite ở chế độ đọc để thực hiện truy vấn dữ liệu.
        SQLiteDatabase db = this.getReadableDatabase();
        // Tính/đọc và lưu giá trị `safeSubject` (safe subject) để sử dụng ở các bước phía sau.
        String safeSubject = subject != null ? subject : "Lập trình";

        // Dùng API query của SQLite để SELECT dữ liệu và nhận về Cursor.
        Cursor cursor = db.query(
                TABLE_QUIZ_ATTEMPTS,
                new String[]{"COUNT(*)"},
                COLUMN_QUIZ_USER_ID + "=? AND " + COLUMN_QUIZ_SUBJECT + "=?",
                new String[]{String.valueOf(userId), safeSubject},
                null, null, null
        );
        // Tính/đọc và lưu giá trị `count` (count) để sử dụng ở các bước phía sau.
        int count = 0;
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (cursor.moveToFirst()) {
            // Đọc giá trị của cột hiện tại từ Cursor.
            count = cursor.getInt(0);
        }
        // Đóng Cursor sau khi đọc xong để giải phóng tài nguyên truy vấn.
        cursor.close();
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return count;
    }

    /** COUNT toàn bộ Quiz attempt của user trong Programming Module. */
    public int getQuizAttemptCountAllSubjects(int userId) {
        // Mở SQLite ở chế độ đọc để thực hiện truy vấn dữ liệu.
        SQLiteDatabase db = this.getReadableDatabase();
        // Dùng API query của SQLite để SELECT dữ liệu và nhận về Cursor.
        Cursor cursor = db.query(
                TABLE_QUIZ_ATTEMPTS,
                new String[]{"COUNT(*)"},
                COLUMN_QUIZ_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );
        // Tính/đọc và lưu giá trị `count` (count) để sử dụng ở các bước phía sau.
        int count = 0;
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (cursor.moveToFirst()) {
            // Đọc giá trị của cột hiện tại từ Cursor.
            count = cursor.getInt(0);
        }
        // Đóng Cursor sau khi đọc xong để giải phóng tài nguyên truy vấn.
        cursor.close();
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return count;
    }

    /** SUM total_exp để Home/Progress tính level. */
    public int getTotalExpAllSubjects(int userId) {
        // Mở SQLite ở chế độ đọc để thực hiện truy vấn dữ liệu.
        SQLiteDatabase db = this.getReadableDatabase();

        // Tính/đọc và lưu giá trị `sql` (sql) để sử dụng ở các bước phía sau.
        String sql = "SELECT COALESCE(SUM(" + COLUMN_PROGRESS_TOTAL_EXP + "),0) FROM "
                + TABLE_USER_PROGRESS + " WHERE "
                + COLUMN_PROGRESS_USER_ID + "=?";

        // Chạy câu SELECT dạng raw SQL và nhận kết quả thông qua Cursor.
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        // Tính/đọc và lưu giá trị `total` (total) để sử dụng ở các bước phía sau.
        int total = 0;
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (cursor.moveToFirst()) {
            // Đọc giá trị của cột hiện tại từ Cursor.
            total = cursor.getInt(0);
        }
        // Đóng Cursor sau khi đọc xong để giải phóng tài nguyên truy vấn.
        cursor.close();
        // Đóng handle database của thao tác hiện tại sau khi xử lý xong.
        db.close();
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return total;
    }

    // Hàm `getUserProgress()`: Trả về giá trị UserProgress hiện đang được đối tượng lưu giữ.
    public Cursor getUserProgress(int userId, String subject) {
        // Mở SQLite ở chế độ đọc để thực hiện truy vấn dữ liệu.
        SQLiteDatabase db = this.getReadableDatabase();
        // Tính/đọc và lưu giá trị `safeSubject` (safe subject) để sử dụng ở các bước phía sau.
        String safeSubject = subject != null ? subject : "Lập trình";

        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return db.query(
                TABLE_USER_PROGRESS,
                null,
                COLUMN_PROGRESS_USER_ID + "=? AND " + COLUMN_PROGRESS_SUBJECT + "=?",
                new String[]{String.valueOf(userId), safeSubject},
                null, null, null
        );
    }
}
