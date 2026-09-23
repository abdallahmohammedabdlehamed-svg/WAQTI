package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.theme.WaqtiDanger
import com.example.ui.theme.WaqtiFocusAccent
import com.example.ui.theme.WaqtiPrayerAccent
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiSuccess
import com.example.ui.theme.WaqtiWarning
import com.example.ui.viewmodel.WaqtiViewModel
import java.util.Locale

@Composable
fun FocusScreen(
    viewModel: WaqtiViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isArabic = language == AppLanguage.ARABIC

    val remainingSec by viewModel.focusRemainingSeconds.collectAsStateWithLifecycle()
    val isRunning by viewModel.isFocusRunning.collectAsStateWithLifecycle()
    val focusMode by viewModel.focusMode.collectAsStateWithLifecycle()
    val activeTask by viewModel.activeFocusTask.collectAsStateWithLifecycle()
    val ambientSound by viewModel.ambientSound.collectAsStateWithLifecycle()
    val totalFocusMins by viewModel.totalFocusMinutes.collectAsStateWithLifecycle()

    var ambientVolume by remember { mutableFloatStateOf(0.75f) }

    val totalSec = when (focusMode) {
        "POMODORO" -> 25 * 60
        "DEEP_WORK" -> 50 * 60
        else -> 90 * 60
    }
    val progress = (totalSec - remainingSec).toFloat() / totalSec

    val minutes = remainingSec / 60
    val seconds = remainingSec % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    // Breathing pulse animation when running
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val ambientOptions = listOf(
        "Rain" to ("🌧️ " + if (isArabic) "مطر هادئ" else "Rain"),
        "Forest" to ("🌲 " + if (isArabic) "غابة طبيعية" else "Forest"),
        "Cafe" to ("☕ " + if (isArabic) "مقهى مريح" else "Cafe"),
        "White" to ("🎧 " + if (isArabic) "عازل الضوضاء" else "Noise"),
        "Ocean" to ("🌊 " + if (isArabic) "أمواج البحر" else "Ocean"),
        "Sanctuary" to ("🕌 " + if (isArabic) "سكينة الحرم" else "Serenity")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("focus_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = Strings.focusModeTitle(language),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isArabic)
                        "مساحة عمل خالية من المشتتات مع أصوات طبيعية محيطية لإنجاز أعمق"
                    else
                        "Distraction-free deep work space with ambient audio for uninterrupted productivity",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Mode Switcher Pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf(
                    "POMODORO" to Strings.pomodoroMode(language),
                    "DEEP_WORK" to Strings.deepWorkMode(language),
                    "CUSTOM" to Strings.customMode(language)
                ).forEach { (mode, label) ->
                    val isSelected = focusMode == mode
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) WaqtiPrimary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.startFocusSession(activeTask, mode)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Glowing Gradient Circular Timer with Ambient Breathing
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(250.dp)
                    .padding(8.dp)
            ) {
                // Canvas Ring with Gradient and Glowing Shadow
                Canvas(modifier = Modifier.size(230.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    // Outer ambient breathing glow when running
                    if (isRunning) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    WaqtiPrimary.copy(alpha = pulseGlow * 0.4f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.width / 2f
                            )
                        )
                    }

                    // Background track
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )

                    // Active Glowing Gradient Arc
                    val gradientBrush = Brush.sweepGradient(
                        colors = listOf(
                            WaqtiPrimary,
                            WaqtiFocusAccent,
                            WaqtiSuccess,
                            WaqtiPrimary
                        ),
                        center = center
                    )

                    drawArc(
                        brush = gradientBrush,
                        startAngle = -90f,
                        sweepAngle = 360f * progress.coerceIn(0f, 1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner Content: Formatted Time + Active Task Title
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeTask,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = WaqtiPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    if (isRunning) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(WaqtiSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "جلسة تركيز نشطة" else "Focus in session",
                                style = MaterialTheme.typography.labelSmall,
                                color = WaqtiSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Main Timer Control Buttons
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Button with tactile haptics
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleFocusTimer()
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) WaqtiWarning else WaqtiPrimary),
                    modifier = Modifier
                        .size(66.dp)
                        .testTag("toggle_timer_button")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Stop / Finish Button
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.finishFocusSession()
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = WaqtiDanger,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Ambient Sound Grid with Volume Slider
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
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
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(WaqtiPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = WaqtiPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = Strings.ambientSounds(language),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isArabic) "أصوات طبيعية لعزل التشتيت وزيادة الاستغراق" else "Background ambience to mask distractions",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (ambientSound != null) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Playing",
                                tint = WaqtiSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sound Pills Grid (2 rows of 3)
                    val chunked = ambientOptions.chunked(3)
                    chunked.forEach { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { (key, label) ->
                                val isSelected = ambientSound == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) WaqtiPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.setAmbientSound(if (isSelected) null else key)
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Ambient Volume Slider when a sound is selected
                    if (ambientSound != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Volume",
                                tint = WaqtiPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Slider(
                                value = ambientVolume,
                                onValueChange = {
                                    ambientVolume = it
                                    viewModel.setAmbientVolume(it)
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = WaqtiPrimary,
                                    activeTrackColor = WaqtiPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${(ambientVolume * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WaqtiPrimary
                            )
                        }
                    }
                }
            }
        }

        // Focus Analytics Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(totalFocusMins ?: 135) / 60}h ${(totalFocusMins ?: 135) % 60}m",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiPrimary
                        )
                        Text(
                            text = if (isArabic) "إجمالي تركيز اليوم" else "Today's Focus",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "5 sessions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiSuccess
                        )
                        Text(
                            text = if (isArabic) "الجلسات المكتملة" else "Completed Sessions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
