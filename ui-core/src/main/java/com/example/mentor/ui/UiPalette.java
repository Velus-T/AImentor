package com.example.mentor.ui;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.widget.Button;

/** Shared control styling, independent of the legacy theme selected by a subject. */
public final class UiPalette {
  private UiPalette() {}

  public static void primary(Button button) {
    style(button, true);
  }

  public static void secondary(Button button) {
    style(button, false);
  }

  private static void style(Button button, boolean primary) {
    android.content.Context c = button.getContext();
    float density = button.getResources().getDisplayMetrics().density;
    GradientDrawable background = new GradientDrawable();
    background.setCornerRadius(12 * density);
    background.setColor(
        button
            .getResources()
            .getColorStateList(
                primary
                    ? R.color.mentor_button_background
                    : R.color.mentor_button_secondary_background,
                c.getTheme()));
    if (!primary)
      background.setStroke(
          Math.max(1, Math.round(density)),
          button.getResources().getColor(R.color.mentor_quiz_border, c.getTheme()));
    button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x227F9CD1), background, null));
    button.setBackgroundTintList(null);
    button.setTextColor(
        button
            .getResources()
            .getColorStateList(
                primary ? R.color.mentor_button_text : R.color.mentor_button_secondary_text,
                c.getTheme()));
    button.setAllCaps(false);
    button.setMinHeight(Math.round(48 * density));
    button.setPadding(
        Math.round(16 * density),
        Math.round(10 * density),
        Math.round(16 * density),
        Math.round(10 * density));
  }

  public static void text(Button button) {
    android.util.TypedValue value = new android.util.TypedValue();
    button
        .getContext()
        .getTheme()
        .resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, value, true);
    button.setBackgroundResource(value.resourceId);
    button.setBackgroundTintList(null);
    button.setTextColor(
        button.getResources().getColor(R.color.mentor_quiz_accent, button.getContext().getTheme()));
    button.setAllCaps(false);
  }
}
