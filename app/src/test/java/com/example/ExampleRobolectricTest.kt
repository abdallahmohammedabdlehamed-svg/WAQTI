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

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `verify app name resource is WAQTI`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WAQTI", appName)
    }

    @Test
    fun `verify prayer times calculator returns all 5 daily prayers`() {
        val prayers = PrayerCalculator.getPrayerTimes()
        val prescribedPrayers = prayers.filter { it.isPrescribedPrayer }
        assertEquals(5, prescribedPrayers.size)
        assertTrue(prayers.size >= 5)
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

    @Test
    fun `verify MainActivity creates and launches successfully`() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertNotNull(activity)
        controller.pause().stop().destroy()
    }

    @Test
    fun `verify full app UI and screens render without crash`() {
        composeTestRule.waitForIdle()
        // Check home screen is rendered
        composeTestRule.onNodeWithTag("home_screen").assertExists()

        // Navigate through all bottom nav tabs
        composeTestRule.onNodeWithTag("nav_today").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_program").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_focus").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_tasks").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_more").performClick()
        composeTestRule.waitForIdle()

        // Return to home
        composeTestRule.onNodeWithTag("nav_home").performClick()
        composeTestRule.waitForIdle()
    }
}
