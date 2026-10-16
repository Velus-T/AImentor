package com.example.historymodule.util;
import android.content.Context;
import com.example.mentor.data.ActiveUser;
public final class HistoryUserSession {
 private HistoryUserSession() {}
 public static void setUserId(Context c,int id) {ActiveUser.set(c,id);}
 public static int getUserId(Context c) {return ActiveUser.get(c);}
}
