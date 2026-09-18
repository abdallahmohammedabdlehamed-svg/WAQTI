package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.RoutineEntity
import com.example.data.local.TaskEntity
import com.example.data.prayer.PrayerCalculator
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.theme.WaqtiCyan
import com.example.ui.theme.WaqtiCyanLight
import com.example.ui.theme.WaqtiDanger
import com.example.ui.theme.WaqtiFocusAccent
import com.example.ui.theme.WaqtiInfo
import com.example.ui.theme.WaqtiPrayerAccent
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiPrimaryContainer
import com.example.ui.theme.WaqtiSecondaryGreen
import com.example.ui.theme.WaqtiSuccess
import com.example.ui.theme.WaqtiWarning
import com.example.ui.viewmodel.WaqtiViewModel

private fun getTaskCategoryIconAndColor(category: String, title: String): Pair<ImageVector, Color> {
    val lower = "$category $title".lowercase()
    return when {
        lower.contains("برمج") || lower.contains("كود") || lower.contains("بورتفوليو") || lower.contains("code") || lower.contains("dev") ->
            Icons.Default.Code to Color(0xFF00A3FF)
        lower.contains("تمر") || lower.contains("رياض") || lower.contains("صحة") || lower.contains("gym") || lower.contains("health") ->
            Icons.Default.FitnessCenter to Color(0xFF10B981)
        lower.contains("درس") || lower.contains("مذاكر") || lower.contains("قراء") || lower.contains("study") || lower.contains("read") ->
            Icons.Default.MenuBook to Color(0xFF8B5CF6)
        lower.contains("صل") || lower.contains("قرآن") || lower.contains("ذكر") || lower.contains("pray") ->
            Icons.Default.Spa to Color(0xFFF59E0B)
        lower.contains("محتو") || lower.contains("فيديو") || lower.contains("تصميم") || lower.contains("design") ->
            Icons.Default.Palette to Color(0xFFEC4899)
        else ->
            Icons.Default.Assignment to Color(0xFF0284C7)
    }
}

@Composable
fun HomeScreen(
    viewModel: WaqtiViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val totalFocusMins by viewModel.totalFocusMinutes.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val completedTasksCount = tasks.count { it.status == "COMPLETED" }
    val totalTasksCount = maxOf(1, tasks.size)
    val progressPercentage = ((completedTasksCount.toFloat() / totalTasksCount) * 100).toInt()

    val currentTask = tasks.firstOrNull { it.status == "IN_PROGRESS" }
        ?: tasks.firstOrNull { it.status != "COMPLETED" }
        ?: tasks.firstOrNull()

    val nextTasks = tasks.filter { it.id != currentTask?.id && it.status != "COMPLETED" }
    val prayerSettings by viewModel.prayerLocationAndCalcSettings.collectAsStateWithLifecycle()
    val nextPrayer = remember(prayerSettings) { viewModel.getNextPrayer() }

    var showAiInsight by remember { mutableStateOf(true) }

    // Dynamic localized date
    val todayCalendar = remember { java.util.Calendar.getInstance() }
    val dayName = remember(language) {
        if (language == AppLanguage.ARABIC) {
            when (todayCalendar.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.SATURDAY -> "السبت"
                java.util.Calendar.SUNDAY -> "الأحد"
                java.util.Calendar.MONDAY -> "الإثنين"
                java.util.Calendar.TUESDAY -> "الثلاثاء"
                java.util.Calendar.WEDNESDAY -> "الأربعاء"
                java.util.Calendar.THURSDAY -> "الخميس"
                java.util.Calendar.FRIDAY -> "الجمعة"
                else -> "اليوم"
            }
        } else {
            when (todayCalendar.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.SATURDAY -> "Saturday"
                java.util.Calendar.SUNDAY -> "Sunday"
                java.util.Calendar.MONDAY -> "Monday"
                java.util.Calendar.TUESDAY -> "Tuesday"
                java.util.Calendar.WEDNESDAY -> "Wednesday"
                java.util.Calendar.THURSDAY -> "Thursday"
                java.util.Calendar.FRIDAY -> "Friday"
                else -> "Today"
            }
        }
    }
    val monthName = remember(language) {
        if (language == AppLanguage.ARABIC) {
            when (todayCalendar.get(java.util.Calendar.MONTH)) {
                0 -> "يناير"; 1 -> "فبراير"; 2 -> "مارس"; 3 -> "أبريل"; 4 -> "مايو"; 5 -> "يونيو"
                6 -> "يوليو"; 7 -> "أغسطس"; 8 -> "سبتمبر"; 9 -> "أكتوبر"; 10 -> "نوفمبر"; 11 -> "ديسمبر"
                else -> ""
            }
        } else {
            when (todayCalendar.get(java.util.Calendar.MONTH)) {
                0 -> "January"; 1 -> "February"; 2 -> "March"; 3 -> "April"; 4 -> "May"; 5 -> "June"
                6 -> "July"; 7 -> "August"; 8 -> "September"; 9 -> "October"; 10 -> "November"; 11 -> "December"
                else -> ""
            }
        }
    }
    val dayOfMonth = remember { todayCalendar.get(java.util.Calendar.DAY_OF_MONTH) }
    val dateFormatted = "$dayName، $dayOfMonth $monthName"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Ambient Hero Banner with Desk Background
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_banner_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    // Desk hero ambient backdrop
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_waqti_hero),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )

                    // Obsidian dark gradient scrim for high contrast and elegance
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x33000000),
                                        Color(0xBB0B1120),
                                        Color(0xF50B1120)
                                    )
                                )
                            )
                    )

                    // Content inside hero
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top row: Date pill & Next Prayer chip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Date pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = dateFormatted,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFE2E8F0),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Next Prayer pill badge
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { viewModel.setCurrentTab(2) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${if (language == AppLanguage.ARABIC) nextPrayer.nameAr else nextPrayer.nameEn} ${nextPrayer.timeFormatted}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }

                        // Bottom row: Greeting & Subtitle
                        Column {
                            val displayName = currentUser?.name?.takeIf { it.isNotBlank() }
                                ?: if (language == AppLanguage.ARABIC) "عبدالله محمد" else "Abdallah Mohammed"
                            Text(
                                text = if (language == AppLanguage.ARABIC) "صباح الخير، $displayName 👋" else "Good morning, $displayName 👋",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (language == AppLanguage.ARABIC) "إدارة الوقت .. وتنظيم المهام" else "Time Management & Daily Tasks Organizer",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        // 2. Daily Progress Summary Bento Card ("إنجاز اليوم")
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(WaqtiCyan.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = WaqtiCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = Strings.dailyProgress(language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${Strings.tasksCompleted(language, completedTasksCount, tasks.size)} • $dateFormatted",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Gauge & Linear Progress Bar Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Circular Gauge Ring
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(54.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { completedTasksCount.toFloat() / totalTasksCount },
                                strokeWidth = 5.dp,
                                color = WaqtiCyan,
                                trackColor = WaqtiCyan.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                text = "$progressPercentage%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = WaqtiCyan
                            )
                        }

                        // Glowing Linear Bar
                        Column(modifier = Modifier.weight(1f)) {
                            LinearProgressIndicator(
                                progress = { completedTasksCount.toFloat() / totalTasksCount },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = WaqtiCyan,
                                trackColor = WaqtiCyan.copy(alpha = 0.15f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dual Stat Chips (الوقت المنجز & الوقت المتبقي)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Time Invested Chip
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(WaqtiSuccess.copy(alpha = 0.08f))
                                .border(1.dp, WaqtiSuccess.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(WaqtiSuccess.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = WaqtiSuccess,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = Strings.timeSpent(language),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                    val focusHours = (totalFocusMins ?: 270) / 60
                                    val focusMins = (totalFocusMins ?: 270) % 60
                                    Text(
                                        text = "${focusHours}h ${focusMins}m",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Time Remaining Chip
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(WaqtiCyan.copy(alpha = 0.08f))
                                .border(1.dp, WaqtiCyan.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(WaqtiCyan.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassEmpty,
                                        contentDescription = null,
                                        tint = WaqtiCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = Strings.timeRemaining(language),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                    val remHours = maxOf(0, 24 - ((totalFocusMins ?: 270) / 60))
                                    Text(
                                        text = "${remHours}h 30m",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Prominent Smart Action Buttons: "أعد تنظيم يومي" and "التأخيرات"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // "أعد تنظيم يومي ✨ >" (Gradient Action Button)
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00A3FF),
                                    Color(0xFF0284C7),
                                    Color(0xFF059669)
                                )
                            )
                        )
                        .clickable { viewModel.triggerSmartReschedule() }
                        .padding(horizontal = 14.dp)
                        .testTag("smart_reschedule_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Strings.smartRescheduleBtn(language),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (language.isRtl) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // "التأخيرات ⏳ >" (Amber Glass Action Button)
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(WaqtiWarning.copy(alpha = 0.12f))
                        .border(1.2.dp, WaqtiWarning.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .clickable { viewModel.triggerImBehind() }
                        .padding(horizontal = 12.dp)
                        .testTag("im_behind_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = WaqtiWarning,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Strings.delays(language),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiWarning
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (language.isRtl) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = WaqtiWarning.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 4. CURRENT TASK Highlight Card or Clean Slate Welcome Card
        if (tasks.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = WaqtiPrimaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (language == AppLanguage.ARABIC) "مرحبًا بك في حسابك الجديد! 🎉" else "Welcome to your new account! 🎉",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == AppLanguage.ARABIC)
                                "حسابك جاهز ونظيف تمامًا. لا توجد أي مهام قديمة، ابدأ الآن بإضافة مهامك اليومية وتنظيم وقتك بذكاء."
                            else
                                "Your account is clean and isolated. No leftover tasks. Start adding your daily tasks now.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.setCurrentTab(1) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.ARABIC) "إضافة مهمة جديدة" else "Add New Task",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            currentTask?.let { task ->
                item {
                    val (catIcon, catColor) = getTaskCategoryIconAndColor(task.category, task.title)
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("current_task_card")
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Header Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Current task pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(WaqtiCyan.copy(alpha = 0.15f))
                                        .border(1.dp, WaqtiCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = WaqtiCyan,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = Strings.currentTask(language),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = WaqtiCyan
                                        )
                                    }
                                }

                                // Duration & Urgent / Priority Tag
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(WaqtiDanger.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(WaqtiDanger)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = Strings.urgentTask(language),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WaqtiDanger,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${task.durationMinutes} دقيقة",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Content row with Category Icon Box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(catColor.copy(alpha = 0.18f))
                                        .border(1.dp, catColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                ) {
                                    Icon(
                                        imageVector = catIcon,
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (task.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = task.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action Buttons Row: ابدأ ▶, إتمام ✔, تأجيل 🕒
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Start button (Electric Blue)
                                Button(
                                    onClick = {
                                        viewModel.startFocusSession(task.title, "POMODORO")
                                        viewModel.setCurrentTab(3)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WaqtiCyan,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .height(46.dp)
                                        .testTag("start_focus_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Strings.start(language), fontWeight = FontWeight.Bold)
                                }

                                // Complete button (Emerald Green)
                                Button(
                                    onClick = { viewModel.toggleTaskCompletion(task) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WaqtiSuccess,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .height(46.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Strings.complete(language), fontWeight = FontWeight.Bold)
                                }

                                // Reschedule button
                                OutlinedButton(
                                    onClick = { viewModel.triggerSmartReschedule() },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .height(46.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Strings.reschedule(language), style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. NEXT UP Timeline Horizontal Strip ("القادم تاليًا")
        if (nextTasks.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = WaqtiCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Strings.nextUp(language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(nextTasks.take(6)) { task ->
                            val (tIcon, tColor) = getTaskCategoryIconAndColor(task.category, task.title)
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, tColor.copy(alpha = 0.35f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .width(210.dp)
                                    .clickable { viewModel.toggleTaskCompletion(task) }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Top Row: Time & Category Badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = task.startTime,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(tColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = tIcon,
                                                    contentDescription = null,
                                                    tint = tColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = task.category.ifBlank { "مهام" },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = tColor,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = task.description.ifBlank { "${task.durationMinutes} دقيقة" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Bottom check circle toggle
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        ) {}

                                        Text(
                                            text = "${task.durationMinutes} دقيقة",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. AI DAILY INSIGHT Card ("اقتراح من وقتي")
        if (showAiInsight) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(WaqtiPrimary.copy(alpha = 0.3f))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = WaqtiPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Strings.aiDailyInsight(language),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = WaqtiPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (language == AppLanguage.ARABIC)
                                "يبدو أن جدولك بعد الساعة 4:00 عصراً مزدحم بالمهام الذهنية. أقترح نقل التمرين الرياضي إلى 6:00 مساءً (بعد المغرب) لمنحك طاقة بدنية أفضل."
                            else
                                "Your late afternoon looks heavily loaded with focus tasks. I suggest moving your workout to 6:00 PM for optimal energy and recovery.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    viewModel.triggerSmartReschedule()
                                    showAiInsight = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WaqtiPrimary)
                            ) {
                                Text(Strings.apply(language))
                            }
                            OutlinedButton(
                                onClick = { showAiInsight = false },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(Strings.dismiss(language))
                            }
                            OutlinedButton(
                                onClick = { viewModel.setCurrentTab(5) }, // Switch to AI Assistant
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(Strings.askWaqti(language))
                            }
                        }
                    }
                }
            }
        }

        // 7. Today's Program & Spiritual Routine Checklist ("برنامج اليوم")
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setCurrentTab(2) } // Open Program Tab
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.myDailyProgram(language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${routines.count { it.isCompleted }} / ${routines.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = WaqtiPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    routines.take(4).forEach { routine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (routine.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                contentDescription = null,
                                tint = if (routine.isCompleted) WaqtiSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable { viewModel.toggleRoutineCompletion(routine) }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (language == AppLanguage.ARABIC) routine.titleAr else routine.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (routine.isCompleted) FontWeight.Normal else FontWeight.Medium,
                                color = if (routine.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = routine.time,
                                style = MaterialTheme.typography.labelSmall,
                                color = WaqtiPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
