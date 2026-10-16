package com.example.asm_final.accounts;
import android.content.Context;
import com.example.asm_final.activities.database.DatabaseHelper;
public final class LocalAccountRepository implements AccountRepository {
    private final Context context;
    public LocalAccountRepository(Context context) { this.context=context.getApplicationContext(); }
    public boolean authenticate(String username,String password) {
        try(DatabaseHelper db=new DatabaseHelper(context)) { return db.checkLogin(username,password); }
    }
    public long register(String username,String password,String email,String phone) {
        try(DatabaseHelper db=new DatabaseHelper(context)) { return db.addUser(username,password,email,phone); }
    }
    public int userId(String username) {
        try(DatabaseHelper db=new DatabaseHelper(context)) { return db.getUserId(username); }
    }
}
