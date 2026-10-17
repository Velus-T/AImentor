package com.example.mentor.data;

import java.util.HashMap;
import java.util.Map;

/** Built once when a history page refreshes; typing does not requery each conversation. */
public final class ConversationSearchIndex {
  private final Map<String, StringBuilder> content = new HashMap<>();
  private final Map<String, String> firstUser = new HashMap<>();

  public void add(String id, String text, boolean user) {
    if (id == null || text == null) return;
    content.computeIfAbsent(id, key -> new StringBuilder()).append(text).append('\n');
    if (user && !text.trim().isEmpty()) firstUser.putIfAbsent(id, text);
  }

  public String text(String id) {
    StringBuilder text = content.get(id);
    return text == null ? "" : text.toString();
  }

  public String firstUser(String id) {
    return firstUser.getOrDefault(id, "");
  }
}
