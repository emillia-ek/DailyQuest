package zsk.mobilki.dailyquest.ui.tasklist;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import zsk.mobilki.dailyquest.R;
import zsk.mobilki.dailyquest.data.model.Task;
import zsk.mobilki.dailyquest.data.remote.BoredApiClient;
import zsk.mobilki.dailyquest.data.repository.QuestRepository;
import zsk.mobilki.dailyquest.ui.adapter.TaskAdapter;

public class TaskListFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private Button buttonAddTaskInline;
    private Button buttonPickDueDate;
    private EditText editTaskTitleInline;
    private CheckBox checkboxSpecialInline;
    private TextView textXpFly;
    private TaskAdapter adapter;
    private QuestRepository repository;
    private final List<Task> activeTasks = new ArrayList<>();
    private long selectedDueDate = 0;
    private boolean isLoadingDailySpecial = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        repository = new QuestRepository(requireContext());

        recyclerView = view.findViewById(R.id.recycler_tasks);
        layoutEmpty = view.findViewById(R.id.layout_empty_tasks);
        buttonAddTaskInline = view.findViewById(R.id.button_add_task_inline);
        buttonPickDueDate = view.findViewById(R.id.button_pick_due_date);
        editTaskTitleInline = view.findViewById(R.id.edit_task_title_inline);
        checkboxSpecialInline = view.findViewById(R.id.checkbox_special_inline);
        textXpFly = view.findViewById(R.id.text_xp_fly);

        buttonPickDueDate.setOnClickListener(v -> showDueDatePicker());
        buttonAddTaskInline.setOnClickListener(v -> addInlineTask());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new TaskAdapter(activeTasks, false);
        adapter.setOnTaskChangeListener((task, isChecked) -> {
            if (isChecked) {
                // Complete task (rewards XP & updates SQLite)
                repository.completeTask(task, true);
                showXpFly(task.getXpReward());
                
                // Wait briefly for a visual check animation before removing from active list
                recyclerView.postDelayed(this::loadActiveTasks, 420);
            }
        });
        recyclerView.setAdapter(adapter);
        ensureDailySpecialTask();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ensureDailySpecialTask();
        loadActiveTasks();
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
                loadActiveTasks();
            }

            @Override
            public void onFailure(Exception e) {
                isLoadingDailySpecial = false;
            }
        });
    }

    private void loadActiveTasks() {
        activeTasks.clear();
        activeTasks.addAll(repository.getActiveTasks());

        if (activeTasks.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        
        adapter.notifyDataSetChanged();
    }

    private void addInlineTask() {
        String title = editTaskTitleInline.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Enter a task title.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isSpecial = checkboxSpecialInline.isChecked();
        int xpReward = isSpecial ? 75 : 50;
        repository.addTask(title, false, xpReward, selectedDueDate);

        editTaskTitleInline.setText("");
        checkboxSpecialInline.setChecked(false);
        selectedDueDate = 0;
        buttonPickDueDate.setText("No due date");
        Toast.makeText(getContext(), "Task added.", Toast.LENGTH_SHORT).show();
        loadActiveTasks();
    }

    private void showDueDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDueDate > 0) {
            calendar.setTimeInMillis(selectedDueDate);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar pickedDate = Calendar.getInstance();
                    pickedDate.set(Calendar.YEAR, year);
                    pickedDate.set(Calendar.MONTH, month);
                    pickedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    pickedDate.set(Calendar.HOUR_OF_DAY, 0);
                    pickedDate.set(Calendar.MINUTE, 0);
                    pickedDate.set(Calendar.SECOND, 0);
                    pickedDate.set(Calendar.MILLISECOND, 0);
                    selectedDueDate = pickedDate.getTimeInMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
                    buttonPickDueDate.setText("Due " + sdf.format(pickedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, "Clear", (dialogInterface, which) -> {
            selectedDueDate = 0;
            buttonPickDueDate.setText("No due date");
        });
        dialog.show();
    }

    private void showXpFly(int reward) {
        textXpFly.setText("+" + reward + " XP");
        textXpFly.bringToFront();
        textXpFly.setVisibility(View.VISIBLE);
        textXpFly.setAlpha(0f);
        textXpFly.setTranslationY(dp(18));

        ObjectAnimator alpha = ObjectAnimator.ofFloat(textXpFly, View.ALPHA, 0f, 1f, 1f, 0f);
        ObjectAnimator move = ObjectAnimator.ofFloat(textXpFly, View.TRANSLATION_Y, dp(18), -dp(56));
        AnimatorSet set = new AnimatorSet();
        set.playTogether(alpha, move);
        set.setDuration(900);
        set.start();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
