package com.example.asm_final.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.asm_final.R;
import java.util.ArrayList;
import java.util.List;

/*
 * Role: Utility class for App Shell, grouping reusable logic such as user sessions, levels, formatting, or data conversion.
 * Purpose: Moving shared logic into helpers reduces duplication and keeps Activities and Fragments focused on screen coordination.
 */
public class SimplePieChartView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();
    private final List<Slice> slices = new ArrayList<>();
    private float totalValue = 0f;

    public static class Slice {
        float value;
        int color;
        /*
         * Responsibility: Performs the `slice` responsibility of `SimplePieChartView` and returns or updates the result for the caller.
         * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
         */
        public Slice(float value, int color) {
            this.value = value;
            this.color = color;
        }
    }

    /*
     * Responsibility: Initializes the `SimplePieChartView` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public SimplePieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(List<Slice> newSlices) {
        slices.clear();
        totalValue = 0;
        if (newSlices != null) {
            for (Slice s : newSlices) {
                if (s.value > 0) {
                    slices.add(s);
                    totalValue += s.value;
                }
            }
        }
        invalidate();
    }

    /*
     * Responsibility: Handles the Android callback or event `onDraw` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (totalValue <= 0 || slices.isEmpty()) {
            // Draw a grey circle if no data
            paint.setColor(ContextCompat.getColor(getContext(), R.color.progressTrackColor));
            canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, Math.min(getWidth(), getHeight()) / 2f - 20, paint);
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2f - 20;
        rectF.set(width / 2f - radius, height / 2f - radius, width / 2f + radius, height / 2f + radius);

        float startAngle = -90f;
        for (Slice slice : slices) {
            float sweepAngle = (slice.value / totalValue) * 360f;
            paint.setColor(slice.color);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
            
            // Draw white separators
            paint.setColor(ContextCompat.getColor(getContext(), R.color.neumorphic_background));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4f);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
            
            startAngle += sweepAngle;
        }
        
        // Draw inner circle for "Donut" effect
        paint.setColor(ContextCompat.getColor(getContext(), R.color.neumorphic_background));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(width / 2f, height / 2f, radius * 0.6f, paint);
    }
}
