package com.example.aimentor.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SqliteDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "studyAI";
    private static final int DB_VERSION = 2;

    // dinh nghia thong tin bang "Users" luu tru tai khoan
    protected static final String TABLE_USERS = "users"; // ten bang du lieu
    // dinh nghia cac cot nam trong bang users do
    protected  static final String ID_USER = "id";
    protected static final String USERNAME_USER = "username";
    protected  static final String PASSWORD_USER = "password";
    protected  static final String EMAIL_USER = "email";
    protected static final String PHONE_USER = "phone";
    protected static final String ROLE_USER = "role";

    // dinh nghia thong tin bang Category
    protected static final String CATEGORY_TABLE = "categories";
    protected static final String ID_CATEGORY = "id";
    protected static final String NAME_CATEGORY = "name_category";
    protected static final String STATUS_CATEGORY = "status_category";
    protected static final String DESCRIPTION_CATEGORY = "description_category";

    // ngay tao du lieu va ngay cap nhat du lieu
    protected static final String CREATED_AT = "createdAt";
    protected static final String UPDATED_AT = "updatedAt";

    public SqliteDbHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            com.example.aimentor.auth.DemoAccounts.seed(db);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String usersTable = " CREATE TABLE " + TABLE_USERS + " ( "
                            + ID_USER + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + USERNAME_USER + " VARCHAR(30) NOT NULL, "
                            + PASSWORD_USER + " VARCHAR(200) NOT NULL, "
                            + EMAIL_USER    + " VARCHAR(60) NOT NULL, "
                            + PHONE_USER    + " VARCHAR(20), "
                            + ROLE_USER     + " TINYINT DEFAULT(1), "
                            + CREATED_AT    + " DATETIME, "
                            + UPDATED_AT    + " DATETIME ) ";

        String categoryTable = " CREATE TABLE " + CATEGORY_TABLE + " ( "
                               + ID_CATEGORY + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                               + NAME_CATEGORY + " VARCHAR(100) NOT NULL, "
                               + DESCRIPTION_CATEGORY + " VARCHAR(200), "
                               + STATUS_CATEGORY + " TINYINT DEFAULT(1), "
                               + CREATED_AT    + " DATETIME, "
                               + UPDATED_AT    + " DATETIME ) ";

        db.execSQL(usersTable); // thuc thi cau lenh SQL va tao bang du lieu
        db.execSQL(categoryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion){
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS); // xoa bang
            db.execSQL("DROP TABLE IF EXISTS " + CATEGORY_TABLE);
            onCreate(db); // tao lai bang du lieu
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
