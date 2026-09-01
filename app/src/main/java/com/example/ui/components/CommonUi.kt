package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun formatArabicCurrency(amount: Double, currency: String = "ر.س"): String {
    val formatter = NumberFormat.getNumberInstance(Locale("ar"))
    formatter.maximumFractionDigits = 2
    formatter.minimumFractionDigits = 0
    return "${formatter.format(amount)} $currency"
}

fun formatArabicDate(timestamp: Long, pattern: String = "yyyy/MM/dd"): String {
    val sdf = SimpleDateFormat(pattern, Locale("ar"))
    return sdf.format(Date(timestamp))
}

fun formatRelativeArabicDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diffDays = ((now - timestamp) / (24 * 60 * 60 * 1000)).toInt()
    return when {
        diffDays == 0 -> "اليوم"
        diffDays == 1 -> "أمس"
        diffDays == 2 -> "منذ يومين"
        diffDays in 3..10 -> "منذ $diffDays أيام"
        diffDays < 0 && -diffDays == 1 -> "غداً"
        diffDays < 0 && -diffDays == 2 -> "بعد يومين"
        diffDays < 0 && -diffDays in 3..10 -> "بعد ${-diffDays} أيام"
        else -> formatArabicDate(timestamp)
    }
}

@Composable
fun StatusBadge(
    isPaidInFull: Boolean,
    isOverdue: Boolean,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text, icon) = when {
        isPaidInFull -> Quadruple(StatusPaidGreenLight, StatusPaidGreenDark, "خالص / مسدد", Icons.Default.CheckCircle)
        isOverdue -> Quadruple(StatusDebtRedLight, StatusDebtRedDark, "متأخر في السداد", Icons.Default.Warning)
        else -> Quadruple(Color(0xFFFEF3C7), Color(0xFF92400E), "مستحق السداد", Icons.Default.Schedule)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 11.sp
                )
            )
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun CustomerAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 46
) {
    val initial = name.trim().takeIf { it.isNotEmpty() }?.first()?.toString() ?: "ع"
    val avatarColors = listOf(
        Color(0xFF0D5C75),
        Color(0xFF1E3A8A),
        Color(0xFF047857),
        Color(0xFFB45309),
        Color(0xFF6B21A8),
        Color(0xFF0369A1)
    )
    val colorIndex = (name.hashCode().let { if (it < 0) -it else it }) % avatarColors.size
    val bg = avatarColors[colorIndex]

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(bg.copy(alpha = 0.15f))
            .border(1.5.dp, bg.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = bg,
                fontSize = (size * 0.42f).sp
            )
        )
    }
}
