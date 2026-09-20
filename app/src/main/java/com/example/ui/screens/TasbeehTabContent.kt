package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.spiritual.AzkarItem
import com.example.data.spiritual.QuranAzkarData
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.theme.WaqtiAzkarAccent
import com.example.ui.theme.WaqtiPrayerAccent
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiSuccess
import com.example.ui.theme.WaqtiWarning
import com.example.ui.viewmodel.WaqtiViewModel
import kotlinx.coroutines.delay

@Composable
fun TasbeehTabContent(
    viewModel: WaqtiViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isArabic = language == AppLanguage.ARABIC

    val dhikrCount by viewModel.dhikrCount.collectAsStateWithLifecycle()
    val dhikrTarget by viewModel.dhikrTarget.collectAsStateWithLifecycle()
    val dhikrStreak by viewModel.dhikrStreak.collectAsStateWithLifecycle()
    val dhikrTotalToday by viewModel.dhikrTotalToday.collectAsStateWithLifecycle()
    val selectedDhikrIndex by viewModel.selectedDhikr.collectAsStateWithLifecycle()
    val milestoneCelebration by viewModel.milestoneCelebration.collectAsStateWithLifecycle()
    val azkarRemainingMap by viewModel.azkarRemainingMap.collectAsStateWithLifecycle()

    var selectedAzkarCategory by remember { mutableIntStateOf(0) } // 0: Morning, 1: Evening, 2: Sleep, 3: After Prayer

    var isButtonPressed by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (isButtonPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "tasbeeh_scale"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Milestone Celebration Banner
        milestoneCelebration?.let { celebrationText ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WaqtiSuccess.copy(alpha = 0.15f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(WaqtiSuccess.copy(alpha = 0.5f))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = WaqtiSuccess)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = celebrationText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiSuccess
                        )
                    }
                    IconButton(
                        onClick = { viewModel.clearMilestone() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = WaqtiSuccess, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // 1. Streak & Daily Stats Bar
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "$dhikrStreak ${if (isArabic) "أيام متتالية" else "days streak"}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiWarning
                        )
                        Text(
                            text = if (isArabic) "سلسلة الذكر اليومي" else "Daily Azkar Streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = WaqtiAzkarAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "$dhikrTotalToday",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiAzkarAccent
                        )
                        Text(
                            text = if (isArabic) "تسبيحة اليوم" else "Total Tasbeeh Today",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Interactive Digital Tasbeeh Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = WaqtiAzkarAccent.copy(alpha = 0.08f)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(WaqtiAzkarAccent.copy(alpha = 0.25f))
            ),
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

                Spacer(modifier = Modifier.height(10.dp))

                val currentPhrase = QuranAzkarData.dhikrPhrases.getOrElse(selectedDhikrIndex) {
                    QuranAzkarData.dhikrPhrases[0]
                }

                Text(
                    text = currentPhrase.first,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = currentPhrase.second,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Dhikr Phrase Selector Chips
                ScrollableTabRow(
                    selectedTabIndex = selectedDhikrIndex,
                    edgePadding = 4.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = {}
                ) {
                    QuranAzkarData.dhikrPhrases.forEachIndexed { index, (ar, _) ->
                        val isSelected = selectedDhikrIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setSelectedDhikr(index)
                            },
                            label = { Text(ar, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WaqtiAzkarAccent,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Tactile Digital Counter Circle with Progress Ring
                Box(
                    modifier = Modifier.size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background & Progress Arc Canvas
                    val progressFraction = if (dhikrTarget > 0) {
                        (dhikrCount % (dhikrTarget + 1)).toFloat() / dhikrTarget.toFloat()
                    } else {
                        1f
                    }

                    Canvas(modifier = Modifier.size(150.dp)) {
                        val strokeWidth = 8.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = Size(diameter, diameter)

                        // Track
                        drawArc(
                            color = Color.Gray.copy(alpha = 0.2f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )

                        // Progress
                        drawArc(
                            color = WaqtiAzkarAccent,
                            startAngle = -90f,
                            sweepAngle = 360f * progressFraction.coerceIn(0f, 1f),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Tactile Circle Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(126.dp)
                            .scale(buttonScale)
                            .clip(CircleShape)
                            .background(WaqtiAzkarAccent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isButtonPressed = true
                                viewModel.incrementDhikr()
                            }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$dhikrCount",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            if (dhikrTarget > 0) {
                                Text(
                                    text = "/ $dhikrTarget",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    LaunchedEffect(isButtonPressed) {
                        if (isButtonPressed) {
                            delay(100)
                            isButtonPressed = false
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Controls: Target Goals and Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Target selector pills: 33, 100, 1000, Free
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(33, 100, 1000, 0).forEach { target ->
                            val label = if (target == 0) (if (isArabic) "حر" else "Free") else "$target"
                            val isSelected = dhikrTarget == target
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) WaqtiAzkarAccent else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setDhikrTarget(target)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Reset button
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.resetDhikr()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 3. Azkar Categories Navigation Tabs
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedAzkarCategory,
                edgePadding = 8.dp,
                containerColor = Color.Transparent,
                contentColor = WaqtiAzkarAccent
            ) {
                val categoryTitles = listOf(
                    if (isArabic) "أذكار الصباح" else "Morning",
                    if (isArabic) "أذكار المساء" else "Evening",
                    if (isArabic) "أذكار النوم" else "Sleep",
                    if (isArabic) "أذكار بعد الصلاة" else "After Prayer"
                )

                categoryTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedAzkarCategory == index,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedAzkarCategory = index
                        },
                        text = { Text(title, fontWeight = if (selectedAzkarCategory == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        // 4. Smooth Transition for Azkar Items
        AnimatedContent(
            targetState = selectedAzkarCategory,
            transitionSpec = {
                fadeIn(spring(stiffness = 300f)) togetherWith fadeOut(spring(stiffness = 300f))
            },
            label = "azkar_category_content"
        ) { targetCat ->
            val list = when (targetCat) {
                0 -> QuranAzkarData.morningAzkarList
                1 -> QuranAzkarData.eveningAzkarList
                2 -> QuranAzkarData.sleepAzkarList
                else -> QuranAzkarData.afterPrayerAzkarList
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                list.forEach { item ->
                    AzkarInteractiveCard(
                        item = item,
                        language = language,
                        remainingCount = azkarRemainingMap[item.id] ?: item.targetCount,
                        onDecrement = {
                            viewModel.decrementAzkarItem(item.id, item.targetCount)
                        },
                        onReset = {
                            viewModel.resetAzkarItem(item.id, item.targetCount)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AzkarInteractiveCard(
    item: AzkarItem,
    language: AppLanguage,
    remainingCount: Int,
    onDecrement: () -> Unit,
    onReset: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isArabic = language == AppLanguage.ARABIC
    val isDone = remainingCount == 0

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "badge_scale"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.surface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDone) 0.dp else 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.textAr,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 26.sp,
                    modifier = Modifier.weight(1f),
                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Interactive Counter Badge
                Box(
                    modifier = Modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDone) WaqtiSuccess.copy(alpha = 0.15f) else WaqtiAzkarAccent.copy(alpha = 0.12f))
                        .border(
                            1.dp,
                            if (isDone) WaqtiSuccess else WaqtiAzkarAccent.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isPressed = true
                            if (isDone) {
                                onReset()
                            } else {
                                onDecrement()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = "Done", tint = WaqtiSuccess, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) "تم" else "Done",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WaqtiSuccess
                            )
                        }
                    } else {
                        Text(
                            text = "$remainingCount / ${item.targetCount}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiAzkarAccent
                        )
                    }
                }

                LaunchedEffect(isPressed) {
                    if (isPressed) {
                        delay(120)
                        isPressed = false
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isArabic) item.benefitAr else item.benefitEn,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else WaqtiAzkarAccent,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
