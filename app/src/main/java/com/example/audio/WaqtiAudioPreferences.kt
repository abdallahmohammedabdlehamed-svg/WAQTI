package com.example.audio

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime

/**
 * Centralized, persistent user audio and notification preferences for WAQTI Audio 2.0.
 */
class WaqtiAudioPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class AudioSettingsState(
        val masterNotificationsEnabled: Boolean = true,
        val masterSoundEnabled: Boolean = true,
        val masterVibrationEnabled: Boolean = true,
        val taskReminderSoundEnabled: Boolean = true,
        val taskCompletionSoundEnabled: Boolean = true,
        val focusSoundEnabled: Boolean = true,
        val aiNotificationSoundEnabled: Boolean = true,
        val quranReminderSoundEnabled: Boolean = true,
        val azkarReminderSoundEnabled: Boolean = true,
        val prayerNotificationEnabled: Boolean = true,
        val adhanEnabledGlobal: Boolean = true,
        val fajrAdhanEnabled: Boolean = true,
        val dhuhrAdhanEnabled: Boolean = true,
        val asrAdhanEnabled: Boolean = true,
        val maghribAdhanEnabled: Boolean = true,
        val ishaAdhanEnabled: Boolean = true,
        val adhanVolume: Float = 0.8f,
        val prePrayerReminderMinutes: Int = 15,
        val quietHoursEnabled: Boolean = false,
        val quietHoursStart: String = "22:00",
        val quietHoursEnd: String = "07:00",
        val quietHoursAllowPrayers: Boolean = true,
        val quietHoursAllowCritical: Boolean = true,
        val selectedTone: String = "DEFAULT",
        val customAdhanUri: String? = null,
        val customAdhanFileName: String? = null,
        val customAdhanDuration: Long? = null,
        val customAdhanEnabled: Boolean = false
    )

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AudioSettingsState> = _settingsFlow.asStateFlow()

    fun getSettings(): AudioSettingsState = _settingsFlow.value

    private fun loadSettings(): AudioSettingsState {
        return AudioSettingsState(
            masterNotificationsEnabled = prefs.getBoolean(KEY_MASTER_NOTIFICATIONS, true),
            masterSoundEnabled = prefs.getBoolean(KEY_MASTER_SOUND, true),
            masterVibrationEnabled = prefs.getBoolean(KEY_MASTER_VIBRATION, true),
            taskReminderSoundEnabled = prefs.getBoolean(KEY_TASK_REMINDER_SOUND, true),
            taskCompletionSoundEnabled = prefs.getBoolean(KEY_TASK_COMPLETION_SOUND, true),
            focusSoundEnabled = prefs.getBoolean(KEY_FOCUS_SOUND, true),
            aiNotificationSoundEnabled = prefs.getBoolean(KEY_AI_NOTIFICATION_SOUND, true),
            quranReminderSoundEnabled = prefs.getBoolean(KEY_QURAN_REMINDER_SOUND, true),
            azkarReminderSoundEnabled = prefs.getBoolean(KEY_AZKAR_REMINDER_SOUND, true),
            prayerNotificationEnabled = prefs.getBoolean(KEY_PRAYER_NOTIFICATIONS, true),
            adhanEnabledGlobal = prefs.getBoolean(KEY_ADHAN_GLOBAL, true),
            fajrAdhanEnabled = prefs.getBoolean(KEY_FAJR_ADHAN, true),
            dhuhrAdhanEnabled = prefs.getBoolean(KEY_DHUHR_ADHAN, true),
            asrAdhanEnabled = prefs.getBoolean(KEY_ASR_ADHAN, true),
            maghribAdhanEnabled = prefs.getBoolean(KEY_MAGHRIB_ADHAN, true),
            ishaAdhanEnabled = prefs.getBoolean(KEY_ISHA_ADHAN, true),
            adhanVolume = prefs.getFloat(KEY_ADHAN_VOLUME, 0.8f).coerceIn(0.0f, 1.0f),
            prePrayerReminderMinutes = prefs.getInt(KEY_PRE_PRAYER_REMINDER_MINUTES, 15),
            quietHoursEnabled = prefs.getBoolean(KEY_QUIET_HOURS_ENABLED, false),
            quietHoursStart = prefs.getString(KEY_QUIET_HOURS_START, "22:00") ?: "22:00",
            quietHoursEnd = prefs.getString(KEY_QUIET_HOURS_END, "07:00") ?: "07:00",
            quietHoursAllowPrayers = prefs.getBoolean(KEY_QUIET_HOURS_ALLOW_PRAYERS, true),
            quietHoursAllowCritical = prefs.getBoolean(KEY_QUIET_HOURS_ALLOW_CRITICAL, true),
            selectedTone = prefs.getString(KEY_SELECTED_TONE, "DEFAULT") ?: "DEFAULT",
            customAdhanUri = prefs.getString(KEY_CUSTOM_ADHAN_URI, null),
            customAdhanFileName = prefs.getString(KEY_CUSTOM_ADHAN_NAME, null),
            customAdhanDuration = if (prefs.contains(KEY_CUSTOM_ADHAN_DURATION)) prefs.getLong(KEY_CUSTOM_ADHAN_DURATION, 0L) else null,
            customAdhanEnabled = prefs.getBoolean(KEY_CUSTOM_ADHAN_ENABLED, false)
        )
    }

    fun updateSettings(transform: (AudioSettingsState) -> AudioSettingsState) {
        val current = _settingsFlow.value
        val updated = transform(current)
        prefs.edit().apply {
            putBoolean(KEY_MASTER_NOTIFICATIONS, updated.masterNotificationsEnabled)
            putBoolean(KEY_MASTER_SOUND, updated.masterSoundEnabled)
            putBoolean(KEY_MASTER_VIBRATION, updated.masterVibrationEnabled)
            putBoolean(KEY_TASK_REMINDER_SOUND, updated.taskReminderSoundEnabled)
            putBoolean(KEY_TASK_COMPLETION_SOUND, updated.taskCompletionSoundEnabled)
            putBoolean(KEY_FOCUS_SOUND, updated.focusSoundEnabled)
            putBoolean(KEY_AI_NOTIFICATION_SOUND, updated.aiNotificationSoundEnabled)
            putBoolean(KEY_QURAN_REMINDER_SOUND, updated.quranReminderSoundEnabled)
            putBoolean(KEY_AZKAR_REMINDER_SOUND, updated.azkarReminderSoundEnabled)
            putBoolean(KEY_PRAYER_NOTIFICATIONS, updated.prayerNotificationEnabled)
            putBoolean(KEY_ADHAN_GLOBAL, updated.adhanEnabledGlobal)
            putBoolean(KEY_FAJR_ADHAN, updated.fajrAdhanEnabled)
            putBoolean(KEY_DHUHR_ADHAN, updated.dhuhrAdhanEnabled)
            putBoolean(KEY_ASR_ADHAN, updated.asrAdhanEnabled)
            putBoolean(KEY_MAGHRIB_ADHAN, updated.maghribAdhanEnabled)
            putBoolean(KEY_ISHA_ADHAN, updated.ishaAdhanEnabled)
            putFloat(KEY_ADHAN_VOLUME, updated.adhanVolume)
            putInt(KEY_PRE_PRAYER_REMINDER_MINUTES, updated.prePrayerReminderMinutes)
            putBoolean(KEY_QUIET_HOURS_ENABLED, updated.quietHoursEnabled)
            putString(KEY_QUIET_HOURS_START, updated.quietHoursStart)
            putString(KEY_QUIET_HOURS_END, updated.quietHoursEnd)
            putBoolean(KEY_QUIET_HOURS_ALLOW_PRAYERS, updated.quietHoursAllowPrayers)
            putBoolean(KEY_QUIET_HOURS_ALLOW_CRITICAL, updated.quietHoursAllowCritical)
            putString(KEY_SELECTED_TONE, updated.selectedTone)
            if (updated.customAdhanUri != null) putString(KEY_CUSTOM_ADHAN_URI, updated.customAdhanUri) else remove(KEY_CUSTOM_ADHAN_URI)
            if (updated.customAdhanFileName != null) putString(KEY_CUSTOM_ADHAN_NAME, updated.customAdhanFileName) else remove(KEY_CUSTOM_ADHAN_NAME)
            if (updated.customAdhanDuration != null) putLong(KEY_CUSTOM_ADHAN_DURATION, updated.customAdhanDuration) else remove(KEY_CUSTOM_ADHAN_DURATION)
            putBoolean(KEY_CUSTOM_ADHAN_ENABLED, updated.customAdhanEnabled)
            apply()
        }
        _settingsFlow.value = updated
    }

    /**
     * Stores user-selected custom Adhan details and enables it.
     */
    fun setCustomAdhan(uri: String, fileName: String, duration: Long?) {
        updateSettings {
            it.copy(
                customAdhanUri = uri,
                customAdhanFileName = fileName,
                customAdhanDuration = duration,
                customAdhanEnabled = true
            )
        }
    }

    /**
     * Clears user-selected custom Adhan and restores built-in default Adhan.
     */
    fun removeCustomAdhan() {
        updateSettings {
            it.copy(
                customAdhanUri = null,
                customAdhanFileName = null,
                customAdhanDuration = null,
                customAdhanEnabled = false
            )
        }
    }

    /**
     * Checks if Adhan is allowed for a specific prayer name using standardized WaqtiPrayer enum.
     */
    fun isAdhanEnabledForPrayer(prayerName: String): Boolean {
        val current = _settingsFlow.value
        if (!current.masterNotificationsEnabled || !current.adhanEnabledGlobal) return false
        val prayer = com.example.data.prayer.WaqtiPrayer.fromString(prayerName)
        return when (prayer) {
            com.example.data.prayer.WaqtiPrayer.FAJR -> current.fajrAdhanEnabled
            com.example.data.prayer.WaqtiPrayer.DHUHR -> current.dhuhrAdhanEnabled
            com.example.data.prayer.WaqtiPrayer.ASR -> current.asrAdhanEnabled
            com.example.data.prayer.WaqtiPrayer.MAGHRIB -> current.maghribAdhanEnabled
            com.example.data.prayer.WaqtiPrayer.ISHA -> current.ishaAdhanEnabled
            null -> current.adhanEnabledGlobal
        }
    }

    /**
     * Prevents duplicate Adhan playback if alarms or receivers fire repeatedly for the same prayer.
     * Evaluates a unique key based on date and prayer identifier, with a 20-minute suppression window.
     */
    fun canPlayAdhan(prayerName: String, nowMillis: Long = System.currentTimeMillis()): Boolean {
        val prayer = com.example.data.prayer.WaqtiPrayer.fromString(prayerName)
        val prayerId = prayer?.id ?: prayerName.trim().lowercase()
        val todayStr = java.time.LocalDate.now().toString()
        val key = "${todayStr}_$prayerId"
        val lastKey = prefs.getString(KEY_LAST_PLAYED_ADHAN_KEY, "") ?: ""
        val lastTime = prefs.getLong(KEY_LAST_PLAYED_ADHAN_TIME, 0L)

        if (lastKey == key && (nowMillis - lastTime) < 20 * 60 * 1000L) {
            return false
        }
        return true
    }

    /**
     * Records the last played Adhan key and timestamp to enforce duplicate prevention.
     */
    fun markAdhanPlayed(prayerName: String, nowMillis: Long = System.currentTimeMillis()) {
        val prayer = com.example.data.prayer.WaqtiPrayer.fromString(prayerName)
        val prayerId = prayer?.id ?: prayerName.trim().lowercase()
        val todayStr = java.time.LocalDate.now().toString()
        val key = "${todayStr}_$prayerId"
        prefs.edit()
            .putString(KEY_LAST_PLAYED_ADHAN_KEY, key)
            .putLong(KEY_LAST_PLAYED_ADHAN_TIME, nowMillis)
            .apply()
    }

    /**
     * Checks whether the current time falls inside quiet hours.
     */
    fun isInQuietHours(now: LocalTime = LocalTime.now()): Boolean {
        val current = _settingsFlow.value
        if (!current.quietHoursEnabled) return false
        return try {
            val start = LocalTime.parse(current.quietHoursStart)
            val end = LocalTime.parse(current.quietHoursEnd)
            if (start.isBefore(end)) {
                // e.g. 13:00 to 15:00
                now.isAfter(start) && now.isBefore(end)
            } else {
                // e.g. 22:00 to 07:00 (crosses midnight)
                now.isAfter(start) || now.isBefore(end)
            }
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val PREFS_NAME = "waqti_audio_2_prefs"

        private const val KEY_MASTER_NOTIFICATIONS = "master_notifications"
        private const val KEY_MASTER_SOUND = "master_sound"
        private const val KEY_MASTER_VIBRATION = "master_vibration"
        private const val KEY_TASK_REMINDER_SOUND = "task_reminder_sound"
        private const val KEY_TASK_COMPLETION_SOUND = "task_completion_sound"
        private const val KEY_FOCUS_SOUND = "focus_sound"
        private const val KEY_AI_NOTIFICATION_SOUND = "ai_notification_sound"
        private const val KEY_QURAN_REMINDER_SOUND = "quran_reminder_sound"
        private const val KEY_AZKAR_REMINDER_SOUND = "azkar_reminder_sound"
        private const val KEY_PRAYER_NOTIFICATIONS = "prayer_notifications"
        private const val KEY_ADHAN_GLOBAL = "adhan_global"
        private const val KEY_FAJR_ADHAN = "fajr_adhan"
        private const val KEY_DHUHR_ADHAN = "dhuhr_adhan"
        private const val KEY_ASR_ADHAN = "asr_adhan"
        private const val KEY_MAGHRIB_ADHAN = "maghrib_adhan"
        private const val KEY_ISHA_ADHAN = "isha_adhan"
        private const val KEY_ADHAN_VOLUME = "adhan_volume"
        private const val KEY_LAST_PLAYED_ADHAN_KEY = "last_played_adhan_key"
        private const val KEY_LAST_PLAYED_ADHAN_TIME = "last_played_adhan_time"
        private const val KEY_PRE_PRAYER_REMINDER_MINUTES = "pre_prayer_reminder_minutes"
        private const val KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled"
        private const val KEY_QUIET_HOURS_START = "quiet_hours_start"
        private const val KEY_QUIET_HOURS_END = "quiet_hours_end"
        private const val KEY_QUIET_HOURS_ALLOW_PRAYERS = "quiet_hours_allow_prayers"
        private const val KEY_QUIET_HOURS_ALLOW_CRITICAL = "quiet_hours_allow_critical"
        private const val KEY_SELECTED_TONE = "selected_tone"
        private const val KEY_CUSTOM_ADHAN_URI = "custom_adhan_uri"
        private const val KEY_CUSTOM_ADHAN_NAME = "custom_adhan_name"
        private const val KEY_CUSTOM_ADHAN_DURATION = "custom_adhan_duration"
        private const val KEY_CUSTOM_ADHAN_ENABLED = "custom_adhan_enabled"

        @Volatile
        private var INSTANCE: WaqtiAudioPreferences? = null

        fun getInstance(context: Context): WaqtiAudioPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WaqtiAudioPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
