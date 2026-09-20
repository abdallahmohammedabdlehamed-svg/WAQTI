package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.notification.PrayerLocationAndCalcSettings
import com.example.data.prayer.PrayerCalculator
import com.example.localization.AppLanguage
import com.example.ui.theme.WaqtiPrayerAccent
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiSuccess
import com.example.ui.theme.WaqtiWarning
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaCompassDialog(
    settings: PrayerLocationAndCalcSettings,
    language: AppLanguage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isArabic = language == AppLanguage.ARABIC

    // Find city coordinates
    val matchedCity = remember(settings.city) {
        PrayerCalculator.supportedCities.find {
            it.nameAr.contains(settings.city) || it.nameEn.contains(settings.city)
        } ?: PrayerCalculator.supportedCities[0]
    }

    val qiblaAngle = remember(matchedCity) {
        PrayerCalculator.calculateQibla(matchedCity.latitude, matchedCity.longitude)
    }

    val distanceKm = remember(matchedCity) {
        PrayerCalculator.getDistanceToMakkah(matchedCity.latitude, matchedCity.longitude)
    }

    // Heading simulation angle (in true devices this comes from SensorManager, simulated here with interactive drag/auto-align)
    var deviceHeading by remember { mutableFloatStateOf(0f) }
    val animatedHeading by animateFloatAsState(
        targetValue = deviceHeading,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 120f),
        label = "compass_heading"
    )

    // Calculate delta between heading and Qibla angle
    val relativeAngle = (qiblaAngle - animatedHeading + 360) % 360
    val isAligned = abs(relativeAngle) <= 4 || abs(relativeAngle - 360) <= 4

    var hasHapticFired by remember { mutableStateOf(false) }
    LaunchedEffect(isAligned) {
        if (isAligned && !hasHapticFired) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            hasHapticFired = true
        } else if (!isAligned) {
            hasHapticFired = false
        }
    }

    val dialBorderColor by animateColorAsState(
        targetValue = if (isAligned) WaqtiSuccess else WaqtiPrayerAccent.copy(alpha = 0.5f),
        label = "border_color"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("qibla_compass_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(WaqtiPrayerAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = WaqtiPrayerAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "بوصلة القبلة الذكية" else "Smart Qibla Compass",
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
                                    text = if (isArabic) matchedCity.nameAr else matchedCity.nameEn,
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

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Bar: Azimuth & Distance to Makkah
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
                            text = "${qiblaAngle.toInt()}°",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiPrayerAccent
                        )
                        Text(
                            text = if (isArabic) "اتجاه القبلة" else "Qibla Azimuth",
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
                            text = "$distanceKm km",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiPrimary
                        )
                        Text(
                            text = if (isArabic) "المسافة إلى مكة" else "Distance to Makkah",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Compass Graphical Dial (Supports touch drag to rotate)
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .border(3.dp, dialBorderColor, CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                deviceHeading = (deviceHeading - dragAmount.x * 0.4f + 360) % 360
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Canvas for degree ticks & Cardinal letters
                    Canvas(
                        modifier = Modifier
                            .size(240.dp)
                            .rotate(-animatedHeading)
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f - 14.dp.toPx()

                        // Draw tick marks every 30 degrees
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

                    // Cardinal labels overlay
                    Box(modifier = Modifier.size(200.dp).rotate(-animatedHeading)) {
                        Text("N", modifier = Modifier.align(Alignment.TopCenter), color = Color.Red, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        Text("S", modifier = Modifier.align(Alignment.BottomCenter), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("E", modifier = Modifier.align(Alignment.CenterEnd), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("W", modifier = Modifier.align(Alignment.CenterStart), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Golden Qibla Arrow Pointer pointing to (qiblaAngle - animatedHeading)
                    val arrowRotation = (qiblaAngle.toFloat() - animatedHeading + 360) % 360
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .rotate(arrowRotation),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(160.dp)) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val path = Path().apply {
                                moveTo(center.x, center.y - 70.dp.toPx()) // Tip pointing North (Qibla)
                                lineTo(center.x + 12.dp.toPx(), center.y - 15.dp.toPx())
                                lineTo(center.x + 4.dp.toPx(), center.y + 40.dp.toPx())
                                lineTo(center.x - 4.dp.toPx(), center.y + 40.dp.toPx())
                                lineTo(center.x - 12.dp.toPx(), center.y - 15.dp.toPx())
                                close()
                            }
                            drawPath(
                                path = path,
                                color = if (isAligned) WaqtiSuccess else WaqtiPrayerAccent
                            )
                        }

                        // Kaaba emblem at top of arrow
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E1E1E))
                                .border(1.5.dp, WaqtiWarning, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🕋", fontSize = 12.sp)
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

                Spacer(modifier = Modifier.height(18.dp))

                // Alignment Status Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAligned) WaqtiSuccess.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
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
                            imageVector = if (isAligned) Icons.Default.CheckCircle else Icons.Default.NearMe,
                            contentDescription = null,
                            tint = if (isAligned) WaqtiSuccess else WaqtiPrayerAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAligned) {
                                if (isArabic) "✓ أنت تواجه القبلة المشرفة بدقة (تقبل الله)" else "✓ Precisely aligned with the Kaaba"
                            } else {
                                if (isArabic) "أدر الهاتف حتى يشير السهم الذهبي للأعلى 🕋" else "Rotate device until the golden arrow points up"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isAligned) WaqtiSuccess else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action row: Align Simulation & Dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // Automatically align to the exact Qibla angle
                            deviceHeading = qiblaAngle.toFloat()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "محاذاة تلقائية" else "Auto Align", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = WaqtiPrayerAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isArabic) "تم" else "Done", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
