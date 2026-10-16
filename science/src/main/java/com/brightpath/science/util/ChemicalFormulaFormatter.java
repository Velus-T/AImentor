package com.brightpath.science.util;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChemicalFormulaFormatter {
    private static final Pattern FORMULA = Pattern.compile("(?<![A-Za-z])(?:(?:[A-Z][a-z]?\\d*){2,}|[A-Z][a-z]?\\d+)(?:[+-]|\\d+[+-])?");
    private static final Pattern DIGIT = Pattern.compile("\\d+");
    private static final Pattern CHARGE = Pattern.compile("(?:\\d+)?[+-]$");

        /*
 * Responsibility: Normalizes or formats the value handled by `format` into a stable representation.
 * Purpose: Improves consistency for display, comparison, caching, storage, or AI prompt construction.
 */
public CharSequence format(String text) {
        if (text == null || text.isEmpty()) return "";

        // 1. Pre-process to clean up messy LaTeX/Text fragments from AI
        String processed = text;
        
        // Handle nested or broken \text{...} and LaTeX markers
        // Remove \text{ and the closing } for those specific patterns
        processed = processed.replaceAll("\\\\text\\{", "");
        
        // Remove LaTeX math markers
        processed = processed.replace("$", "");
        
        // Handle chemical arrows
        processed = processed.replace("\\rightarrow", "→")
                             .replace("\\longrightarrow", "⟶")
                             .replace("\\rightleftharpoons", "⇌");

        // Convert LaTeX sub/sup to HTML tags
        // Handle both _{2} and _2
        processed = processed.replaceAll("_\\{([^}]*)\\}", "<sub>$1</sub>");
        processed = processed.replaceAll("_([0-9a-z])", "<sub>$1</sub>");
        
        processed = processed.replaceAll("\\^\\{([^}]*)\\}", "<sup>$1</sup>");
        processed = processed.replaceAll("\\^([0-9a-z+-])", "<sup>$1</sup>");

        // 2. Support basic Markdown bold/italic from AI
        processed = processed.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>")
                             .replaceAll("\\*(.*?)\\*", "<i>$1</i>")
                             .replace("\n", "<br/>");

        // Clean up any remaining stray braces that were part of \text{}
        // Only if they seem to be orphaned from a \text cleanup
        // We do a simple approach: if we have H} we clean it.
        // A better way is to match the earlier \text{ removal.
        processed = processed.replaceAll("([A-Z][a-z]?)\\}", "$1");
        processed = processed.replaceAll("\\(([gls])\\)\\}", "($1)");

        // 3. Convert to Spanned using HTML
        CharSequence spanned = Html.fromHtml(processed, Html.FROM_HTML_MODE_COMPACT);

        SpannableStringBuilder result = new SpannableStringBuilder(spanned);
        String plainText = result.toString();

        // 4. Fallback for plain formulas like H2O (not already subbed)
        Matcher formulaMatcher = FORMULA.matcher(plainText);
        while (formulaMatcher.find()) {
            String token = formulaMatcher.group();
            int tokenStart = formulaMatcher.start();
            
            Matcher chargeMatcher = CHARGE.matcher(token);
            int chargeStart = token.length();
            if (chargeMatcher.find()) {
                chargeStart = chargeMatcher.start();
                int s = tokenStart + chargeStart;
                int e = tokenStart + token.length();
                if (result.getSpans(s, e, SuperscriptSpan.class).length == 0) {
                    applySuperscript(result, s, e);
                }
            }
            
            Matcher digitMatcher = DIGIT.matcher(token.substring(0, chargeStart));
            while (digitMatcher.find()) {
                int start = tokenStart + digitMatcher.start();
                int end = tokenStart + digitMatcher.end();
                if (result.getSpans(start, end, SubscriptSpan.class).length == 0 &&
                    result.getSpans(start, end, SuperscriptSpan.class).length == 0) {
                    result.setSpan(new SubscriptSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    result.setSpan(new RelativeSizeSpan(0.75f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return result;
    }

        /*
 * Responsibility: Applies the transformation/style/rule represented by `applySuperscript` to the current Science data or UI state.
 * Purpose: Encapsulates the change in one place so it can be reused consistently.
 */
private void applySuperscript(SpannableStringBuilder result, int start, int end) {
        result.setSpan(new SuperscriptSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        result.setSpan(new RelativeSizeSpan(0.75f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
