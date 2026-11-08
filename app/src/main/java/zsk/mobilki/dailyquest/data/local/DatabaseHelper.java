package zsk.mobilki.dailyquest.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import zsk.mobilki.dailyquest.data.model.FocusSession;
import zsk.mobilki.dailyquest.data.model.Task;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "daily_quest.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_IS_COMPLETED = "is_completed";
    private static final String COLUMN_IS_API_TASK = "is_api_task";
    private static final String COLUMN_XP_REWARD = "xp_reward";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_COMPLETED_AT = "completed_at";
    private static final String COLUMN_DUE_DATE = "due_date";

    private static final String TABLE_FOCUS = "focus_sessions";
    private static final String COLUMN_FOCUS_ID = "id";
    private static final String COLUMN_FOCUS_DURATION = "duration_millis";
    private static final String COLUMN_FOCUS_TIMESTAMP = "timestamp";
    private static final String COLUMN_FOCUS_COINS = "coins_earned";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTasksTable = "CREATE TABLE " + TABLE_TASKS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_IS_COMPLETED + " INTEGER DEFAULT 0,"
                + COLUMN_IS_API_TASK + " INTEGER DEFAULT 0,"
                + COLUMN_XP_REWARD + " INTEGER DEFAULT 10,"
                + COLUMN_CREATED_AT + " INTEGER,"
                + COLUMN_COMPLETED_AT + " INTEGER DEFAULT 0,"
                + COLUMN_DUE_DATE + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(createTasksTable);
        createFocusSessionsTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            addColumnIfMissing(db, TABLE_TASKS, COLUMN_DUE_DATE, "INTEGER DEFAULT 0");
            createFocusSessionsTable(db);
        }

        if (oldVersion < 3) {
            addColumnIfMissing(db, TABLE_TASKS, COLUMN_DUE_DATE, "INTEGER DEFAULT 0");
            createFocusSessionsTable(db);
        }
    }

    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(COLUMN_IS_API_TASK, task.isApiTask() ? 1 : 0);
        values.put(COLUMN_XP_REWARD, task.getXpReward());
        values.put(COLUMN_CREATED_AT, task.getCreatedAt());
        values.put(COLUMN_COMPLETED_AT, task.getCompletedAt());
        values.put(COLUMN_DUE_DATE, task.getDueDate());

        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    public List<Task> getActiveTasks() {
        List<Task> taskList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS
                + " WHERE " + COLUMN_IS_COMPLETED + " = 0"
                + " ORDER BY CASE WHEN " + COLUMN_DUE_DATE + " = 0 THEN 1 ELSE 0 END, "
                + COLUMN_DUE_DATE + " ASC, "
                + COLUMN_CREATED_AT + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                taskList.add(readTask(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    public List<Task> getTodayActiveTasks() {
        long startOfDay = startOfToday();
        long startOfTomorrow = startOfTomorrow();

        List<Task> taskList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS
                + " WHERE " + COLUMN_IS_COMPLETED + " = 0"
                + " AND (" + COLUMN_DUE_DATE + " = 0 OR (" + COLUMN_DUE_DATE + " >= ? AND " + COLUMN_DUE_DATE + " < ?))"
                + " ORDER BY CASE WHEN " + COLUMN_DUE_DATE + " = 0 THEN 1 ELSE 0 END, "
                + COLUMN_DUE_DATE + " ASC, "
                + COLUMN_CREATED_AT + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(startOfDay), String.valueOf(startOfTomorrow)});

        if (cursor.moveToFirst()) {
            do {
                taskList.add(readTask(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    public List<Task> getCompletedTasks() {
        List<Task> taskList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_IS_COMPLETED + " = 1 ORDER BY " + COLUMN_COMPLETED_AT + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                taskList.add(readTask(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    public boolean hasApiTaskForToday() {
        long startOfDay = startOfToday();
        long startOfTomorrow = startOfTomorrow();

        String selectQuery = "SELECT " + COLUMN_ID + " FROM " + TABLE_TASKS
                + " WHERE " + COLUMN_IS_API_TASK + " = 1"
                + " AND " + COLUMN_XP_REWARD + " = 200"
                + " AND " + COLUMN_CREATED_AT + " >= ?"
                + " AND " + COLUMN_CREATED_AT + " < ?"
                + " LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(startOfDay), String.valueOf(startOfTomorrow)});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(COLUMN_IS_API_TASK, task.isApiTask() ? 1 : 0);
        values.put(COLUMN_XP_REWARD, task.getXpReward());
        values.put(COLUMN_COMPLETED_AT, task.getCompletedAt());
        values.put(COLUMN_DUE_DATE, task.getDueDate());

        int rows = db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        return rows;
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public long addFocusSession(long durationMillis, long timestamp, int coinsEarned) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FOCUS_DURATION, durationMillis);
        values.put(COLUMN_FOCUS_TIMESTAMP, timestamp);
        values.put(COLUMN_FOCUS_COINS, coinsEarned);

        long id = db.insert(TABLE_FOCUS, null, values);
        db.close();
        return id;
    }

    public List<FocusSession> getFocusSessions() {
        List<FocusSession> list = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_FOCUS + " ORDER BY " + COLUMN_FOCUS_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                FocusSession session = new FocusSession(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOCUS_ID)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FOCUS_DURATION)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FOCUS_TIMESTAMP)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOCUS_COINS))
                );
                list.add(session);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, null, null);
        db.delete(TABLE_FOCUS, null, null);
        db.close();
    }

    private void createFocusSessionsTable(SQLiteDatabase db) {
        String createFocusTable = "CREATE TABLE IF NOT EXISTS " + TABLE_FOCUS + "("
                + COLUMN_FOCUS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FOCUS_DURATION + " INTEGER,"
                + COLUMN_FOCUS_TIMESTAMP + " INTEGER,"
                + COLUMN_FOCUS_COINS + " INTEGER"
                + ")";
        db.execSQL(createFocusTable);
    }

    private void addColumnIfMissing(SQLiteDatabase db, String tableName, String columnName, String columnDefinition) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        boolean exists = false;
        if (cursor.moveToFirst()) {
            do {
                String existingColumn = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                if (columnName.equals(existingColumn)) {
                    exists = true;
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (!exists) {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
        }
    }

    private Task readTask(Cursor cursor) {
        return new Task(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1,
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_API_TASK)) == 1,
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_XP_REWARD)),
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COMPLETED_AT)),
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE))
        );
    }

    private long startOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long startOfTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startOfToday());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTimeInMillis();
    }
}
