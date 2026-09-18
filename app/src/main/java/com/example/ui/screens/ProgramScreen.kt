package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.prayer.PrayerCalculator
import com.example.data.spiritual.QuranAzkarData
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.theme.WaqtiAzkarAccent
import com.example.ui.theme.WaqtiPrayerAccent
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiQuranAccent
import com.example.ui.theme.WaqtiSuccess
import com.example.ui.theme.WaqtiWarning
import com.example.ui.viewmodel.WaqtiViewModel

@Composable
fun ProgramScreen(
    viewModel: WaqtiViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val ayahIndex by viewModel.ayahIndex.collectAsStateWithLifecycle()
    val dhikrCount by viewModel.dhikrCount.collectAsStateWithLifecycle()

    var selectedSubTab by remember { mutableStateOf(0) } // 0: Program, 1: Prayers & Quran, 2: Azkar & Dhikr
    val currentAyah = QuranAzkarData.authenticVerses[ayahIndex]
    val prayerTimes = remember { PrayerCalculator.getPrayerTimes() }
    val nextPrayer = remember { PrayerCalculator.getNextPrayer() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("program_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = Strings.myDailyProgram(language),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (language == AppLanguage.ARABIC)
                        "تنظيم الحياة اليومية، العبادات، الرياضة، والعادات الشخصية في خطة واحدة متوازنة"
                    else
                        "Organizing daily life, worship, fitness, and habits in one harmonious rhythm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Sub-tabs
        item {
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = WaqtiPrimary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text(if (language == AppLanguage.ARABIC) "الروتين اليومي" else "Routines") }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text(if (language == AppLanguage.ARABIC) "الصلوات والقرآن" else "Prayers & Quran") }
                )
                Tab(
                    selected = selectedSubTab == 2,
                    onClick = { selectedSubTab = 2 },
                    text = { Text(if (language == AppLanguage.ARABIC) "الأذكار والتسبيح" else "Azkar & Dhikr") }
                )
            }
        }

        // 1. Ayah of the Moment ("آية اليوم")
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WaqtiQuranAccent.copy(alpha = 0.08f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(WaqtiQuranAccent.copy(alpha = 0.3f))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = WaqtiQuranAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Strings.ayahOfTheMoment(language),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = WaqtiQuranAccent
                            )
                        }

                        IconButton(
                            onClick = { viewModel.selectNextAyah() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Next Ayah",
                                tint = WaqtiQuranAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "﴿ ${currentAyah.arabicText} ﴾",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentAyah.englishTranslation,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${currentAyah.surahNameAr} • ${currentAyah.reference}",
                        style = MaterialTheme.typography.labelSmall,
                        color = WaqtiQuranAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // SubTab Content:
        if (selectedSubTab == 0) {
            // ROUTINES LIST
            item {
                Text(
                    text = if (language == AppLanguage.ARABIC) "أنشطة البرنامج اليومي الثابتة والمرنة" else "Daily Program Scheduled Activities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(routines) { routine ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (routine.isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.toggleRoutineCompletion(routine) }) {
                            Icon(
                                imageVector = if (routine.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                contentDescription = null,
                                tint = if (routine.isCompleted) WaqtiSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (language == AppLanguage.ARABIC) routine.titleAr else routine.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (routine.isCompleted) FontWeight.Normal else FontWeight.Bold,
                                    color = if (routine.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                if (routine.isProtected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = Strings.fixedTag(language),
                                        tint = WaqtiPrayerAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${routine.time} • ${routine.durationMinutes} min • ${routine.category}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (routine.streak > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(WaqtiPrimary.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "🔥 ${routine.streak}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WaqtiPrimary
                                )
                            }
                        }
                    }
                }
            }
        } else if (selectedSubTab == 1) {
            // PRAYERS & QURAN
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WaqtiPrayerAccent.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = Strings.nextPrayer(language),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = WaqtiPrayerAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (language == AppLanguage.ARABIC) nextPrayer.nameAr else nextPrayer.nameEn,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WaqtiPrayerAccent
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = nextPrayer.timeFormatted,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WaqtiPrayerAccent
                                )
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "متبقي ${nextPrayer.minutesUntil} دقيقة" else "${nextPrayer.minutesUntil} mins remaining",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WaqtiPrayerAccent
                                )
                            }
                        }
                    }
                }
            }

            // Five Prayers Grid
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "مواقيت الصلاة اليومية (أوقات محمية)" else "Daily Prayers (Protected Time)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        prayerTimes.forEach { prayer ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (prayer.isNext) WaqtiPrimary else if (prayer.isPast) WaqtiSuccess else WaqtiPrayerAccent)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) prayer.nameAr else prayer.nameEn,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = prayer.timeFormatted,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (prayer.isNext) WaqtiPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = WaqtiPrayerAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quran Routine Card ("ورد القرآن")
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = WaqtiQuranAccent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Strings.quranRoutine(language),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "🔥 9 days streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = WaqtiQuranAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (language == AppLanguage.ARABIC)
                                "الهدف اليومي: 20 دقيقة (أو حزب واحد). يمكنك إكمال وردك عندما يناسبك بهدوء."
                            else
                                "Daily goal: 20 minutes (or 1 Hizb). You can continue your reading whenever it works for you.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                viewModel.startFocusSession(if (language == AppLanguage.ARABIC) "ورد القرآن الكريم" else "Quran Routine", "POMODORO")
                                viewModel.setCurrentTab(3) // Focus tab
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WaqtiQuranAccent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (language == AppLanguage.ARABIC) "ابدأ جلسة الورد الآن (20 دقيقة)" else "Start Quran Session (20m)")
                        }
                    }
                }
            }
        } else {
            // AZKAR & DHIKR
            // Smart Dhikr Interactive Counter
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WaqtiAzkarAccent.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = Strings.smartDhikr(language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiAzkarAccent
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = QuranAzkarData.dhikrPhrases.first().first,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = QuranAzkarData.dhikrPhrases.first().second,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        // Big Interactive Tasbeeh Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(WaqtiAzkarAccent)
                                .clickable { viewModel.incrementDhikr() }
                        ) {
                            Text(
                                text = "$dhikrCount",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (language == AppLanguage.ARABIC) "اضغط للتسبيح" else "Tap to count",
                            style = MaterialTheme.typography.labelSmall,
                            color = WaqtiAzkarAccent
                        )
                    }
                }
            }

            // Morning & Evening Azkar Cards
            item {
                Text(
                    text = Strings.morningAzkar(language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(QuranAzkarData.morningAzkarList) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = item.textAr,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == AppLanguage.ARABIC) item.benefitAr else item.benefitEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = WaqtiAzkarAccent
                        )
                    }
                }
            }
        }
    }
}
