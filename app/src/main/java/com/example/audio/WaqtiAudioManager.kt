package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Centralized, lifecycle-safe Audio Manager for WAQTI Audio 2.0.
 * Handles audio focus, user preferences, fallback tone generation, vibration, and resource release.
 */
class WaqtiAudioManager private constructor(private val appContext: Context) {

    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val preferences = WaqtiAudioPreferences.getInstance(appContext)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile
    private var currentMediaPlayer: MediaPlayer? = null

    @Volatile
    private var ambientMediaPlayer: MediaPlayer? = null

    @Volatile
    private var activeFocusRequest: AudioFocusRequest? = null

    private var lastPlayTimestamp = 0L

    enum class AudioType {
        NOTIFICATION,
        REMINDER,
        TASK_COMPLETION,
        FOCUS_START,
        FOCUS_WARNING,
        FOCUS_COMPLETION,
        WARNING,
        AI_NOTIFICATION,
        QURAN_REMINDER,
        AZKAR_REMINDER,
        ADHAN
    }

    enum class VibrationType {
        SHORT,
        DOUBLE,
        CONFIRMATION,
        WARNING
    }

    /**
     * Plays the standard notification sound or tone.
     */
    fun playNotificationSound() {
        playAudio(AudioType.NOTIFICATION)
    }

    // Clean API aliases matching architecture requirements
    fun playNotification() = playNotificationSound()
    fun playReminder() = playReminderSound()
    fun playTaskComplete() = playTaskCompleteSound()
    fun playFocusStart() = playFocusStartSound()
    fun playFocusWarning() = playFocusWarningSound()
    fun playFocusComplete() = playFocusCompleteSound()
    fun playWarning() = playWarningSound()
    fun playHabitComplete() {
        playTaskCompleteSound()
    }
    fun playAiNotification() {
        val settings = preferences.getSettings()
        if (!settings.aiNotificationSoundEnabled) return
        playAudio(AudioType.AI_NOTIFICATION)
        playVibration(VibrationType.SHORT)
    }

    /**
     * Plays the reminder sound.
     */
    fun playReminderSound() {
        val settings = preferences.getSettings()
        if (!settings.taskReminderSoundEnabled) return
        playAudio(AudioType.REMINDER)
    }

    /**
     * Plays the task completion sound.
     */
    fun playTaskCompleteSound() {
        val settings = preferences.getSettings()
        if (!settings.taskCompletionSoundEnabled) return
        playAudio(AudioType.TASK_COMPLETION)
        playVibration(VibrationType.CONFIRMATION)
    }

    /**
     * Plays the focus start sound.
     */
    fun playFocusStartSound() {
        val settings = preferences.getSettings()
        if (!settings.focusSoundEnabled) return
        playAudio(AudioType.FOCUS_START)
        playVibration(VibrationType.SHORT)
    }

    /**
     * Plays the focus warning sound (e.g. 5 minutes remaining).
     */
    fun playFocusWarningSound() {
        val settings = preferences.getSettings()
        if (!settings.focusSoundEnabled) return
        playAudio(AudioType.FOCUS_WARNING)
        playVibration(VibrationType.DOUBLE)
    }

    /**
     * Plays the focus completion sound.
     */
    fun playFocusCompleteSound() {
        val settings = preferences.getSettings()
        if (!settings.focusSoundEnabled) return
        playAudio(AudioType.FOCUS_COMPLETION)
        playVibration(VibrationType.CONFIRMATION)
    }

    /**
     * Plays the warning sound.
     */
    fun playWarningSound() {
        playAudio(AudioType.WARNING)
        playVibration(VibrationType.WARNING)
    }

    /**
     * Plays a sound for testing purposes (bypasses quiet hours, but still respects system silent mode).
     */
    fun playTestSound(type: AudioType, onStarted: (() -> Unit)? = null, onCompleted: (() -> Unit)? = null) {
        stopAll()
        scope.launch {
            try {
                onStarted?.invoke()
                playAudioInternal(type, isTest = true, onCompleted = onCompleted)
            } catch (e: Exception) {
                Log.e(TAG, "Failed test sound: ${e.message}")
                onCompleted?.invoke()
            }
        }
    }

    /**
     * Dispatches audio playback based on type, checking user preferences and system state.
     */
    fun playAudio(type: AudioType) {
        scope.launch {
            playAudioInternal(type, isTest = false, onCompleted = null)
        }
    }

    private fun playAudioInternal(type: AudioType, isTest: Boolean, onCompleted: (() -> Unit)?) {
        val settings = preferences.getSettings()

        // 1. Check Master sound enabled
        if (!isTest && (!settings.masterNotificationsEnabled || !settings.masterSoundEnabled)) {
            onCompleted?.invoke()
            return
        }

        // 2. Check Quiet Hours (unless it's an explicit user test or allowed prayer/critical)
        if (!isTest && preferences.isInQuietHours()) {
            val isPrayer = (type == AudioType.ADHAN)
            if (isPrayer && !settings.quietHoursAllowPrayers) {
                onCompleted?.invoke()
                return
            }
            if (!isPrayer && !settings.quietHoursAllowCritical) {
                onCompleted?.invoke()
                return
            }
        }

        // 3. Check System Ringer Mode
        val ringerMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
        if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
            onCompleted?.invoke()
            return
        }
        if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            // Vibrate instead of playing sound
            playVibration(VibrationType.SHORT)
            onCompleted?.invoke()
            return
        }

        // Debounce multiple rapid taps
        val now = System.currentTimeMillis()
        if (!isTest && now - lastPlayTimestamp < DEBOUNCE_MS) {
            onCompleted?.invoke()
            return
        }
        lastPlayTimestamp = now

        // 4. Request Transient Audio Focus
        requestTransientAudioFocus()

        // 5. Attempt playing bundled raw resource
        val rawResName = getRawResourceNameForType(type)
        val resId = if (rawResName != null) {
            appContext.resources.getIdentifier(rawResName, "raw", appContext.packageName)
        } else 0

        if (type == AudioType.ADHAN) {
            val source = WaqtiAdhanService.resolveAdhanSource(appContext)
            when (source) {
                is WaqtiAdhanService.AdhanSource.Custom -> {
                    Log.d("WAQTI_CUSTOM_ADHAN", "Using custom Adhan in AudioManager: uri=${source.uri}")
                    try {
                        stopAll()
                        val player = MediaPlayer().apply {
                            setDataSource(appContext, source.uri)
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )
                            setOnCompletionListener { mp ->
                                try { mp.release() } catch (_: Exception) {}
                                if (currentMediaPlayer == mp) currentMediaPlayer = null
                                abandonTransientAudioFocus()
                                onCompleted?.invoke()
                            }
                            setOnErrorListener { mp, what, extra ->
                                Log.e("WAQTI_CUSTOM_ADHAN", "Custom Adhan playback failed in AudioManager: what=$what extra=$extra")
                                try { mp.release() } catch (_: Exception) {}
                                if (currentMediaPlayer == mp) currentMediaPlayer = null
                                abandonTransientAudioFocus()
                                onCompleted?.invoke()
                                true
                            }
                            prepare()
                            start()
                        }
                        currentMediaPlayer = player
                        return
                    } catch (e: Exception) {
                        Log.w("WAQTI_CUSTOM_ADHAN", "Custom Adhan unavailable in AudioManager: ${e.message}. Falling back to default Adhan.")
                    }
                }
                is WaqtiAdhanService.AdhanSource.Default -> {
                    Log.d("WAQTI_CUSTOM_ADHAN", "Using built-in default Adhan in AudioManager")
                }
            }

            // Fallback or default R.raw.waqti_adhan
            if (resId != 0) {
                Log.d("WAQTI_ADHAN_DEBUG", "Using production Adhan resource: R.raw.waqti_adhan")
                Log.d("WAQTI_ADHAN_DEBUG", "resource ID: $resId")
                Log.d("WAQTI_ADHAN_DEBUG", "file name: waqti_adhan.mp3")
                Log.d("WAQTI_ADHAN_DEBUG", "MediaPlayer data source: android.resource://${appContext.packageName}/$resId")
                playRawResource(resId, onCompleted)
            } else {
                Log.e("WAQTI_ADHAN_DEBUG", "playback error: R.raw.waqti_adhan not found. No fallback audio allowed.")
                abandonTransientAudioFocus()
                onCompleted?.invoke()
            }
            return
        }

        if (resId != 0) {
            playRawResource(resId, onCompleted)
        } else {
            // 6. Graceful procedural tone fallback for general notifications and reminders
            playProceduralFallback(type)
            onCompleted?.invoke()
        }
    }

    private fun getRawResourceNameForType(type: AudioType): String? {
        return when (type) {
            AudioType.NOTIFICATION, AudioType.AI_NOTIFICATION -> "waqti_notification"
            AudioType.REMINDER, AudioType.QURAN_REMINDER, AudioType.AZKAR_REMINDER -> "waqti_reminder"
            AudioType.TASK_COMPLETION -> "waqti_task_complete"
            AudioType.FOCUS_START -> "waqti_focus_start"
            AudioType.FOCUS_WARNING -> "waqti_focus_warning"
            AudioType.FOCUS_COMPLETION -> "waqti_focus_complete"
            AudioType.WARNING -> "waqti_warning"
            AudioType.ADHAN -> "waqti_adhan"
        }
    }

    private fun playRawResource(resId: Int, onCompleted: (() -> Unit)?) {
        try {
            stopAll()
            val player = MediaPlayer.create(appContext, resId)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setOnCompletionListener { mp ->
                    try {
                        mp.release()
                    } catch (e: Exception) {
                        // Ignore release errors
                    }
                    if (currentMediaPlayer == mp) {
                        currentMediaPlayer = null
                    }
                    abandonTransientAudioFocus()
                    onCompleted?.invoke()
                }
                setOnErrorListener { mp, _, _ ->
                    try {
                        mp.release()
                    } catch (e: Exception) {
                        // Ignore
                    }
                    if (currentMediaPlayer == mp) {
                        currentMediaPlayer = null
                    }
                    abandonTransientAudioFocus()
                    onCompleted?.invoke()
                    true
                }
                start()
            }
            currentMediaPlayer = player
        } catch (e: Throwable) {
            Log.w(TAG, "Failed playing raw resource $resId: ${e.message}. Falling back.")
            abandonTransientAudioFocus()
            onCompleted?.invoke()
        }
    }

    private fun playProceduralFallback(type: AudioType) {
        val toneType = when (type) {
            AudioType.NOTIFICATION, AudioType.AI_NOTIFICATION -> WaqtiAudioToneGenerator.ToneType.NOTIFICATION
            AudioType.REMINDER, AudioType.QURAN_REMINDER, AudioType.AZKAR_REMINDER -> WaqtiAudioToneGenerator.ToneType.REMINDER
            AudioType.TASK_COMPLETION -> WaqtiAudioToneGenerator.ToneType.TASK_COMPLETION
            AudioType.FOCUS_START -> WaqtiAudioToneGenerator.ToneType.FOCUS_START
            AudioType.FOCUS_WARNING -> WaqtiAudioToneGenerator.ToneType.FOCUS_WARNING
            AudioType.FOCUS_COMPLETION -> WaqtiAudioToneGenerator.ToneType.FOCUS_COMPLETION
            AudioType.WARNING -> WaqtiAudioToneGenerator.ToneType.WARNING
            AudioType.ADHAN -> {
                Log.e("WAQTI_ADHAN_DEBUG", "playback error: Adhan must never use procedural fallback tone.")
                abandonTransientAudioFocus()
                return
            }
        }
        WaqtiAudioToneGenerator.playTone(toneType)
        abandonTransientAudioFocus()
    }

    /**
     * Vibrates with the specified pattern, respecting user preferences and system settings.
     */
    fun playVibration(type: VibrationType) {
        val settings = preferences.getSettings()
        if (!settings.masterVibrationEnabled) return

        try {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator == null || !vibrator.hasVibrator()) return

            val effect = when (type) {
                VibrationType.SHORT -> VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
                VibrationType.DOUBLE -> VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1)
                VibrationType.CONFIRMATION -> VibrationEffect.createWaveform(longArrayOf(0, 60, 60, 120), -1)
                VibrationType.WARNING -> VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 200), -1)
            }
            vibrator.vibrate(effect)
        } catch (e: Throwable) {
            Log.w(TAG, "Vibration execution failed: ${e.message}")
        }
    }

    private fun requestTransientAudioFocus() {
        val am = audioManager ?: return
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val focusReq = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener { /* transient ducking handled automatically */ }
                .build()
            activeFocusRequest = focusReq
            am.requestAudioFocus(focusReq)
        } catch (e: Exception) {
            Log.w(TAG, "Audio focus request failed: ${e.message}")
        }
    }

    private fun abandonTransientAudioFocus() {
        val am = audioManager ?: return
        val req = activeFocusRequest ?: return
        try {
            am.abandonAudioFocusRequest(req)
            activeFocusRequest = null
        } catch (e: Exception) {
            Log.w(TAG, "Audio focus abandon failed: ${e.message}")
        }
    }

    /**
     * Controls ambient background sounds for the Focus screen.
     * Looks up raw resources (e.g. ambient_rain, ambient_forest, etc.) safely without crashing.
     */
     fun setFocusAmbientSound(soundKey: String?, volume: Float = 0.5f) {
         scope.launch {
             stopFocusAmbient()
             if (soundKey.isNullOrBlank()) return@launch

             val safeKey = soundKey.trim().lowercase()
             val resName = "ambient_$safeKey"
             val resId = appContext.resources.getIdentifier(resName, "raw", appContext.packageName)
             if (resId != 0) {
                 try {
                     val player = MediaPlayer.create(appContext, resId)?.apply {
                         isLooping = true
                         val v = volume.coerceIn(0.0f, 1.0f)
                         setVolume(v, v)
                         setAudioAttributes(
                             AudioAttributes.Builder()
                                 .setUsage(AudioAttributes.USAGE_MEDIA)
                                 .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                 .build()
                         )
                         start()
                     }
                     ambientMediaPlayer = player
                 } catch (e: Exception) {
                     Log.w(TAG, "Failed playing ambient sound $soundKey: ${e.message}")
                 }
             } else {
                 Log.d(TAG, "Ambient resource $resName not bundled; skipping playback gracefully.")
             }
         }
     }

     fun setFocusAmbientVolume(volume: Float) {
         val v = volume.coerceIn(0.0f, 1.0f)
         try {
             ambientMediaPlayer?.setVolume(v, v)
         } catch (e: Exception) {
             // Ignore
         }
     }

     fun stopFocusAmbient() {
         try {
             ambientMediaPlayer?.let { mp ->
                 if (mp.isPlaying) {
                     mp.stop()
                 }
                 mp.release()
             }
         } catch (e: Exception) {
             // Ignore
         } finally {
             ambientMediaPlayer = null
         }
     }

    /**
     * Immediately stops any currently playing audio and releases player resources.
     */
    fun stopAll() {
        try {
            currentMediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
        } catch (e: Exception) {
            // Ignore stop errors
        } finally {
            currentMediaPlayer = null
            abandonTransientAudioFocus()
        }
        stopFocusAmbient()
    }

    /**
     * Releases manager resources.
     */
    fun release() {
        stopAll()
    }

    companion object {
        private const val TAG = "WaqtiAudioManager"
        private const val DEBOUNCE_MS = 250L

        @Volatile
        private var INSTANCE: WaqtiAudioManager? = null

        fun getInstance(context: Context): WaqtiAudioManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WaqtiAudioManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
