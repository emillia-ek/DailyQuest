package zsk.mobilki.dailyquest.data.model;

public class FocusSession {
    private int id;
    private long durationMillis;
    private long timestamp;
    private int coinsEarned;

    public FocusSession() {
    }

    public FocusSession(long durationMillis, long timestamp, int coinsEarned) {
        this.durationMillis = durationMillis;
        this.timestamp = timestamp;
        this.coinsEarned = coinsEarned;
    }

    public FocusSession(int id, long durationMillis, long timestamp, int coinsEarned) {
        this.id = id;
        this.durationMillis = durationMillis;
        this.timestamp = timestamp;
        this.coinsEarned = coinsEarned;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getCoinsEarned() {
        return coinsEarned;
    }

    public void setCoinsEarned(int coinsEarned) {
        this.coinsEarned = coinsEarned;
    }
}
