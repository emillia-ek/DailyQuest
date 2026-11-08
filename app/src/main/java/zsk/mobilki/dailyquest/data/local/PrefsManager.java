package zsk.mobilki.dailyquest.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class PrefsManager {

    private static final String PREF_NAME = "daily_quest_prefs";

    private static final String KEY_TOTAL_XP = "key_total_xp";
    private static final String KEY_STREAK = "key_streak";
    private static final String KEY_LAST_ACTIVE_TIME = "key_last_active_time";
    private static final String KEY_FOCUS_SESSIONS = "key_focus_sessions";
    private static final String KEY_TOTAL_FOCUS_MILLIS = "key_total_focus_millis";
    private static final String KEY_COINS = "key_coins";
    private static final String KEY_OWNED_ACCESSORIES = "key_owned_accessories";
    private static final String KEY_EQUIPPED_ACCESSORY = "key_equipped_accessory";

    private static final String KEY_DAILY_QUEST_TITLE = "key_daily_quest_title";
    private static final String KEY_DAILY_QUEST_DATE = "key_daily_quest_date";
    private static final String KEY_DAILY_QUEST_COMPLETED = "key_daily_quest_completed";

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    //XP
    public int getTotalXp() {
        return prefs.getInt(KEY_TOTAL_XP, 0);
    }

    public void addXp(int amount) {
        int currentXp = getTotalXp();
        prefs.edit().putInt(KEY_TOTAL_XP, currentXp + amount).apply();
    }

    public int getLevel() {
        return (getTotalXp() / 1000) + 1;
    }

    public int getProgressXp() {
        return getTotalXp() % 1000;
    }

    public int getXpToNextLevel() {
        return 1000;
    }

    public String getLevelTitle() {
        int level = getLevel();
        if (level >= 20) return "Deep Work Master";
        if (level >= 10) return "Focus Mage";
        if (level >= 5) return "Apprentice";
        return "Beginner";
    }

    // Streak
    public int getStreak() {
        return prefs.getInt(KEY_STREAK, 0);
    }

    public void setStreak(int streak) {
        prefs.edit().putInt(KEY_STREAK, streak).apply();
    }

    public void incrementStreak() {
        setStreak(getStreak() + 1);
    }

    public void recordFocusSession(long durationMillis, int coinsEarned) {
        prefs.edit()
                .putInt(KEY_FOCUS_SESSIONS, getFocusSessions() + 1)
                .putLong(KEY_TOTAL_FOCUS_MILLIS, getTotalFocusMillis() + durationMillis)
                .putInt(KEY_COINS, getCoins() + coinsEarned)
                .apply();
    }

    public int getFocusSessions() {
        return prefs.getInt(KEY_FOCUS_SESSIONS, 0);
    }

    public long getTotalFocusMillis() {
        return prefs.getLong(KEY_TOTAL_FOCUS_MILLIS, 0);
    }

    public int getCoins() {
        return prefs.getInt(KEY_COINS, 0);
    }

    public boolean spendCoins(int amount) {
        int currentCoins = getCoins();
        if (currentCoins < amount) {
            return false;
        }
        prefs.edit().putInt(KEY_COINS, currentCoins - amount).apply();
        return true;
    }

    public boolean ownsAccessory(String accessoryId) {
        return getOwnedAccessories().contains(accessoryId);
    }

    public void markAccessoryOwned(String accessoryId) {
        Set<String> ownedAccessories = getOwnedAccessories();
        ownedAccessories.add(accessoryId);
        prefs.edit().putStringSet(KEY_OWNED_ACCESSORIES, ownedAccessories).apply();
    }

    private Set<String> getOwnedAccessories() {
        Object storedValue = prefs.getAll().get(KEY_OWNED_ACCESSORIES);
        if (storedValue instanceof Set) {
            return new HashSet<>(prefs.getStringSet(KEY_OWNED_ACCESSORIES, new HashSet<>()));
        }

        Set<String> accessories = new HashSet<>();
        if (storedValue instanceof String) {
            String savedAccessories = (String) storedValue;
            if (!savedAccessories.isEmpty()) {
                for (String accessoryId : savedAccessories.split(",")) {
                    accessories.add(accessoryId);
                }
            }
            prefs.edit().putStringSet(KEY_OWNED_ACCESSORIES, accessories).apply();
        }
        return accessories;
    }

    public String getEquippedAccessory() {
        return prefs.getString(KEY_EQUIPPED_ACCESSORY, "");
    }

    public void setEquippedAccessory(String accessoryId) {
        prefs.edit().putString(KEY_EQUIPPED_ACCESSORY, accessoryId).apply();
    }

    public long getLastActiveTime() {
        return prefs.getLong(KEY_LAST_ACTIVE_TIME, 0);
    }

    public void setLastActiveTime(long timestamp) {
        prefs.edit().putLong(KEY_LAST_ACTIVE_TIME, timestamp).apply();
    }

    //Daily quest
    public String getDailyQuestTitle() {
        return prefs.getString(KEY_DAILY_QUEST_TITLE, "");
    }

    public void setDailyQuestTitle(String title) {
        prefs.edit().putString(KEY_DAILY_QUEST_TITLE, title).apply();
    }

    public String getDailyQuestDate() {
        return prefs.getString(KEY_DAILY_QUEST_DATE, "");
    }

    public void setDailyQuestDate(String dateStr) {
        prefs.edit().putString(KEY_DAILY_QUEST_DATE, dateStr).apply();
    }

    public void setDailyQuestCompleted(boolean completed) {
        prefs.edit().putBoolean(KEY_DAILY_QUEST_COMPLETED, completed).apply();
    }

    public void resetProgress() {
        prefs.edit().clear().apply();
    }
}
