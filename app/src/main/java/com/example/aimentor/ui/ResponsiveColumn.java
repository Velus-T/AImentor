package com.example.aimentor.ui;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.example.aimentor.R;
/** Keeps forms readable on tablets while filling compact windows. */
public class ResponsiveColumn extends LinearLayout {
    public ResponsiveColumn(Context context, AttributeSet attrs) { super(context, attrs); }
    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        int available = MeasureSpec.getSize(widthSpec);
        int max = getResources().getDimensionPixelSize(R.dimen.content_max_width);
        int base = getResources().getDimensionPixelSize(R.dimen.page_gutter);
        int horizontal = base + Math.max(0, (available - max) / 2);
        setPaddingRelative(horizontal, base, horizontal, base);
        super.onMeasure(widthSpec, heightSpec);
    }
}
