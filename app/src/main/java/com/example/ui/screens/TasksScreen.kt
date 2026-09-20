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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import com.example.domain.ai.WaqtiAiEngine
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.theme.WaqtiDanger
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiSuccess
import com.example.ui.theme.WaqtiWarning
import com.example.ui.viewmodel.WaqtiViewModel

@Composable
fun TasksScreen(
    viewModel: WaqtiViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0: All Tasks, 1: Projects & Goals, 2: Inbox
    var quickThought by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf("ALL") }
    var duplicateError by remember { mutableStateOf<String?>(null) }

    // Guarantee uniqueness of tasks by normalized title (case-insensitive & trimmed)
    val uniqueTasks = remember(tasks) {
        tasks.groupBy { it.title.trim().lowercase() }
            .map { (_, group) ->
                group.maxWithOrNull(
                    compareBy<TaskEntity> { task ->
                        when (task.status) {
                            "IN_PROGRESS" -> 3
                            "PLANNED" -> 2
                            "COMPLETED" -> 1
                            else -> 0
                        }
                    }.thenBy { it.id }
                ) ?: group.first()
            }
    }

    val filteredTasks = uniqueTasks.filter {
        if (categoryFilter == "ALL") true else it.category == categoryFilter
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("tasks_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = Strings.tabTasks(language),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (language == AppLanguage.ARABIC) "إدارة المهام، المشاريع، وصندوق الوارد السريع" else "Task backlog, project hierarchy, and quick capture inbox",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Subtabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = WaqtiPrimary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(if (language == AppLanguage.ARABIC) "قائمة المهام" else "Tasks") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(if (language == AppLanguage.ARABIC) "المشاريع والأهداف" else "Projects & Goals") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(if (language == AppLanguage.ARABIC) "صندوق الوارد" else "Inbox") }
                )
            }
        }

        if (selectedTab == 0) {
            // Category Filter Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "الكل", "WORK" to "عمل", "STUDY" to "دراسة", "PERSONAL" to "شخصي", "HEALTH" to "صحة").forEach { (cat, labelAr) ->
                        val isSelected = categoryFilter == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) WaqtiPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { categoryFilter = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.ARABIC) labelAr else cat,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Tasks List
            items(filteredTasks, key = { it.id }) { task ->
                val isCompleted = task.status == "COMPLETED"
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.toggleTaskCompletion(task) }) {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = if (isCompleted) WaqtiSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Bold,
                                    color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${task.startTime} • ${task.durationMinutes} min • ${task.projectName.ifBlank { task.category }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (task.priority == "HIGH") WaqtiDanger.copy(alpha = 0.12f) else WaqtiWarning.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = task.priority,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (task.priority == "HIGH") WaqtiDanger else WaqtiWarning
                                )
                            }

                            IconButton(onClick = { viewModel.deleteTask(task) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                            }
                        }

                        // Subtasks display if available
                        if (task.subtasksRaw.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "الخطوات الفرعية (مولدة بالذكاء الاصطناعي):" else "Subtasks (AI Breakdown):",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = WaqtiPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    task.subtasksRaw.lines().forEach { step ->
                                        if (step.isNotBlank()) {
                                            Text(
                                                text = "• $step",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (!isCompleted) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "✨ ${Strings.breakdownWithAi(language)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WaqtiPrimary,
                                    modifier = Modifier.clickable { viewModel.triggerTaskBreakdown(task) }
                                )
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // PROJECTS & GOALS
            val projectList = listOf(
                Triple("Personal Portfolio", 70, "Career Growth"),
                Triple("JS Mastery", 50, "Pro Development"),
                Triple("WAQTI Platform", 85, "Product Launch"),
                Triple("Fitness Transformation", 40, "Health 2026")
            )

            items(projectList) { (title, progress, goal) ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = WaqtiPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "$progress%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = WaqtiPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${if (language == AppLanguage.ARABIC) "الهدف المرتبط" else "Linked Goal"}: $goal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = WaqtiPrimary,
                            trackColor = WaqtiPrimary.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        } else {
            // INBOX QUICK CAPTURE
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Strings.inboxQuickCapture(language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == AppLanguage.ARABIC) "اكتب أي فكرة أو مهمة وسيقوم الذكاء الاصطناعي بتصنيفها وجدولتها تلقائيًا" else "Capture thoughts and ideas; AI will automatically classify and schedule them",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = quickThought,
                            onValueChange = {
                                quickThought = it
                                duplicateError = null
                            },
                            placeholder = { Text(if (language == AppLanguage.ARABIC) "مثال: مراجعة عقود السيرفر مع مزود الخدمة يوم الأحد" else "e.g. Renew cloud servers on Sunday") },
                            isError = duplicateError != null,
                            supportingText = if (duplicateError != null) {
                                {
                                    Text(
                                        text = duplicateError.orEmpty(),
                                        color = WaqtiDanger,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val trimmed = quickThought.trim()
                                if (trimmed.isNotBlank()) {
                                    val classified = WaqtiAiEngine.classifyInboxThought(trimmed)
                                    val success = viewModel.addTask(classified.title, classified.priority, classified.durationMinutes, classified.category)
                                    if (success) {
                                        quickThought = ""
                                        duplicateError = null
                                        selectedTab = 0
                                    } else {
                                        duplicateError = if (language == AppLanguage.ARABIC)
                                            "⚠️ هذه المهمة موجودة بالفعل في قائمتك (تم منع التكرار)"
                                        else
                                            "⚠️ This task already exists in your tasks list (duplicate prevented)"
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WaqtiPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Strings.organizeWithAi(language))
                        }
                    }
                }
            }
        }
    }
}
