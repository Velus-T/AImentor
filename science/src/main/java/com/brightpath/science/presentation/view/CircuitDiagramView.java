package com.brightpath.science.presentation.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.brightpath.science.R;

/** Topology-aware schematic: wires terminate at components instead of bypassing them. */
public final class CircuitDiagramView extends View {
  private final Paint wire = new Paint(3), label = new Paint(3), lamp = new Paint(3);
  private boolean parallel, closed = true, energized = true;

  public CircuitDiagramView(Context c, AttributeSet attrs) {
    super(c, attrs);
    wire.setColor(ContextCompat.getColor(c, R.color.science_text_primary));
    wire.setStrokeWidth(2.5f);
    wire.setStyle(Paint.Style.STROKE);
    label.setColor(ContextCompat.getColor(c, R.color.science_text_primary));
    label.setTextSize(16);
    label.setTextAlign(Paint.Align.CENTER);
  }

  public void setParallel(boolean value) {
    parallel = value;
    invalidate();
  }

  public void setSwitchClosed(boolean value) {
    closed = value;
    invalidate();
  }

  public void setEnergized(boolean value) {
    energized = value;
    invalidate();
  }

  private void line(Canvas c, float x1, float y1, float x2, float y2) {
    c.drawLine(x1, y1, x2, y2, wire);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    float scale = Math.min(getWidth() / 360f, getHeight() / 300f);
    if (scale <= 0) return;
    canvas.save();
    canvas.translate((getWidth() - 360 * scale) / 2, (getHeight() - 300 * scale) / 2);
    canvas.scale(scale, scale);
    line(canvas, 42, 50, 156, 50);
    line(canvas, 204, 50, 318, 50);
    canvas.drawCircle(156, 50, 3, wire);
    canvas.drawCircle(204, 50, 3, wire);
    line(canvas, 159, 50, closed ? 201 : 195, closed ? 50 : 28);
    canvas.drawText(
        getResources()
            .getString(
                closed
                    ? R.string.science_circuit_closed_short
                    : R.string.science_circuit_open_short),
        180,
        20,
        label);
    line(canvas, 42, 50, 42, 140);
    line(canvas, 42, 160, 42, 250);
    line(canvas, 22, 140, 62, 140);
    line(canvas, 30, 160, 54, 160);
    canvas.drawText("+", 16, 136, label);
    canvas.drawText("−", 16, 174, label);
    canvas.drawText("U", 78, 155, label);
    if (parallel) {
      line(canvas, 318, 50, 318, 220);
      line(canvas, 42, 250, 112, 250);
      line(canvas, 112, 130, 112, 250);
      line(canvas, 112, 130, 183, 130);
      resistor(canvas, 215, 130);
      line(canvas, 247, 130, 318, 130);
      line(canvas, 112, 220, 197, 220);
      lamp(canvas, 215, 220);
      line(canvas, 233, 220, 318, 220);
      canvas.drawText("R1", 215, 105, label);
      canvas.drawText("R2", 215, 258, label);
      for (float y : new float[] {130, 220}) {
        canvas.drawCircle(112, y, 3, wire);
        canvas.drawCircle(318, y, 3, wire);
      }
    } else {
      line(canvas, 318, 50, 318, 250);
      line(canvas, 42, 250, 98, 250);
      resistor(canvas, 130, 250);
      line(canvas, 162, 250, 232, 250);
      lamp(canvas, 250, 250);
      line(canvas, 268, 250, 318, 250);
      canvas.drawText("R1", 130, 282, label);
      canvas.drawText("R2", 250, 282, label);
    }
    canvas.restore();
  }

  private void resistor(Canvas c, float x, float y) {
    c.drawRect(x - 32, y - 10, x + 32, y + 10, wire);
  }

  private void lamp(Canvas c, float x, float y) {
    lamp.setColor(
        ContextCompat.getColor(
            getContext(),
            closed && energized ? R.color.science_warning : R.color.science_surface_variant));
    c.drawCircle(x, y, 18, lamp);
    c.drawCircle(x, y, 18, wire);
    line(c, x - 11, y - 11, x + 11, y + 11);
    line(c, x + 11, y - 11, x - 11, y + 11);
  }
}
