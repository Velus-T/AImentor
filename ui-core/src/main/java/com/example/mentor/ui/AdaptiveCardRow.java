package com.example.mentor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import java.util.IdentityHashMap;
import java.util.Map;

/** Keeps feature cards readable on narrow windows and at larger font scales. */
public final class AdaptiveCardRow extends LinearLayout {
    private final Map<View, LayoutParams> horizontal = new IdentityHashMap<>();
    public AdaptiveCardRow(Context context, AttributeSet attrs) { super(context, attrs); }
    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        int available = MeasureSpec.getSize(widthSpec) - getPaddingLeft() - getPaddingRight();
        float density = getResources().getDisplayMetrics().density;
        float fontScale = getResources().getConfiguration().fontScale;
        boolean stack = available / density < getChildCount() * 156 * Math.max(1f, fontScale);
        setOrientation(stack ? VERTICAL : HORIZONTAL);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            if (!horizontal.containsKey(child)) horizontal.put(child, new LayoutParams(params));
            LayoutParams original = horizontal.get(child);
            params.width = stack ? LayoutParams.MATCH_PARENT : original.width;
            params.height = stack ? LayoutParams.WRAP_CONTENT : original.height;
            params.weight = stack ? 0 : original.weight;
            params.setMarginStart(stack ? 0 : original.getMarginStart());
            params.setMarginEnd(stack ? 0 : original.getMarginEnd());
            params.bottomMargin = stack ? (int) (12 * density) : original.bottomMargin;
        }
        super.onMeasure(widthSpec, heightSpec);
    }
}
