package com.brightpath.science.util;
import android.content.Context;
import com.example.mentor.data.ActiveUser;
public final class ScienceUserSession {
 private final Context context;
 public ScienceUserSession(Context c) {context=c.getApplicationContext();}
 public int getUserId() {return ActiveUser.get(context);}
 public static void setUserId(Context c,int id) {ActiveUser.set(c,id);}
}
