package zsk.mobilki.dailyquest.data.local;

import android.content.Context;

import java.util.Calendar;

import zsk.mobilki.dailyquest.data.model.Task;

public class DebugSeedData {

    // zmien te opcję na false lub usuń DebugSeedData.seedIfNeeded(...) z MainActivity,
    // gdy nie chcesz, aby dane testowe pojawiały się w czystej instalacji debugowania
    // (dodane aby ułatwić testowanie :))
    private static final boolean ENABLED = true;

    private static final long MINUTE = 60 * 1000L;

    private DebugSeedData() {
    }

    public static void seedIfNeeded(Context context) {
        if (!ENABLED) return;

        DatabaseHelper db = new DatabaseHelper(context);
        PrefsManager prefs = new PrefsManager(context);

        boolean hasData = !db.getActiveTasks().isEmpty()
                || !db.getCompletedTasks().isEmpty()
                || !db.getFocusSessions().isEmpty()
                || prefs.getTotalXp() > 0;

        if (hasData) return;

        long today = startOfDay(0);
        long tomorrow = startOfDay(1);
        long inTwoDays = startOfDay(2);

        db.addTask(new Task("Buy milk", false, false, 50, today));
        db.addTask(new Task("Go for a walk", false, false, 50, tomorrow));
        db.addTask(new Task("Do a mobile app project", false, true, 75, inTwoDays));
        db.addTask(new Task("Write dziennik praktyk", false, false, 50));

        addCompletedTask(db, "Sleep", 50, startOfDay(-1) + 10 * 60 * MINUTE);
        addCompletedTask(db, "Eating", 75, startOfDay(-2) + 16 * 60 * MINUTE);

        addFocusSession(db, prefs, 25 * MINUTE, startOfDay(0) + 9 * 60 * MINUTE, 10);
        addFocusSession(db, prefs, 45 * MINUTE, startOfDay(-1) + 15 * 60 * MINUTE, 18);
        addFocusSession(db, prefs, 25 * MINUTE, startOfDay(-2) + 11 * 60 * MINUTE, 10);
        addFocusSession(db, prefs, 50 * MINUTE, startOfDay(-4) + 18 * 60 * MINUTE, 20);

        prefs.addXp(325);
        prefs.setStreak(3);
        prefs.setLastActiveTime(System.currentTimeMillis());
    }

    private static void addCompletedTask(DatabaseHelper db, String title, int xpReward, long completedAt) {
        Task task = new Task(title, true, false, xpReward);
        task.setCompletedAt(completedAt);
        db.addTask(task);
    }

    private static void addFocusSession(DatabaseHelper db, PrefsManager prefs, long durationMillis, long timestamp, int coins) {
        db.addFocusSession(durationMillis, timestamp, coins);
        prefs.recordFocusSession(durationMillis, coins);
    }

    private static long startOfDay(int offsetDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, offsetDays);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
