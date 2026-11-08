package zsk.mobilki.dailyquest;

import android.content.Context;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import zsk.mobilki.dailyquest.data.repository.QuestRepository;

public class FocusSessionManager {

    public enum FocusState {
        IDLE,
        RUNNING,
        PAUSED
    }

    public interface OnTimerChangeListener {
        void onTick(long millisRemaining, float progress);
        void onFinish(int xpReward);
        void onStateChanged(FocusState state);
    }

    public static final long FOCUS_DURATION_MILLIS = 25 * 60 * 1000L;

    private static FocusSessionManager instance;
    private final Context appContext;
    private final QuestRepository repository;
    private final List<OnTimerChangeListener> listeners = new ArrayList<>();

    private CountDownTimer countDownTimer;
    private FocusState currentState = FocusState.IDLE;
    private long millisRemaining = FOCUS_DURATION_MILLIS;

    private FocusSessionManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.repository = new QuestRepository(this.appContext);
    }

    public static synchronized FocusSessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new FocusSessionManager(context);
        }
        return instance;
    }

    public void registerListener(OnTimerChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            listener.onStateChanged(currentState);
            listener.onTick(millisRemaining, getProgress());
        }
    }

    public void unregisterListener(OnTimerChangeListener listener) {
        listeners.remove(listener);
    }

    public FocusState getCurrentState() {
        return currentState;
    }

    public long getMillisRemaining() {
        return millisRemaining;
    }

    public float getProgress() {
        return 1f - (millisRemaining / (float) FOCUS_DURATION_MILLIS);
    }

    public synchronized void startOrResume() {
        if (currentState == FocusState.RUNNING) return;

        currentState = FocusState.RUNNING;
        notifyStateChanged();

        countDownTimer = new CountDownTimer(millisRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisRemaining = millisUntilFinished;
                notifyTick();
            }

            @Override
            public void onFinish() {
                completeSession(FOCUS_DURATION_MILLIS, true);
            }
        }.start();
    }

    public synchronized void pause() {
        if (currentState != FocusState.RUNNING) return;

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        currentState = FocusState.PAUSED;
        notifyStateChanged();
        notifyTick();
    }

    public synchronized void stop() {
        long elapsedMillis = FOCUS_DURATION_MILLIS - millisRemaining;
        if (elapsedMillis > 0) {
            completeSession(elapsedMillis, false);
            return;
        }

        reset();
    }

    public synchronized void reset() {
        cancelTimer();
        currentState = FocusState.IDLE;
        millisRemaining = FOCUS_DURATION_MILLIS;
        notifyStateChanged();
        notifyTick();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private synchronized void completeSession(long durationMillis, boolean fullSession) {
        cancelTimer();
        currentState = FocusState.IDLE;
        millisRemaining = FOCUS_DURATION_MILLIS;

        // xp i coins
        int rewardXp = repository.completeFocusSession(durationMillis);
        vibrate();

        for (OnTimerChangeListener listener : new ArrayList<>(listeners)) {
            listener.onFinish(rewardXp);
            listener.onStateChanged(currentState);
            listener.onTick(millisRemaining, 0f);
        }

        String message = fullSession ? "Focus complete. +" : "Focus saved. +";
        Toast.makeText(appContext, message + rewardXp + " XP", Toast.LENGTH_SHORT).show();
    }

    private void notifyTick() {
        float progress = getProgress();
        for (OnTimerChangeListener listener : new ArrayList<>(listeners)) {
            listener.onTick(millisRemaining, progress);
        }
    }

    private void notifyStateChanged() {
        for (OnTimerChangeListener listener : new ArrayList<>(listeners)) {
            listener.onStateChanged(currentState);
        }
    }

    private void vibrate() {
        try {
            Vibrator vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator == null || !vibrator.hasVibrator()) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
        } catch (SecurityException ignored) {
        }
    }
}
