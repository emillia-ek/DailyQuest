package zsk.mobilki.dailyquest.ui.home;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import zsk.mobilki.dailyquest.R;

public class FocusTimerView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();

    private ValueAnimator animator;
    private float progress;
    private float phase;
    private boolean running;

    public FocusTimerView(Context context) {
        super(context);
        init();
    }

    public FocusTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        int blue = getResources().getColor(R.color.cobalt_blue, null);
        int cream = getResources().getColor(R.color.cream_card, null);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(dp(16));
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(cream);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(dp(16));
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(blue);

        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(dp(28));
        glowPaint.setStrokeCap(Paint.Cap.ROUND);
        glowPaint.setColor(adjustAlpha(blue, 0.16f));
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    public void setRunning(boolean running) {
        this.running = running;
        if (running) {
            startAnimator();
        } else {
            stopAnimator();
        }
        invalidate();
    }

    private void startAnimator() {
        if (animator != null && animator.isRunning()) return;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1200);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            phase = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private void stopAnimator() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimator();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float size = Math.min(getWidth(), getHeight());
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = size * 0.39f;
        float pulse = running ? (float) Math.sin(phase * Math.PI * 2) * dp(3) : 0f;

        arcBounds.set(cx - radius - pulse, cy - radius - pulse, cx + radius + pulse, cy + radius + pulse);
        canvas.drawArc(arcBounds, -90, 360, false, trackPaint);
        if (progress > 0f) {
            canvas.drawArc(arcBounds, -90, 360 * progress, false, glowPaint);
            canvas.drawArc(arcBounds, -90, 360 * progress, false, progressPaint);
        }
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int adjustAlpha(int color, float factor) {
        return Color.argb(Math.round(Color.alpha(color) * factor), Color.red(color), Color.green(color), Color.blue(color));
    }
}
