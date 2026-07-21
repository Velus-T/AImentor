package com.example.aimentor.science.presentation.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public final class CircuitDiagramView extends View {
    private final Paint wirePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lampPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean parallel;
    private boolean switchClosed = true;

    public CircuitDiagramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        wirePaint.setColor(Color.rgb(25, 70, 110));
        wirePaint.setStrokeWidth(dp(3));
        wirePaint.setStyle(Paint.Style.STROKE);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(dp(14));
        lampPaint.setColor(Color.rgb(255, 193, 7));
        lampPaint.setStyle(Paint.Style.FILL);
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
        invalidate();
    }

    public void setSwitchClosed(boolean switchClosed) {
        this.switchClosed = switchClosed;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float left = dp(35);
        float right = getWidth() - dp(35);
        float top = dp(55);
        float bottom = getHeight() - dp(55);

        drawBattery(canvas, left, (top + bottom) / 2);
        drawSwitch(canvas, (left + right) / 2, top);
        canvas.drawLine(left, top, (left + right) / 2 - dp(35), top, wirePaint);
        canvas.drawLine((left + right) / 2 + dp(35), top, right, top, wirePaint);
        canvas.drawLine(left, top, left, (top + bottom) / 2 - dp(26), wirePaint);
        canvas.drawLine(left, (top + bottom) / 2 + dp(26), left, bottom, wirePaint);
        canvas.drawLine(left, bottom, right, bottom, wirePaint);
        canvas.drawLine(right, top, right, bottom, wirePaint);

        if (parallel) drawParallelComponents(canvas, left, right, top, bottom);
        else drawSeriesComponents(canvas, left, right, bottom);

        canvas.drawText("Battery", left + dp(12), (top + bottom) / 2 + dp(5), textPaint);
        canvas.drawText(switchClosed ? "Closed switch" : "Open switch",
                (left + right) / 2 - dp(42), top - dp(16), textPaint);
    }

    private void drawSeriesComponents(Canvas canvas, float left, float right, float bottom) {
        float mid = (left + right) / 2;
        drawResistor(canvas, mid - dp(75), bottom);
        drawLamp(canvas, mid + dp(70), bottom);
        canvas.drawText("Resistor", mid - dp(105), bottom - dp(22), textPaint);
        canvas.drawText("Lamp", mid + dp(48), bottom - dp(22), textPaint);
    }

    private void drawParallelComponents(Canvas canvas, float left, float right, float top, float bottom) {
        float branchTop = top + dp(95);
        float branchBottom = bottom - dp(85);
        canvas.drawLine(left + dp(65), branchTop, right - dp(65), branchTop, wirePaint);
        canvas.drawLine(left + dp(65), branchBottom, right - dp(65), branchBottom, wirePaint);
        canvas.drawLine(left + dp(65), top, left + dp(65), bottom, wirePaint);
        canvas.drawLine(right - dp(65), top, right - dp(65), bottom, wirePaint);
        drawResistor(canvas, (left + right) / 2, branchTop);
        drawLamp(canvas, (left + right) / 2, branchBottom);
        canvas.drawText("Branch 1: resistor", left + dp(85), branchTop - dp(18), textPaint);
        canvas.drawText("Branch 2: lamp", left + dp(85), branchBottom - dp(18), textPaint);
    }

    private void drawBattery(Canvas canvas, float x, float y) {
        canvas.drawLine(x - dp(12), y - dp(25), x - dp(12), y + dp(25), wirePaint);
        canvas.drawLine(x + dp(10), y - dp(16), x + dp(10), y + dp(16), wirePaint);
    }

    private void drawSwitch(Canvas canvas, float x, float y) {
        canvas.drawCircle(x - dp(28), y, dp(4), wirePaint);
        canvas.drawCircle(x + dp(28), y, dp(4), wirePaint);
        if (switchClosed) canvas.drawLine(x - dp(24), y, x + dp(24), y, wirePaint);
        else canvas.drawLine(x - dp(24), y, x + dp(18), y - dp(25), wirePaint);
    }

    private void drawResistor(Canvas canvas, float x, float y) {
        RectF rect = new RectF(x - dp(45), y - dp(12), x + dp(45), y + dp(12));
        canvas.drawRect(rect, wirePaint);
    }

    private void drawLamp(Canvas canvas, float x, float y) {
        canvas.drawCircle(x, y, dp(20), lampPaint);
        canvas.drawCircle(x, y, dp(20), wirePaint);
        canvas.drawLine(x - dp(13), y - dp(13), x + dp(13), y + dp(13), wirePaint);
        canvas.drawLine(x + dp(13), y - dp(13), x - dp(13), y + dp(13), wirePaint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
