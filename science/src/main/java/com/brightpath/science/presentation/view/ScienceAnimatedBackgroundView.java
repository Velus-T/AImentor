package com.brightpath.science.presentation.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.brightpath.science.domain.model.ScienceSubject;

import java.util.Random;

/**
 * Lightweight Science-only animated background.
 * It stops in battery saver, when hidden/detached, or when the user disables animation.
 */
public final class ScienceAnimatedBackgroundView extends View {
    private static final int PARTICLE_COUNT = 18;
    private static final long FRAME_DELAY_MS = 33L;

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Particle[] particles = new Particle[PARTICLE_COUNT];
    private final Random random = new Random(2206L);

    private ScienceSubject subject = ScienceSubject.PHYSICS;
    private boolean userEnabled = true;
    private boolean running;
    private long lastFrameNanos;
    private int widthPx = 1;
    private int heightPx = 1;

        /*
 * Responsibility: Initializes ScienceAnimatedBackgroundView and receives or prepares the dependencies/state required by this custom Android View.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceAnimatedBackgroundView(Context context) {
        this(context, null);
    }

        /*
 * Responsibility: Initializes ScienceAnimatedBackgroundView and receives or prepares the dependencies/state required by this custom Android View.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceAnimatedBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setClickable(false);
        setFocusable(false);
        linePaint.setStrokeWidth(dp(0.8f));
        for (int i = 0; i < particles.length; i++) particles[i] = new Particle();
    }

        /*
 * Responsibility: Updates the state or value handled by `setSubject` for ScienceAnimatedBackgroundView.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
public void setSubject(ScienceSubject subject) {
        this.subject = subject == null ? ScienceSubject.PHYSICS : subject;
        rebuildGradient();
        invalidate();
    }

        /*
 * Responsibility: Updates the state or value handled by `setAnimationEnabled` for ScienceAnimatedBackgroundView.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
public void setAnimationEnabled(boolean enabled) {
        userEnabled = enabled;
        updateRunningState();
        invalidate();
    }

        /*
 * Responsibility: Creates the required navigation/action and opens the Science destination handled by `start`.
 * Purpose: Keeps navigation code separate from the business logic executed by the destination screen.
 */
public void start() {
        updateRunningState();
    }

        /*
 * Responsibility: Performs the `stop` operation for ScienceAnimatedBackgroundView within the Science Module.
 * Purpose: Encapsulates this responsibility inside the custom Android View so the code is easier to read, test, maintain, and locate during the review.
 */
public void stop() {
        running = false;
        lastFrameNanos = 0L;
    }

        /*
 * Responsibility: Updates the state or value handled by `updateRunningState` for ScienceAnimatedBackgroundView.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
private void updateRunningState() {
        PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        boolean batterySaver = powerManager != null && powerManager.isPowerSaveMode();
        running = userEnabled && !batterySaver && isAttachedToWindow()
                && getWindowVisibility() == VISIBLE && getVisibility() == VISIBLE;
        lastFrameNanos = 0L;
        if (running) postInvalidateOnAnimation();
    }

        /*
 * Responsibility: Performs the `onAttachedToWindow` operation for ScienceAnimatedBackgroundView within the Science Module.
 * Purpose: Encapsulates this responsibility inside the custom Android View so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateRunningState();
    }

        /*
 * Responsibility: Performs the `onDetachedFromWindow` operation for ScienceAnimatedBackgroundView within the Science Module.
 * Purpose: Encapsulates this responsibility inside the custom Android View so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

        /*
 * Responsibility: Performs the `onWindowVisibilityChanged` operation for ScienceAnimatedBackgroundView within the Science Module.
 * Purpose: Encapsulates this responsibility inside the custom Android View so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        updateRunningState();
    }

        /*
 * Responsibility: Performs the `onSizeChanged` operation for ScienceAnimatedBackgroundView within the Science Module.
 * Purpose: Encapsulates this responsibility inside the custom Android View so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        widthPx = Math.max(1, w);
        heightPx = Math.max(1, h);
        rebuildGradient();
        for (Particle particle : particles) {
            particle.x = random.nextFloat() * widthPx;
            particle.y = random.nextFloat() * heightPx;
            particle.radius = dp(2.5f + random.nextFloat() * 4.5f);
            particle.velocityX = dp(-0.10f + random.nextFloat() * 0.20f);
            particle.velocityY = dp(-0.13f + random.nextFloat() * 0.17f);
            particle.alpha = 22 + random.nextInt(38);
        }
    }

        /*
 * Responsibility: Performs the `onDraw` operation for ScienceAnimatedBackgroundView within the Science Module.
 * Purpose: Encapsulates this responsibility inside the custom Android View so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, widthPx, heightPx, backgroundPaint);

        long now = System.nanoTime();
        float frameScale = lastFrameNanos == 0L
                ? 1f
                : Math.min(2f, Math.max(0.25f, (now - lastFrameNanos) / 33_000_000f));
        lastFrameNanos = now;

        int accent = accentColor(subject);
        float linkLimit = dp(88f);
        float linkLimitSquared = linkLimit * linkLimit;
        for (int i = 0; i < particles.length; i++) {
            Particle particle = particles[i];
            if (running) particle.move(frameScale, widthPx, heightPx);
            particlePaint.setColor(withAlpha(accent, particle.alpha));
            canvas.drawCircle(particle.x, particle.y, particle.radius, particlePaint);

            for (int j = i + 1; j < particles.length; j++) {
                Particle other = particles[j];
                float dx = particle.x - other.x;
                float dy = particle.y - other.y;
                float distanceSquared = dx * dx + dy * dy;
                if (distanceSquared < linkLimitSquared) {
                    float ratio = 1f - (float) Math.sqrt(distanceSquared) / linkLimit;
                    linePaint.setColor(withAlpha(accent, Math.round(18f * ratio)));
                    canvas.drawLine(particle.x, particle.y, other.x, other.y, linePaint);
                }
            }
        }

        if (running && isAttachedToWindow()) postInvalidateDelayed(FRAME_DELAY_MS);
    }

        /*
 * Responsibility: Performs the `rebuildGradient` operation for ScienceAnimatedBackgroundView within the Science Module.
 * Purpose: Encapsulates this responsibility inside the custom Android View so the code is easier to read, test, maintain, and locate during the review.
 */
private void rebuildGradient() {
        if (widthPx <= 0 || heightPx <= 0) return;
        boolean night = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        int[] colors;
        if (night) {
            switch (subject) {
                case CHEMISTRY:
                    colors = new int[]{Color.rgb(4, 30, 35), Color.rgb(8, 47, 55), Color.rgb(7, 19, 23)};
                    break;
                case BIOLOGY:
                    colors = new int[]{Color.rgb(7, 28, 20), Color.rgb(13, 52, 35), Color.rgb(7, 19, 23)};
                    break;
                case PHYSICS:
                default:
                    colors = new int[]{Color.rgb(12, 22, 48), Color.rgb(24, 29, 72), Color.rgb(7, 19, 23)};
                    break;
            }
        } else {
            switch (subject) {
                case CHEMISTRY:
                    colors = new int[]{Color.rgb(224, 249, 250), Color.rgb(236, 253, 245), Color.rgb(243, 248, 248)};
                    break;
                case BIOLOGY:
                    colors = new int[]{Color.rgb(232, 250, 238), Color.rgb(244, 252, 232), Color.rgb(243, 248, 248)};
                    break;
                case PHYSICS:
                default:
                    colors = new int[]{Color.rgb(232, 241, 255), Color.rgb(238, 238, 255), Color.rgb(243, 248, 248)};
                    break;
            }
        }
        backgroundPaint.setShader(new LinearGradient(
                0f, 0f, widthPx, heightPx, colors, null, Shader.TileMode.CLAMP));
    }

        /*
 * Responsibility: Performs the `accentColor` operation for ScienceAnimatedBackgroundView within the Science Module.
 * Purpose: Encapsulates this responsibility inside the custom Android View so the code is easier to read, test, maintain, and locate during the review.
 */
private int accentColor(ScienceSubject currentSubject) {
        boolean night = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        switch (currentSubject) {
            case CHEMISTRY:
                return night ? Color.rgb(103, 232, 249) : Color.rgb(8, 145, 178);
            case BIOLOGY:
                return night ? Color.rgb(134, 239, 172) : Color.rgb(22, 163, 74);
            case PHYSICS:
            default:
                return night ? Color.rgb(165, 180, 252) : Color.rgb(79, 70, 229);
        }
    }

        /*
 * Responsibility: Performs the `withAlpha` operation for ScienceAnimatedBackgroundView within the Science Module.
 * Purpose: Encapsulates this responsibility inside the custom Android View so the code is easier to read, test, maintain, and locate during the review.
 */
private int withAlpha(int color, int alpha) {
        return Color.argb(Math.max(0, Math.min(255, alpha)),
                Color.red(color), Color.green(color), Color.blue(color));
    }

        /*
 * Responsibility: Performs the `dp` operation for ScienceAnimatedBackgroundView within the Science Module.
 * Purpose: Encapsulates this responsibility inside the custom Android View so the code is easier to read, test, maintain, and locate during the review.
 */
private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static final class Particle {
        private float x;
        private float y;
        private float radius;
        private float velocityX;
        private float velocityY;
        private int alpha;

                /*
 * Responsibility: Performs the `move` operation for Particle within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
private void move(float frameScale, int width, int height) {
            x += velocityX * frameScale;
            y += velocityY * frameScale;
            if (x < -radius) x = width + radius;
            else if (x > width + radius) x = -radius;
            if (y < -radius) y = height + radius;
            else if (y > height + radius) y = -radius;
        }
    }
}
