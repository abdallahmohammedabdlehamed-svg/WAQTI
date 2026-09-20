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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.components.WaqtiOfficialLogo
import com.example.ui.theme.WaqtiDanger
import com.example.ui.theme.WaqtiPrayerAccent
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiSecondaryGreen
import com.example.ui.theme.WaqtiSuccess
import com.example.ui.theme.WaqtiWarning
import com.example.ui.viewmodel.WaqtiViewModel

@Composable
fun MoreScreen(
    viewModel: WaqtiViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val userPlan by viewModel.userPlan.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var activeSubSection by remember { mutableStateOf(0) } // 0: AI Assistant, 1: Analytics, 2: Pricing & Pro, 3: Account & Admin
    var chatInput by remember { mutableStateOf("") }

    val quickPrompts = if (language == AppLanguage.ARABIC) listOf(
        "أعد تنظيم يومي 🔄",
        "لخص مهامي اليوم 📋",
        "اقترح ورد قرآني 📖",
        "جدولة مهمة عاجلة ⚡",
        "ماذا أفعل الآن؟ 🤔",
        "خفف جدول اليوم 🌿",
        "أسبوعك مع وقتي 📊"
    ) else listOf(
        "Reschedule my day 🔄",
        "Summarize today's tasks 📋",
        "Suggest Quran routine 📖",
        "Schedule urgent task ⚡",
        "What should I do now? 🤔",
        "Lighten my schedule 🌿",
        "Weekly review 📊"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("more_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Strings.tabMore(language),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userPlan,
                        style = MaterialTheme.typography.labelSmall,
                        color = WaqtiPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Quick Language Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(WaqtiPrimary.copy(alpha = 0.12f))
                        .clickable {
                            viewModel.setLanguage(if (language == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC)
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = WaqtiPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.ARABIC) "English" else "العربية",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiPrimary
                        )
                    }
                }
            }
        }

        // WAQTI Smart Notifications Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, WaqtiPrimary.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setShowNotificationCenter(true) }
                    .testTag("more_notifications_center_button")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(WaqtiPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = WaqtiPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "إشعارات وقتي الذكية" else "WAQTI Smart Notifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (language == AppLanguage.ARABIC) "تخصيص الأذان، الأذكار، المهام، الهدوء، ومنع الإرهاق"
                            else "Customize Azan, Azkar, Tasks, Quiet Hours & Spacing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = if (language == AppLanguage.ARABIC) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Subtabs
        item {
            TabRow(
                selectedTabIndex = activeSubSection,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = WaqtiPrimary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = activeSubSection == 0,
                    onClick = { activeSubSection = 0 },
                    text = { Text(if (language == AppLanguage.ARABIC) "مساعد وقتي" else "AI Chat") }
                )
                Tab(
                    selected = activeSubSection == 1,
                    onClick = { activeSubSection = 1 },
                    text = { Text(if (language == AppLanguage.ARABIC) "التحليلات" else "Analytics") }
                )
                Tab(
                    selected = activeSubSection == 2,
                    onClick = { activeSubSection = 2 },
                    text = { Text(if (language == AppLanguage.ARABIC) "الترقية" else "Pro") }
                )
                Tab(
                    selected = activeSubSection == 3,
                    onClick = { activeSubSection = 3 },
                    text = { Text(if (language == AppLanguage.ARABIC) "المسؤول" else "Admin") }
                )
            }
        }

        if (activeSubSection == 0) {
            // AI ASSISTANT CHAT
            item {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(WaqtiPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WaqtiPrimary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "مساعد وقتي الذكي (ChatGPT)" else "Waqti AI (ChatGPT)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "مجاني وغير محدود • ردود فورية واحترافية" else "Free & Unlimited • Smart Responses",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WaqtiSecondaryGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Clear conversation button
                            IconButton(
                                onClick = { viewModel.clearAiChat() },
                                modifier = Modifier.testTag("clear_ai_chat_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear Chat",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick prompt chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(quickPrompts) { prompt ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(WaqtiPrimary.copy(alpha = 0.1f))
                                        .clickable { viewModel.sendAiMessage(prompt) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = WaqtiPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Chat Messages
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            aiMessages.forEach { msg ->
                                val isUser = msg.sender == "USER"
                                val text = if (language == AppLanguage.ARABIC) msg.textAr else msg.textEn
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isUser) WaqtiPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(12.dp)
                                            .fillMaxWidth(0.85f)
                                    ) {
                                        Column {
                                            Text(
                                                text = text,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (msg.suggestionAction == "APPLY_RESCHEDULE") {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(
                                                    onClick = { viewModel.triggerSmartReschedule() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = WaqtiSuccess),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(34.dp)
                                                ) {
                                                    Text(Strings.apply(language), fontSize = 12.sp)
                                                }
                                            } else if (msg.suggestionAction == "IM_BEHIND") {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(
                                                    onClick = { viewModel.triggerImBehind() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = WaqtiWarning),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(34.dp)
                                                ) {
                                                    Text(Strings.apply(language), fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Thinking indicator
                            if (isAiThinking) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp),
                                        color = WaqtiPrimary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "جاري استدعاء ChatGPT وصياغة الرد الذكي..." else "ChatGPT is generating a thoughtful response...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WaqtiPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Input Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = {
                                    Text(
                                        if (language == AppLanguage.ARABIC) "اسأل ChatGPT لتنظيم وقتك أو حل مشكلة..." else "Ask ChatGPT anything about your day..."
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (chatInput.isNotBlank() && !isAiThinking) {
                                        viewModel.sendAiMessage(chatInput)
                                        chatInput = ""
                                    }
                                },
                                enabled = !isAiThinking
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = if (isAiThinking) Color.Gray else WaqtiPrimary)
                            }
                        }
                    }
                }
            }
        } else if (activeSubSection == 1) {
            // ANALYTICS & WEEKLY REVIEW
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Strings.weeklyReview(language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("84%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = WaqtiPrimary)
                                Text(if (language == AppLanguage.ARABIC) "إتمام المهام" else "Task Completion", style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("14.5h", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = WaqtiSuccess)
                                Text(if (language == AppLanguage.ARABIC) "ساعات التركيز" else "Focus Hours", style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("96%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = WaqtiPrayerAccent)
                                Text(if (language == AppLanguage.ARABIC) "الصلاة في وقتها" else "Prayer Consistency", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (language == AppLanguage.ARABIC)
                                "💡 ذروة طاقتك وإنتاجيتك هذا الأسبوع كانت بين الساعة 09:30 و 11:30 صباحًا. حافظ على حجز هذه الفترة لمهامك الأكثر أهمية."
                            else
                                "💡 Your peak focus window this week was between 9:30 AM and 11:30 AM. Protect this slot for your highest-impact goals.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else if (activeSubSection == 2) {
            // PRICING & SUBSCRIPTION PRO
            item {
                var isYearlyBilling by remember { mutableStateOf(true) }
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WaqtiPrimary.copy(alpha = 0.08f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(WaqtiPrimary.copy(alpha = 0.4f))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("WAQTI PRO", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = WaqtiPrimary)
                                Text(
                                    text = if (isYearlyBilling)
                                        (if (language == AppLanguage.ARABIC) "$3.33 / شهر ($39.99 سنوياً)" else "$3.33 / mo ($39.99/yr)")
                                    else
                                        (if (language == AppLanguage.ARABIC) "$4.99 / شهرياً" else "$4.99 / month"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(Icons.Default.Star, contentDescription = null, tint = WaqtiWarning, modifier = Modifier.size(32.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Billing Cycle Selector Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (!isYearlyBilling) WaqtiPrimary else Color.Transparent)
                                    .clickable { isYearlyBilling = false }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "شهري ($4.99)" else "Monthly ($4.99)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (!isYearlyBilling) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isYearlyBilling) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isYearlyBilling) WaqtiPrimary else Color.Transparent)
                                    .clickable { isYearlyBilling = true }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "سنوي ($39.99)" else "Yearly ($39.99)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isYearlyBilling) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isYearlyBilling) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(WaqtiSuccess)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("خصم 33%", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        val proFeatures = listOf(
                            "إعادة الجدولة الذكية التلقائية (Unlimited Smart Reschedule)",
                            "تقسيم المهام المعقدة بالذكاء الاصطناعي (AI Task Breakdown)",
                            "جدول الصلاة والورد المتكامل وحماية أوقات العبادة",
                            "أصوات التركيز المحيطية غير المحدودة والتحليلات العميقة",
                            "المزامنة الآمنة والمشفرة والنسخ الاحتياطي"
                        )
                        proFeatures.forEach { feat ->
                            Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = WaqtiSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(feat, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.upgradeToPro() },
                            colors = ButtonDefaults.buttonColors(containerColor = WaqtiPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (language == AppLanguage.ARABIC) "تفعيل اشتراك وقتي برو (تجربة مجانية)" else "Start 7-Day Free Pro Trial")
                        }
                    }
                }
            }
        } else {
            // ACCOUNT & ADMIN DASHBOARD
            item {
                // User Profile & Authentication Management Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(WaqtiPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = WaqtiPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.name ?: (if (language == AppLanguage.ARABIC) "عبدالله محمد" else "Abdallah Mohammed"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentUser?.email ?: "abdallahmohammedabdlehamed@gmail.com",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(WaqtiSecondaryGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = currentUser?.plan ?: "PRO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WaqtiSecondaryGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.setShowAuthScreen(true) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "تبديل الحساب" else "Switch Account",
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = { viewModel.logout() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WaqtiDanger.copy(alpha = 0.12f),
                                    contentColor = WaqtiDanger
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = WaqtiDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "تسجيل الخروج" else "Sign Out",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Brand & App Identity Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WaqtiOfficialLogo(size = 56.dp, showBorder = true)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (language == AppLanguage.ARABIC) "وَقـتِـي | WAQTI" else "WAQTI | وقتي",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = WaqtiPrimary
                        )
                        Text(
                            text = Strings.appSlogan(language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == AppLanguage.ARABIC) "مدعوم بـ ChatGPT المجاني • إصدار 2.4.0" else "Powered by Free ChatGPT • v2.4.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = WaqtiSecondaryGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = WaqtiPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Strings.adminDashboard(language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        val metrics = listOf(
                            "Total Active Users" to "28,420",
                            "Monthly Recurring Revenue (MRR)" to "$14,210",
                            "AI Tokens Consumed (Today)" to "1.42M",
                            "System Latency" to "42ms (Healthy)",
                            "Room Database Size" to "1.8 MB (Local SQLite)"
                        )

                        metrics.forEach { (label, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WaqtiPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
