package zsk.mobilki.dailyquest.ui.statistics;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import zsk.mobilki.dailyquest.R;
import zsk.mobilki.dailyquest.data.model.FocusSession;
import zsk.mobilki.dailyquest.data.repository.QuestRepository;

public class StatisticsFragment extends Fragment {

    private TextView textTotalFocus;
    private TextView textStatsStreak;
    private TextView textLevelHistory;
    private LinearLayout layoutWeeklyChart;
    private QuestRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        repository = new QuestRepository(requireContext());

        textTotalFocus = view.findViewById(R.id.text_total_focus);
        textStatsStreak = view.findViewById(R.id.text_stats_streak);
        textLevelHistory = view.findViewById(R.id.text_level_history);
        layoutWeeklyChart = view.findViewById(R.id.layout_weekly_chart);

        updateStats();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStats();
    }

    private void updateStats() {
        List<FocusSession> sessions = repository.getFocusSessionsList();
        long totalMinutes = repository.getTotalFocusMillisFromHistory() / 60000;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        textTotalFocus.setText(hours > 0
                ? String.format(Locale.getDefault(), "%dh %02dm", hours, minutes)
                : minutes + "m");
        textStatsStreak.setText(String.valueOf(repository.getStreak()));
        textLevelHistory.setText(buildProgressSummary(sessions.size()));
        renderWeeklyChart(sessions);
    }

    private String buildProgressSummary(int sessionCount) {
        return "Level " + repository.getLevel() + " - " + repository.getLevelTitle()
                + "\nTotal XP: " + repository.getTotalXp()
                + "\nCompleted tasks: " + repository.getCompletedTasks().size()
                + "\nFocus sessions: " + sessionCount;
    }

    private void renderWeeklyChart(List<FocusSession> sessions) {
        layoutWeeklyChart.removeAllViews();
        String[] labels = {"M", "T", "W", "T", "F", "S", "S"};
        long[] values = buildWeekValues(sessions);
        long max = 1;
        for (long value : values) {
            max = Math.max(max, value);
        }

        for (int i = 0; i < labels.length; i++) {
            LinearLayout column = new LinearLayout(requireContext());
            column.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
            column.setOrientation(LinearLayout.VERTICAL);

            View bar = new View(requireContext());
            int height = dp(18 + (int) (values[i] * 112 / max));
            bar.setBackground(makeBarDrawable(isTodayColumn(i)));
            column.addView(bar, new LinearLayout.LayoutParams(dp(22), height));

            TextView label = new TextView(requireContext());
            label.setText(labels[i]);
            label.setTextColor(getResources().getColor(R.color.gray_text, null));
            label.setTextSize(11);
            label.setTypeface(getResources().getFont(R.font.outfit));
            label.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            labelParams.topMargin = dp(8);
            column.addView(label, labelParams);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            if (i > 0) params.leftMargin = dp(8);
            layoutWeeklyChart.addView(column, params);
        }
    }

    private long[] buildWeekValues(List<FocusSession> sessions) {
        long[] values = new long[7];
        Calendar startOfWeek = Calendar.getInstance();
        int dayOfWeek = startOfWeek.get(Calendar.DAY_OF_WEEK);
        int daysFromMonday = (dayOfWeek + 5) % 7;
        startOfWeek.add(Calendar.DAY_OF_YEAR, -daysFromMonday);
        startOfWeek.set(Calendar.HOUR_OF_DAY, 0);
        startOfWeek.set(Calendar.MINUTE, 0);
        startOfWeek.set(Calendar.SECOND, 0);
        startOfWeek.set(Calendar.MILLISECOND, 0);
        long startMillis = startOfWeek.getTimeInMillis();

        for (FocusSession session : sessions) {
            long offset = session.getTimestamp() - startMillis;
            int dayIndex = (int) (offset / (24 * 60 * 60 * 1000L));
            if (dayIndex >= 0 && dayIndex < values.length) {
                values[dayIndex] += session.getDurationMillis() / 60000;
            }
        }
        return values;
    }

    private boolean isTodayColumn(int columnIndex) {
        Calendar today = Calendar.getInstance();
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        return columnIndex == (dayOfWeek + 5) % 7;
    }

    private GradientDrawable makeBarDrawable(boolean active) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dp(8));
        drawable.setColor(getResources().getColor(active ? R.color.cobalt_blue : R.color.cream_card, null));
        if (!active) {
            drawable.setStroke(dp(1), getResources().getColor(R.color.cobalt_blue, null));
        }
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
