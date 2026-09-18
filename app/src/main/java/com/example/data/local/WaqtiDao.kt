package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WaqtiDao {
    // Tasks
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY startTime ASC")
    fun getTasksForUser(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND status != 'INBOX' ORDER BY startTime ASC")
    fun getPlannedTasksForUser(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND status = 'INBOX' ORDER BY id DESC")
    fun getInboxTasksForUser(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY startTime ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status != 'INBOX' ORDER BY startTime ASC")
    fun getPlannedTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'INBOX' ORDER BY id DESC")
    fun getInboxTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM tasks WHERE userId = :userId")
    suspend fun deleteTasksForUser(userId: String)

    // Routines (Daily Program)
    @Query("SELECT * FROM routines ORDER BY time ASC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<RoutineEntity>)

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)

    // Habits
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    // Focus Sessions
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions")
    fun getTotalFocusMinutes(): Flow<Int?>

    // Users & Authentication
    @Query("SELECT * FROM users WHERE isActive = 1 LIMIT 1")
    fun getActiveUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveUser(): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("UPDATE users SET isActive = 0")
    suspend fun clearActiveUsers()

    @Query("UPDATE users SET isActive = 1 WHERE id = :userId")
    suspend fun setActiveUser(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    // Notification Preferences
    @Query("SELECT * FROM notification_preferences WHERE userId = :userId")
    fun getNotificationPreferences(userId: String): Flow<List<com.example.data.notification.NotificationPreferenceEntity>>

    @Query("SELECT * FROM notification_preferences WHERE userId = :userId AND category = :category LIMIT 1")
    suspend fun getPreferenceByCategory(userId: String, category: String): com.example.data.notification.NotificationPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationPreference(pref: com.example.data.notification.NotificationPreferenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationPreferences(prefs: List<com.example.data.notification.NotificationPreferenceEntity>)

    @Update
    suspend fun updateNotificationPreference(pref: com.example.data.notification.NotificationPreferenceEntity)

    // Notification Schedules
    @Query("SELECT * FROM notification_schedules WHERE userId = :userId AND status = 'SCHEDULED' ORDER BY scheduledAt ASC")
    fun getUpcomingSchedules(userId: String): Flow<List<com.example.data.notification.NotificationScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: com.example.data.notification.NotificationScheduleEntity): Long

    @Query("UPDATE notification_schedules SET status = :status WHERE id = :id")
    suspend fun updateScheduleStatus(id: Long, status: String)

    @Query("DELETE FROM notification_schedules WHERE sourceEntityId = :sourceEntityId")
    suspend fun deleteScheduleBySource(sourceEntityId: String)

    @Query("DELETE FROM notification_schedules WHERE userId = :userId")
    suspend fun clearSchedulesForUser(userId: String)

    // Notification Logs
    @Query("SELECT * FROM notification_logs WHERE userId = :userId ORDER BY deliveredAt DESC LIMIT 100")
    fun getRecentLogs(userId: String): Flow<List<com.example.data.notification.NotificationLogEntity>>

    @Query("SELECT COUNT(*) FROM notification_logs WHERE userId = :userId AND deliveredAt >= :sinceTimestamp")
    suspend fun getNotificationCountSince(userId: String, sinceTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM notification_logs WHERE userId = :userId AND action IN ('DISMISSED', 'SNOOZED') AND deliveredAt >= :sinceTimestamp")
    suspend fun getFatigueCountSince(userId: String, sinceTimestamp: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: com.example.data.notification.NotificationLogEntity): Long

    @Query("UPDATE notification_logs SET action = :action, openedAt = :openedAt WHERE notificationId = :notificationId")
    suspend fun updateLogAction(notificationId: Long, action: String, openedAt: Long?)
}
