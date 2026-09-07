package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GenziiBlue

@Composable
fun GenziiEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    containerColor: Color = GenziiBlue,
    iconTint: Color = Color.White
) {
    val cornerRadius = size * 0.28f
    val innerSquareSize = size * 0.44f

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        // Professional Polish rotated diamond emblem
        Box(
            modifier = Modifier
                .size(innerSquareSize)
                .rotate(45f)
                .border(width = (size.value * 0.06f).coerceAtLeast(1.5f).dp, color = iconTint, shape = RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun GenziiBrandHeader(
    modifier: Modifier = Modifier,
    emblemSize: Dp = 32.dp,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    showTagline: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GenziiEmblem(size = emblemSize)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Genzii",
            fontSize = (emblemSize.value * 0.65f).sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = (-0.5).sp
        )
    }
}

