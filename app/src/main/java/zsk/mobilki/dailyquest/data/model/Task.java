package zsk.mobilki.dailyquest.data.model;

public class Task {
    private int id;
    private String title;
    private boolean isCompleted;
    private boolean isApiTask;
    private int xpReward;
    private long createdAt;
    private long completedAt;
    private long dueDate;

    public Task() {
    }

    public Task(String title, boolean isCompleted, boolean isApiTask, int xpReward) {
        this.title = title;
        this.isCompleted = isCompleted;
        this.isApiTask = isApiTask;
        this.xpReward = xpReward;
        this.createdAt = System.currentTimeMillis();
        this.completedAt = 0;
        this.dueDate = 0;
    }

    public Task(String title, boolean isCompleted, boolean isApiTask, int xpReward, long dueDate) {
        this.title = title;
        this.isCompleted = isCompleted;
        this.isApiTask = isApiTask;
        this.xpReward = xpReward;
        this.createdAt = System.currentTimeMillis();
        this.completedAt = 0;
        this.dueDate = dueDate;
    }

    public Task(int id, String title, boolean isCompleted, boolean isApiTask, int xpReward, long createdAt, long completedAt) {
        this.id = id;
        this.title = title;
        this.isCompleted = isCompleted;
        this.isApiTask = isApiTask;
        this.xpReward = xpReward;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.dueDate = 0;
    }

    public Task(int id, String title, boolean isCompleted, boolean isApiTask, int xpReward, long createdAt, long completedAt, long dueDate) {
        this.id = id;
        this.title = title;
        this.isCompleted = isCompleted;
        this.isApiTask = isApiTask;
        this.xpReward = xpReward;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.dueDate = dueDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isApiTask() {
        return isApiTask;
    }

    public void setApiTask(boolean apiTask) {
        isApiTask = apiTask;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }
}
