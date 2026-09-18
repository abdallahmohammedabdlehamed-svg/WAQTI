package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.notification.*
import com.example.localization.AppLanguage
import com.example.ui.theme.WaqtiDanger
import com.example.ui.theme.WaqtiPrayerAccent
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiSecondaryGreen
import com.example.ui.theme.WaqtiSuccess
import com.example.ui.theme.WaqtiWarning
import com.example.ui.viewmodel.WaqtiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    viewModel: WaqtiViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val preferences by viewModel.notificationPreferences.collectAsStateWithLifecycle()
    val upcomingSchedules by viewModel.upcomingSchedules.collectAsStateWithLifecycle()
    val prayerSettings by viewModel.prayerLocationAndCalcSettings.collectAsStateWithLifecycle()
    val advancedSettings by viewModel.advancedNotificationSettings.collectAsStateWithLifecycle()
    val isFatigueAlert by viewModel.isFatigueAlert.collectAsStateWithLifecycle()
    val isRecalculating by viewModel.isNotificationEngineRecalculating.collectAsStateWithLifecycle()

    var selectedSectionTab by remember { mutableIntStateOf(0) } // 0: Categories, 1: Prayer Engine, 2: Smart Control & Logs
    var expandedCategoryId by remember { mutableStateOf<String?>(null) }

    val isArabic = language == AppLanguage.ARABIC

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            ),
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("notification_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = if (isArabic) "رجوع" else "Back"
                    )
                }
            },
            title = {
                Column {
                    Text(
                        text = if (isArabic) "إشعارات وقتي الذكية" else "WAQTI Smart Notifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) "التذكير المناسب في الوقت المناسب دون إزعاج" else "The right reminder at the right time without noise",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        viewModel.recalculateNotifications()
                        Toast.makeText(
                            context,
                            if (isArabic) "تمت إعادة جدولة الإشعارات الذكية بنجاح" else "Smart notifications rescheduled successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.testTag("reschedule_all_button")
                ) {
                    if (isRecalculating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recalculate",
                            tint = WaqtiPrimary
                        )
                    }
                }
            }
        )

        // Section Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedSectionTab == 0,
                onClick = { selectedSectionTab = 0 },
                label = { Text(if (isArabic) "فئات الإشعارات" else "Categories") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WaqtiPrimary.copy(alpha = 0.15f),
                    selectedLabelColor = WaqtiPrimary
                ),
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = selectedSectionTab == 1,
                onClick = { selectedSectionTab = 1 },
                label = { Text(if (isArabic) "أوقات الصلاة" else "Prayer Engine") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WaqtiPrayerAccent.copy(alpha = 0.15f),
                    selectedLabelColor = WaqtiPrayerAccent
                ),
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = selectedSectionTab == 2,
                onClick = { selectedSectionTab = 2 },
                label = { Text(if (isArabic) "التحكم والجدول" else "Smart Limits") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WaqtiSecondaryGreen.copy(alpha = 0.15f),
                    selectedLabelColor = WaqtiSecondaryGreen
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Notification Fatigue Warning Banner (if active)
        AnimatedVisibility(visible = isFatigueAlert) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = WaqtiWarning.copy(alpha = 0.14f)),
                border = BorderStroke(1.dp, WaqtiWarning.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WaqtiWarning,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "تنبيه إرهاق الإشعارات (Fatigue Alert)" else "Notification Fatigue Warning",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiWarning
                        )
                        Text(
                            text = if (isArabic) "لاحظنا وصول إشعارات كثيرة متتالية. تم تفعيل التباعد التلقائي لحماية تركيزك."
                            else "High notification volume detected. Smart spacing has been auto-engaged to protect your focus.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedSectionTab) {
                0 -> {
                    // CATEGORIES LIST
                    item {
                        NotificationHeaderHero(isArabic = isArabic)
                    }

                    items(preferences, key = { it.category }) { pref ->
                        CategoryNotificationCard(
                            pref = pref,
                            isExpanded = expandedCategoryId == pref.category,
                            onToggleExpand = {
                                expandedCategoryId = if (expandedCategoryId == pref.category) null else pref.category
                            },
                            onPreferenceChange = { updated ->
                                viewModel.updateNotificationPreference(updated)
                            },
                            onTestNotification = {
                                viewModel.sendTestNotification(pref.category)
                                Toast.makeText(
                                    context,
                                    if (isArabic) "تم إرسال إشعار تجريبي لـ ${pref.displayNameAr}" else "Test notification sent for ${pref.displayNameEn}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            isArabic = isArabic
                        )
                    }
                }

                1 -> {
                    // PRAYER ENGINE SETTINGS
                    item {
                        PrayerEngineConfigurationView(
                            settings = prayerSettings,
                            onUpdateSettings = { viewModel.updatePrayerSettings(it) },
                            viewModel = viewModel,
                            isArabic = isArabic
                        )
                    }
                }

                2 -> {
                    // SMART ADVANCED SETTINGS & UPCOMING SCHEDULES
                    item {
                        AdvancedControlsCard(
                            settings = advancedSettings,
                            onUpdateSettings = { viewModel.updateAdvancedSettings(it) },
                            isArabic = isArabic
                        )
                    }

                    item {
                        UpcomingSchedulesList(
                            schedules = upcomingSchedules,
                            isArabic = isArabic
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationHeaderHero(isArabic: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(WaqtiPrimary, WaqtiSecondaryGreen)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = if (isArabic) "محرك وقتي الذكي للمواقيت" else "WAQTI Smart Engine",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isArabic) "يفصل بين الإشعارات بـ 20 دقيقة على الأقل، ويمنع التعارض مع الصلوات وساعات الهدوء."
                    else "Enforces a 20-min minimum gap and protects prayer times & quiet hours.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CategoryNotificationCard(
    pref: NotificationPreferenceEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onPreferenceChange: (NotificationPreferenceEntity) -> Unit,
    onTestNotification: () -> Unit,
    isArabic: Boolean
) {
    val categoryIcon = getCategoryIcon(pref.category)
    val categoryColor = getCategoryColor(pref.category)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pref.enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            1.dp,
            if (pref.enabled) categoryColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_category_${pref.category}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(categoryColor.copy(alpha = if (pref.enabled) 0.15f else 0.06f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = if (pref.enabled) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) pref.displayNameAr else pref.displayNameEn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (pref.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${pref.frequency} • ${pref.soundTone} • ${if (pref.reminderOffsetMinutes == 0) (if (isArabic) "في الموعد" else "At time") else "${pref.reminderOffsetMinutes}m before"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Master switch for category
                Switch(
                    checked = pref.enabled,
                    onCheckedChange = { onPreferenceChange(pref.copy(enabled = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = categoryColor),
                    modifier = Modifier.testTag("switch_${pref.category}")
                )

                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded detail configuration drawer
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // 1. Sound Tone Selection
                    Text(
                        text = if (isArabic) "نغمة الإشعار" else "Notification Sound Tone",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                    val availableTones: List<Pair<String, String>> = if (pref.category == NotificationCategory.PRAYER) {
                        listOf(
                            NotificationSoundTone.AZAN_FULL to (if (isArabic) "الأذان كامل" else "Full Azan"),
                            NotificationSoundTone.AZAN_TAKBEER to (if (isArabic) "التكبير فقط" else "Takbeer Only"),
                            NotificationSoundTone.QUIET_CHIME to (if (isArabic) "رنين هادئ" else "Quiet Chime"),
                            NotificationSoundTone.SILENT to (if (isArabic) "صامت" else "Silent")
                        )
                    } else {
                        listOf(
                            NotificationSoundTone.SOFT_CHIME to (if (isArabic) "نغمة هادئة" else "Soft Chime"),
                            NotificationSoundTone.QUIET_CHIME to (if (isArabic) "رنين خفيف" else "Gentle Chime"),
                            NotificationSoundTone.VIBRATE_ONLY to (if (isArabic) "اهتزاز فقط" else "Vibrate Only"),
                            NotificationSoundTone.SILENT to (if (isArabic) "صامت" else "Silent")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (toneItem in availableTones) {
                            val toneKey = toneItem.first
                            val toneLabel = toneItem.second
                            val isSelected = pref.soundTone == toneKey
                            val surfaceColor = MaterialTheme.colorScheme.surface
                            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) categoryColor.copy(alpha = 0.18f)
                                        else surfaceColor
                                    )
                                    .clickable { onPreferenceChange(pref.copy(soundTone = toneKey)) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = toneLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) categoryColor else onSurfaceColor
                                )
                            }
                        }
                    }

                    // 2. Timing Before Reminder
                    Text(
                        text = if (isArabic) "توقيت التذكير" else "Reminder Timing",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                    val timingOptions: List<Pair<Int, String>> = listOf(
                        0 to (if (isArabic) "في الموعد" else "At time"),
                        5 to (if (isArabic) "قبل بـ 5 د" else "5m before"),
                        10 to (if (isArabic) "قبل بـ 10 د" else "10m before"),
                        15 to (if (isArabic) "قبل بـ 15 د" else "15m before")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (opt in timingOptions) {
                            val min = opt.first
                            val label = opt.second
                            val isSelected = pref.reminderOffsetMinutes == min
                            val surfaceColor = MaterialTheme.colorScheme.surface
                            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) categoryColor.copy(alpha = 0.18f)
                                        else surfaceColor
                                    )
                                    .clickable { onPreferenceChange(pref.copy(reminderOffsetMinutes = min)) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) categoryColor else onSurfaceColor
                                )
                            }
                        }
                    }

                    // 3. Frequency Options
                    Text(
                        text = if (isArabic) "معدل التكرار" else "Notification Frequency",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                    val freqOptions = listOf(
                        "BALANCED" to (if (isArabic) "متوازن" else "Balanced"),
                        "MINIMAL" to (if (isArabic) "أدنى حد" else "Minimal"),
                        "HIGH" to (if (isArabic) "مكثف" else "High")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        freqOptions.forEach { (freqKey, freqLabel) ->
                            val isSelected = pref.frequency == freqKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) categoryColor.copy(alpha = 0.18f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable { onPreferenceChange(pref.copy(frequency = freqKey)) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = freqLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) categoryColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 4. Test Notification Button
                    Button(
                        onClick = onTestNotification,
                        colors = ButtonDefaults.buttonColors(containerColor = categoryColor),
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "اختبار إشعار ${pref.displayNameAr}" else "Test ${pref.displayNameEn} Notification",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerEngineConfigurationView(
    settings: PrayerLocationAndCalcSettings,
    onUpdateSettings: (PrayerLocationAndCalcSettings) -> Unit,
    viewModel: WaqtiViewModel,
    isArabic: Boolean
) {
    val prayerTimes = remember(settings) {
        viewModel.getTodayPrayerTimes()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // City & Calculation Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, WaqtiPrayerAccent.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = WaqtiPrayerAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "الموقع والحساب الفلكي" else "Location & Calculation Method",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // City Selector
                Text(
                    text = if (isArabic) "المدينة المختارة:" else "Selected City:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val cities = listOf(
                    Triple("مكة المكرمة", "Makkah", 21.4225 to 39.8262),
                    Triple("الرياض", "Riyadh", 24.7136 to 46.6753),
                    Triple("القاهرة", "Cairo", 30.0444 to 31.2357),
                    Triple("دبي", "Dubai", 25.2048 to 55.2708),
                    Triple("إسطنبول", "Istanbul", 41.0082 to 28.9784)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cities.forEach { (nameAr, nameEn, coords) ->
                        val isSelected = settings.cityName == nameAr || settings.cityName == nameEn
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) WaqtiPrayerAccent.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable {
                                    onUpdateSettings(
                                        settings.copy(
                                            cityName = if (isArabic) nameAr else nameEn,
                                            latitude = coords.first,
                                            longitude = coords.second
                                        )
                                    )
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isArabic) nameAr else nameEn,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) WaqtiPrayerAccent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Calculation Method
                Text(
                    text = if (isArabic) "طريقة الحساب:" else "Calculation Authority:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val methods = listOf(
                    "UMM_AL_QURA" to (if (isArabic) "أم القرى" else "Umm Al-Qura"),
                    "EGYPTIAN" to (if (isArabic) "المصرية" else "Egyptian"),
                    "MWL" to (if (isArabic) "رابطة العالم الإسلامي" else "MWL"),
                    "KARACHI" to (if (isArabic) "كراتشي" else "Karachi")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    methods.forEach { (methodKey, methodLabel) ->
                        val isSelected = settings.calcMethod == methodKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) WaqtiPrayerAccent.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { onUpdateSettings(settings.copy(calcMethod = methodKey)) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = methodLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) WaqtiPrayerAccent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Today Prayer Times with Individual Toggles
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isArabic) "مواقيت الصلوات الخمس اليوم والتنبيهات" else "Today's Five Prayers & Alarms",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                prayerTimes.forEach { prayer ->
                    val isPrayerEnabled = when (prayer.nameEn) {
                        "Fajr" -> settings.fajrEnabled
                        "Dhuhr" -> settings.dhuhrEnabled
                        "Asr" -> settings.asrEnabled
                        "Maghrib" -> settings.maghribEnabled
                        "Isha" -> settings.ishaEnabled
                        else -> true
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (prayer.isNext) WaqtiPrayerAccent.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (prayer.isNext) WaqtiPrayerAccent else WaqtiPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (prayer.isNext) Color.White else WaqtiPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isArabic) prayer.nameAr else prayer.nameEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (prayer.isNext) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(WaqtiPrayerAccent)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isArabic) "القادمة" else "Next",
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = prayer.timeFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Switch per prayer
                        Switch(
                            checked = isPrayerEnabled,
                            onCheckedChange = { checked ->
                                val updated = when (prayer.nameEn) {
                                    "Fajr" -> settings.copy(fajrEnabled = checked)
                                    "Dhuhr" -> settings.copy(dhuhrEnabled = checked)
                                    "Asr" -> settings.copy(asrEnabled = checked)
                                    "Maghrib" -> settings.copy(maghribEnabled = checked)
                                    "Isha" -> settings.copy(ishaEnabled = checked)
                                    else -> settings
                                }
                                onUpdateSettings(updated)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = WaqtiPrayerAccent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdvancedControlsCard(
    settings: AdvancedNotificationSettings,
    onUpdateSettings: (AdvancedNotificationSettings) -> Unit,
    isArabic: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, WaqtiSecondaryGreen.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = WaqtiSecondaryGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabic) "إدارة منع الإرهاق والهدوء" else "Fatigue Prevention & Quiet Hours",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // 1. Quiet Hours Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "ساعات الهدوء (Quiet Hours)" else "Quiet Hours",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) "من ${settings.quietHoursStart} إلى ${settings.quietHoursEnd} (مع استثناء أذان الفجر)"
                        else "From ${settings.quietHoursStart} to ${settings.quietHoursEnd} (Fajr excepted)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.quietHoursEnabled,
                    onCheckedChange = { onUpdateSettings(settings.copy(quietHoursEnabled = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = WaqtiSecondaryGreen)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // 2. Collision Spacing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "منع التصادم والازدحام" else "Collision Prevention",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) "فصل تلقائي بـ ${settings.collisionMinSpacingMinutes} دقيقة بين أي إشعارين"
                        else "Auto min gap: ${settings.collisionMinSpacingMinutes} mins between any alerts",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.collisionPreventionEnabled,
                    onCheckedChange = { onUpdateSettings(settings.copy(collisionPreventionEnabled = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = WaqtiSecondaryGreen)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // 3. Daily Cap Slider
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isArabic) "الحد الأقصى للإشعارات يوميًا" else "Max Daily Notifications Cap",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${settings.maxDailyCap} " + (if (isArabic) "إشعار" else "alerts"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = WaqtiSecondaryGreen
                    )
                }

                Slider(
                    value = settings.maxDailyCap.toFloat(),
                    onValueChange = { onUpdateSettings(settings.copy(maxDailyCap = it.toInt())) },
                    valueRange = 5f..30f,
                    steps = 25,
                    colors = SliderDefaults.colors(
                        thumbColor = WaqtiSecondaryGreen,
                        activeTrackColor = WaqtiSecondaryGreen
                    )
                )
            }
        }
    }
}

@Composable
fun UpcomingSchedulesList(
    schedules: List<com.example.data.notification.NotificationScheduleEntity>,
    isArabic: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "الإشعارات القادمة المجدولة" else "Upcoming Notification Queue",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(WaqtiPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${schedules.size} " + (if (isArabic) "في الطابور" else "queued"),
                        style = MaterialTheme.typography.labelSmall,
                        color = WaqtiPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (schedules.isEmpty()) {
                Text(
                    text = if (isArabic) "لا توجد إشعارات في قائمة الانتظار حالياً." else "No pending scheduled alerts at the moment.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                val timeFormat = remember { SimpleDateFormat("HH:mm - dd MMM", Locale.getDefault()) }
                schedules.take(8).forEach { item ->
                    val dateFormatted = remember(item.scheduledEpochMillis) {
                        timeFormat.format(Date(item.scheduledEpochMillis))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(getCategoryColor(item.category).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(item.category),
                                contentDescription = null,
                                tint = getCategoryColor(item.category),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) item.titleAr else item.titleEn,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isArabic) item.bodyAr else item.bodyEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        Text(
                            text = dateFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = WaqtiPrimary
                        )
                    }
                }
            }
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        NotificationCategory.PRAYER -> Icons.Default.Spa
        NotificationCategory.MORNING_AZKAR, NotificationCategory.EVENING_AZKAR -> Icons.Default.MenuBook
        NotificationCategory.QURAN_ROUTINE, NotificationCategory.QURAN, NotificationCategory.QURAN_VERSE -> Icons.Default.MenuBook
        NotificationCategory.DHIKR -> Icons.Default.Spa
        NotificationCategory.WATER -> Icons.Default.LocalDrink
        NotificationCategory.BREAK -> Icons.Default.Timer
        NotificationCategory.SLEEP -> Icons.Default.Bedtime
        NotificationCategory.EXERCISE -> Icons.Default.FitnessCenter
        NotificationCategory.TASKS, NotificationCategory.TASK -> Icons.Default.CheckCircle
        NotificationCategory.DEADLINES, NotificationCategory.DEADLINE -> Icons.Default.Alarm
        NotificationCategory.FOCUS -> Icons.Default.Timer
        NotificationCategory.AI_SUGGESTIONS, NotificationCategory.AI_SUGGESTION -> Icons.Default.AutoAwesome
        else -> Icons.Default.Notifications
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        NotificationCategory.PRAYER -> WaqtiPrayerAccent
        NotificationCategory.MORNING_AZKAR, NotificationCategory.EVENING_AZKAR -> Color(0xFF10B981)
        NotificationCategory.QURAN_ROUTINE, NotificationCategory.QURAN, NotificationCategory.QURAN_VERSE -> Color(0xFF059669)
        NotificationCategory.DHIKR -> Color(0xFF0D9488)
        NotificationCategory.WATER -> Color(0xFF0284C7)
        NotificationCategory.BREAK -> Color(0xFFF59E0B)
        NotificationCategory.SLEEP -> Color(0xFF6366F1)
        NotificationCategory.EXERCISE -> Color(0xFFEF4444)
        NotificationCategory.TASKS, NotificationCategory.TASK -> WaqtiPrimary
        NotificationCategory.DEADLINES, NotificationCategory.DEADLINE -> WaqtiDanger
        NotificationCategory.FOCUS -> Color(0xFF8B5CF6)
        NotificationCategory.AI_SUGGESTIONS, NotificationCategory.AI_SUGGESTION -> Color(0xFFEC4899)
        else -> WaqtiPrimary
    }
}
