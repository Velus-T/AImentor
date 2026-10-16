package com.example.mentor.ui;
import android.content.Context;
import java.util.Date;
public final class TimeLabels {
 private TimeLabels() {}
 public static String date(Context c,String raw) {long time=com.example.mentor.data.Timestamps.parse(raw);return time<=0?c.getString(R.string.mentor_time_unknown):android.text.format.DateFormat.getMediumDateFormat(c).format(new Date(time));}
 public static String time(Context c,String raw) {long time=com.example.mentor.data.Timestamps.parse(raw);return time<=0?"":android.text.format.DateFormat.getTimeFormat(c).format(new Date(time));}
 public static String dateTime(Context c,String raw) {String date=date(c,raw),time=time(c,raw);return time.isEmpty()?date:date+" · "+time;}
}
