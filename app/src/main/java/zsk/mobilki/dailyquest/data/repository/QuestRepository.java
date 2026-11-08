package zsk.mobilki.dailyquest.data.repository;

import android.content.Context;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import zsk.mobilki.dailyquest.FocusSessionManager;
import zsk.mobilki.dailyquest.data.local.DatabaseHelper;
import zsk.mobilki.dailyquest.data.local.PrefsManager;
import zsk.mobilki.dailyquest.data.model.FocusSession;
import zsk.mobilki.dailyquest.data.model.Task;
import zsk.mobilki.dailyquest.data.remote.BoredApiClient;

public class QuestRepository {

    public static final int DAILY_SPECIAL_XP = 200;

    private final DatabaseHelper dbHelper;
    private final PrefsManager prefsManager;
    private final BoredApiClient apiClient;

    public QuestRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
        this.prefsManager = new PrefsManager(context);
        this.apiClient = new BoredApiClient();
        checkAndResetStreakOnLaunch();
    }

    public List<Task> getActiveTasks() {
        return dbHelper.getActiveTasks();
    }

    public List<Task> getTodayActiveTasks() {
        return dbHelper.getTodayActiveTasks();
    }

    public List<Task> getCompletedTasks() {
        return dbHelper.getCompletedTasks();
    }

    public long addTask(String title, boolean isApiTask, int xpReward) {
        Task task = new Task(title, false, isApiTask, xpReward);
        return dbHelper.addTask(task);
    }

    public long addTask(String title, boolean isApiTask, int xpReward, long dueDate) {
        Task task = new Task(title, false, isApiTask, xpReward, dueDate);
        return dbHelper.addTask(task);
    }

    public void completeTask(Task task, boolean isChecked) {
        task.setCompleted(isChecked);
        if (isChecked) {
            task.setCompletedAt(System.currentTimeMillis());
            dbHelper.updateTask(task);
            
            int reward = task.getXpReward();
            prefsManager.addXp(reward);
            if (task.isApiTask() && task.getXpReward() == DAILY_SPECIAL_XP) {
                prefsManager.setDailyQuestCompleted(true);
            }

            updateStreakOnTaskCompleted();
        } else {
            task.setCompletedAt(0);
            dbHelper.updateTask(task);

            int reward = task.getXpReward();
            prefsManager.addXp(-reward);
            if (task.isApiTask() && task.getXpReward() == DAILY_SPECIAL_XP) {
                prefsManager.setDailyQuestCompleted(false);
            }
        }
    }

    public void deleteTask(int id) {
        dbHelper.deleteTask(id);
    }

    public int getTotalXp() {
        return prefsManager.getTotalXp();
    }

    public int getLevel() {
        return prefsManager.getLevel();
    }

    public int getProgressXp() {
        return prefsManager.getProgressXp();
    }

    public int getXpToNextLevel() {
        return prefsManager.getXpToNextLevel();
    }

    public String getLevelTitle() {
        return prefsManager.getLevelTitle();
    }

    public int getStreak() {
        return prefsManager.getStreak();
    }

    public int getFocusSessions() {
        return prefsManager.getFocusSessions();
    }

    public int getCoins() {
        return prefsManager.getCoins();
    }

    public boolean ownsAccessory(String accessoryId) {
        return prefsManager.ownsAccessory(accessoryId);
    }

    public String getEquippedAccessory() {
        return prefsManager.getEquippedAccessory();
    }

    public boolean buyAccessory(String accessoryId, int price) {
        if (prefsManager.ownsAccessory(accessoryId)) {
            return true;
        }
        if (!prefsManager.spendCoins(price)) {
            return false;
        }
        prefsManager.markAccessoryOwned(accessoryId);
        prefsManager.setEquippedAccessory(accessoryId);
        return true;
    }

    public boolean equipAccessory(String accessoryId) {
        if (!prefsManager.ownsAccessory(accessoryId)) {
            return false;
        }
        prefsManager.setEquippedAccessory(accessoryId);
        return true;
    }

    public void unequipAccessory() {
        prefsManager.setEquippedAccessory("");
    }

    public int getFocusRewardXp() {
        return getStreak() > 0 ? 50 : 25;
    }

    public int completeFocusSession(long durationMillis) {
        int rewardXp = calculateFocusRewardXp(durationMillis);
        int rewardCoins = calculateFocusRewardCoins(durationMillis);
        prefsManager.addXp(rewardXp);
        prefsManager.recordFocusSession(durationMillis, rewardCoins);
        dbHelper.addFocusSession(durationMillis, System.currentTimeMillis(), rewardCoins);
        updateStreakOnTaskCompleted();
        return rewardXp;
    }

    private int calculateFocusRewardXp(long durationMillis) {
        long safeDuration = Math.max(0, Math.min(durationMillis, FocusSessionManager.FOCUS_DURATION_MILLIS));
        if (safeDuration == 0) {
            return 0;
        }
        int fullReward = getFocusRewardXp();
        return Math.max(1, Math.round(fullReward * (safeDuration / (float) FocusSessionManager.FOCUS_DURATION_MILLIS)));
    }

    private int calculateFocusRewardCoins(long durationMillis) {
        long safeDuration = Math.max(0, Math.min(durationMillis, FocusSessionManager.FOCUS_DURATION_MILLIS));
        if (safeDuration == 0) {
            return 0;
        }
        return Math.max(1, Math.round(10 * (safeDuration / (float) FocusSessionManager.FOCUS_DURATION_MILLIS)));
    }

    public List<FocusSession> getFocusSessionsList() {
        return dbHelper.getFocusSessions();
    }

    public long getTotalFocusMillisFromHistory() {
        long total = 0;
        for (FocusSession session : getFocusSessionsList()) {
            total += session.getDurationMillis();
        }
        return total;
    }

    public void resetAllData() {
        prefsManager.resetProgress();
        dbHelper.clearAllData();
    }

    private void updateStreakOnTaskCompleted() {
        long now = System.currentTimeMillis();
        long lastActive = prefsManager.getLastActiveTime();
        
        String todayStr = getFormattedDate(now);
        String lastActiveStr = getFormattedDate(lastActive);

        if (lastActive == 0) {
            prefsManager.setStreak(1);
        } else if (isYesterday(lastActive)) {
            prefsManager.incrementStreak();
        } else if (!todayStr.equals(lastActiveStr)) {
            prefsManager.setStreak(1);
        }

        prefsManager.setLastActiveTime(now);
    }

    private void checkAndResetStreakOnLaunch() {
        long lastActive = prefsManager.getLastActiveTime();
        if (lastActive > 0) {
            long now = System.currentTimeMillis();
            String todayStr = getFormattedDate(now);
            String lastActiveStr = getFormattedDate(lastActive);
            
            if (!todayStr.equals(lastActiveStr) && !isYesterday(lastActive)) {
                prefsManager.setStreak(0);
            }
        }
    }

    public boolean hasTodaySpecialTask() {
        return dbHelper.hasApiTaskForToday();
    }

    public void ensureTodaySpecialTask(BoredApiClient.Callback callback) {
        String todayStr = getFormattedDate(System.currentTimeMillis());
        if (todayStr.equals(prefsManager.getDailyQuestDate()) && dbHelper.hasApiTaskForToday()) {
            callback.onSuccess(prefsManager.getDailyQuestTitle());
            return;
        }

        fetchNewDailyQuest(new BoredApiClient.Callback() {
            @Override
            public void onSuccess(String activityTitle) {
                if (!dbHelper.hasApiTaskForToday()) {
                    Task dailyTask = new Task(activityTitle, false, true, DAILY_SPECIAL_XP, startOfToday());
                    dbHelper.addTask(dailyTask);
                }
                callback.onSuccess(activityTitle);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void fetchNewDailyQuest(BoredApiClient.Callback callback) {
        String todayStr = getFormattedDate(System.currentTimeMillis());
        
        apiClient.fetchDailyQuestOrFallback(new BoredApiClient.Callback() {
            @Override
            public void onSuccess(String activityTitle) {
                prefsManager.setDailyQuestTitle(activityTitle);
                prefsManager.setDailyQuestDate(todayStr);
                prefsManager.setDailyQuestCompleted(false);
                callback.onSuccess(activityTitle);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    private String getFormattedDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private boolean isYesterday(long timestamp) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        return getFormattedDate(timestamp).equals(getFormattedDate(yesterday.getTimeInMillis()));
    }

    private long startOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
