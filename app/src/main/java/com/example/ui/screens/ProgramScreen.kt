package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.RoutineEntity
import com.example.data.spiritual.QuranAzkarData
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.components.QiblaCompassDialog
import com.example.ui.theme.WaqtiPrayerAccent
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiQuranAccent
import com.example.ui.theme.WaqtiSuccess
import com.example.ui.viewmodel.WaqtiViewModel
import kotlinx.coroutines.delay

@Composable
fun ProgramScreen(
    viewModel: WaqtiViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val ayahIndex by viewModel.ayahIndex.collectAsStateWithLifecycle()
    val showQiblaCompass by viewModel.showQiblaCompass.collectAsStateWithLifecycle()
    val prayerSettings by viewModel.prayerLocationAndCalcSettings.collectAsStateWithLifecycle()

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Routines, 1: Prayers & Quran, 2: Azkar & Dhikr
    val currentAyah = QuranAzkarData.authenticVerses[ayahIndex]

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
                    text = { Text(if (language == AppLanguage.ARABIC) "الصلوات والقبلة" else "Prayers & Qibla") }
                )
                Tab(
                    selected = selectedSubTab == 2,
                    onClick = { selectedSubTab = 2 },
                    text = { Text(if (language == AppLanguage.ARABIC) "الأذكار والتسبيح" else "Azkar & Tasbeeh") }
                )
            }
        }

        // Ayah of the Moment ("آية اليوم")
        item {
            AyahOfTheMomentCard(
                currentAyah = currentAyah,
                language = language,
                onNextAyah = { viewModel.selectNextAyah() }
            )
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

            items(routines, key = { it.id }) { routine ->
                RoutineItemWithMicroAnimation(
                    routine = routine,
                    language = language,
                    onToggle = { viewModel.toggleRoutineCompletion(routine) }
                )
            }
        } else if (selectedSubTab == 1) {
            // PRAYERS & QURAN TAB
            item {
                PrayersTabContent(
                    viewModel = viewModel,
                    language = language
                )
            }
        } else {
            // AZKAR & TASBEEH TAB
            item {
                TasbeehTabContent(
                    viewModel = viewModel,
                    language = language
                )
            }
        }
    }

    // Smart Qibla Compass Dialog
    if (showQiblaCompass) {
        QiblaCompassDialog(
            settings = prayerSettings,
            language = language,
            onDismiss = { viewModel.setShowQiblaCompass(false) }
        )
    }
}

@Composable
private fun AyahOfTheMomentCard(
    currentAyah: com.example.data.spiritual.QuranVerse,
    language: AppLanguage,
    onNextAyah: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isRefreshPressed by remember { mutableStateOf(false) }
    val refreshScale by animateFloatAsState(
        targetValue = if (isRefreshPressed) 0.8f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "refresh_scale"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WaqtiQuranAccent.copy(alpha = 0.08f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(WaqtiQuranAccent.copy(alpha = 0.3f))
        ),
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
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isRefreshPressed = true
                        onNextAyah()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .scale(refreshScale)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Next Ayah",
                        tint = WaqtiQuranAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                LaunchedEffect(isRefreshPressed) {
                    if (isRefreshPressed) {
                        delay(150)
                        isRefreshPressed = false
                    }
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

@Composable
private fun RoutineItemWithMicroAnimation(
    routine: RoutineEntity,
    language: AppLanguage,
    onToggle: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isChecking by remember { mutableStateOf(false) }

    val iconScale by animateFloatAsState(
        targetValue = if (isChecking) 1.3f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "routine_check_scale"
    )

    val checkTint by animateColorAsState(
        targetValue = if (routine.isCompleted) WaqtiSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "routine_check_tint"
    )

    val cardAlpha = if (routine.isCompleted) 0.65f else 1f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (routine.isCompleted)
                MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (routine.isCompleted) 0.dp else 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isChecking = true
                    onToggle()
                },
                modifier = Modifier.scale(iconScale)
            ) {
                Icon(
                    imageVector = if (routine.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                    contentDescription = null,
                    tint = checkTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            LaunchedEffect(isChecking) {
                if (isChecking) {
                    delay(200)
                    isChecking = false
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) routine.titleAr else routine.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (routine.isCompleted) FontWeight.Normal else FontWeight.Bold,
                        color = if (routine.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (routine.isCompleted) TextDecoration.LineThrough else TextDecoration.None
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
