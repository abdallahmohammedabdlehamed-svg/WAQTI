package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.components.WaqtiDialogs
import com.example.ui.components.WaqtiOfficialLogo
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.NotificationCenterScreen
import com.example.ui.screens.ProgramScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiTheme
import com.example.ui.theme.WaqtiWarning
import com.example.ui.viewmodel.WaqtiViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: WaqtiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val language by viewModel.language.collectAsStateWithLifecycle()
            val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                WaqtiTheme {
                    WaqtiApp(viewModel = viewModel)
                }
            }
        }
    }
}

data class NavItem(
    val titleKey: (AppLanguage) -> String,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaqtiApp(viewModel: WaqtiViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isUserAuthenticated by viewModel.isUserAuthenticated.collectAsStateWithLifecycle()
    val showAuthScreen by viewModel.showAuthScreen.collectAsStateWithLifecycle()
    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val authErrorMessage by viewModel.authErrorMessage.collectAsStateWithLifecycle()

    val showNotificationCenter by viewModel.showNotificationCenter.collectAsStateWithLifecycle()

    if (!isUserAuthenticated || showAuthScreen) {
        AuthScreen(
            language = language,
            isLoading = isAuthLoading,
            errorMessage = authErrorMessage,
            onLogin = { email, pass -> viewModel.login(email, pass) {} },
            onRegister = { name, email, pass -> viewModel.register(name, email, pass) {} },
            onContinueAsGuest = { viewModel.continueAsGuest() },
            onDismiss = if (isUserAuthenticated) { { viewModel.setShowAuthScreen(false) } } else null
        )
        return
    }

    if (showNotificationCenter) {
        NotificationCenterScreen(
            viewModel = viewModel,
            onBack = { viewModel.setShowNotificationCenter(false) }
        )
        return
    }

    val navItems = listOf(
        NavItem({ Strings.tabHome(it) }, Icons.Default.Home, "nav_home"),
        NavItem({ Strings.tabToday(it) }, Icons.Default.CalendarToday, "nav_today"),
        NavItem({ Strings.tabProgram(it) }, Icons.Default.Spa, "nav_program"),
        NavItem({ Strings.tabFocus(it) }, Icons.Default.Timer, "nav_focus"),
        NavItem({ Strings.tabTasks(it) }, Icons.Default.CheckCircle, "nav_tasks"),
        NavItem({ Strings.tabMore(it) }, Icons.Default.MoreHoriz, "nav_more")
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 700.dp

        if (isExpanded) {
            // Tablet / Desktop Canonical Layout with NavigationRail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = WaqtiPrimary,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            WaqtiOfficialLogo(size = 48.dp, showBorder = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(Strings.appTitle(language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                ) {
                    navItems.forEachIndexed { index, item ->
                        NavigationRailItem(
                            selected = currentTab == index,
                            onClick = { viewModel.setCurrentTab(index) },
                            icon = { Icon(item.icon, contentDescription = item.titleKey(language)) },
                            label = { Text(item.titleKey(language), fontSize = 11.sp) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }

                Scaffold(
                    topBar = {
                        WaqtiTopBar(viewModel = viewModel, language = language)
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ScreenContent(currentTab = currentTab, viewModel = viewModel)
                    }
                }
            }
        } else {
            // Handheld Mobile Phone Layout with Bottom NavigationBar
            Scaffold(
                topBar = {
                    WaqtiTopBar(viewModel = viewModel, language = language)
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .testTag("bottom_navigation_bar")
                            .navigationBarsPadding()
                    ) {
                        navItems.forEachIndexed { index, item ->
                            val isSelected = currentTab == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.setCurrentTab(index) },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.titleKey(language),
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.titleKey(language),
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    ScreenContent(currentTab = currentTab, viewModel = viewModel)
                }
            }
        }
    }

    // Active Dialogs & Modals overlay
    WaqtiDialogs(viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaqtiTopBar(viewModel: WaqtiViewModel, language: AppLanguage) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WaqtiOfficialLogo(
                    size = 38.dp,
                    showBorder = true
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Strings.appTitle(language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " | WAQTI",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = Strings.appSlogan(language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                }
            }
        },
        actions = {
            // Quick "التأخيرات" (Delays) button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, WaqtiWarning.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .background(WaqtiWarning.copy(alpha = 0.12f))
                    .clickable { viewModel.triggerImBehind() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("top_bar_im_behind")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = Strings.imBehindBtn(language),
                        tint = WaqtiWarning,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Strings.imBehindBtn(language),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = WaqtiWarning
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // User Profile Avatar with Initials and Active indicator
            val userName = currentUser?.name?.takeIf { it.isNotBlank() } ?: if (language == AppLanguage.ARABIC) "عبدالله" else "Abdallah"
            val initial = userName.firstOrNull()?.toString() ?: "U"
            IconButton(
                onClick = { viewModel.setShowAuthScreen(true) },
                modifier = Modifier.testTag("user_profile_button")
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.WaqtiSuccess)
                            .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape)
                    )
                }
            }

            // Notifications Center Bell Icon
            val isFatigueAlert by viewModel.isFatigueAlert.collectAsStateWithLifecycle()
            IconButton(
                onClick = { viewModel.setShowNotificationCenter(true) },
                modifier = Modifier.testTag("topbar_notifications_button")
            ) {
                BadgedBox(
                    badge = {
                        if (isFatigueAlert) {
                            Badge(containerColor = WaqtiWarning)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = if (language == AppLanguage.ARABIC) "إشعارات وقتي" else "Notifications",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Language Switcher Toggle
            IconButton(
                onClick = {
                    viewModel.setLanguage(if (language == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC)
                },
                modifier = Modifier.testTag("language_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Switch Language",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Composable
fun ScreenContent(currentTab: Int, viewModel: WaqtiViewModel) {
    Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
        when (tab) {
            0 -> HomeScreen(viewModel = viewModel)
            1 -> TodayScreen(viewModel = viewModel)
            2 -> ProgramScreen(viewModel = viewModel)
            3 -> FocusScreen(viewModel = viewModel)
            4 -> TasksScreen(viewModel = viewModel)
            5 -> MoreScreen(viewModel = viewModel)
            else -> HomeScreen(viewModel = viewModel)
        }
    }
}
