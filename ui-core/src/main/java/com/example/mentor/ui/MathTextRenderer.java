package com.example.mentor.ui;

import android.webkit.WebView;

/** Locally bundled KaTeX; saved formulas never require a CDN or an API request. */
public final class MathTextRenderer {
  private MathTextRenderer() {}

  public static void render(WebView web, String text, int size) {
    web.setBackgroundColor(android.graphics.Color.TRANSPARENT);
    web.getSettings().setJavaScriptEnabled(true);
    web.getSettings().setAllowFileAccess(false);
    web.getSettings().setAllowContentAccess(false);
    web.getSettings().setBlockNetworkLoads(true);
    web.getSettings()
        .setTextZoom(Math.round(100 * web.getResources().getConfiguration().fontScale));
    String escaped =
        android.text.TextUtils.htmlEncode(text == null ? "" : text).replace("\n", "<br>");
    String color =
        String.format(
            java.util.Locale.ROOT,
            "#%06X",
            0xFFFFFF
                & web.getResources()
                    .getColor(R.color.mentor_quiz_text, web.getContext().getTheme()));
    web.loadDataWithBaseURL(
        "file:///android_asset/katex/",
        "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport'"
            + " content='width=device-width,initial-scale=1'><link rel='stylesheet'"
            + " href='katex.min.css'><script defer src='katex.min.js'></script><script defer"
            + " src='contrib/auto-render.min.js'"
            + " onload=\"renderMathInElement(document.body,{delimiters:[{left:'$$',right:'$$',display:true},{left:'$',right:'$',display:false}],throwOnError:false})\"></script><style>body{margin:0;padding:8px"
            + " 0;color:"
            + color
            + ";font:"
            + size
            + "px sans-serif;overflow-wrap:anywhere}.katex-display{overflow-x:auto}</style></head><body>"
            + escaped
            + "</body></html>",
        "text/html",
        "UTF-8",
        null);
  }
}
