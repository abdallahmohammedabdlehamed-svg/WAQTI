package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiSuccess
import com.example.ui.theme.WaqtiWarning
import com.example.ui.viewmodel.WaqtiViewModel

@Composable
fun WaqtiDialogs(viewModel: WaqtiViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val rescheduleResult by viewModel.rescheduleResult.collectAsStateWithLifecycle()
    val breakdownTask by viewModel.breakdownTask.collectAsStateWithLifecycle()
    val breakdownSteps by viewModel.breakdownSteps.collectAsStateWithLifecycle()
    val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle()

    // 1. Reschedule / I'm Behind Dialog
    rescheduleResult?.let { res ->
        val explanation = if (language == AppLanguage.ARABIC) res.explanationAr else res.explanationEn
        AlertDialog(
            onDismissRequest = { viewModel.dismissReschedule() },
            icon = {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = WaqtiPrimary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = if (language == AppLanguage.ARABIC) "إعادة تنظيم اليوم الذكية" else "Intelligent Day Rescheduling",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (language == AppLanguage.ARABIC) "أهم التعديلات:" else "Key Adjustments:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = WaqtiPrimary
                    )

                    res.changes.forEach { change ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = WaqtiSuccess,
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = change,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.applyReschedule() },
                    colors = ButtonDefaults.buttonColors(containerColor = WaqtiPrimary),
                    modifier = Modifier.testTag("confirm_reschedule_button")
                ) {
                    Text(if (language == AppLanguage.ARABIC) "تطبيق التعديلات" else "Apply Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissReschedule() }) {
                    Text(if (language == AppLanguage.ARABIC) "إلغاء" else "Cancel")
                }
            }
        )
    }

    // 2. Task Breakdown Dialog
    breakdownTask?.let { task ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissBreakdown() },
            icon = {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WaqtiPrimary)
            },
            title = {
                Text(
                    text = if (language == AppLanguage.ARABIC) "تقسيم المهمة: ${task.title}" else "Task Breakdown: ${task.title}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ARABIC)
                            "قام الذكاء الاصطناعي بتحويل مهمتك إلى خطوات تنفيذية واضحة:"
                        else
                            "AI generated clear, executable subtasks for your goal:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    breakdownSteps.forEachIndexed { idx, step ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(WaqtiPrimary.copy(alpha = 0.12f))
                            ) {
                                Text("${idx + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WaqtiPrimary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(step, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.applyBreakdownSubtasks() },
                    colors = ButtonDefaults.buttonColors(containerColor = WaqtiPrimary)
                ) {
                    Text(if (language == AppLanguage.ARABIC) "حفظ الخطوات" else "Save Subtasks")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissBreakdown() }) {
                    Text(if (language == AppLanguage.ARABIC) "إغلاق" else "Close")
                }
            }
        )
    }
}
