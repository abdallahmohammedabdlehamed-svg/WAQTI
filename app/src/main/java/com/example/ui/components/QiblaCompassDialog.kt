package com.example.ui.components

import android.app.Activity
import android.view.Surface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.notification.PrayerLocationAndCalcSettings
import com.example.data.prayer.WaqtiQiblaManager
import com.example.localization.AppLanguage
import com.example.ui.theme.WaqtiPrayerAccent
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiSuccess
import com.example.ui.theme.WaqtiWarning
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaCompassDialog(
    settings: PrayerLocationAndCalcSettings,
    language: AppLanguage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isArabic = language == AppLanguage.ARABIC

    // High-Precision Qibla Manager
    val qiblaManager = remember { WaqtiQiblaManager(context) }

    // Display rotation compensation
    val displayRotation = remember(context) {
        val windowManager = (context as? Activity)?.windowManager
        @Suppress("DEPRECATION")
        windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
    }

    // Register sensor listener on dialog open and unregister on dismiss (battery safe)
    DisposableEffect(qiblaManager) {
        qiblaManager.startListening(displayRotation)
        onDispose {
            qiblaManager.stopListening()
        }
    }

    // Update location coordinates dynamically whenever settings change
    LaunchedEffect(settings.latitude, settings.longitude, settings.cityName) {
        qiblaManager.updateLocation(settings.latitude, settings.longitude, settings.cityName)
    }

    val compassState by qiblaManager.compassState.collectAsStateWithLifecycle()

    var showDetails by remember { mutableStateOf(false) }

    // Haptic feedback trigger on exact alignment
    var hasHapticFired by remember { mutableStateOf(false) }
    LaunchedEffect(compassState.isAligned) {
        if (compassState.isAligned && !hasHapticFired) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            hasHapticFired = true
        } else if (!compassState.isAligned) {
            hasHapticFired = false
        }
    }

    val animatedDialRotation by animateFloatAsState(
        targetValue = compassState.trueHeading,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 140f),
        label = "compass_dial_rotation"
    )

    val animatedArrowRotation by animateFloatAsState(
        targetValue = compassState.relativeQiblaAngle,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 140f),
        label = "qibla_arrow_rotation"
    )

    val dialBorderColor by animateColorAsState(
        targetValue = if (compassState.isAligned) WaqtiSuccess else WaqtiPrayerAccent.copy(alpha = 0.5f),
        label = "border_color"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("qibla_compass_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
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
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "بوصلة القبلة عالية الدقة" else "High-Precision Qibla",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${settings.cityName}، ${settings.governorate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Sensor Availability Warning Banner
                if (!compassState.isSensorAvailable) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic)
                                    "هذا الجهاز لا يدعم البوصلة المطلوبة بدقة."
                                else
                                    "This device does not provide the required compass sensors.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Calibration Warning Banner (Only shown if sensor accuracy is low or unreliable)
                if (compassState.needsCalibration) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WaqtiWarning.copy(alpha = 0.15f)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(WaqtiWarning, WaqtiWarning.copy(alpha = 0.5f)))),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = WaqtiWarning, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "البوصلة تحتاج إلى معايرة" else "Compass calibration needed",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WaqtiWarning
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isArabic)
                                    "حرّك الهاتف في شكل رقم 8 عدة مرات بعيدًا عن الأجهزة المعدنية والمغناطيسية."
                                else
                                    "Move your device in a figure-8 motion away from metallic objects.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Bar: Qibla Bearing & Distance to Kaaba
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.1f°", compassState.qiblaBearing),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiPrayerAccent
                        )
                        Text(
                            text = if (isArabic) "اتجاه القبلة (من الشمال الحقيقي)" else "Qibla Bearing (True N)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${compassState.distanceToKaabaKm} km",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiPrimary
                        )
                        Text(
                            text = if (isArabic) "المسافة إلى الكعبة" else "Distance to Kaaba",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Compass Graphical Dial (High-Precision Real Sensors)
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .border(3.dp, dialBorderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Canvas for degree ticks & True North indicators
                    Canvas(
                        modifier = Modifier
                            .size(250.dp)
                            .rotate(-animatedDialRotation)
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f - 14.dp.toPx()

                        // Draw tick marks every 15 degrees
                        for (deg in 0 until 360 step 15) {
                            val isMajor = deg % 90 == 0
                            val isSemi = deg % 30 == 0
                            val tickLength = if (isMajor) 14.dp.toPx() else if (isSemi) 8.dp.toPx() else 4.dp.toPx()
                            val strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.dp.toPx()
                            val rad = Math.toRadians(deg.toDouble())

                            val startX = center.x + (radius - tickLength) * sin(rad).toFloat()
                            val startY = center.y - (radius - tickLength) * cos(rad).toFloat()
                            val endX = center.x + radius * sin(rad).toFloat()
                            val endY = center.y - radius * cos(rad).toFloat()

                            val tickColor = if (deg == 0) Color.Red else Color.Gray.copy(alpha = if (isMajor) 0.8f else 0.4f)
                            drawLine(
                                color = tickColor,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = strokeWidth
                            )
                        }
                    }

                    // Cardinal labels overlay (N, S, E, W) rotating with the compass dial
                    Box(modifier = Modifier.size(208.dp).rotate(-animatedDialRotation)) {
                        Text(
                            "N",
                            modifier = Modifier.align(Alignment.TopCenter),
                            color = Color.Red,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                        Text(
                            "S",
                            modifier = Modifier.align(Alignment.BottomCenter),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            "E",
                            modifier = Modifier.align(Alignment.CenterEnd),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            "W",
                            modifier = Modifier.align(Alignment.CenterStart),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Golden Qibla Arrow Pointer pointing directly to Kaaba relative angle
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .rotate(animatedArrowRotation),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(170.dp)) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val path = Path().apply {
                                moveTo(center.x, center.y - 74.dp.toPx()) // Tip pointing to Qibla
                                lineTo(center.x + 13.dp.toPx(), center.y - 15.dp.toPx())
                                lineTo(center.x + 4.dp.toPx(), center.y + 44.dp.toPx())
                                lineTo(center.x - 4.dp.toPx(), center.y + 44.dp.toPx())
                                lineTo(center.x - 13.dp.toPx(), center.y - 15.dp.toPx())
                                close()
                            }
                            drawPath(
                                path = path,
                                color = if (compassState.isAligned) WaqtiSuccess else WaqtiPrayerAccent
                            )
                        }

                        // Kaaba emblem at the top rim of the pointer arrow
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .size(26.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFF1E1E1E))
                                .border(1.5.dp, WaqtiWarning, RoundedCornerShape(5.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🕋", fontSize = 13.sp)
                        }
                    }

                    // Center Hub
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(2.dp, WaqtiPrayerAccent, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Alignment Status Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (compassState.isAligned) WaqtiSuccess.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (compassState.isAligned) Icons.Default.CheckCircle else Icons.Default.NearMe,
                            contentDescription = null,
                            tint = if (compassState.isAligned) WaqtiSuccess else WaqtiPrayerAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (compassState.isAligned) {
                                if (isArabic) "أنت في اتجاه القبلة (تقبل الله)" else "You are facing the Qibla"
                            } else {
                                if (isArabic) "أدر الهاتف حتى يشير السهم الذهبي للأعلى 🕋" else "Rotate device until the golden arrow points up"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (compassState.isAligned) WaqtiSuccess else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Technical Details Expandable Toggle ("تفاصيل البوصلة")
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showDetails = !showDetails }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "تفاصيل البوصلة الفلكية" else "Technical Compass Details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (showDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }

                AnimatedVisibility(
                    visible = showDetails,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            TechnicalRow(
                                label = if (isArabic) "زاوية القبلة:" else "Qibla Bearing:",
                                value = String.format(Locale.US, "%.1f°", compassState.qiblaBearing)
                            )
                            TechnicalRow(
                                label = if (isArabic) "اتجاه الهاتف (شمال حقيقي):" else "Current True Heading:",
                                value = String.format(Locale.US, "%.1f°", compassState.trueHeading)
                            )
                            TechnicalRow(
                                label = if (isArabic) "فارق الزاوية:" else "Angle Difference:",
                                value = String.format(Locale.US, "%+.1f°", compassState.angleDifference)
                            )
                            TechnicalRow(
                                label = if (isArabic) "الانحراف المغناطيسي:" else "Magnetic Declination:",
                                value = String.format(Locale.US, "%+.1f°", compassState.magneticDeclination)
                            )
                            TechnicalRow(
                                label = if (isArabic) "نوع المستشعر المستخدم:" else "Active Sensor:",
                                value = compassState.sensorTypeUsed
                            )
                            TechnicalRow(
                                label = if (isArabic) "إحداثيات الموقع:" else "Coordinates:",
                                value = String.format(Locale.US, "%.4f, %.4f", settings.latitude, settings.longitude)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = WaqtiPrayerAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isArabic) "إغلاق" else "Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TechnicalRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
