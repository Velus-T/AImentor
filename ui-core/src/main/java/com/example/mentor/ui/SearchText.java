package com.example.mentor.ui;

/** Predictable local search, including Vietnamese without accents. */
public final class SearchText {
  private SearchText() {}

  public static boolean contains(String text, String query) {
    return normalize(text).contains(normalize(query));
  }

  private static String normalize(String value) {
    if (value == null) return "";
    return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
        .replaceAll("\\p{M}+", "")
        .toLowerCase(java.util.Locale.ROOT)
        .replace('đ', 'd')
        .trim();
  }
}
