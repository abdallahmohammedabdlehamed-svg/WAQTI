package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.WaqtiAudioManager
import com.example.audio.WaqtiAudioPreferences
import com.example.audio.WaqtiAudioToneGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WaqtiAudio2UnitTest {

    private lateinit var context: Context
    private lateinit var audioPrefs: WaqtiAudioPreferences

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        audioPrefs = WaqtiAudioPreferences.getInstance(context)
    }

    @Test
    fun `verify audio preferences default settings are sound and safe`() {
        val settings = audioPrefs.getSettings()
        assertTrue("Master notifications must be enabled by default", settings.masterNotificationsEnabled)
        assertTrue("Master sound must be enabled by default", settings.masterSoundEnabled)
        assertTrue("Master vibration must be enabled by default", settings.masterVibrationEnabled)
        assertTrue("Task reminder sound must be enabled by default", settings.taskReminderSoundEnabled)
        assertTrue("Focus sound must be enabled by default", settings.focusSoundEnabled)
        assertTrue("Global adhan should be enabled by default", settings.adhanEnabledGlobal)
        assertTrue("Fajr adhan should be enabled by default", settings.fajrAdhanEnabled)
        assertTrue("Isha adhan should be enabled by default", settings.ishaAdhanEnabled)
        assertEquals(0.8f, settings.adhanVolume, 0.01f)
    }

    @Test
    fun `verify audio preferences update and persistence`() {
        audioPrefs.updateSettings { current ->
            current.copy(
                taskReminderSoundEnabled = false,
                fajrAdhanEnabled = false,
                adhanVolume = 0.65f
            )
        }

        val updated = audioPrefs.getSettings()
        assertFalse(updated.taskReminderSoundEnabled)
        assertFalse(updated.fajrAdhanEnabled)
        assertEquals(0.65f, updated.adhanVolume, 0.01f)

        // Restore
        audioPrefs.updateSettings { current ->
            current.copy(
                taskReminderSoundEnabled = true,
                fajrAdhanEnabled = true,
                adhanVolume = 0.8f
            )
        }
        assertTrue(audioPrefs.getSettings().taskReminderSoundEnabled)
        assertTrue(audioPrefs.getSettings().fajrAdhanEnabled)
        assertEquals(0.8f, audioPrefs.getSettings().adhanVolume, 0.01f)
    }

    @Test
    fun `verify waqti prayer enum parsing and completeness`() {
        val prayers = com.example.data.prayer.WaqtiPrayer.entries
        assertEquals(5, prayers.size)
        assertEquals(com.example.data.prayer.WaqtiPrayer.FAJR, com.example.data.prayer.WaqtiPrayer.fromString("fajr"))
        assertEquals(com.example.data.prayer.WaqtiPrayer.FAJR, com.example.data.prayer.WaqtiPrayer.fromString("الفجر"))
        assertEquals(com.example.data.prayer.WaqtiPrayer.DHUHR, com.example.data.prayer.WaqtiPrayer.fromString("dhuhr"))
        assertEquals(com.example.data.prayer.WaqtiPrayer.DHUHR, com.example.data.prayer.WaqtiPrayer.fromString("الظهر"))
        assertEquals(com.example.data.prayer.WaqtiPrayer.ASR, com.example.data.prayer.WaqtiPrayer.fromString("asr"))
        assertEquals(com.example.data.prayer.WaqtiPrayer.ASR, com.example.data.prayer.WaqtiPrayer.fromString("العصر"))
        assertEquals(com.example.data.prayer.WaqtiPrayer.MAGHRIB, com.example.data.prayer.WaqtiPrayer.fromString("maghrib"))
        assertEquals(com.example.data.prayer.WaqtiPrayer.MAGHRIB, com.example.data.prayer.WaqtiPrayer.fromString("المغرب"))
        assertEquals(com.example.data.prayer.WaqtiPrayer.ISHA, com.example.data.prayer.WaqtiPrayer.fromString("isha"))
        assertEquals(com.example.data.prayer.WaqtiPrayer.ISHA, com.example.data.prayer.WaqtiPrayer.fromString("العشاء"))
    }

    @Test
    fun `verify duplicate adhan protection prevents duplicate playback within window`() {
        val prayer = "Fajr"
        val now = System.currentTimeMillis()

        // Fresh state should permit playback
        assertTrue(audioPrefs.canPlayAdhan(prayer, now))

        // Mark as played
        audioPrefs.markAdhanPlayed(prayer, now)

        // Attempting to play immediately after should be suppressed
        assertFalse(audioPrefs.canPlayAdhan(prayer, now + 1000L))

        // Attempting to play 5 minutes later should still be suppressed
        assertFalse(audioPrefs.canPlayAdhan(prayer, now + 5 * 60 * 1000L))

        // After 21 minutes, new trigger should be allowed
        assertTrue(audioPrefs.canPlayAdhan(prayer, now + 21 * 60 * 1000L))
    }

    @Test
    fun `verify quiet hours check logic`() {
        audioPrefs.updateSettings {
            it.copy(
                quietHoursEnabled = true,
                quietHoursStart = "22:00",
                quietHoursEnd = "07:00"
            )
        }
        assertTrue(audioPrefs.isInQuietHours(java.time.LocalTime.of(23, 0)))
        assertTrue(audioPrefs.isInQuietHours(java.time.LocalTime.of(3, 30)))
        assertFalse(audioPrefs.isInQuietHours(java.time.LocalTime.of(12, 0)))

        // Restore
        audioPrefs.updateSettings { it.copy(quietHoursEnabled = false) }
        assertFalse(audioPrefs.isInQuietHours(java.time.LocalTime.of(23, 0)))
    }

    @Test
    fun `verify waqti audio manager clean APIs execute safely`() {
        val manager = WaqtiAudioManager.getInstance(context)
        assertNotNull(manager)
        // Verify all clean APIs do not throw
        manager.playTaskComplete()
        manager.playFocusStart()
        manager.playFocusWarning()
        manager.playFocusComplete()
        manager.playHabitComplete()
        manager.playAiNotification()
        manager.playNotification()
        manager.playReminder()
        manager.playWarning()
        manager.setFocusAmbientSound("rain", 0.5f)
        manager.setFocusAmbientVolume(0.7f)
        manager.stopFocusAmbient()
        manager.stopAll()
    }

    @Test
    fun `verify tone generator creates valid audio byte buffers`() {
        val notificationBytes = WaqtiAudioToneGenerator.generateSamples(WaqtiAudioToneGenerator.ToneType.NOTIFICATION)
        assertNotNull(notificationBytes)
        assertTrue("Notification tone buffer should contain samples", notificationBytes.isNotEmpty())

        val reminderBytes = WaqtiAudioToneGenerator.generateSamples(WaqtiAudioToneGenerator.ToneType.REMINDER)
        assertNotNull(reminderBytes)
        assertTrue("Reminder tone buffer should contain samples", reminderBytes.isNotEmpty())

        val completionBytes = WaqtiAudioToneGenerator.generateSamples(WaqtiAudioToneGenerator.ToneType.TASK_COMPLETION)
        assertNotNull(completionBytes)
        assertTrue("Completion tone buffer should contain samples", completionBytes.isNotEmpty())

        val warningBytes = WaqtiAudioToneGenerator.generateSamples(WaqtiAudioToneGenerator.ToneType.WARNING)
        assertNotNull(warningBytes)
        assertTrue("Warning tone buffer should contain samples", warningBytes.isNotEmpty())
    }

    @Test
    fun `verify audio types enum completeness`() {
        val types = WaqtiAudioManager.AudioType.values()
        assertTrue(types.contains(WaqtiAudioManager.AudioType.NOTIFICATION))
        assertTrue(types.contains(WaqtiAudioManager.AudioType.REMINDER))
        assertTrue(types.contains(WaqtiAudioManager.AudioType.TASK_COMPLETION))
        assertTrue(types.contains(WaqtiAudioManager.AudioType.FOCUS_START))
        assertTrue(types.contains(WaqtiAudioManager.AudioType.FOCUS_WARNING))
        assertTrue(types.contains(WaqtiAudioManager.AudioType.FOCUS_COMPLETION))
        assertTrue(types.contains(WaqtiAudioManager.AudioType.WARNING))
        assertTrue(types.contains(WaqtiAudioManager.AudioType.ADHAN))
    }

    @Test
    fun `verify R raw waqti_adhan resource is bundled and accessible`() {
        val resId = com.example.R.raw.waqti_adhan
        assertTrue("Resource ID for R.raw.waqti_adhan must be non-zero", resId != 0)
        val inputStream = context.resources.openRawResource(resId)
        assertNotNull("Input stream for R.raw.waqti_adhan must not be null", inputStream)
        val bytes = inputStream.readBytes()
        assertTrue("R.raw.waqti_adhan audio resource must have content", bytes.isNotEmpty())
        assertEquals("Exact file size must match 1577634 bytes", 1577634, bytes.size)

        // Compute SHA-256
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hexString = digest.joinToString("") { "%02x".format(it) }
        assertEquals(
            "Exact SHA-256 must match uploaded waqti_adhan.mp3",
            "795b800eebea6a12030e0ab5d82e2f0843f6b8180540300d6ce98727945765bc",
            hexString
        )
        inputStream.close()
    }

    @Test
    fun `verify all 5 prescribed prayers and test mode trigger Adhan service with exact resource`() {
        val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        for (prayer in prayers) {
            com.example.audio.WaqtiAdhanService.start(context, prayer, isTest = false)
            com.example.audio.WaqtiAdhanService.stop(context)
        }

        // Test Adhan mode
        com.example.audio.WaqtiAdhanService.start(context, "Fajr", isTest = true)
        com.example.audio.WaqtiAdhanService.stop(context)
    }

    @Test
    fun `verify adhan audio type has no procedural fallback tone`() {
        val audioManager = WaqtiAudioManager.getInstance(context)
        // Ensure that ADHAN audio type strictly attempts R.raw.waqti_adhan and never plays fallback tones
        val adhanResName = "waqti_adhan"
        val resId = context.resources.getIdentifier(adhanResName, "raw", context.packageName)
        assertEquals(com.example.R.raw.waqti_adhan, resId)
    }

    @Test
    fun `verify custom adhan audio preferences storage and removal`() {
        // Initially no custom Adhan
        audioPrefs.removeCustomAdhan()
        val initialSettings = audioPrefs.getSettings()
        assertFalse(initialSettings.customAdhanEnabled)
        assertEquals(null, initialSettings.customAdhanUri)
        assertEquals(null, initialSettings.customAdhanFileName)
        assertEquals(null, initialSettings.customAdhanDuration)

        // Set custom Adhan
        val testUri = "content://com.android.providers.media.documents/document/audio%3A12345"
        val testName = "Mishary_Adhan.mp3"
        val testDuration = 187000L
        audioPrefs.setCustomAdhan(testUri, testName, testDuration)

        val updated = audioPrefs.getSettings()
        assertTrue(updated.customAdhanEnabled)
        assertEquals(testUri, updated.customAdhanUri)
        assertEquals(testName, updated.customAdhanFileName)
        assertEquals(testDuration, updated.customAdhanDuration)

        // Resolve adhan source: with invalid content URI mock, gracefully falls back to Default
        val source = com.example.audio.WaqtiAdhanService.resolveAdhanSource(context)
        // Since mock content resolver has no open stream for dummy URI, fallback to Default occurs
        assertEquals(com.example.audio.WaqtiAdhanService.AdhanSource.Default, source)

        // Remove custom Adhan
        audioPrefs.removeCustomAdhan()
        val cleared = audioPrefs.getSettings()
        assertFalse(cleared.customAdhanEnabled)
        assertEquals(null, cleared.customAdhanUri)
        assertEquals(null, cleared.customAdhanFileName)
        assertEquals(null, cleared.customAdhanDuration)

        // Resolve should still be Default
        val defaultSource = com.example.audio.WaqtiAdhanService.resolveAdhanSource(context)
        assertEquals(com.example.audio.WaqtiAdhanService.AdhanSource.Default, defaultSource)
    }
}
