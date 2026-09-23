package com.example

import com.example.data.local.RoutineEntity
import com.example.data.local.TaskEntity
import com.example.data.prayer.PrayerCalculator
import com.example.domain.ai.AiActionPayload
import com.example.domain.ai.AiChatMessage
import com.example.domain.ai.AiIntent
import com.example.domain.ai.WaqtiAiContext
import com.example.domain.ai.WaqtiAiEngine
import com.example.domain.ai.WaqtiAiService
import com.example.domain.ai.WaqtiAiSystemPrompt
import com.example.localization.AppLanguage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WaqtiAi2UnitTest {

    @Test
    fun `verify WaqtiAiContext builds safe summary without private data`() {
        val tasks = listOf(
            TaskEntity(title = "Design Mockup", priority = "HIGH", startTime = "14:00", endTime = "15:00"),
            TaskEntity(title = "Check Emails", priority = "LOW", status = "COMPLETED")
        )
        val routines = listOf(
            RoutineEntity(title = "Morning Adhkar", titleAr = "أذكار الصباح", category = "SPIRITUAL", time = "06:00", isCompleted = true)
        )
        val nextPrayer = PrayerCalculator.getNextPrayer()

        val context = WaqtiAiContext.build(
            tasks = tasks,
            routines = routines,
            nextPrayer = nextPrayer,
            isFocusRunning = true,
            activeFocusTask = "Design Mockup",
            language = AppLanguage.ARABIC
        )

        val summary = context.toPromptSummary()
        assertTrue(summary.contains("Design Mockup"))
        assertTrue(summary.contains("أذكار الصباح"))
        assertTrue(summary.contains("Prayer"))
        // Verify privacy: no passwords or tokens
        assertTrue(!summary.contains("password"))
        assertTrue(!summary.contains("token"))
    }

    @Test
    fun `verify WaqtiAiSystemPrompt contains prayer safeguards and action schema`() {
        val promptAr = WaqtiAiSystemPrompt.getSystemPrompt(AppLanguage.ARABIC, null)
        assertTrue(promptAr.contains("مساعد وقتي الذكي"))
        assertTrue(promptAr.contains("الصلاة"))
        assertTrue(promptAr.contains("```action"))

        val promptEn = WaqtiAiSystemPrompt.getSystemPrompt(AppLanguage.ENGLISH, null)
        assertTrue(promptEn.contains("Waqti AI"))
        assertTrue(promptEn.contains("prayer", ignoreCase = true))
        assertTrue(promptEn.contains("```action"))
    }

    @Test
    fun `verify parseResponse extracts action and leaves clean message`() {
        val rawResponse = """
            بالتأكيد، لقد قمت بجدولة مهمة 'مراجعة الكود' في تمام الساعة 4 عصراً لمدة 45 دقيقة.
            ```action
            {"action":"CREATE_TASK","title":"مراجعة الكود","durationMinutes":45,"priority":"HIGH"}
            ```
            هل تود البدء في جلسة تركيز فوراً؟
        """.trimIndent()

        val result = WaqtiAiService.parseModelResponse(rawResponse)
        assertEquals(AiIntent.CREATE_TASK, result.intent)
        assertNotNull(result.action)
        assertEquals("CREATE_TASK", result.action?.actionType)
        assertEquals("مراجعة الكود", result.action?.title)
        assertEquals(45, result.action?.durationMinutes)
        assertEquals("HIGH", result.action?.priority)

        // Verify JSON block was stripped from user message
        assertTrue(!result.message.contains("```action"))
        assertTrue(result.message.contains("بالتأكيد"))
        assertTrue(result.message.contains("هل تود البدء"))
    }

    @Test
    fun `verify fallbackLocalIntelligence returns appropriate responses and intents`() {
        // Test Reschedule intent fallback
        val rescheduleResult = WaqtiAiService.fallbackLocalIntelligence("أعد تنظيم يومي", null, AppLanguage.ARABIC)
        assertEquals(AiIntent.RESCHEDULE_DAY, rescheduleResult.intent)
        assertEquals("APPLY_RESCHEDULE", rescheduleResult.action?.actionType)

        // Test Behind intent fallback
        val behindResult = WaqtiAiService.fallbackLocalIntelligence("أنا متأخر وخفف الجدول", null, AppLanguage.ARABIC)
        assertEquals(AiIntent.IM_BEHIND, behindResult.intent)
        assertEquals("IM_BEHIND", behindResult.action?.actionType)

        // Test General Q&A fallback
        val qaResult = WaqtiAiService.fallbackLocalIntelligence("ما هي أفضل طريقة لتنظيم الوقت في رمضان؟", null, AppLanguage.ARABIC)
        assertEquals(AiIntent.GENERAL_QUESTION, qaResult.intent)
        assertTrue(qaResult.message.isNotEmpty())
    }

    @Test
    fun `verify dynamic task breakdown fallback handles coding and project queries`() = runBlocking {
        val steps = WaqtiAiService.breakdownTaskDynamic("بناء متجر إلكتروني متكامل", AppLanguage.ARABIC)
        assertTrue(steps.isNotEmpty())
        assertTrue(steps.size >= 4)
    }

    @Test
    fun `verify WaqtiAiEngine queryAiAssistant returns rich message with action`() = runBlocking {
        val message = WaqtiAiEngine.queryAiAssistant(
            query = "اتأخرت",
            history = emptyList(),
            context = null,
            lang = AppLanguage.ARABIC
        )

        assertEquals("WAQTI", message.sender)
        assertTrue(message.textAr.isNotEmpty())
        assertNotNull(message.actionPayload)
        assertEquals("IM_BEHIND", message.actionPayload?.actionType)
    }
}
