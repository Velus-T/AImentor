package com.example.mentor.data;
import android.content.Context;
public final class ActiveUser {
 private ActiveUser() {}
 public static int get(Context context) {return context==null?0:Math.max(0,context.getApplicationContext().getSharedPreferences("mentor_active_user",0).getInt("id",0));}
 public static void set(Context context,int id) {if(context!=null)context.getApplicationContext().getSharedPreferences("mentor_active_user",0).edit().putInt("id",Math.max(0,id)).apply();}
 public static void clear(Context context) {set(context,0);}
}
