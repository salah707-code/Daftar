package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AlertEntity
import com.example.ui.components.DaftarBrandLogo
import com.example.ui.components.formatArabicCurrency
import com.example.ui.components.formatArabicDate
import com.example.ui.theme.StatusDebtRed
import com.example.ui.theme.StatusDebtRedLight
import com.example.ui.theme.StatusWarningOrange
import com.example.ui.theme.StatusWarningOrangeLight
import com.example.ui.viewmodel.DaftarViewModel

enum class AlertFilter(val title: String) {
    ALL("الكل"),
    UNREAD("غير مقروءة"),
    OVERDUE("المتأخرات فقط")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: DaftarViewModel,
    onNavigateToCustomer: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val alerts by viewModel.allAlerts.collectAsState(initial = emptyList())
    val unreadCount by viewModel.unreadAlertsCount.collectAsState(initial = 0)
    val settings by viewModel.settings.collectAsState()

    var selectedFilter by remember { mutableStateOf(AlertFilter.ALL) }

    val filteredAlerts = remember(alerts, selectedFilter) {
        when (selectedFilter) {
            AlertFilter.ALL -> alerts
            AlertFilter.UNREAD -> alerts.filter { !it.isRead }
            AlertFilter.OVERDUE -> alerts.filter { it.isOverdue }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DaftarBrandLogo(size = 36.dp, showText = false)
                        Column {
                            Text(
                                text = "التنبيهات والاستحقاقات",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (unreadCount > 0) "$unreadCount تنبيه غير مقروء" else "جميع التنبيهات مقروءة",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (unreadCount > 0) StatusDebtRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    if (alerts.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                viewModel.markAllAlertsAsRead()
                                Toast.makeText(context, "تم تعيين جميع التنبيهات كمقروءة", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("تحديد الكل كمقروء", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Filter tabs
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AlertFilter.values()) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.title, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            }
        }
    ) { innerPadding ->
        if (filteredAlerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = "لا توجد تنبيهات حالياً",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "سوف تظهر هنا إشعارات بالعملاء المتأخرين في السداد والديون القريبة تلقائياً.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAlerts, key = { it.id }) { alert ->
                    AlertItemCard(
                        alert = alert,
                        currency = settings.currencySymbol,
                        onCardClick = {
                            if (!alert.isRead) {
                                viewModel.markAlertAsRead(alert.id)
                            }
                            onNavigateToCustomer(alert.customerId)
                        },
                        onMarkRead = { viewModel.markAlertAsRead(alert.id) },
                        onDelete = { viewModel.deleteAlert(alert.id) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun AlertItemCard(
    alert: AlertEntity,
    currency: String,
    onCardClick: () -> Unit,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit
) {
    val isOverdue = alert.isOverdue
    val containerBg = if (alert.isRead) MaterialTheme.colorScheme.surface
                      else if (isOverdue) StatusDebtRedLight else StatusWarningOrangeLight

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (alert.isRead) 1.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isOverdue) StatusDebtRed.copy(alpha = 0.15f) else StatusWarningOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isOverdue) Icons.Default.Warning else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (isOverdue) StatusDebtRed else StatusWarningOrange,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isOverdue) StatusDebtRed else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (!alert.isRead) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "جديد",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المبلغ: ${formatArabicCurrency(alert.amount, currency)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isOverdue) StatusDebtRed else MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "موعد الاستحقاق: ${formatArabicDate(alert.dueDate)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!alert.isRead) {
                        TextButton(onClick = onMarkRead) {
                            Text("تعيين كمقروء", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف التنبيه",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
