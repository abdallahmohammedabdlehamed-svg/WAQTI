package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.prayer.PrayerCalculator
import com.example.data.prayer.PrayerTime
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.theme.WaqtiPrayerAccent
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiQuranAccent
import com.example.ui.theme.WaqtiSuccess
import com.example.ui.theme.WaqtiWarning
import com.example.ui.viewmodel.WaqtiViewModel

@Composable
fun PrayersTabContent(
    viewModel: WaqtiViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isArabic = language == AppLanguage.ARABIC
    val settings by viewModel.prayerLocationAndCalcSettings.collectAsStateWithLifecycle()
    val preAthanOffset by viewModel.preAthanAlertOffsetMinutes.collectAsStateWithLifecycle()

    val prayerTimes = remember(settings) { PrayerCalculator.getPrayerTimes(settings = settings) }
    val nextPrayer = remember(settings) { PrayerCalculator.getNextPrayer(settings = settings) }

    val qiblaAngle = remember(settings.latitude, settings.longitude) {
        PrayerCalculator.calculateQibla(settings.latitude, settings.longitude).toInt()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Next Prayer Hero Card (Glowing, High Contrast)
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = WaqtiPrayerAccent.copy(alpha = 0.12f)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(WaqtiPrayerAccent, WaqtiPrayerAccent.copy(alpha = 0.4f))
                ),
                width = 1.5.dp
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge: Next Prayer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(WaqtiPrayerAccent)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Strings.nextPrayer(language),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Countdown pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(WaqtiWarning.copy(alpha = 0.15f))
                            .border(1.dp, WaqtiWarning.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isArabic) "متبقي ${nextPrayer.minutesUntil} دقيقة" else "${nextPrayer.minutesUntil}m remaining",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiWarning
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = if (isArabic) nextPrayer.nameAr else nextPrayer.nameEn,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = WaqtiPrayerAccent
                        )
                        Text(
                            text = if (isArabic) "موعد الصلاة القادمة لحماية وقتك" else "Next scheduled sacred window",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = nextPrayer.timeFormatted,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = WaqtiPrayerAccent
                    )
                }
            }
        }

        // 2. Smart Qibla Compass Quick Access Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(WaqtiPrayerAccent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = WaqtiPrayerAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isArabic) "بوصلة القبلة الذكية 🕋" else "Smart Qibla Compass 🕋",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "زاوية القبلة: $qiblaAngle° من الشمال (${settings.cityName})" else "Azimuth: $qiblaAngle° N (${settings.cityName})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.setShowQiblaCompass(true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaqtiPrayerAccent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(if (isArabic) "فتح البوصلة" else "Open Compass", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. Five Daily Prayers List (Enhanced High-Contrast Indicators)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "مواقيت الصلاة اليومية (أوقات محمية)" else "Daily Prayers (Protected Time)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = WaqtiPrayerAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                prayerTimes.forEach { prayer ->
                    PrayerRowItem(
                        prayer = prayer,
                        isArabic = isArabic,
                        onToggleNotification = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                }
            }
        }

        // 4. Customizable "أوقات محمية" & Pre-Athan Alert Selector
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = WaqtiWarning)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "تنبيهات ما قبل الأذان (حماية أوقات الصلاة)" else "Pre-Athan Alerts (Protected Window)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isArabic)
                        "حدد متى ينبهك التطبيق للاستعداد للوضوء قبل دخول وقت الفريضة ومنع المهام الطارئة:"
                    else
                        "Select when to notify you before prayer to prepare and guard your focus window:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                val alertOptions = listOf(
                    0 to if (isArabic) "عند الأذان" else "At Athan",
                    5 to if (isArabic) "5 دقائق قبل" else "5m before",
                    10 to if (isArabic) "10 دقائق قبل" else "10m before",
                    15 to if (isArabic) "15 دقيقة قبل" else "15m before"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    alertOptions.forEach { (mins, label) ->
                        val isSelected = preAthanOffset == mins
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setPreAthanAlertOffset(mins)
                            },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WaqtiPrayerAccent.copy(alpha = 0.15f),
                                selectedLabelColor = WaqtiPrayerAccent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 5. Quran Routine Card
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
                    text = if (isArabic)
                        "الهدف اليومي: 20 دقيقة (أو حزب واحد). جلسة هادئة تحفظ بها بركة يومك وإنتاجيتك."
                    else
                        "Daily goal: 20 minutes (or 1 Hizb). A serene focus window preserving blessing in your day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.startFocusSession(if (isArabic) "ورد القرآن الكريم" else "Quran Routine", "POMODORO")
                        viewModel.setCurrentTab(3) // Switch to Focus tab
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaqtiQuranAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isArabic) "ابدأ جلسة الورد الآن (20 دقيقة)" else "Start Quran Session (20m)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PrayerRowItem(
    prayer: PrayerTime,
    isArabic: Boolean,
    onToggleNotification: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }

    // Dynamic high-contrast container styling based on prayer status
    val containerColor = when {
        prayer.isNext -> WaqtiPrimary.copy(alpha = 0.08f)
        prayer.isPast -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        prayer.isNext -> WaqtiPrimary.copy(alpha = 0.4f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Indicator dot / checkmark + Prayer Name + Status badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (prayer.isPast) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Passed",
                    tint = WaqtiSuccess,
                    modifier = Modifier.size(18.dp)
                )
            } else if (prayer.isNext) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(WaqtiPrimary)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(WaqtiPrayerAccent)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isArabic) prayer.nameAr else prayer.nameEn,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (prayer.isNext) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (prayer.isNext) WaqtiPrimary else MaterialTheme.colorScheme.onSurface
                    )

                    if (prayer.isNext) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WaqtiPrimary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isArabic) "القادمة" else "NEXT",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (prayer.isPast) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "مضت ✓" else "Completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = WaqtiSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Right: Time + Bell Toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = prayer.timeFormatted,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (prayer.isNext) FontWeight.Black else FontWeight.Bold,
                color = if (prayer.isNext) WaqtiPrimary else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = {
                    isMuted = !isMuted
                    onToggleNotification()
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                    contentDescription = "Toggle Notification",
                    tint = if (isMuted) MaterialTheme.colorScheme.outline else WaqtiPrayerAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
