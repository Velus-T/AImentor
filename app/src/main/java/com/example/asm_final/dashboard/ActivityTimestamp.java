package com.example.asm_final.dashboard;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.util.*;
public final class ActivityTimestamp {
    private ActivityTimestamp() {}
    public static long parse(String value) {
        if(value==null||value.trim().isEmpty()) return 0;
        String text=value.trim();
        try {
            long number=Long.parseLong(text);
            return number<=0?0:(number<100000000000L?number*1000:number);
        } catch(NumberFormatException ignored) {
            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ROOT);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));format.setLenient(false);
            ParsePosition position=new ParsePosition(0);Date parsed=format.parse(text,position);
            return parsed==null||position.getIndex()!=text.length()?0:Math.max(0,parsed.getTime());
        }
    }
}
