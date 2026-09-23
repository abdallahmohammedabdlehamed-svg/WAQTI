package com.example.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.style.TextOverflow
import com.example.localization.Strings
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
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val audioSettings by viewModel.audioSettings.collectAsStateWithLifecycle()

    var selectedSectionTab by remember { mutableIntStateOf(0) } // 0: Categories, 1: Prayer Engine, 2: Audio 2.0, 3: Smart Control & Logs
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
                .horizontalScroll(rememberScrollState())
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
                )
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
                )
            )

            FilterChip(
                selected = selectedSectionTab == 2,
                onClick = { selectedSectionTab = 2 },
                label = { Text(if (isArabic) "الأصوات والتنبيهات 2.0" else "Audio & Alerts 2.0") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WaqtiPrimary.copy(alpha = 0.15f),
                    selectedLabelColor = WaqtiPrimary
                )
            )

            FilterChip(
                selected = selectedSectionTab == 3,
                onClick = { selectedSectionTab = 3 },
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
                )
            )
        }

        // System Notification Disabled Warning Banner
        AnimatedVisibility(visible = !notificationsEnabled) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = WaqtiDanger.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, WaqtiDanger.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = WaqtiDanger,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "إشعارات النظام غير مفعّلة!" else "System Notifications Disabled!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiDanger
                        )
                        Text(
                            text = if (isArabic) "يرجى منح إذن الإشعارات لتصلك مواقيت الصلاة والمهام اليومية في وقتها المحدد."
                            else "Please grant notification permission so you receive prayers and task reminders on time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.triggerRequestNotificationPermission() },
                            colors = ButtonDefaults.buttonColors(containerColor = WaqtiDanger),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = if (isArabic) "تفعيل الإشعارات الآن 🔔" else "Enable Notifications Now 🔔",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
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
                    // AUDIO AND ALERTS 2.0 CONFIGURATION
                    item {
                        AudioAndAlertsConfigurationView(
                            audioSettings = audioSettings,
                            onUpdateAudioSettings = { viewModel.updateAudioSettings(it) },
                            viewModel = viewModel,
                            isArabic = isArabic
                        )
                    }
                }

                3 -> {
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
    val context = LocalContext.current
    var isGpsLoading by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            isGpsLoading = true
            viewModel.refreshLocationFromGps { success ->
                isGpsLoading = false
                Toast.makeText(
                    context,
                    if (success) {
                        if (isArabic) "تم تحديد الموقع بدقة عبر GPS بنجاح 🛰️" else "Location updated via GPS 🛰️"
                    } else {
                        if (isArabic) "تعذر جلب موقع GPS، تم الاحتفاظ بالموقع الحالي" else "Could not obtain GPS, kept current location"
                    },
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                if (isArabic) "يرجى منح إذن الموقع لتحديد مواقيت الصلاة بدقة" else "Please grant location permission for accurate prayer times",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val prayerTimes = remember(settings) {
        viewModel.getTodayPrayerTimes()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Current Location & GPS Action Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, WaqtiPrayerAccent.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = WaqtiPrayerAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "الموقع الحالي للمواقيت" else "Active Prayer Location",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Source Tag Pill
                    val sourceText = when (settings.locationSource) {
                        "GPS" -> if (isArabic) "تلقائي GPS" else "GPS Auto"
                        "MANUAL" -> if (isArabic) "يدوي" else "Manual"
                        else -> if (isArabic) "افتراضي (المنصورة)" else "Default (Mansoura)"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (settings.locationSource == "GPS") WaqtiSuccess.copy(alpha = 0.15f)
                                else WaqtiPrayerAccent.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = sourceText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (settings.locationSource == "GPS") WaqtiSuccess else WaqtiPrayerAccent
                        )
                    }
                }

                // Detailed Location Text & Coordinates
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "${settings.cityName}، ${settings.governorate}، ${settings.countryName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "خط العرض: ${String.format(Locale.US, "%.4f", settings.latitude)}° | خط الطول: ${String.format(Locale.US, "%.4f", settings.longitude)}°",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // GPS Refresh Button
                Button(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaqtiPrayerAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    enabled = !isGpsLoading
                ) {
                    if (isGpsLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isArabic) "جاري تحديد الموقع..." else "Locating GPS...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "تحديد موقعي الحالي تلقائياً (GPS)" else "Use Current Location (GPS)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 2. City Presets (Mansoura as Default + Egyptian Governorates & Holy Cities)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isArabic) "اختيار مدينة محددة (مصر والعالم الإسلامي):" else "Select Preset City:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                val presetCities = com.example.data.prayer.PrayerCalculator.supportedCities
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetCities.forEach { preset ->
                        val isSelected = (settings.cityName == preset.cityNameAr || settings.cityName == preset.cityNameEn)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.selectCityPreset(preset)
                            },
                            label = {
                                Text(
                                    text = if (isArabic) "${preset.cityNameAr} (${preset.governorateAr})" else "${preset.cityNameEn} (${preset.governorateEn})",
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WaqtiPrayerAccent.copy(alpha = 0.2f),
                                selectedLabelColor = WaqtiPrayerAccent
                            )
                        )
                    }
                }
            }
        }

        // 3. Calculation Method Selector
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isArabic) "طريقة الحساب الفلكي المعتمدة:" else "Calculation Authority:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                val methods = listOf(
                    "EGYPTIAN" to (if (isArabic) "الهيئة المصرية العامة للمساحة (مصر)" else "Egyptian General Authority (Egypt)"),
                    "UMM_AL_QURA" to (if (isArabic) "أم القرى (مكة المكرمة)" else "Umm Al-Qura (Makkah)"),
                    "MWL" to (if (isArabic) "رابطة العالم الإسلامي" else "Muslim World League (MWL)"),
                    "KARACHI" to (if (isArabic) "جامعة العلوم الإسلامية (كراتشي)" else "Univ. of Islamic Sciences (Karachi)"),
                    "ISNA" to (if (isArabic) "الجمعية الإسلامية لأمريكا الشمالية (ISNA)" else "ISNA (North America)")
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    methods.forEach { (methodKey, methodLabel) ->
                        val isSelected = settings.calcMethod == methodKey
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) WaqtiPrayerAccent.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .clickable { onUpdateSettings(settings.copy(calcMethod = methodKey)) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = methodLabel,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) WaqtiPrayerAccent else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = WaqtiPrayerAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Today Prayer Times with Individual Toggles
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
                                if (!prayer.isPrescribedPrayer) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isArabic) "(شروق الشمس)" else "(Sunrise)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = prayer.timeFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Switch per prayer (for obligatory prayers)
                        if (prayer.isPrescribedPrayer) {
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

@Composable
fun AudioAndAlertsConfigurationView(
    audioSettings: com.example.audio.WaqtiAudioPreferences.AudioSettingsState,
    onUpdateAudioSettings: ((com.example.audio.WaqtiAudioPreferences.AudioSettingsState) -> com.example.audio.WaqtiAudioPreferences.AudioSettingsState) -> Unit,
    viewModel: WaqtiViewModel,
    isArabic: Boolean
) {
    val context = LocalContext.current
    val lang = if (isArabic) AppLanguage.ARABIC else AppLanguage.ENGLISH

    var showRemoveCustomAdhanDialog by remember { mutableStateOf(false) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val success = viewModel.onCustomAdhanFileSelected(uri, context)
            if (success) {
                Toast.makeText(context, Strings.adhanAudioSelected(lang), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, Strings.unsupportedAudioFile(lang), Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showRemoveCustomAdhanDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveCustomAdhanDialog = false },
            title = { Text(text = Strings.removeCustomAdhan(lang), fontWeight = FontWeight.Bold) },
            text = { Text(text = Strings.removeCustomAdhanConfirm(lang)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeCustomAdhan(context)
                        showRemoveCustomAdhanDialog = false
                        Toast.makeText(context, Strings.customAdhanRemoved(lang), Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(text = Strings.confirm(lang), color = WaqtiDanger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveCustomAdhanDialog = false }) {
                    Text(text = Strings.cancel(lang))
                }
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Hero Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WaqtiPrimary.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, WaqtiPrimary.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(WaqtiPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = WaqtiPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = if (isArabic) "محرك الأصوات والتنبيهات 2.0" else "WAQTI Audio & Alerts 2.0",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) "تحكم كامل في نغمات المهام، نداء الأذان، جلسات التركيز، واختبار الأصوات مباشرة."
                        else "Complete control over task chimes, Adhan audio, focus cues, and live test sounds.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 1. Master Controls Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isArabic) "مفاتيح التحكم العامة" else "Master Audio Controls",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Master Notifications Toggle
                AudioToggleRow(
                    title = if (isArabic) "كافة الإشعارات" else "Master Notifications",
                    subtitle = if (isArabic) "تشغيل أو إيقاف وصول الإشعارات بالكامل" else "Global on/off switch for all notifications",
                    icon = Icons.Default.NotificationsActive,
                    iconTint = WaqtiPrimary,
                    checked = audioSettings.masterNotificationsEnabled,
                    onCheckedChange = { chk ->
                        onUpdateAudioSettings { it.copy(masterNotificationsEnabled = chk) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Master Sound Toggle
                AudioToggleRow(
                    title = if (isArabic) "تشغيل الأصوات العامة" else "Master Sound",
                    subtitle = if (isArabic) "تفعيل الرنين الصوتي للإشعارات والتنبيهات" else "Enable audible tones and ringers globally",
                    icon = Icons.Default.VolumeUp,
                    iconTint = WaqtiPrimary,
                    checked = audioSettings.masterSoundEnabled,
                    onCheckedChange = { chk ->
                        onUpdateAudioSettings { it.copy(masterSoundEnabled = chk) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Master Vibration Toggle
                AudioToggleRow(
                    title = if (isArabic) "الاهتزاز" else "Master Vibration",
                    subtitle = if (isArabic) "تفعيل الاهتزاز مع التنبيهات" else "Trigger haptic vibrations with alerts",
                    icon = Icons.Default.Vibration,
                    iconTint = WaqtiSecondaryGreen,
                    checked = audioSettings.masterVibrationEnabled,
                    onCheckedChange = { chk ->
                        onUpdateAudioSettings { it.copy(masterVibrationEnabled = chk) }
                    }
                )
            }
        }

        // 2. Category Sound Controls Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isArabic) "أصوات الفئات المختلفة" else "Sound by Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                AudioToggleRow(
                    title = if (isArabic) "تذكيرات المهام اليومية" else "Task Reminders",
                    subtitle = if (isArabic) "نغمة هادئة عند موعد المهمة" else "Gentle chime on task due times",
                    icon = Icons.Default.CheckCircle,
                    iconTint = WaqtiPrimary,
                    checked = audioSettings.taskReminderSoundEnabled,
                    onCheckedChange = { chk ->
                        onUpdateAudioSettings { it.copy(taskReminderSoundEnabled = chk) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                AudioToggleRow(
                    title = if (isArabic) "نغمة إنجاز المهام" else "Task Completion Chime",
                    subtitle = if (isArabic) "نغمة إيجابية فور تحديد المهمة كمكتملة" else "Rewarding chime on checking off a task",
                    icon = Icons.Default.CheckCircle,
                    iconTint = WaqtiSuccess,
                    checked = audioSettings.taskCompletionSoundEnabled,
                    onCheckedChange = { chk ->
                        onUpdateAudioSettings { it.copy(taskCompletionSoundEnabled = chk) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                AudioToggleRow(
                    title = if (isArabic) "أصوات جلسات التركيز (بومودورو)" else "Focus Sessions Sound",
                    subtitle = if (isArabic) "صوت البدء، رنين الإنجاز، وتنبيه قبل 5 دقائق" else "Start chime, completion bell & 5-min warning",
                    icon = Icons.Default.Timer,
                    iconTint = Color(0xFF8B5CF6),
                    checked = audioSettings.focusSoundEnabled,
                    onCheckedChange = { chk ->
                        onUpdateAudioSettings { it.copy(focusSoundEnabled = chk) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                AudioToggleRow(
                    title = if (isArabic) "اقتراحات مساعد وقتي الذكي" else "AI Suggestions Sound",
                    subtitle = if (isArabic) "صوت تنبيه التوصيات وإعادة الجدولة" else "Sound on AI dynamic recommendations",
                    icon = Icons.Default.AutoAwesome,
                    iconTint = Color(0xFFEC4899),
                    checked = audioSettings.aiNotificationSoundEnabled,
                    onCheckedChange = { chk ->
                        onUpdateAudioSettings { it.copy(aiNotificationSoundEnabled = chk) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                AudioToggleRow(
                    title = if (isArabic) "ورد القرآن الكريم" else "Quran Routine Sound",
                    subtitle = if (isArabic) "نغمة هادئة لوقت التلاوة اليومي" else "Tranquil sound for Quran recitation",
                    icon = Icons.Default.MenuBook,
                    iconTint = Color(0xFF059669),
                    checked = audioSettings.quranReminderSoundEnabled,
                    onCheckedChange = { chk ->
                        onUpdateAudioSettings { it.copy(quranReminderSoundEnabled = chk) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                AudioToggleRow(
                    title = if (isArabic) "أذكار الصباح والمساء" else "Azkar Reminders Sound",
                    subtitle = if (isArabic) "تنبيه لطيف بموعد الأذكار" else "Soft notification for Morning/Evening Azkar",
                    icon = Icons.Default.Spa,
                    iconTint = Color(0xFF10B981),
                    checked = audioSettings.azkarReminderSoundEnabled,
                    onCheckedChange = { chk ->
                        onUpdateAudioSettings { it.copy(azkarReminderSoundEnabled = chk) }
                    }
                )
            }
        }

        // 3. Prayer & Adhan Controls Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, WaqtiPrayerAccent.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Spa, contentDescription = null, tint = WaqtiPrayerAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "الصلوات ونداء الأذان" else "Prayers & Adhan Audio",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                AudioToggleRow(
                    title = if (isArabic) "تنبيهات مواقيت الصلاة" else "Prayer Alerts",
                    subtitle = if (isArabic) "إرسال إشعارات مواقيت الصلوات الخمس" else "Send notifications on prayer times",
                    icon = Icons.Default.NotificationsActive,
                    iconTint = WaqtiPrayerAccent,
                    checked = audioSettings.prayerNotificationEnabled,
                    onCheckedChange = { chk ->
                        onUpdateAudioSettings { it.copy(prayerNotificationEnabled = chk) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                AudioToggleRow(
                    title = if (isArabic) "صوت الأذان العام" else "Master Adhan Audio",
                    subtitle = if (isArabic) "تشغيل صوت الأذان الشرعي عند حلول الوقت" else "Play authentic Adhan recitation at prayer time",
                    icon = Icons.Default.VolumeUp,
                    iconTint = WaqtiPrayerAccent,
                    checked = audioSettings.adhanEnabledGlobal,
                    onCheckedChange = { chk ->
                        onUpdateAudioSettings { it.copy(adhanEnabledGlobal = chk) }
                    }
                )

                Text(
                    text = if (isArabic) "تخصيص الأذان لكل صلاة منفردة:" else "Per-Prayer Adhan Controls:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Individual 5 Prayers Adhan Toggles
                val prayersList = listOf(
                    Triple(if (isArabic) "صلاة الفجر" else "Fajr", audioSettings.fajrAdhanEnabled) { chk: Boolean ->
                        onUpdateAudioSettings { it.copy(fajrAdhanEnabled = chk) }
                    },
                    Triple(if (isArabic) "صلاة الظهر" else "Dhuhr", audioSettings.dhuhrAdhanEnabled) { chk: Boolean ->
                        onUpdateAudioSettings { it.copy(dhuhrAdhanEnabled = chk) }
                    },
                    Triple(if (isArabic) "صلاة العصر" else "Asr", audioSettings.asrAdhanEnabled) { chk: Boolean ->
                        onUpdateAudioSettings { it.copy(asrAdhanEnabled = chk) }
                    },
                    Triple(if (isArabic) "صلاة المغرب" else "Maghrib", audioSettings.maghribAdhanEnabled) { chk: Boolean ->
                        onUpdateAudioSettings { it.copy(maghribAdhanEnabled = chk) }
                    },
                    Triple(if (isArabic) "صلاة العشاء" else "Isha", audioSettings.ishaAdhanEnabled) { chk: Boolean ->
                        onUpdateAudioSettings { it.copy(ishaAdhanEnabled = chk) }
                    }
                )

                prayersList.forEach { (name, isAdhanOn, onToggle) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = isAdhanOn,
                            onCheckedChange = onToggle,
                            colors = SwitchDefaults.colors(checkedThumbColor = WaqtiPrayerAccent)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Adhan Volume Slider with percentage readout
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = WaqtiPrayerAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "مستوى صوت الأذان" else "Adhan Volume",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${(audioSettings.adhanVolume * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiPrayerAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = audioSettings.adhanVolume,
                        onValueChange = { newVol ->
                            onUpdateAudioSettings { it.copy(adhanVolume = newVol) }
                        },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = WaqtiPrayerAccent,
                            activeTrackColor = WaqtiPrayerAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("adhan_volume_slider")
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Quiet hours allowance toggle
                AudioToggleRow(
                    title = if (isArabic) "السماح بالأذان أثناء الهدوء" else "Allow Adhan during Quiet Hours",
                    subtitle = if (isArabic) "تشغيل نداء الصلاة حتى وإن كانت ساعات الهدوء مفعلة" else "Play Adhan even if quiet hours are currently active",
                    icon = Icons.Default.Bedtime,
                    iconTint = WaqtiPrayerAccent,
                    checked = audioSettings.quietHoursAllowPrayers,
                    onCheckedChange = { chk ->
                        onUpdateAudioSettings { it.copy(quietHoursAllowPrayers = chk) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Custom Adhan Audio Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = WaqtiPrayerAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.adhanAudioSectionTitle(lang),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val isCustom = audioSettings.customAdhanEnabled && !audioSettings.customAdhanUri.isNullOrBlank()

                    if (isCustom) {
                        Text(
                            text = Strings.customAdhan(lang),
                            style = MaterialTheme.typography.labelMedium,
                            color = WaqtiPrayerAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = WaqtiPrayerAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = audioSettings.customAdhanFileName ?: "Custom Adhan",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        audioSettings.customAdhanDuration?.let { durMs ->
                            if (durMs > 0) {
                                val totalSec = durMs / 1000
                                val min = totalSec / 60
                                val sec = totalSec % 60
                                Text(
                                    text = String.format("%02d:%02d", min, sec),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = Strings.defaultAdhan(lang),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Strings.defaultAdhanName(lang),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                audioPickerLauncher.launch(arrayOf("audio/*", "audio/mpeg"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("choose_custom_adhan_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCustom) Strings.changeAdhanAudio(lang) else Strings.chooseAdhanAudio(lang),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        if (isCustom) {
                            OutlinedButton(
                                onClick = {
                                    showRemoveCustomAdhanDialog = true
                                },
                                modifier = Modifier.testTag("remove_custom_adhan_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = WaqtiDanger),
                                border = BorderStroke(1.dp, WaqtiDanger.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = WaqtiDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = Strings.removeCustomAdhan(lang),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = WaqtiDanger
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action Buttons: Test Adhan & Stop Adhan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.playTestAdhan("Fajr")
                            Toast.makeText(context, if (isArabic) "بدء تجربة صوت الأذان الحقيقي" else "Playing authentic Adhan audio", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("test_adhan_action_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WaqtiPrayerAccent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "🔊 تجربة الأذان" else "🔊 Test Adhan",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.stopAdhan()
                            Toast.makeText(context, if (isArabic) "تم إيقاف الأذان" else "Adhan stopped", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("stop_adhan_action_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WaqtiDanger),
                        border = BorderStroke(1.dp, WaqtiDanger.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            tint = WaqtiDanger,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "إيقاف" else "Stop",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiDanger
                        )
                    }
                }
            }
        }

        // 4. Quiet Hours Behavior Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bedtime, contentDescription = null, tint = Color(0xFF6366F1))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "استثناءات ساعات الهدوء والنوم" else "Quiet Hours Audio Policy",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                AudioToggleRow(
                    title = if (isArabic) "السماح بالصلوات أثناء الهدوء" else "Allow Prayers during Quiet Hours",
                    subtitle = if (isArabic) "تجاوز وضع الهدوء لصلاة الفجر والعشاء" else "Exempt prayer alerts from being silenced",
                    icon = Icons.Default.Spa,
                    iconTint = WaqtiPrayerAccent,
                    checked = audioSettings.quietHoursAllowPrayers,
                    onCheckedChange = { chk ->
                        onUpdateAudioSettings { it.copy(quietHoursAllowPrayers = chk) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                AudioToggleRow(
                    title = if (isArabic) "السماح بالتنبيهات الحرجة" else "Allow Critical Alerts",
                    subtitle = if (isArabic) "تجاوز وضع الهدوء للمهام ذات الأولوية القصوى" else "Exempt critical deadline notifications",
                    icon = Icons.Default.Alarm,
                    iconTint = WaqtiDanger,
                    checked = audioSettings.quietHoursAllowCritical,
                    onCheckedChange = { chk ->
                        onUpdateAudioSettings { it.copy(quietHoursAllowCritical = chk) }
                    }
                )
            }
        }

        // 5. Interactive Sound Testing Console (LIVE AUDIO BUTTONS)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, WaqtiPrimary.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = WaqtiPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "منصة اختبار الأصوات الحية" else "Live Audio Test Console",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (isArabic) "اضغط على أي زر لتشغيل النغمة التوليدية فورا على جهازك والتأكد من وضوح الصوت:"
                    else "Tap any button to trigger immediate procedural tone playback on your device:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 2x3 Grid of Sound Test Buttons
                val testButtons = listOf(
                    Triple(
                        if (isArabic) "🔔 إشعار عام" else "🔔 Notification",
                        "test_sound_notification"
                    ) {
                        viewModel.playTestSound(com.example.audio.WaqtiAudioManager.AudioType.NOTIFICATION)
                        Toast.makeText(context, if (isArabic) "تشغيل نغمة الإشعار" else "Playing notification sound", Toast.LENGTH_SHORT).show()
                    },
                    Triple(
                        if (isArabic) "⏰ تذكير مهمة" else "⏰ Reminder",
                        "test_sound_reminder"
                    ) {
                        viewModel.playTestSound(com.example.audio.WaqtiAudioManager.AudioType.REMINDER)
                        Toast.makeText(context, if (isArabic) "تشغيل نغمة التذكير" else "Playing reminder chime", Toast.LENGTH_SHORT).show()
                    },
                    Triple(
                        if (isArabic) "✨ إنجاز مهمة" else "✨ Completion",
                        "test_sound_completion"
                    ) {
                        viewModel.playTestSound(com.example.audio.WaqtiAudioManager.AudioType.TASK_COMPLETION)
                        Toast.makeText(context, if (isArabic) "تشغيل نغمة الإنجاز" else "Playing completion chime", Toast.LENGTH_SHORT).show()
                    },
                    Triple(
                        if (isArabic) "🎯 بدء التركيز" else "🎯 Focus Start",
                        "test_sound_focus"
                    ) {
                        viewModel.playTestSound(com.example.audio.WaqtiAudioManager.AudioType.FOCUS_START)
                        Toast.makeText(context, if (isArabic) "تشغيل نغمة بدء التركيز" else "Playing focus start sound", Toast.LENGTH_SHORT).show()
                    },
                    Triple(
                        if (isArabic) "⚠️ تحذير 5 دقائق" else "⚠️ Warning",
                        "test_sound_warning"
                    ) {
                        viewModel.playTestSound(com.example.audio.WaqtiAudioManager.AudioType.WARNING)
                        Toast.makeText(context, if (isArabic) "تشغيل نغمة التحذير" else "Playing warning sound", Toast.LENGTH_SHORT).show()
                    },
                    Triple(
                        if (isArabic) "🕌 صوت الأذان" else "🕌 Test Adhan",
                        "test_sound_adhan"
                    ) {
                        viewModel.playTestAdhan("Fajr")
                        Toast.makeText(context, if (isArabic) "بدء نداء الأذان التجريبي" else "Playing test Adhan", Toast.LENGTH_SHORT).show()
                    }
                )

                testButtons.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (label, tag, action) ->
                            Button(
                                onClick = action,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag(tag),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tag == "test_sound_adhan") WaqtiPrayerAccent else WaqtiPrimary
                                )
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Stop All Audio Button
                OutlinedButton(
                    onClick = {
                        viewModel.stopAdhan()
                        Toast.makeText(context, if (isArabic) "تم إيقاف كافة الأصوات" else "Stopped all audio", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("stop_audio_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WaqtiDanger),
                    border = BorderStroke(1.dp, WaqtiDanger.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = WaqtiDanger,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "إيقاف الصوت والأذان فورا ⏹️" else "Stop All Audio & Adhan ⏹️",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = WaqtiDanger
                    )
                }
            }
        }

        // 6. Ambient Sounds Status Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = WaqtiPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "الأصوات الطبيعية المحيطية (Ambient Audio)" else "Ambient Focus Backgrounds",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (isArabic)
                        "الأصوات الطبيعية (Rain, Forest, Cafe, White Noise, Ocean, Sanctuary) مهيأة في شاشة التركيز. عند توفر ملفات صوتية في res/raw سيتم تشغيلها تلقائيا، مع معالجة آمنة بدون انقطاع أو افتعال ملفات وهمية."
                    else
                        "Ambient loops (Rain, Forest, Cafe, White Noise, Ocean, Sanctuary) are mapped in the Focus screen. They will smoothly play when local assets are placed in res/raw, with safe non-blocking fallbacks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AudioToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = iconTint)
        )
    }
}

