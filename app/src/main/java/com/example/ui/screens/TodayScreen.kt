package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TaskEntity
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.theme.WaqtiDanger
import com.example.ui.theme.WaqtiPrayerAccent
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiSuccess
import com.example.ui.theme.WaqtiWarning
import com.example.ui.viewmodel.WaqtiViewModel

data class TimelineItem(
    val id: String,
    val time: String,
    val title: String,
    val subtitle: String,
    val durationMin: Int,
    val isProtected: Boolean,
    val isCompleted: Boolean,
    val isRoutine: Boolean,
    val priority: String,
    val rawTask: TaskEntity? = null
)

@Composable
fun TodayScreen(
    viewModel: WaqtiViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val routines by viewModel.routines.collectAsStateWithLifecycle()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDuration by remember { mutableStateOf("45") }
    var newTaskPriority by remember { mutableStateOf("MEDIUM") }
    var addTaskError by remember { mutableStateOf<String?>(null) }

    val timelineItems = remember(tasks, routines, language) {
        val taskItems = tasks.map { t ->
            TimelineItem(
                id = "task_${t.id}",
                time = t.startTime,
                title = t.title,
                subtitle = t.projectName.ifBlank { t.category },
                durationMin = t.durationMinutes,
                isProtected = t.isProtected,
                isCompleted = t.status == "COMPLETED",
                isRoutine = false,
                priority = t.priority,
                rawTask = t
            )
        }
        val routineItems = routines.map { r ->
            TimelineItem(
                id = "routine_${r.id}",
                time = r.time,
                title = if (language == AppLanguage.ARABIC) r.titleAr else r.title,
                subtitle = if (language == AppLanguage.ARABIC) "برنامجي اليومي (${r.category})" else "Daily Routine (${r.category})",
                durationMin = r.durationMinutes,
                isProtected = r.isProtected,
                isCompleted = r.isCompleted,
                isRoutine = true,
                priority = if (r.isProtected) "HIGH" else "MEDIUM"
            )
        }
        (taskItems + routineItems)
            .distinctBy { "${it.isRoutine}_${it.time}_${it.title.trim().lowercase()}" }
            .sortedBy { it.time }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = WaqtiPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = Strings.addTask(language))
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("today_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Strings.timelineTitle(language),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (language == AppLanguage.ARABIC) "أوقاتك وأنشطتك مرتبة حسب الساعات ومحمية ذكياً" else "Your hours and commitments synchronized intelligently",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { viewModel.triggerSmartReschedule() },
                        colors = ButtonDefaults.buttonColors(containerColor = WaqtiPrimary.copy(alpha = 0.15f), contentColor = WaqtiPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Strings.smartRescheduleBtn(language), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(timelineItems, key = { it.id }) { item ->
                TimelineCard(
                    item = item,
                    language = language,
                    onToggleComplete = {
                        if (item.rawTask != null) {
                            viewModel.toggleTaskCompletion(item.rawTask)
                        } else {
                            // Routine toggle
                            val routine = routines.find { "routine_${it.id}" == item.id }
                            routine?.let { viewModel.toggleRoutineCompletion(it) }
                        }
                    },
                    onStartFocus = {
                        if (item.rawTask != null) {
                            viewModel.startFocusSession(item.title, "POMODORO")
                            viewModel.setCurrentTab(3)
                        }
                    },
                    onBreakdown = {
                        item.rawTask?.let { viewModel.triggerTaskBreakdown(it) }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(60.dp)) // Padding for FAB
            }
        }
    }

    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text(Strings.addTask(language), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = {
                            newTaskTitle = it
                            addTaskError = null
                        },
                        label = { Text(if (language == AppLanguage.ARABIC) "عنوان المهمة" else "Task Title") },
                        placeholder = { Text(Strings.taskTitleHint(language)) },
                        isError = addTaskError != null,
                        supportingText = if (addTaskError != null) {
                            {
                                Text(
                                    text = addTaskError.orEmpty(),
                                    color = WaqtiDanger,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newTaskDuration,
                            onValueChange = { newTaskDuration = it },
                            label = { Text(if (language == AppLanguage.ARABIC) "المدة (د)" else "Duration (m)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (language == AppLanguage.ARABIC) "الأولوية" else "Priority", style = MaterialTheme.typography.labelSmall)
                            Row {
                                listOf("HIGH", "MEDIUM", "LOW").forEach { p ->
                                    val isSelected = newTaskPriority == p
                                    Text(
                                        text = p.take(1),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) WaqtiPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) WaqtiPrimary.copy(alpha = 0.15f) else Color.Transparent)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .clickable { newTaskPriority = p }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dur = newTaskDuration.toIntOrNull() ?: 45
                        val added = viewModel.addTask(newTaskTitle, newTaskPriority, dur, "WORK")
                        if (added) {
                            newTaskTitle = ""
                            addTaskError = null
                            showAddTaskDialog = false
                        } else {
                            addTaskError = if (language == AppLanguage.ARABIC)
                                "⚠️ هذه المهمة موجودة بالفعل في جدولك اليومي"
                            else
                                "⚠️ This task already exists in your schedule"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaqtiPrimary),
                    modifier = Modifier.testTag("submit_task_button")
                ) {
                    Text(if (language == AppLanguage.ARABIC) "إضافة" else "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text(if (language == AppLanguage.ARABIC) "إلغاء" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun TimelineCard(
    item: TimelineItem,
    language: AppLanguage,
    onToggleComplete: () -> Unit,
    onStartFocus: () -> Unit,
    onBreakdown: () -> Unit
) {
    val cardBg = when {
        item.isCompleted -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        item.isProtected -> WaqtiPrayerAccent.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isCompleted) 0.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Indicator Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(54.dp)
            ) {
                Text(
                    text = item.time,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isProtected) WaqtiPrayerAccent else WaqtiPrimary
                )
                Text(
                    text = "${item.durationMin} m",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .width(2.dp)
                    .height(38.dp)
                    .background(if (item.isProtected) WaqtiPrayerAccent.copy(alpha = 0.4f) else WaqtiPrimary.copy(alpha = 0.2f))
            )

            // Content Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.Bold,
                        color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (item.isProtected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = Strings.fixedTag(language),
                            tint = WaqtiPrayerAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (item.rawTask != null && !item.isCompleted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.breakdownWithAi(language),
                            style = MaterialTheme.typography.labelSmall,
                            color = WaqtiPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onBreakdown() }
                        )
                    }
                }
            }

            // Actions
            if (item.rawTask != null && !item.isCompleted) {
                IconButton(onClick = onStartFocus) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Focus", tint = WaqtiPrimary)
                }
            }

            IconButton(onClick = onToggleComplete) {
                Icon(
                    imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                    contentDescription = null,
                    tint = if (item.isCompleted) WaqtiSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
