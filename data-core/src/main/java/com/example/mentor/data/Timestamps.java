package com.example.mentor.data;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

public final class Timestamps {
  private Timestamps() {}

  public static long parse(String value) {
    if (value == null || value.trim().isEmpty()) return 0;
    String text = value.trim();
    try {
      long number = Long.parseLong(text);
      return number <= 0 ? 0 : (number < 100000000000L ? number * 1000 : number);
    } catch (NumberFormatException ignored) {
      String[] formats = {
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd",
        "dd/MM/yyyy HH:mm:ss",
        "dd/MM/yyyy HH:mm",
        "dd/MM/yyyy",
        "dd-MM-yyyy",
        "MM/dd/yyyy"
      };
      for (String pattern : formats) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ROOT);
        format.setLenient(false);
        format.setTimeZone(
            pattern.startsWith("yyyy") ? TimeZone.getTimeZone("UTC") : TimeZone.getDefault());
        ParsePosition position = new ParsePosition(0);
        Date parsed = format.parse(text, position);
        if (parsed != null && position.getIndex() == text.length())
          return Math.max(0, parsed.getTime());
      }
      return 0;
    }
  }
}
