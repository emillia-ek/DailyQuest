package zsk.mobilki.dailyquest.ui.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import zsk.mobilki.dailyquest.R;
import zsk.mobilki.dailyquest.data.model.Task;
import zsk.mobilki.dailyquest.data.repository.QuestRepository;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskChangeListener {
        void onTaskStatusChanged(Task task, boolean isChecked);
    }

    private final List<Task> taskList;
    private final boolean isHistory;
    private OnTaskChangeListener listener;

    public TaskAdapter(List<Task> taskList, boolean isHistory) {
        this.taskList = taskList;
        this.isHistory = isHistory;
    }

    public void setOnTaskChangeListener(OnTaskChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;
        private final ImageView imageStar;
        private final TextView textTitle;
        private final TextView textDate;
        private final TextView textXp;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_task);
            imageStar = itemView.findViewById(R.id.image_star);
            textTitle = itemView.findViewById(R.id.text_task_title);
            textDate = itemView.findViewById(R.id.text_task_date);
            textXp = itemView.findViewById(R.id.text_xp_reward);
        }

        public void bind(Task task) {
            textTitle.setText(task.getTitle());
            textXp.setText("+" + task.getXpReward() + " XP");
            imageStar.setVisibility(task.isApiTask() && task.getXpReward() == QuestRepository.DAILY_SPECIAL_XP ? View.VISIBLE : View.GONE);
            checkBox.setOnCheckedChangeListener(null);

            if (isHistory) {
                checkBox.setChecked(true);
                checkBox.setEnabled(false);
                textTitle.setPaintFlags(textTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                textTitle.setTextColor(itemView.getContext().getResources().getColor(R.color.gray_text, null));
                
                if (task.getCompletedAt() > 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    String dateStr = sdf.format(new Date(task.getCompletedAt()));
                    textDate.setText("Done: " + dateStr);
                    textDate.setVisibility(View.VISIBLE);
                } else {
                    textDate.setVisibility(View.GONE);
                }
            } else {
                checkBox.setChecked(task.isCompleted());
                checkBox.setEnabled(true);
                if (task.getDueDate() > 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                    textDate.setText("Due: " + sdf.format(new Date(task.getDueDate())));
                    textDate.setVisibility(View.VISIBLE);
                } else {
                    textDate.setVisibility(View.GONE);
                }
                
                if (task.isCompleted()) {
                    textTitle.setPaintFlags(textTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    textTitle.setTextColor(itemView.getContext().getResources().getColor(R.color.gray_text, null));
                } else {
                    textTitle.setPaintFlags(textTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    textTitle.setTextColor(itemView.getContext().getResources().getColor(R.color.navy_dark, null));
                }

                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        textTitle.setPaintFlags(textTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        textTitle.setTextColor(itemView.getContext().getResources().getColor(R.color.gray_text, null));
                    } else {
                        textTitle.setPaintFlags(textTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        textTitle.setTextColor(itemView.getContext().getResources().getColor(R.color.navy_dark, null));
                    }
                    if (listener != null) {
                        listener.onTaskStatusChanged(task, isChecked);
                    }
                });
            }
        }
    }
}
