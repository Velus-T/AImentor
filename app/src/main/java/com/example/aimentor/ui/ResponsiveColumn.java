package com.example.aimentor.ui;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.example.aimentor.R;
/** Keeps forms readable on tablets while filling compact windows. */
public class ResponsiveColumn extends LinearLayout {
    private final int maxContentWidth;
    public ResponsiveColumn(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.ResponsiveColumn);
        maxContentWidth = values.getDimensionPixelSize(R.styleable.ResponsiveColumn_contentMaxWidth,
                getResources().getDimensionPixelSize(R.dimen.content_max_width));
        values.recycle();
    }
    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        int available = MeasureSpec.getSize(widthSpec);
        int max = maxContentWidth;
        int base = getResources().getDimensionPixelSize(R.dimen.page_gutter);
        int horizontal = base + Math.max(0, (available - max) / 2);
        setPaddingRelative(horizontal, base, horizontal, base);
        super.onMeasure(widthSpec, heightSpec);
    }
}
