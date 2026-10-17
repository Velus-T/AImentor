package com.brightpath.science.util;

import java.util.Locale;

/** Stable storage codes, with aliases for notes saved using translated labels. */
public final class ScienceNoteSubject {
  private static final String[] CODES = {"physics", "chemistry", "biology", "other"};
  private ScienceNoteSubject() {}
  public static int index(String value) {
    String s = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    if (s.equals("physics") || s.equals("vật lý") || s.equals("vật lí")) return 0;
    if (s.equals("chemistry") || s.equals("hóa học") || s.equals("hoá học")) return 1;
    if (s.equals("biology") || s.equals("sinh học")) return 2;
    return 3;
  }
  public static String code(int index) { return CODES[Math.max(0, Math.min(3, index))]; }
  public static String normalize(String value) { return code(index(value)); }
}
