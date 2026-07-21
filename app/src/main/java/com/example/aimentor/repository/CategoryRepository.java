package com.example.aimentor.repository;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;

import com.example.aimentor.databases.SqliteDbHelper;
import com.example.aimentor.models.CategoryModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CategoryRepository extends SqliteDbHelper {
    public CategoryRepository(@Nullable Context context) {
        super(context);
    }

    private String getCurrentDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date()); // lay ra duoc ngay thang hien tai va gio phut giay
    }

    public long saveNewCategory(String name, String description){
        String currentDate = getCurrentDate();
        ContentValues values = new ContentValues();
        values.put(NAME_CATEGORY, name);
        values.put(DESCRIPTION_CATEGORY, description);
        values.put(STATUS_CATEGORY, 1);
        values.put(CREATED_AT, currentDate);
        SQLiteDatabase db = this.getWritableDatabase();
        long insert = db.insert(CATEGORY_TABLE, null, values);
        db.close();
        return  insert;
    }

    @SuppressLint("Range")
    public ArrayList<CategoryModel> getAllCategories(){
        ArrayList<CategoryModel> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + CATEGORY_TABLE + " ORDER BY " + CREATED_AT + " DESC ", null);
        if (data.getCount() > 0) {
            if (data.moveToFirst()){
                do {
                    categories.add(
                            new CategoryModel(
                                    data.getInt(data.getColumnIndex(ID_CATEGORY)),
                                    data.getString(data.getColumnIndex(NAME_CATEGORY)),
                                    data.getString(data.getColumnIndex(DESCRIPTION_CATEGORY)),
                                    data.getInt(data.getColumnIndex(STATUS_CATEGORY)),
                                    data.getString(data.getColumnIndex(CREATED_AT)),
                                    data.getString(data.getColumnIndex(UPDATED_AT))
                            )
                    );
                } while (data.moveToNext());
            }
        }
        db.close();
        data.close();
        return categories;
    }
}
