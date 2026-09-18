package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.localization.AppLanguage
import com.example.ui.theme.WaqtiPrimary
import com.example.ui.theme.WaqtiSecondaryGreen

@Composable
fun WaqtiOfficialLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showBorder: Boolean = false
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.22f))
            .background(Color.White)
            .then(
                if (showBorder) {
                    Modifier.border(1.dp, WaqtiPrimary.copy(alpha = 0.15f), RoundedCornerShape(size * 0.22f))
                } else Modifier
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_waqti_logo),
            contentDescription = "WAQTI Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(size)
        )
    }
}

@Composable
fun WaqtiBrandHeader(
    modifier: Modifier = Modifier,
    language: AppLanguage = AppLanguage.ARABIC,
    logoSize: Dp = 80.dp,
    showSlogan: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(logoSize)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp), spotColor = WaqtiPrimary.copy(alpha = 0.2f))
                .border(1.dp, WaqtiPrimary.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                .padding(4.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_waqti_logo),
                contentDescription = "WAQTI Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(logoSize - 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "وَقـتِـي",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = WaqtiPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(WaqtiSecondaryGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "WAQTI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WaqtiSecondaryGreen
                )
            }
        }

        if (showSlogan) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (language == AppLanguage.ARABIC) "إدارة الوقت .. وتنظيم المهام اليومية" else "Time Management & Daily Tasks Organizer",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
