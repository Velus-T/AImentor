package com.example.asm_final.utils;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/** Scrollable forms remain readable on compact screens and centered on tablets. */
public final class AuthColumn extends LinearLayout {
    public AuthColumn(Context context, AttributeSet attributes) { super(context, attributes); }
    @Override protected void onMeasure(int width, int height) {
        float density = getResources().getDisplayMetrics().density;
        int available = MeasureSpec.getSize(width);
        int max = Math.round(560 * density), gutter = Math.round(20 * density);
        int horizontal = gutter + Math.max(0, (available - max) / 2);
        setPaddingRelative(horizontal, Math.round(24 * density), horizontal, Math.round(24 * density));
        super.onMeasure(width, height);
    }
}
