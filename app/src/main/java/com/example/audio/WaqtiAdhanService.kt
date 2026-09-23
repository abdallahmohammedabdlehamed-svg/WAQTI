package com.example.audio

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Resources
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.prayer.WaqtiPrayer
import com.example.notification.WaqtiNotificationChannels

/**
 * Dedicated short-lived Foreground Service for reliable Adhan audio playback.
 * Supports:
 * 1) User-selected custom MP3/audio file via Storage Access Framework
 * 2) Built-in authentic default Adhan recording from R.raw.waqti_adhan
 * Ensures background, screen-off, and lock-screen playback without being killed by Android Doze.
 * Automatically stops itself and releases all resources when playback completes.
 */
class WaqtiAdhanService : Service() {

    sealed class AdhanSource {
        data class Custom(val uri: Uri, val name: String, val durationMs: Long?) : AdhanSource()
        data object Default : AdhanSource()
    }

    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null
    private var prayerName: String = "Prayer"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_ADHAN) {
            Log.d(TAG, "Playback stopped: Stop Adhan action received via notification or intent")
            stopAdhanPlayback()
            return START_NOT_STICKY
        }

        prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "Prayer"
        val isTest = intent?.getBooleanExtra(EXTRA_IS_TEST, false) ?: false

        Log.d(TAG, "Service started for prayer: $prayerName (isTest=$isTest)")

        // 1. Check user preferences for this specific prayer.
        // Test Adhan button MUST bypass prayer-time checks, quiet hours, and duplicate suppression.
        val prefs = WaqtiAudioPreferences.getInstance(this)
        if (!isTest) {
            if (!prefs.isAdhanEnabledForPrayer(prayerName)) {
                Log.d(TAG, "Adhan disabled for $prayerName in user settings. Stopping service.")
                stopSelf()
                return START_NOT_STICKY
            }
        } else {
            Log.d(TAG, "Test mode active: Bypassing prayer-time checks, quiet hours, and duplicate suppression.")
        }

        // 2. Post foreground notification immediately as required by Android 8.0+
        val notification = buildForegroundNotification(prayerName)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Any MediaPlayer exception / foreground service error: ${e.message}", e)
            stopSelf()
            return START_NOT_STICKY
        }

        // 3. Play the authentic Adhan audio
        playAdhanAudio(prayerName, isTest)

        return START_NOT_STICKY
    }

    private fun buildForegroundNotification(prayer: String): Notification {
        val stopIntent = Intent(this, WaqtiAdhanService::class.java).apply {
            action = ACTION_STOP_ADHAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1001,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            1002,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prayerEnum = WaqtiPrayer.fromString(prayer)
        val isArabic = resources.configuration.locales[0].language.startsWith("ar")

        val prayerDisplay = when {
            prayerEnum != null -> if (isArabic) prayerEnum.nameAr else prayerEnum.nameEn
            else -> prayer
        }

        val title = if (isArabic) "حان الآن وقت صلاة $prayerDisplay" else "$prayerDisplay prayer time"
        val text = if (isArabic) "حي على الصلاة • حي على الفلاح" else "Come to prayer • Come to success"
        val stopBtnText = if (isArabic) "إيقاف الأذان" else "Stop Adhan"

        return NotificationCompat.Builder(this, WaqtiNotificationChannels.CHANNEL_PRAYERS_V2)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(android.R.drawable.ic_media_pause, stopBtnText, stopPendingIntent)
            .build()
    }

    private fun playAdhanAudio(prayer: String, isTest: Boolean) {
        // Request Audio Focus
        val focusGranted = requestAudioFocus()
        Log.d(TAG, "Audio focus result: $focusGranted")
        if (!focusGranted) {
            Log.w(TAG, "Audio focus denied for Adhan. Stopping playback.")
            stopAdhanPlayback()
            return
        }

        val prefs = WaqtiAudioPreferences.getInstance(this)
        val userVolume = prefs.getSettings().adhanVolume.coerceIn(0.0f, 1.0f)

        try {
            val source = resolveAdhanSource(this)
            var player: MediaPlayer? = null
            var isCustomAudio = false

            when (source) {
                is AdhanSource.Custom -> {
                    Log.d(TAG_CUSTOM, "Using custom Adhan")
                    Log.d(TAG_CUSTOM, "Custom Adhan selected: uri=${source.uri}")
                    Log.d(TAG_CUSTOM, "Custom Adhan name: ${source.name}")
                    Log.d(TAG_CUSTOM, "Custom Adhan duration: ${source.durationMs ?: 0}ms")
                    try {
                        player = MediaPlayer().apply {
                            setDataSource(applicationContext, source.uri)
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )
                            setVolume(userVolume, userVolume)
                            prepare()
                        }
                        isCustomAudio = true
                        Log.d(TAG_CUSTOM, "Custom Adhan playback started")
                    } catch (e: Exception) {
                        Log.e(TAG_CUSTOM, "Custom Adhan playback failed: ${e.message}")
                        Log.w(TAG_CUSTOM, "Falling back to default Adhan")
                        try { player?.release() } catch (_: Exception) {}
                        player = null
                    }
                }
                is AdhanSource.Default -> {
                    Log.d(TAG_CUSTOM, "Using built-in default Adhan")
                }
            }

            // Fallback or default playback using built-in R.raw.waqti_adhan
            if (player == null) {
                val resId = R.raw.waqti_adhan
                val fileName = "waqti_adhan.mp3"
                val dataSource = "android.resource://$packageName/$resId"

                Log.d(TAG, "Using production Adhan resource: R.raw.waqti_adhan")
                Log.d(TAG, "resource ID: $resId")
                Log.d(TAG, "file name: $fileName")
                Log.d(TAG, "MediaPlayer data source: $dataSource")

                player = MediaPlayer.create(this, resId)?.apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setVolume(userVolume, userVolume)
                }
            }

            mediaPlayer = player?.apply {
                try {
                    setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                } catch (e: Exception) {
                    Log.w(TAG, "WakeMode assignment skipped: ${e.message}")
                }

                setOnCompletionListener {
                    if (isCustomAudio) {
                        Log.d(TAG_CUSTOM, "Custom Adhan playback completed")
                    }
                    Log.d(TAG, "playback completed")
                    stopAdhanPlayback()
                }
                setOnErrorListener { _, what, extra ->
                    if (isCustomAudio) {
                        Log.e(TAG_CUSTOM, "Custom Adhan playback failed: what=$what extra=$extra")
                    }
                    Log.e(TAG, "playback error: what=$what extra=$extra")
                    stopAdhanPlayback()
                    true
                }

                start()
                Log.d(TAG, "playback started (prayer=$prayer, volume=$userVolume, isCustom=$isCustomAudio)")
            }

            if (mediaPlayer == null) {
                Log.e(TAG, "playback error: MediaPlayer.create returned null. No fallback audio allowed.")
                stopAdhanPlayback()
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "playback error: R.raw.waqti_adhan resource not found: ${e.message}. No fallback audio allowed.", e)
            stopAdhanPlayback()
        } catch (e: Exception) {
            Log.e(TAG, "playback error: Failed to initialize or play Adhan audio: ${e.message}", e)
            stopAdhanPlayback()
        }
    }

    private fun requestAudioFocus(): Boolean {
        val am = audioManager ?: return false
        return try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                        focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                    ) {
                        Log.d(TAG, "Playback stopped: Audio focus lost ($focusChange) during Adhan.")
                        stopAdhanPlayback()
                    }
                }
                .build()
            focusRequest = req
            val result = am.requestAudioFocus(req)
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } catch (e: Exception) {
            Log.e(TAG, "Any MediaPlayer exception / Audio focus request failed: ${e.message}", e)
            true
        }
    }

    private fun releaseAudioFocus() {
        val am = audioManager ?: return
        val req = focusRequest ?: return
        try {
            am.abandonAudioFocusRequest(req)
            focusRequest = null
        } catch (e: Exception) {
            // Ignore release errors
        }
    }

    private fun stopAdhanPlayback() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
            Log.d(TAG, "Playback stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Any MediaPlayer exception on stop: ${e.message}", e)
        } finally {
            mediaPlayer = null
            releaseAudioFocus()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            stopSelf()
        }
    }

    override fun onDestroy() {
        stopAdhanPlayback()
        super.onDestroy()
    }

    companion object {
        const val TAG = "WAQTI_ADHAN_DEBUG"
        const val TAG_CUSTOM = "WAQTI_CUSTOM_ADHAN"
        private const val NOTIFICATION_ID = 8801

        const val ACTION_START_ADHAN = "com.example.action.START_ADHAN"
        const val ACTION_STOP_ADHAN = "com.example.action.STOP_ADHAN"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_IS_TEST = "extra_is_test"

        /**
         * Resolves whether to play user-selected custom Adhan audio or built-in default R.raw.waqti_adhan.
         * Priority:
         * 1) Valid custom Adhan URI
         * 2) Built-in default R.raw.waqti_adhan
         */
        fun resolveAdhanSource(context: Context): AdhanSource {
            val prefs = WaqtiAudioPreferences.getInstance(context)
            val settings = prefs.getSettings()
            val uriStr = settings.customAdhanUri

            if (settings.customAdhanEnabled && !uriStr.isNullOrBlank()) {
                try {
                    val uri = Uri.parse(uriStr)
                    // Validate stream readability
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val testByte = stream.read()
                        if (testByte != -1) {
                            val fileName = settings.customAdhanFileName ?: "Custom Adhan"
                            return AdhanSource.Custom(
                                uri = uri,
                                name = fileName,
                                durationMs = settings.customAdhanDuration
                            )
                        }
                    } ?: run {
                        Log.w(TAG_CUSTOM, "Custom Adhan unavailable")
                        Log.w(TAG_CUSTOM, "Falling back to default Adhan")
                    }
                } catch (e: Exception) {
                    Log.w(TAG_CUSTOM, "Custom Adhan unavailable: ${e.message}")
                    Log.w(TAG_CUSTOM, "Falling back to default Adhan")
                }
            }
            return AdhanSource.Default
        }

        fun start(context: Context, prayerName: String, isTest: Boolean = false) {
            val intent = Intent(context, WaqtiAdhanService::class.java).apply {
                action = ACTION_START_ADHAN
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_IS_TEST, isTest)
            }
            try {
                Log.d(TAG, "Service start requested for prayer: $prayerName (isTest=$isTest)")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Any MediaPlayer exception / Service start failure: ${e.message}", e)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, WaqtiAdhanService::class.java).apply {
                action = ACTION_STOP_ADHAN
            }
            try {
                Log.d(TAG, "Playback stop requested via service intent")
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Could not send stop intent: ${e.message}")
            }
        }
    }
}
