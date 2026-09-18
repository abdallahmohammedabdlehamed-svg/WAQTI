package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppLanguage
import com.example.ui.components.WaqtiBrandHeader
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiSecondaryGreen

@Composable
fun AuthScreen(
    language: AppLanguage,
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (email: String, pass: String) -> Unit,
    onRegister: (name: String, email: String, pass: String) -> Unit,
    onContinueAsGuest: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    val isAr = language == AppLanguage.ARABIC
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register

    var loginEmail by remember { mutableStateOf("abdallahmohammedabdlehamed@gmail.com") }
    var loginPassword by remember { mutableStateOf("123456") }

    var registerName by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerConfirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button if dismissible
                if (onDismiss != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Brand Header with official logo
                WaqtiBrandHeader(
                    language = language,
                    logoSize = 88.dp,
                    showSlogan = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tabs: Login / Register
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = WaqtiPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .padding(3.dp),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .clip(RoundedCornerShape(12.dp)),
                            color = WaqtiPrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { 
                            selectedTab = 0 
                            localError = null
                        },
                        text = {
                            Text(
                                text = if (isAr) "تسجيل الدخول" else "Sign In",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { 
                            selectedTab = 1 
                            localError = null
                        },
                        text = {
                            Text(
                                text = if (isAr) "حساب جديد" else "Register",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Error Message banner
                val activeError = localError ?: errorMessage
                AnimatedVisibility(visible = !activeError.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = activeError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Card Container for fields
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (selectedTab == 1) {
                            // Full Name field for registration
                            OutlinedTextField(
                                value = registerName,
                                onValueChange = { 
                                    registerName = it
                                    localError = null
                                },
                                label = { Text(if (isAr) "الاسم الكامل" else "Full Name") },
                                placeholder = { Text(if (isAr) "مثال: أحمد علي" else "e.g. Ahmed Ali") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = WaqtiPrimary
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WaqtiPrimary,
                                    focusedLabelColor = WaqtiPrimary
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Email Field
                        OutlinedTextField(
                            value = if (selectedTab == 0) loginEmail else registerEmail,
                            onValueChange = { 
                                if (selectedTab == 0) loginEmail = it else registerEmail = it
                                localError = null
                            },
                            label = { Text(if (isAr) "البريد الإلكتروني" else "Email Address") },
                            placeholder = { Text(if (isAr) "name@example.com" else "name@example.com") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = WaqtiPrimary
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WaqtiPrimary,
                                focusedLabelColor = WaqtiPrimary
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password Field
                        OutlinedTextField(
                            value = if (selectedTab == 0) loginPassword else registerPassword,
                            onValueChange = { 
                                if (selectedTab == 0) loginPassword = it else registerPassword = it
                                localError = null
                            },
                            label = { Text(if (isAr) "كلمة المرور" else "Password") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = WaqtiPrimary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WaqtiPrimary,
                                focusedLabelColor = WaqtiPrimary
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (selectedTab == 1) ImeAction.Next else ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (selectedTab == 0) onLogin(loginEmail, loginPassword)
                                }
                            )
                        )

                        if (selectedTab == 1) {
                            Spacer(modifier = Modifier.height(14.dp))
                            // Confirm Password
                            OutlinedTextField(
                                value = registerConfirmPassword,
                                onValueChange = { 
                                    registerConfirmPassword = it 
                                    localError = null
                                },
                                label = { Text(if (isAr) "تأكيد كلمة المرور" else "Confirm Password") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = WaqtiPrimary
                                    )
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_password_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WaqtiPrimary,
                                    focusedLabelColor = WaqtiPrimary
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (registerPassword != registerConfirmPassword) {
                                            localError = if (isAr) "كلمتا المرور غير متطابقتين" else "Passwords do not match"
                                        } else {
                                            onRegister(registerName, registerEmail, registerPassword)
                                        }
                                    }
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (selectedTab == 0) {
                                    onLogin(loginEmail, loginPassword)
                                } else {
                                    if (registerName.isBlank()) {
                                        localError = if (isAr) "يرجى كتابة الاسم" else "Please enter your name"
                                        return@Button
                                    }
                                    if (registerEmail.isBlank()) {
                                        localError = if (isAr) "يرجى كتابة البريد الإلكتروني" else "Please enter your email"
                                        return@Button
                                    }
                                    if (registerPassword.length < 6) {
                                        localError = if (isAr) "كلمة المرور يجب ألا تقل عن 6 أحرف" else "Password must be at least 6 characters"
                                        return@Button
                                    }
                                    if (registerPassword != registerConfirmPassword) {
                                        localError = if (isAr) "كلمتا المرور غير متطابقتين" else "Passwords do not match"
                                        return@Button
                                    }
                                    localError = null
                                    onRegister(registerName, registerEmail, registerPassword)
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WaqtiPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auth_submit_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(
                                    text = if (selectedTab == 0) {
                                        if (isAr) "تسجيل الدخول إلى وقتي" else "Sign In to Waqti"
                                    } else {
                                        if (isAr) "إنشاء حساب جديد" else "Create Waqti Account"
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Guest mode button
                        TextButton(
                            onClick = onContinueAsGuest,
                            modifier = Modifier.testTag("guest_mode_button")
                        ) {
                            Text(
                                text = if (isAr) "المتابعة كضيف بدون تسجيل" else "Continue as Guest",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Feature Highlights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FeatureBadge(
                        title = if (isAr) "ذكاء ChatGPT" else "ChatGPT AI",
                        desc = if (isAr) "ردود احترافية مجانية" else "Free & Unlimited"
                    )
                    FeatureBadge(
                        title = if (isAr) "مزامنة سحابية" else "Cloud Sync",
                        desc = if (isAr) "حفظ الجداول والمهام" else "Safe Persistence"
                    )
                    FeatureBadge(
                        title = if (isAr) "خصوصية تامة" else "100% Private",
                        desc = if (isAr) "حماية كاملة للبيانات" else "Encrypted & Secure"
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureBadge(title: String, desc: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(WaqtiSecondaryGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = WaqtiSecondaryGreen,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontSize = 10.sp
        )
    }
}
