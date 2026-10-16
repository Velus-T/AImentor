package com.example.asm_final.accounts;
import android.content.Context;
import android.database.Cursor;
import com.example.asm_final.activities.database.DatabaseHelper;
public final class LocalUserProfileRepository implements UserProfileRepository {
    private final Context context;
    public LocalUserProfileRepository(Context context) { this.context=context.getApplicationContext(); }
    public UserProfile load(String username) {
        try(DatabaseHelper db=new DatabaseHelper(context);Cursor c=db.getUserData(username)) {
            if(c==null||!c.moveToFirst()) return null;
            return new UserProfile(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_FULLNAME)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS)),c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL)),c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_AVATAR)));
        }
    }
    public boolean update(String username,UserProfile profile,String newPassword) {
        try(DatabaseHelper db=new DatabaseHelper(context)) {
            return db.updateUser(username,profile.fullName,profile.address,profile.phone,profile.email,newPassword,profile.avatar);
        }
    }
}
