package com.example.asm_final.utils;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridLayout;

/** Four original module cards wrap into a scrollable column on compact windows. */
public final class AdaptiveSubjectGrid extends GridLayout {
    public AdaptiveSubjectGrid(Context context, AttributeSet attrs) { super(context, attrs); }
    @Override protected void onMeasure(int width, int height) {
        float density = getResources().getDisplayMetrics().density;
        int columns = MeasureSpec.getSize(width) / density >= 600 ? 2 : 1;
        setColumnCount(columns);
        setRowCount((getChildCount() + columns - 1) / columns);
        for (int i = 0; i < getChildCount(); i++) {
            LayoutParams params = (LayoutParams) getChildAt(i).getLayoutParams();
            params.columnSpec = spec(i % columns, 1f);
            params.rowSpec = spec(i / columns);
            params.width = 0;
            params.height = LayoutParams.WRAP_CONTENT;
        }
        super.onMeasure(width, height);
    }
}
