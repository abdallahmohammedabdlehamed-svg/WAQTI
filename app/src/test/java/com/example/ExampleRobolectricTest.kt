package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.TaskEntity
import com.example.data.prayer.PrayerCalculator
import com.example.domain.ai.WaqtiAiEngine
import com.example.localization.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource is WAQTI`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WAQTI", appName)
    }

    @Test
    fun `verify prayer times calculator returns all 5 daily prayers`() {
        val prayers = PrayerCalculator.getPrayerTimes()
        assertEquals(5, prayers.size)
        val prayerNames = prayers.map { it.nameEn }
        assertTrue(prayerNames.contains("Fajr"))
        assertTrue(prayerNames.contains("Dhuhr"))
        assertTrue(prayerNames.contains("Asr"))
        assertTrue(prayerNames.contains("Maghrib"))
        assertTrue(prayerNames.contains("Isha"))

        val next = PrayerCalculator.getNextPrayer()
        assertNotNull(next)
    }

    @Test
    fun `verify AI Task Breakdown generates actionable subtasks`() {
        val steps = WaqtiAiEngine.breakdownTask("Personal Portfolio")
        assertTrue(steps.isNotEmpty())
        assertTrue(steps.size >= 4)
    }

    @Test
    fun `verify Im Behind feature recalibrates schedule without guilt`() {
        val sampleTasks = listOf(
            TaskEntity(title = "Morning Deep Work", priority = "HIGH", startTime = "09:00", endTime = "10:00"),
            TaskEntity(title = "Read Articles", priority = "LOW", startTime = "11:00", endTime = "11:30")
        )
        val result = WaqtiAiEngine.handleImBehind(sampleTasks, AppLanguage.ARABIC)
        assertTrue(result.explanationAr.contains("ولا يهمك"))
        assertTrue(result.changes.isNotEmpty())
    }
}
