package com.example.data.repository

import com.example.data.local.FocusSessionEntity
import com.example.data.local.HabitEntity
import com.example.data.local.RoutineEntity
import com.example.data.local.TaskEntity
import com.example.data.local.WaqtiDao
import kotlinx.coroutines.flow.Flow

class WaqtiRepository(private val dao: WaqtiDao) {
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val plannedTasks: Flow<List<TaskEntity>> = dao.getPlannedTasks()
    val inboxTasks: Flow<List<TaskEntity>> = dao.getInboxTasks()
    val allRoutines: Flow<List<RoutineEntity>> = dao.getAllRoutines()
    val allHabits: Flow<List<HabitEntity>> = dao.getAllHabits()
    val allFocusSessions: Flow<List<FocusSessionEntity>> = dao.getAllFocusSessions()
    val totalFocusMinutes: Flow<Int?> = dao.getTotalFocusMinutes()
    val currentUser: Flow<com.example.data.local.UserEntity?> = dao.getActiveUserFlow()

    fun getTasksForUser(userId: String): Flow<List<TaskEntity>> = dao.getTasksForUser(userId)
    fun getPlannedTasksForUser(userId: String): Flow<List<TaskEntity>> = dao.getPlannedTasksForUser(userId)
    fun getInboxTasksForUser(userId: String): Flow<List<TaskEntity>> = dao.getInboxTasksForUser(userId)

    suspend fun getActiveUser(): com.example.data.local.UserEntity? = dao.getActiveUser()

    suspend fun switchActiveUser(user: com.example.data.local.UserEntity) {
        dao.clearActiveUsers()
        dao.insertUser(user.copy(isActive = true))
    }

    suspend fun setActiveUserById(userId: String) {
        dao.clearActiveUsers()
        dao.setActiveUser(userId)
    }

    suspend fun ensureDefaultUserExists(): com.example.data.local.UserEntity {
        val existingActive = dao.getActiveUser()
        if (existingActive != null) return existingActive

        val existingDefault = dao.getUserById("user_default_01")
        if (existingDefault != null) {
            dao.setActiveUser(existingDefault.id)
            return existingDefault.copy(isActive = true)
        }

        val defaultUser = com.example.data.local.UserEntity(
            id = "user_default_01",
            name = "عبدالله محمد",
            email = "abdallahmohammedabdlehamed@gmail.com",
            passwordHash = "123456",
            plan = "PRO",
            isActive = true
        )
        dao.insertUser(defaultUser)
        return defaultUser
    }

    suspend fun insertUser(user: com.example.data.local.UserEntity) = dao.insertUser(user)
    suspend fun getUserByEmail(email: String): com.example.data.local.UserEntity? = dao.getUserByEmail(email)
    suspend fun getUserById(id: String): com.example.data.local.UserEntity? = dao.getUserById(id)
    suspend fun deleteAllUsers() = dao.deleteAllUsers()

    suspend fun insertTask(task: TaskEntity): Long = dao.insertTask(task)
    suspend fun insertTasks(tasks: List<TaskEntity>) = dao.insertTasks(tasks)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = dao.deleteTask(task)
    suspend fun deleteTaskById(id: Long) = dao.deleteTaskById(id)
    suspend fun getTaskCount(): Int = dao.getTaskCount()
    suspend fun getAllTasksDirect(): List<TaskEntity> = dao.getAllTasksDirect()
    suspend fun deleteTasksByIds(ids: List<Long>) = dao.deleteTasksByIds(ids)
    suspend fun getRoutineCount(): Int = dao.getRoutineCount()

    suspend fun insertRoutine(routine: RoutineEntity): Long = dao.insertRoutine(routine)
    suspend fun updateRoutine(routine: RoutineEntity) = dao.updateRoutine(routine)
    suspend fun deleteRoutine(routine: RoutineEntity) = dao.deleteRoutine(routine)
    suspend fun getAllRoutinesDirect(): List<RoutineEntity> = dao.getAllRoutinesDirect()
    suspend fun deleteRoutinesByIds(ids: List<Long>) = dao.deleteRoutinesByIds(ids)

    suspend fun insertHabit(habit: HabitEntity): Long = dao.insertHabit(habit)
    suspend fun updateHabit(habit: HabitEntity) = dao.updateHabit(habit)

    suspend fun insertFocusSession(session: FocusSessionEntity): Long = dao.insertFocusSession(session)

    // Notification Preferences & Schedules
    fun getNotificationPreferences(userId: String) = dao.getNotificationPreferences(userId)
    suspend fun updateNotificationPreference(pref: com.example.data.notification.NotificationPreferenceEntity) = dao.insertNotificationPreference(pref)
    suspend fun insertNotificationPreferences(prefs: List<com.example.data.notification.NotificationPreferenceEntity>) = dao.insertNotificationPreferences(prefs)
    fun getUpcomingSchedules(userId: String) = dao.getUpcomingSchedules(userId)
    fun getRecentLogs(userId: String) = dao.getRecentLogs(userId)
}
