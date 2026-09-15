package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val sizePx = size.minDimension
        val stroke = sizePx * 0.22f
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (sizePx - stroke) / 2f
        val red = Color(0xFFEA4335)
        val yellow = Color(0xFFFBBC05)
        val green = Color(0xFF34A853)
        val blue = Color(0xFF4285F4)
        val arcRect = Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius)

        drawArc(
            color = red,
            startAngle = 180f,
            sweepAngle = 105f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            topLeft = arcRect.topLeft,
            size = arcRect.size
        )
        drawArc(
            color = yellow,
            startAngle = 120f,
            sweepAngle = 60f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            topLeft = arcRect.topLeft,
            size = arcRect.size
        )
        drawArc(
            color = green,
            startAngle = 0f,
            sweepAngle = 120f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            topLeft = arcRect.topLeft,
            size = arcRect.size
        )
        drawArc(
            color = blue,
            startAngle = 285f,
            sweepAngle = 75f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            topLeft = arcRect.topLeft,
            size = arcRect.size
        )
        drawLine(
            color = blue,
            start = Offset(center.x, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = stroke,
            cap = StrokeCap.Square
        )
    }
}

@Composable
fun FacebookLogoIcon(modifier: Modifier = Modifier.size(20.dp)) {
    val fbBlue = Color(0xFF1877F2)
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        drawCircle(color = fbBlue, radius = radius, center = center)
        val path = Path().apply {
            val w = size.width
            val h = size.height
            moveTo(w * 0.58f, h * 0.85f)
            lineTo(w * 0.58f, h * 0.55f)
            lineTo(w * 0.70f, h * 0.55f)
            lineTo(w * 0.72f, h * 0.42f)
            lineTo(w * 0.58f, h * 0.42f)
            lineTo(w * 0.58f, h * 0.34f)
            cubicTo(w * 0.58f, h * 0.26f, w * 0.62f, h * 0.22f, w * 0.72f, h * 0.22f)
            lineTo(w * 0.72f, h * 0.12f)
            cubicTo(w * 0.64f, h * 0.12f, w * 0.46f, h * 0.14f, w * 0.46f, h * 0.32f)
            lineTo(w * 0.46f, h * 0.42f)
            lineTo(w * 0.36f, h * 0.42f)
            lineTo(w * 0.36f, h * 0.55f)
            lineTo(w * 0.46f, h * 0.55f)
            lineTo(w * 0.46f, h * 0.85f)
            close()
        }
        drawPath(path = path, color = Color.White)
    }
}

@Composable
fun SocialLoginRow(
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedButton(
            onClick = onGoogleClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                GoogleLogoIcon(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Google",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A)
                )
            }
        }
        OutlinedButton(
            onClick = onFacebookClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                FacebookLogoIcon(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Facebook",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

@Composable
fun SocialCircleIconsRow(
    onFacebookClick: () -> Unit,
    onGoogleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Facebook Circular Button
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onFacebookClick() }
                .testTag("facebook_sign_in_button"),
            shape = CircleShape,
            color = Color(0xFF1877F2),
            shadowElevation = 2.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "f",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Google Circular Button
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onGoogleClick() }
                .testTag("google_sign_in_button"),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 2.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                GoogleLogoIcon(modifier = Modifier.size(22.dp))
            }
        }
    }
}
