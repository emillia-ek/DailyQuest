package zsk.mobilki.dailyquest.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import zsk.mobilki.dailyquest.R;
import zsk.mobilki.dailyquest.data.model.FocusSession;
import zsk.mobilki.dailyquest.data.repository.QuestRepository;

public class FocusHistoryFragment extends Fragment {

    private LinearLayout layoutFocusHistory;
    private TextView textTotalFocus;
    private TextView textSummary;
    private TextView textCount;
    private TextView textRewards;
    private QuestRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_focus_history, container, false);
        repository = new QuestRepository(requireContext());
        layoutFocusHistory = view.findViewById(R.id.layout_focus_history);
        textTotalFocus = view.findViewById(R.id.text_focus_history_total);
        textSummary = view.findViewById(R.id.text_focus_history_summary);
        textCount = view.findViewById(R.id.text_focus_history_count);
        textRewards = view.findViewById(R.id.text_focus_history_rewards);
        renderHistory();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        renderHistory();
    }

    private void renderHistory() {
        layoutFocusHistory.removeAllViews();
        List<FocusSession> sessions = repository.getFocusSessionsList();
        long totalMillis = 0;
        for (FocusSession session : sessions) {
            totalMillis += session.getDurationMillis();
        }

        textTotalFocus.setText(formatDuration(totalMillis));
        int sessionCount = sessions.size();
        textSummary.setText(sessionCount + (sessionCount == 1 ? " session completed" : " sessions completed"));
        textCount.setText(String.valueOf(sessions.size()));
        textRewards.setText(String.valueOf(repository.getCoins()));

        if (sessions.isEmpty()) {
            layoutFocusHistory.addView(makeEmptyState());
            return;
        }

        for (FocusSession session : sessions) {
            layoutFocusHistory.addView(makeSessionRow(session));
        }
    }

    private View makeSessionRow(FocusSession session) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.card_filled);
        row.setPadding(dp(14), dp(14), dp(14), dp(14));

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(R.drawable.icon_dark_fit);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(42), dp(42));
        iconParams.rightMargin = dp(12);
        row.addView(icon, iconParams);

        LinearLayout details = new LinearLayout(requireContext());
        details.setOrientation(LinearLayout.VERTICAL);

        TextView title = makeText(formatDuration(session.getDurationMillis()), 18, R.color.cobalt_blue);
        details.addView(title);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' HH:mm", Locale.getDefault());
        TextView date = makeText(sdf.format(new Date(session.getTimestamp())), 12, R.color.gray_text);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dateParams.topMargin = dp(4);
        details.addView(date, dateParams);

        row.addView(details, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView reward = makeText("+" + session.getCoinsEarned(), 13, R.color.cobalt_blue);
        reward.setBackgroundResource(R.drawable.chip_xp);
        reward.setPadding(dp(12), dp(6), dp(12), dp(6));
        row.addView(reward);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = dp(10);
        row.setLayoutParams(rowParams);
        return row;
    }

    private View makeEmptyState() {
        LinearLayout empty = new LinearLayout(requireContext());
        empty.setOrientation(LinearLayout.VERTICAL);
        empty.setGravity(android.view.Gravity.CENTER);
        empty.setBackgroundResource(R.drawable.card_filled);
        empty.setPadding(dp(20), dp(24), dp(20), dp(24));

        ImageView image = new ImageView(requireContext());
        image.setImageResource(R.drawable.dark_angry_cat_fit);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        empty.addView(image, new LinearLayout.LayoutParams(dp(100), dp(100)));

        TextView title = makeText("No focus sessions yet", 17, R.color.cobalt_blue);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(10);
        empty.addView(title, titleParams);

        TextView subtitle = makeText("Complete a session from Home to start your log.", 13, R.color.gray_text);
        subtitle.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = dp(4);
        empty.addView(subtitle, subtitleParams);

        return empty;
    }

    private TextView makeText(String text, int sizeSp, int colorRes) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextSize(sizeSp);
        textView.setTextColor(getResources().getColor(colorRes, null));
        textView.setTypeface(getResources().getFont(R.font.outfit));
        return textView;
    }

    private String formatDuration(long durationMillis) {
        long totalMinutes = durationMillis / 60000;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %02dm", hours, minutes);
        }
        return minutes + "m";
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
