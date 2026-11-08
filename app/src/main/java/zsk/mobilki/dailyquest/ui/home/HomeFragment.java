package zsk.mobilki.dailyquest.ui.home;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Locale;

import zsk.mobilki.dailyquest.R;
import zsk.mobilki.dailyquest.FocusSessionManager;
import zsk.mobilki.dailyquest.data.model.Task;
import zsk.mobilki.dailyquest.data.remote.BoredApiClient;
import zsk.mobilki.dailyquest.data.repository.QuestRepository;

public class HomeFragment extends Fragment {

    private TextView textLevel;
    private TextView textLevelTitle;
    private TextView textXpSummary;
    private TextView textMultiplier;
    private TextView textTimer;
    private TextView textFocusState;
    private TextView textXpPopup;
    private TextView textStreak;
    private TextView textSessions;
    private TextView textCoins;
    private ProgressBar progressXp;
    private LinearLayout layoutTodayTasks;
    private FocusTimerView focusTimerView;
    private Button buttonStartFocus;
    private Button buttonResetFocus;
    private Button buttonStopFocus;
    private LinearLayout layoutSecondaryControls;

    private QuestRepository repository;
    private FocusSessionManager timerManager;
    private boolean isLoadingDailySpecial = false;

    private final FocusSessionManager.OnTimerChangeListener timerListener = new FocusSessionManager.OnTimerChangeListener() {
        @Override
        public void onTick(long millisRemaining, float progress) {
            long minutes = millisRemaining / 60000;
            long seconds = (millisRemaining / 1000) % 60;
            textTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            focusTimerView.setProgress(progress);
            if (timerManager.getCurrentState() == FocusSessionManager.FocusState.RUNNING) {
                animateTick();
            }
        }

        @Override
        public void onFinish(int xpReward) {
            showXpPopup(xpReward);
            updateUi();
        }

        @Override
        public void onStateChanged(FocusSessionManager.FocusState state) {
            if (state == FocusSessionManager.FocusState.IDLE) {
                buttonStartFocus.setText("Start Focus Session");
                textFocusState.setText("Ready for deep work");
                layoutSecondaryControls.setVisibility(View.GONE);
                focusTimerView.setRunning(false);
            } else if (state == FocusSessionManager.FocusState.RUNNING) {
                buttonStartFocus.setText("Pause Session");
                textFocusState.setText("Focus session running");
                layoutSecondaryControls.setVisibility(View.VISIBLE);
                focusTimerView.setRunning(true);
            } else if (state == FocusSessionManager.FocusState.PAUSED) {
                buttonStartFocus.setText("Resume Session");
                textFocusState.setText("Session paused");
                layoutSecondaryControls.setVisibility(View.VISIBLE);
                focusTimerView.setRunning(false);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        repository = new QuestRepository(requireContext());
        timerManager = FocusSessionManager.getInstance(requireContext());

        textLevel = view.findViewById(R.id.text_level);
        textLevelTitle = view.findViewById(R.id.text_level_title);
        textXpSummary = view.findViewById(R.id.text_xp_summary);
        textMultiplier = view.findViewById(R.id.text_multiplier);
        textTimer = view.findViewById(R.id.text_timer);
        textFocusState = view.findViewById(R.id.text_focus_state);
        textXpPopup = view.findViewById(R.id.text_xp_popup);
        textStreak = view.findViewById(R.id.text_streak);
        textSessions = view.findViewById(R.id.text_sessions);
        textCoins = view.findViewById(R.id.text_coins);
        progressXp = view.findViewById(R.id.progress_xp);
        layoutTodayTasks = view.findViewById(R.id.layout_today_tasks);
        focusTimerView = view.findViewById(R.id.focus_timer_view);

        buttonStartFocus = view.findViewById(R.id.button_start_focus);
        buttonResetFocus = view.findViewById(R.id.button_reset_focus);
        buttonStopFocus = view.findViewById(R.id.button_stop_focus);
        layoutSecondaryControls = view.findViewById(R.id.layout_secondary_controls);

        buttonStartFocus.setOnClickListener(v -> {
            FocusSessionManager.FocusState state = timerManager.getCurrentState();
            if (state == FocusSessionManager.FocusState.RUNNING) {
                timerManager.pause();
            } else {
                timerManager.startOrResume();
            }
        });

        buttonResetFocus.setOnClickListener(v -> timerManager.reset());
        buttonStopFocus.setOnClickListener(v -> timerManager.stop());

        updateUi();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        timerManager.registerListener(timerListener);
        ensureDailySpecialTask();
        updateUi();
    }

    @Override
    public void onPause() {
        timerManager.unregisterListener(timerListener);
        super.onPause();
    }

    private void updateUi() {
        int level = repository.getLevel();
        int progressXpVal = repository.getProgressXp();
        int xpToNext = repository.getXpToNextLevel();
        int streak = repository.getStreak();

        textLevel.setText("LVL " + level);
        textLevelTitle.setText(repository.getLevelTitle());
        textXpSummary.setText(progressXpVal + " / " + xpToNext + " XP");
        progressXp.setMax(xpToNext);
        progressXp.setProgress(progressXpVal);
        textMultiplier.setText(streak > 0 ? "x2 streak" : "x1 streak");
        textStreak.setText(String.valueOf(streak));
        textSessions.setText(String.valueOf(repository.getFocusSessions()));
        textCoins.setText(String.valueOf(repository.getCoins()));
        renderTodayTasks();

        long millis = timerManager.getMillisRemaining();
        long minutes = millis / 60000;
        long seconds = (millis / 1000) % 60;
        textTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        focusTimerView.setProgress(timerManager.getProgress());
    }

    private void ensureDailySpecialTask() {
        if (isLoadingDailySpecial || repository.hasTodaySpecialTask()) {
            return;
        }

        isLoadingDailySpecial = true;
        repository.ensureTodaySpecialTask(new BoredApiClient.Callback() {
            @Override
            public void onSuccess(String activityTitle) {
                isLoadingDailySpecial = false;
                updateUi();
            }

            @Override
            public void onFailure(Exception e) {
                isLoadingDailySpecial = false;
            }
        });
    }

    private void renderTodayTasks() {
        layoutTodayTasks.removeAllViews();
        List<Task> tasks = repository.getTodayActiveTasks();
        int count = Math.min(3, tasks.size());

        if (count == 0) {
            addTaskPreview("Open Tasks to get today's +200 XP special", false);
            return;
        }

        for (int i = 0; i < count; i++) {
            addTaskPreview(tasks.get(i).getTitle(), true);
        }
    }

    private void addTaskPreview(String title, boolean active) {
        TextView row = new TextView(requireContext());
        row.setText((active ? "☐ " : "+ ") + title);
        row.setTextColor(getResources().getColor(active ? R.color.text_primary : R.color.gray_text, null));
        row.setTextSize(15);
        row.setTypeface(getResources().getFont(R.font.outfit));
        row.setPadding(0, dp(5), 0, dp(5));
        layoutTodayTasks.addView(row, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
    }

    private void animateTick() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(textTimer, View.SCALE_X, 1f, 1.035f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(textTimer, View.SCALE_Y, 1f, 1.035f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(260);
        set.start();
    }

    private void showXpPopup(int reward) {
        textXpPopup.setText("+" + reward + " XP");
        textXpPopup.setVisibility(View.VISIBLE);
        textXpPopup.setAlpha(0f);
        textXpPopup.setTranslationY(dp(24));

        ObjectAnimator alpha = ObjectAnimator.ofFloat(textXpPopup, View.ALPHA, 0f, 1f, 1f, 0f);
        ObjectAnimator move = ObjectAnimator.ofFloat(textXpPopup, View.TRANSLATION_Y, dp(24), -dp(38));
        AnimatorSet set = new AnimatorSet();
        set.playTogether(alpha, move);
        set.setDuration(1800);
        set.start();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
