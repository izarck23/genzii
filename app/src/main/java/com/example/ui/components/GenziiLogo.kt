package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GenziiBadge(
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    bgColor: Color = Color(0xFF005AC1),
    letterColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "g",
            color = letterColor,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.58f).sp,
            lineHeight = (size.value * 0.58f).sp
        )
    }
}

@Composable
fun GenziiLogoHeader(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    isDark: Boolean = false
) {
    val badgeBg = if (isDark) Color.White else Color(0xFF005AC1)
    val badgeText = if (isDark) Color(0xFF005AC1) else Color.White
    val labelColor = if (isDark) Color.White else Color(0xFF0F172A)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GenziiBadge(
            size = size,
            bgColor = badgeBg,
            letterColor = badgeText
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "genzii",
            fontSize = (size.value * 0.75f).sp,
            fontWeight = FontWeight.Bold,
            color = labelColor,
            letterSpacing = (-0.5).sp
        )
    }
}
