package com.example.aimentor.repository;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;

import com.example.aimentor.databases.SqliteDbHelper;
import com.example.aimentor.models.UserModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserRepository extends SqliteDbHelper {
    public UserRepository(@Nullable Context context) {
        super(context);
    }

    private String getCurrentDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date()); // lay ra duoc ngay thang hien tai va gio phut giay
    }

    public long saveUserAccount(String username, String password, String email, String phone){
        String currentDate = getCurrentDate();
        ContentValues values = new ContentValues();
        values.put(USERNAME_USER, username);
        values.put(PASSWORD_USER, password);
        values.put(EMAIL_USER, email);
        values.put(PHONE_USER, phone);
        values.put(ROLE_USER, 1);
        values.put(CREATED_AT, currentDate);
        SQLiteDatabase db = this.getWritableDatabase();
        long insert = db.insert(TABLE_USERS, null, values);
        db.close();
        return  insert;
    }

    @SuppressLint("Range")
    public UserModel loginUser(String username, String password){
        UserModel user = new UserModel();
        // tao 1 mot mang chua thong tin du lieu cua tai khoan
        String[] cols = {ID_USER, USERNAME_USER, EMAIL_USER, PHONE_USER, ROLE_USER};
        // Select id, username, email, phone, role from Users where username = ? and password = ?
        String condition = USERNAME_USER + " =? AND " + PASSWORD_USER + " =? ";
        String[] params = { username, password };
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor data = db.query(TABLE_USERS, cols, condition, params, null, null, null);
        if (data.getCount() > 0){
            data.moveToFirst();
            user.setId(data.getInt(data.getColumnIndex(ID_USER)));
            user.setUsername(data.getString(data.getColumnIndex(USERNAME_USER)));
            user.setEmail(data.getString(data.getColumnIndex(EMAIL_USER)));
            user.setPhone(data.getString(data.getColumnIndex(PHONE_USER)));
            user.setRole(data.getInt(data.getColumnIndex(ROLE_USER)));
        }
        data.close();
        db.close();
        return user;
    }
}
