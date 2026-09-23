package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FocusSessionEntity
import com.example.data.local.HabitEntity
import com.example.data.local.RoutineEntity
import com.example.data.local.TaskEntity
import com.example.data.local.WaqtiDatabase
import com.example.data.prayer.PrayerCalculator
import com.example.data.prayer.PrayerTime
import com.example.data.repository.WaqtiRepository
import com.example.data.spiritual.QuranAzkarData
import com.example.domain.ai.AiActionPayload
import com.example.domain.ai.AiChatMessage
import com.example.domain.ai.RescheduleResult
import com.example.domain.ai.WaqtiAiContext
import com.example.domain.ai.WaqtiAiEngine
import com.example.domain.ai.WaqtiAiService
import com.example.localization.AppLanguage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WaqtiViewModel(application: Application) : AndroidViewModel(application) {
    private val database = WaqtiDatabase.getInstance(application)
    private val repository = WaqtiRepository(database.waqtiDao())

    // WAQTI Audio 2.0 Engine
    val audioManager = com.example.audio.WaqtiAudioManager.getInstance(application)
    val audioPreferences = com.example.audio.WaqtiAudioPreferences.getInstance(application)
    val audioSettings: StateFlow<com.example.audio.WaqtiAudioPreferences.AudioSettingsState> = audioPreferences.settingsFlow

    // User & Authentication State
    val currentUser: StateFlow<com.example.data.local.UserEntity?> = repository.currentUser
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    val tasks: StateFlow<List<TaskEntity>> = currentUser
        .flatMapLatest { user ->
            val uid = user?.id ?: "user_default_01"
            repository.getTasksForUser(uid)
        }
        .map { rawTasks ->
            // Prevent duplicate tasks by normalized title (trimmed, case-insensitive)
            rawTasks.groupBy { it.title.trim().lowercase() }
                .map { (_, group) ->
                    group.maxWithOrNull(
                        compareBy<TaskEntity> { task ->
                            when (task.status) {
                                "IN_PROGRESS" -> 3
                                "PLANNED" -> 2
                                "COMPLETED" -> 1
                                else -> 0
                            }
                        }.thenBy { it.id }
                    ) ?: group.first()
                }
                .sortedBy { it.startTime }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routines: StateFlow<List<RoutineEntity>> = repository.allRoutines
        .map { rawRoutines ->
            rawRoutines.groupBy {
                val cleanAr = it.titleAr.trim().lowercase()
                val cleanEn = it.title.trim().lowercase()
                "${it.time.trim()}::$cleanAr::$cleanEn"
            }.map { (_, group) ->
                group.maxWithOrNull(
                    compareBy<RoutineEntity> { if (it.isCompleted) 1 else 0 }
                        .thenBy { it.streak }
                        .thenBy { it.id }
                ) ?: group.first()
            }.sortedBy { it.time }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusSessions: StateFlow<List<FocusSessionEntity>> = repository.allFocusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFocusMinutes: StateFlow<Int?> = repository.totalFocusMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 135)

    init {
        viewModelScope.launch {
            repository.ensureDefaultUserExists()
        }
    }

    private val _isUserAuthenticated = MutableStateFlow(true)
    val isUserAuthenticated: StateFlow<Boolean> = _isUserAuthenticated.asStateFlow()

    private val _showAuthScreen = MutableStateFlow(false)
    val showAuthScreen: StateFlow<Boolean> = _showAuthScreen.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    // App Language State
    private val _language = MutableStateFlow(AppLanguage.ARABIC)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Navigation Tab (0: Home, 1: Today, 2: Program, 3: Focus, 4: Tasks, 5: More)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Focus Session State
    private val _focusRemainingSeconds = MutableStateFlow(25 * 60)
    val focusRemainingSeconds: StateFlow<Int> = _focusRemainingSeconds.asStateFlow()

    private val _isFocusRunning = MutableStateFlow(false)
    val isFocusRunning: StateFlow<Boolean> = _isFocusRunning.asStateFlow()

    private val _focusMode = MutableStateFlow("POMODORO") // POMODORO, DEEP_WORK, CUSTOM
    val focusMode: StateFlow<String> = _focusMode.asStateFlow()

    private val _activeFocusTask = MutableStateFlow("العمل على الـPortfolio")
    val activeFocusTask: StateFlow<String> = _activeFocusTask.asStateFlow()

    private val _ambientSound = MutableStateFlow<String?>("Rain")
    val ambientSound: StateFlow<String?> = _ambientSound.asStateFlow()

    private var timerJob: Job? = null

    // Reschedule & "I'm Behind" Result Dialog
    private val _rescheduleResult = MutableStateFlow<RescheduleResult?>(null)
    val rescheduleResult: StateFlow<RescheduleResult?> = _rescheduleResult.asStateFlow()

    // Task Breakdown State
    private val _breakdownTask = MutableStateFlow<TaskEntity?>(null)
    val breakdownTask: StateFlow<TaskEntity?> = _breakdownTask.asStateFlow()

    private val _breakdownSteps = MutableStateFlow<List<String>>(emptyList())
    val breakdownSteps: StateFlow<List<String>> = _breakdownSteps.asStateFlow()

    // AI Chat State
    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                sender = "WAQTI",
                textAr = "مرحبًا بك في وقتي 👋! أنا مساعدك الذكي المتطور (WAQTI AI 2.0). يمكنك سؤالي في أي مجال (برمجة، دراسة، كتابة، تقنية) أو طلبي لتنظيم يومك وحماية أوقات صلواتك وراحتك بذكاء. كيف أساعدك اليوم؟",
                textEn = "Welcome to WAQTI 👋! I'm your advanced AI assistant (WAQTI AI 2.0). Ask me anything (coding, studying, writing, tech) or let me smartly organize your day and protect your prayer and rest times. How can I help?"
            )
        )
    )
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    // Quran Ayah Index & Dhikr Counter
    private val _ayahIndex = MutableStateFlow(0)
    val ayahIndex: StateFlow<Int> = _ayahIndex.asStateFlow()

    private val _dhikrCount = MutableStateFlow(17)
    val dhikrCount: StateFlow<Int> = _dhikrCount.asStateFlow()

    private val _selectedDhikr = MutableStateFlow(0)
    val selectedDhikr: StateFlow<Int> = _selectedDhikr.asStateFlow()

    private val _dhikrTarget = MutableStateFlow(33)
    val dhikrTarget: StateFlow<Int> = _dhikrTarget.asStateFlow()

    private val _dhikrStreak = MutableStateFlow(7)
    val dhikrStreak: StateFlow<Int> = _dhikrStreak.asStateFlow()

    private val _dhikrTotalToday = MutableStateFlow(132)
    val dhikrTotalToday: StateFlow<Int> = _dhikrTotalToday.asStateFlow()

    private val _milestoneCelebration = MutableStateFlow<String?>(null)
    val milestoneCelebration: StateFlow<String?> = _milestoneCelebration.asStateFlow()

    private val _azkarRemainingMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val azkarRemainingMap: StateFlow<Map<String, Int>> = _azkarRemainingMap.asStateFlow()

    private val _showQiblaCompass = MutableStateFlow(false)
    val showQiblaCompass: StateFlow<Boolean> = _showQiblaCompass.asStateFlow()

    private val _preAthanAlertOffsetMinutes = MutableStateFlow(10)
    val preAthanAlertOffsetMinutes: StateFlow<Int> = _preAthanAlertOffsetMinutes.asStateFlow()

    // Modals & UI Toggles
    private val _showPricingModal = MutableStateFlow(false)
    val showPricingModal: StateFlow<Boolean> = _showPricingModal.asStateFlow()

    private val _showAdminDashboard = MutableStateFlow(false)
    val showAdminDashboard: StateFlow<Boolean> = _showAdminDashboard.asStateFlow()

    private val _showNotificationCenter = MutableStateFlow(false)
    val showNotificationCenter: StateFlow<Boolean> = _showNotificationCenter.asStateFlow()

    private val _showOnboarding = MutableStateFlow(false)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    private val _userPlan = MutableStateFlow("WAQTI PRO (7 days trial)")
    val userPlan: StateFlow<String> = _userPlan.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _requestPermissionTrigger = MutableStateFlow(0)
    val requestPermissionTrigger: StateFlow<Int> = _requestPermissionTrigger.asStateFlow()

    init {
        // 1. Ensure notification channels are ready and clear any old duplicate notification spam
        try {
            com.example.notification.WaqtiNotificationChannels.createChannels(application)
            com.example.notification.WaqtiNotificationPoster.cancelAllActiveNotifications(application)
            checkNotificationStatus()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Clean up any existing duplicate tasks and routines in the database, and initialize sample data if empty
        viewModelScope.launch {
            cleanUpDuplicateTasks()
            cleanUpDuplicateRoutines()
            delay(200)
            if (repository.getTaskCount() == 0 && repository.getRoutineCount() == 0) {
                WaqtiDatabase.populateInitialData(database.waqtiDao())
            }
            // Auto schedule today's notifications
            recalculateNotifications()
        }
    }

    /**
     * Finds and deletes any duplicate tasks in the local Room database,
     * ensuring each task title exists only once per user.
     */
    fun cleanUpDuplicateTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allTasks = repository.getAllTasksDirect()
                if (allTasks.size <= 1) return@launch

                val grouped = allTasks.groupBy {
                    "${it.userId.trim().lowercase()}::${it.title.trim().lowercase()}"
                }

                val duplicatesToDelete = mutableListOf<Long>()

                for ((_, group) in grouped) {
                    if (group.size > 1) {
                        // Keep the best entry: prefer IN_PROGRESS, then PLANNED, then COMPLETED, break ties with highest id
                        val toKeep = group.maxWithOrNull(
                            compareBy<TaskEntity> { task ->
                                when (task.status) {
                                    "IN_PROGRESS" -> 3
                                    "PLANNED" -> 2
                                    "COMPLETED" -> 1
                                    else -> 0
                                }
                            }.thenBy { it.id }
                        ) ?: group.first()

                        val toRemove = group.filter { it.id != toKeep.id }.map { it.id }
                        duplicatesToDelete.addAll(toRemove)
                    }
                }

                if (duplicatesToDelete.isNotEmpty()) {
                    repository.deleteTasksByIds(duplicatesToDelete)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Finds and deletes any duplicate routines (البرنامج اليومي) in the local Room database,
     * ensuring each routine title/time exists only once.
     */
    fun cleanUpDuplicateRoutines() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allRoutines = repository.getAllRoutinesDirect()
                if (allRoutines.size <= 1) return@launch

                val grouped = allRoutines.groupBy {
                    val cleanAr = it.titleAr.trim().lowercase()
                    val cleanEn = it.title.trim().lowercase()
                    "${it.time.trim()}::$cleanAr::$cleanEn"
                }

                val duplicatesToDelete = mutableListOf<Long>()

                for ((_, group) in grouped) {
                    if (group.size > 1) {
                        val toKeep = group.maxWithOrNull(
                            compareBy<RoutineEntity> { if (it.isCompleted) 1 else 0 }
                                .thenBy { it.streak }
                                .thenBy { it.id }
                        ) ?: group.first()

                        val toRemove = group.filter { it.id != toKeep.id }.map { it.id }
                        duplicatesToDelete.addAll(toRemove)
                    }
                }

                if (duplicatesToDelete.isNotEmpty()) {
                    repository.deleteRoutinesByIds(duplicatesToDelete)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun checkNotificationStatus() {
        val app = getApplication<Application>()
        val areEnabled = androidx.core.app.NotificationManagerCompat.from(app).areNotificationsEnabled()
        _notificationsEnabled.value = areEnabled
    }

    fun triggerRequestNotificationPermission() {
        _requestPermissionTrigger.value += 1
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        _notificationsEnabled.value = granted
        if (granted) {
            recalculateNotifications()
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun setCurrentTab(tab: Int) {
        _currentTab.value = tab
    }

    // Task Actions
    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val updated = if (task.status == "COMPLETED") {
                task.copy(status = "PLANNED")
            } else {
                audioManager.playTaskCompleteSound()
                task.copy(status = "COMPLETED")
            }
            repository.updateTask(updated)
        }
    }

    fun addTask(title: String, priority: String, durationMin: Int, category: String): Boolean {
        val trimmed = title.trim()
        if (trimmed.isBlank()) return false
        
        // Prevent adding duplicate task with same title (case-insensitive)
        val alreadyExists = tasks.value.any { it.title.trim().equals(trimmed, ignoreCase = true) }
        if (alreadyExists) {
            return false
        }

        val currentUserId = currentUser.value?.id ?: "user_default_01"
        viewModelScope.launch {
            val newTask = TaskEntity(
                userId = currentUserId,
                title = trimmed,
                priority = priority,
                durationMinutes = durationMin,
                category = category,
                status = "PLANNED",
                startTime = "15:00",
                endTime = "15:45"
            )
            repository.insertTask(newTask)
        }
        return true
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Routine & Habit Actions
    fun toggleRoutineCompletion(routine: RoutineEntity) {
        viewModelScope.launch {
            val updated = routine.copy(
                isCompleted = !routine.isCompleted,
                streak = if (!routine.isCompleted) routine.streak + 1 else maxOf(0, routine.streak - 1)
            )
            repository.updateRoutine(updated)
        }
    }

    fun toggleHabitCompletion(habit: HabitEntity) {
        viewModelScope.launch {
            val updated = habit.copy(
                completedToday = !habit.completedToday,
                streak = if (!habit.completedToday) habit.streak + 1 else maxOf(0, habit.streak - 1)
            )
            repository.updateHabit(updated)
        }
    }

    // Smart Rescheduling Trigger
    fun triggerSmartReschedule() {
        val currentTaskList = tasks.value
        val routineList = routines.value
        val result = WaqtiAiEngine.computeSmartReschedule(currentTaskList, routineList, _language.value)
        _rescheduleResult.value = result
    }

    // "I'm Behind" Trigger
    fun triggerImBehind() {
        val currentTaskList = tasks.value
        val result = WaqtiAiEngine.handleImBehind(currentTaskList, _language.value)
        _rescheduleResult.value = result
    }

    fun applyReschedule() {
        val result = _rescheduleResult.value ?: return
        val currentUserId = currentUser.value?.id ?: "user_default_01"
        viewModelScope.launch {
            val tasksWithUserId = result.updatedTasks.map { it.copy(userId = currentUserId) }
            repository.insertTasks(tasksWithUserId)
            _rescheduleResult.value = null
        }
    }

    fun dismissReschedule() {
        _rescheduleResult.value = null
    }

    // Task Breakdown Trigger
    fun triggerTaskBreakdown(task: TaskEntity) {
        _breakdownTask.value = task
        _breakdownSteps.value = WaqtiAiEngine.breakdownTask(task.title)

        // Enhance dynamically with Gemini
        viewModelScope.launch {
            try {
                val dynamicSteps = WaqtiAiService.breakdownTaskDynamic(task.title, _language.value)
                if (dynamicSteps.isNotEmpty() && _breakdownTask.value?.id == task.id) {
                    _breakdownSteps.value = dynamicSteps
                }
            } catch (e: Exception) {
                // Keep local deterministic steps
            }
        }
    }

    fun dismissBreakdown() {
        _breakdownTask.value = null
        _breakdownSteps.value = emptyList()
    }

    fun applyBreakdownSubtasks() {
        val task = _breakdownTask.value ?: return
        val steps = _breakdownSteps.value
        if (steps.isNotEmpty()) {
            viewModelScope.launch {
                val updated = task.copy(subtasksRaw = steps.joinToString("\n"))
                repository.updateTask(updated)
                dismissBreakdown()
            }
        }
    }

    // Focus Session Controls
    fun startFocusSession(taskTitle: String, mode: String = "POMODORO") {
        _activeFocusTask.value = taskTitle
        _focusMode.value = mode
        _focusRemainingSeconds.value = when (mode) {
            "POMODORO" -> 25 * 60
            "DEEP_WORK" -> 50 * 60
            else -> 90 * 60
        }
        _isFocusRunning.value = true
        audioManager.playFocusStartSound()

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isFocusRunning.value && _focusRemainingSeconds.value > 0) {
                delay(1000)
                _focusRemainingSeconds.value -= 1
                if (_focusRemainingSeconds.value == 300) {
                    // 5-minute warning sound
                    audioManager.playFocusWarningSound()
                }
            }
            if (_focusRemainingSeconds.value <= 0) {
                finishFocusSession()
            }
        }
    }

    fun toggleFocusTimer() {
        if (_isFocusRunning.value) {
            _isFocusRunning.value = false
            timerJob?.cancel()
        } else {
            _isFocusRunning.value = true
            timerJob = viewModelScope.launch {
                while (_isFocusRunning.value && _focusRemainingSeconds.value > 0) {
                    delay(1000)
                    _focusRemainingSeconds.value -= 1
                    if (_focusRemainingSeconds.value == 300) {
                        audioManager.playFocusWarningSound()
                    }
                }
                if (_focusRemainingSeconds.value <= 0) {
                    finishFocusSession()
                }
            }
        }
    }

    fun finishFocusSession() {
        _isFocusRunning.value = false
        timerJob?.cancel()
        audioManager.playFocusCompleteSound()
        val totalSec = when (_focusMode.value) {
            "POMODORO" -> 25 * 60
            "DEEP_WORK" -> 50 * 60
            else -> 90 * 60
        }
        val elapsedMinutes = maxOf(1, (totalSec - _focusRemainingSeconds.value) / 60)
        viewModelScope.launch {
            repository.insertFocusSession(
                FocusSessionEntity(
                    taskTitle = _activeFocusTask.value,
                    durationMinutes = elapsedMinutes,
                    mode = _focusMode.value
                )
            )
            _focusRemainingSeconds.value = 25 * 60
        }
    }

    fun setAmbientSound(sound: String?) {
        _ambientSound.value = sound
        audioManager.setFocusAmbientSound(sound)
    }

    fun setAmbientVolume(volume: Float) {
        audioManager.setFocusAmbientVolume(volume)
    }

    // AI Chat Interaction (WAQTI AI 2.0 with Gemini & Context)
    fun sendAiMessage(query: String) {
        if (query.isBlank()) return
        val userMsg = AiChatMessage(
            sender = "USER",
            textAr = query,
            textEn = query
        )
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            try {
                val liveContext = WaqtiAiContext.build(
                    tasks = tasks.value,
                    routines = routines.value,
                    nextPrayer = try { getNextPrayer() } catch (e: Exception) { null },
                    isFocusRunning = _isFocusRunning.value,
                    activeFocusTask = _activeFocusTask.value,
                    language = _language.value
                )

                val reply = WaqtiAiEngine.queryAiAssistant(
                    query = query,
                    history = _aiMessages.value,
                    context = liveContext,
                    lang = _language.value
                )
                _aiMessages.value = _aiMessages.value + reply
            } catch (e: Exception) {
                val fallback = WaqtiAiEngine.getAssistantResponse(query, _language.value)
                _aiMessages.value = _aiMessages.value + fallback
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun executeAiAction(action: AiActionPayload, messageId: String) {
        viewModelScope.launch {
            when (action.actionType) {
                "CREATE_TASK" -> {
                    val uid = currentUser.value?.id ?: "user_default_01"
                    val newTask = TaskEntity(
                        userId = uid,
                        title = action.title ?: "مهمة جديدة",
                        description = "تمت إضافتها عبر مساعد وقتي الذكي",
                        status = "PLANNED",
                        priority = action.priority ?: "MEDIUM",
                        category = action.category ?: "WORK",
                        durationMinutes = action.durationMinutes ?: 45,
                        startTime = action.startTime ?: "16:00",
                        endTime = "16:45"
                    )
                    repository.insertTask(newTask)
                }
                "APPLY_RESCHEDULE" -> {
                    triggerSmartReschedule()
                }
                "IM_BEHIND" -> {
                    triggerImBehind()
                }
                "START_FOCUS" -> {
                    val taskName = action.title ?: (_activeFocusTask.value ?: "جلسة تركيز")
                    startFocusSession(taskName)
                    _currentTab.value = 3 // Focus tab
                }
                "BREAKDOWN_TASK" -> {
                    val uid = currentUser.value?.id ?: "user_default_01"
                    val steps = action.breakdownSteps ?: emptyList()
                    val newTask = TaskEntity(
                        userId = uid,
                        title = action.title ?: "مشروع جديد",
                        description = "خطة عمل مجزأة بالذكاء الاصطناعي",
                        status = "PLANNED",
                        priority = "HIGH",
                        category = "WORK",
                        durationMinutes = 60,
                        subtasksRaw = steps.joinToString("\n")
                    )
                    repository.insertTask(newTask)
                }
            }

            // Mark action as executed in the conversation
            _aiMessages.value = _aiMessages.value.map { msg ->
                if (msg.id == messageId && msg.actionPayload != null) {
                    msg.copy(actionPayload = msg.actionPayload.copy(isExecuted = true))
                } else {
                    msg
                }
            }
        }
    }

    fun clearAiChat() {
        _aiMessages.value = listOf(
            AiChatMessage(
                sender = "WAQTI",
                textAr = "تم بدء محادثة جديدة مع مساعد وقتي الذكي 2.0. يمكنك سؤالي في أي موضوع عام (برمجة، دراسة، كتابة، نصائح) أو تنظيم مهامك وجدولك بذكاء. كيف أساعدك الآن؟",
                textEn = "Started a fresh session with Waqti AI Assistant 2.0. You can ask me anything (coding, studying, writing, advice) or smartly manage your tasks and schedule. How can I help?"
            )
        )
    }

    // User Authentication Methods
    fun setShowAuthScreen(show: Boolean) {
        _showAuthScreen.value = show
        _authErrorMessage.value = null
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _authErrorMessage.value = if (_language.value == AppLanguage.ARABIC) "يرجى إدخال بريد إلكتروني صحيح" else "Please enter a valid email"
            return
        }
        if (password.length < 6) {
            _authErrorMessage.value = if (_language.value == AppLanguage.ARABIC) "كلمة المرور يجب ألا تقل عن 6 أحرف" else "Password must be at least 6 characters"
            return
        }

        _isAuthLoading.value = true
        _authErrorMessage.value = null

        viewModelScope.launch {
            delay(500) // smooth authentic auth transition
            val user = repository.getUserByEmail(email.trim().lowercase())
            if (user != null) {
                if (user.passwordHash == password) {
                    repository.setActiveUserById(user.id)
                    _isUserAuthenticated.value = true
                    _showAuthScreen.value = false
                    _isAuthLoading.value = false
                    onSuccess()
                } else {
                    _isAuthLoading.value = false
                    _authErrorMessage.value = if (_language.value == AppLanguage.ARABIC) "كلمة المرور غير صحيحة" else "Incorrect password"
                }
            } else {
                // If user doesn't exist yet, automatically register and sign in with fresh isolated workspace
                val newUser = com.example.data.local.UserEntity(
                    id = "user_" + System.currentTimeMillis(),
                    name = email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() },
                    email = email.trim().lowercase(),
                    passwordHash = password,
                    plan = "PRO",
                    isActive = true
                )
                repository.switchActiveUser(newUser)
                _isUserAuthenticated.value = true
                _showAuthScreen.value = false
                _isAuthLoading.value = false
                onSuccess()
            }
        }
    }

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) {
        if (name.isBlank()) {
            _authErrorMessage.value = if (_language.value == AppLanguage.ARABIC) "يرجى إدخال اسمك الكريم" else "Please enter your name"
            return
        }
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _authErrorMessage.value = if (_language.value == AppLanguage.ARABIC) "يرجى إدخال بريد إلكتروني صحيح" else "Please enter a valid email"
            return
        }
        if (password.length < 6) {
            _authErrorMessage.value = if (_language.value == AppLanguage.ARABIC) "كلمة المرور يجب ألا تقل عن 6 أحرف" else "Password must be at least 6 characters"
            return
        }

        _isAuthLoading.value = true
        _authErrorMessage.value = null

        viewModelScope.launch {
            delay(500)
            val existing = repository.getUserByEmail(email.trim().lowercase())
            val userToActivate = if (existing != null) {
                existing.copy(name = name.trim(), passwordHash = password, isActive = true)
            } else {
                com.example.data.local.UserEntity(
                    id = "user_" + System.currentTimeMillis(),
                    name = name.trim(),
                    email = email.trim().lowercase(),
                    passwordHash = password,
                    plan = "PRO",
                    isActive = true
                )
            }
            repository.switchActiveUser(userToActivate)
            _isUserAuthenticated.value = true
            _showAuthScreen.value = false
            _isAuthLoading.value = false
            onSuccess()
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isUserAuthenticated.value = false
            _showAuthScreen.value = true
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            val guestUser = com.example.data.local.UserEntity(
                id = "user_guest",
                name = if (_language.value == AppLanguage.ARABIC) "ضيف وقتي" else "Guest",
                email = "guest@waqti.app",
                passwordHash = "guest123",
                plan = "FREE",
                isActive = true
            )
            repository.switchActiveUser(guestUser)
            _isUserAuthenticated.value = true
            _showAuthScreen.value = false
        }
    }

    // Dhikr & Ayah
    fun incrementDhikr() {
        val next = _dhikrCount.value + 1
        val target = _dhikrTarget.value
        _dhikrCount.value = if (target > 0 && next > target) 1 else next
        _dhikrTotalToday.value += 1
        if (target > 0 && next == target) {
            _milestoneCelebration.value = if (_language.value == AppLanguage.ARABIC) 
                "مبارك! أتممت $target تسبيحة ✨ تقبل الله منك" 
            else 
                "Milestone Reached! Completed $target Tasbeeh ✨"
        }
    }

    fun resetDhikr() {
        _dhikrCount.value = 0
    }

    fun setDhikrTarget(target: Int) {
        _dhikrTarget.value = target
        _dhikrCount.value = 0
    }

    fun clearMilestone() {
        _milestoneCelebration.value = null
    }

    fun decrementAzkarItem(id: String, defaultCount: Int) {
        val current = _azkarRemainingMap.value[id] ?: defaultCount
        if (current > 0) {
            val newCount = current - 1
            _azkarRemainingMap.value = _azkarRemainingMap.value + (id to newCount)
            _dhikrTotalToday.value += 1
        }
    }

    fun resetAzkarItem(id: String, defaultCount: Int) {
        _azkarRemainingMap.value = _azkarRemainingMap.value + (id to defaultCount)
    }

    fun selectNextAyah() {
        _ayahIndex.value = (_ayahIndex.value + 1) % QuranAzkarData.authenticVerses.size
    }

    fun setSelectedDhikr(index: Int) {
        _selectedDhikr.value = index
        _dhikrCount.value = 0
    }

    fun setShowQiblaCompass(show: Boolean) {
        _showQiblaCompass.value = show
    }

    fun setPreAthanAlertOffset(minutes: Int) {
        _preAthanAlertOffsetMinutes.value = minutes
        val updated = _advancedNotificationSettings.value.copy(prayerAdvanceMinutes = minutes)
        updateAdvancedSettings(updated)
    }

    // Modals
    fun setShowPricingModal(show: Boolean) { _showPricingModal.value = show }
    fun setShowAdminDashboard(show: Boolean) { _showAdminDashboard.value = show }
    fun setShowNotificationCenter(show: Boolean) { _showNotificationCenter.value = show }
    fun setShowOnboarding(show: Boolean) { _showOnboarding.value = show }

    fun upgradeToPro() {
        _userPlan.value = "WAQTI PRO (Active)"
        _showPricingModal.value = false
    }

    // Notification & Prayer Settings State
    val locationPreferences = com.example.data.prayer.WaqtiLocationPreferences.getInstance(application)
    val locationManager = com.example.data.prayer.WaqtiLocationManager(application)
    val qiblaManager = com.example.data.prayer.WaqtiQiblaManager(application)

    private val _prayerLocationAndCalcSettings = MutableStateFlow(locationPreferences.getSettings())
    val prayerLocationAndCalcSettings: StateFlow<com.example.data.notification.PrayerLocationAndCalcSettings> = _prayerLocationAndCalcSettings.asStateFlow()

    init {
        val initialLoc = locationManager.getEffectiveLocationSettings()
        _prayerLocationAndCalcSettings.value = initialLoc
        qiblaManager.updateLocation(initialLoc.latitude, initialLoc.longitude, initialLoc.cityName)
    }

    private val _advancedNotificationSettings = MutableStateFlow(com.example.data.notification.AdvancedNotificationSettings())
    val advancedNotificationSettings: StateFlow<com.example.data.notification.AdvancedNotificationSettings> = _advancedNotificationSettings.asStateFlow()

    val notificationPreferences: StateFlow<List<com.example.data.notification.NotificationPreferenceEntity>> = currentUser
        .flatMapLatest { user ->
            val uid = user?.id ?: "user_default_01"
            repository.getNotificationPreferences(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingSchedules: StateFlow<List<com.example.data.notification.NotificationScheduleEntity>> = currentUser
        .flatMapLatest { user ->
            val uid = user?.id ?: "user_default_01"
            repository.getUpcomingSchedules(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isFatigueAlert = MutableStateFlow(false)
    val isFatigueAlert: StateFlow<Boolean> = _isFatigueAlert.asStateFlow()

    private val _isNotificationEngineRecalculating = MutableStateFlow(false)
    val isNotificationEngineRecalculating: StateFlow<Boolean> = _isNotificationEngineRecalculating.asStateFlow()

    fun updateNotificationPreference(pref: com.example.data.notification.NotificationPreferenceEntity) {
        viewModelScope.launch {
            repository.updateNotificationPreference(pref)
            com.example.notification.SmartNotificationEngine.recalculateAndScheduleAll(
                context = getApplication(),
                advancedSettings = _advancedNotificationSettings.value,
                prayerCalcSettings = _prayerLocationAndCalcSettings.value
            )
        }
    }

    fun updatePrayerSettings(settings: com.example.data.notification.PrayerLocationAndCalcSettings) {
        _prayerLocationAndCalcSettings.value = settings
        locationPreferences.saveSettings(settings)
        qiblaManager.updateLocation(settings.latitude, settings.longitude, settings.cityName)
        viewModelScope.launch {
            com.example.notification.SmartNotificationEngine.recalculateAndScheduleAll(
                context = getApplication(),
                advancedSettings = _advancedNotificationSettings.value,
                prayerCalcSettings = settings
            )
        }
    }

    fun selectCityPreset(preset: com.example.data.prayer.PrayerCalculator.CityPreset) {
        val isArabic = _language.value == com.example.localization.AppLanguage.ARABIC
        locationPreferences.updateLocation(
            cityNameAr = preset.cityNameAr,
            cityNameEn = preset.cityNameEn,
            governorateAr = preset.governorateAr,
            governorateEn = preset.governorateEn,
            countryAr = preset.countryAr,
            countryEn = preset.countryEn,
            latitude = preset.latitude,
            longitude = preset.longitude,
            source = "MANUAL",
            isArabic = isArabic
        )
        val updated = locationPreferences.getSettings()
        updatePrayerSettings(updated)
    }

    fun refreshLocationFromGps(onResult: ((Boolean) -> Unit)? = null) {
        locationManager.refreshCurrentLocation { success, updatedSettings ->
            _prayerLocationAndCalcSettings.value = updatedSettings
            qiblaManager.updateLocation(updatedSettings.latitude, updatedSettings.longitude, updatedSettings.cityName)
            recalculateNotifications()
            onResult?.invoke(success)
        }
    }

    fun updateAdvancedSettings(settings: com.example.data.notification.AdvancedNotificationSettings) {
        _advancedNotificationSettings.value = settings
        viewModelScope.launch {
            com.example.notification.SmartNotificationEngine.recalculateAndScheduleAll(
                context = getApplication(),
                advancedSettings = settings,
                prayerCalcSettings = _prayerLocationAndCalcSettings.value
            )
        }
    }

    fun recalculateNotifications() {
        viewModelScope.launch {
            _isNotificationEngineRecalculating.value = true
            com.example.notification.SmartNotificationEngine.recalculateAndScheduleAll(
                context = getApplication(),
                advancedSettings = _advancedNotificationSettings.value,
                prayerCalcSettings = _prayerLocationAndCalcSettings.value
            )
            _isFatigueAlert.value = com.example.notification.SmartNotificationEngine.checkFatigueStatus(getApplication())
            _isNotificationEngineRecalculating.value = false
        }
    }

    fun sendTestNotification(category: String) {
        com.example.notification.SmartNotificationEngine.sendTestNotification(getApplication(), category)
    }

    fun getNextPrayer(): PrayerTime {
        return PrayerCalculator.getNextPrayer(
            settings = _prayerLocationAndCalcSettings.value
        )
    }

    fun getTodayPrayerTimes(): List<PrayerTime> {
        return PrayerCalculator.getPrayerTimes(
            settings = _prayerLocationAndCalcSettings.value
        )
    }

    // Audio 2.0 & Adhan Controls
    fun updateAudioSettings(transform: (com.example.audio.WaqtiAudioPreferences.AudioSettingsState) -> com.example.audio.WaqtiAudioPreferences.AudioSettingsState) {
        audioPreferences.updateSettings(transform)
    }

    fun playTestSound(type: com.example.audio.WaqtiAudioManager.AudioType) {
        audioManager.playTestSound(type)
    }

    fun playTestAdhan(prayerName: String = "Fajr") {
        android.util.Log.d("WAQTI_ADHAN_DEBUG", "Test button clicked for prayer: $prayerName")
        com.example.audio.WaqtiAdhanService.start(getApplication(), prayerName, isTest = true)
    }

    fun stopAdhan() {
        android.util.Log.d("WAQTI_ADHAN_DEBUG", "Stop Adhan requested from UI")
        com.example.audio.WaqtiAdhanService.stop(getApplication())
        audioManager.stopAll()
    }

    /**
     * Handles custom Adhan file selection from system Storage Access Framework picker.
     * Extracts display name, extracts duration, persists URI permission, and updates preferences.
     * Returns true if valid audio file was successfully processed and saved.
     */
    fun onCustomAdhanFileSelected(uri: Uri, context: Context): Boolean {
        return try {
            // Take persistable URI permission if supported
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                Log.w("WAQTI_CUSTOM_ADHAN", "takePersistableUriPermission skipped/failed: ${e.message}")
            }

            // Extract display name
            var fileName = "Custom Adhan"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        val retrieved = cursor.getString(nameIndex)
                        if (!retrieved.isNullOrBlank()) {
                            fileName = retrieved
                        }
                    }
                }
            } catch (e: Exception) {
                fileName = uri.lastPathSegment ?: "Custom Adhan"
            }

            // Validate MIME type and audio extensions
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val isAudio = mimeType.startsWith("audio/") ||
                    fileName.endsWith(".mp3", ignoreCase = true) ||
                    fileName.endsWith(".m4a", ignoreCase = true) ||
                    fileName.endsWith(".wav", ignoreCase = true) ||
                    fileName.endsWith(".aac", ignoreCase = true) ||
                    fileName.endsWith(".ogg", ignoreCase = true)

            if (!isAudio) {
                Log.w("WAQTI_CUSTOM_ADHAN", "Unsupported audio file selected: $fileName, mime: $mimeType")
                return false
            }

            // Extract duration
            var durationMs: Long? = null
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                durationMs = durStr?.toLongOrNull()
                retriever.release()
            } catch (e: Exception) {
                Log.w("WAQTI_CUSTOM_ADHAN", "Could not extract duration: ${e.message}")
            }

            Log.d("WAQTI_CUSTOM_ADHAN", "Custom Adhan selected: uri=$uri")
            Log.d("WAQTI_CUSTOM_ADHAN", "Custom Adhan name: $fileName")
            Log.d("WAQTI_CUSTOM_ADHAN", "Custom Adhan duration: ${durationMs ?: 0}ms")

            audioPreferences.setCustomAdhan(
                uri = uri.toString(),
                fileName = fileName,
                duration = durationMs
            )
            true
        } catch (e: Exception) {
            Log.e("WAQTI_CUSTOM_ADHAN", "Failed selecting custom Adhan: ${e.message}", e)
            false
        }
    }

    /**
     * Clears user-selected custom Adhan and restores built-in default Adhan.
     */
    fun removeCustomAdhan(context: Context) {
        stopAdhan()
        val currentUri = audioSettings.value.customAdhanUri
        if (!currentUri.isNullOrBlank()) {
            try {
                context.contentResolver.releasePersistableUriPermission(
                    Uri.parse(currentUri),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.w("WAQTI_CUSTOM_ADHAN", "releasePersistableUriPermission failed: ${e.message}")
            }
        }
        audioPreferences.removeCustomAdhan()
        Log.d("WAQTI_CUSTOM_ADHAN", "Custom Adhan removed. Restored built-in default Adhan.")
    }
}
