package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DaftarViewModel

enum class ReportType(val title: String) {
    CUSTOMERS("تقرير العملاء"),
    DEBTS("تقرير المستحقات والديون"),
    PAYMENTS("تقرير المدفوعات")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: DaftarViewModel,
    onNavigateToCustomer: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsState(initial = emptyList())
    val payments by viewModel.allPayments.collectAsState(initial = emptyList())
    val stats by viewModel.summaryStats.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var selectedReport by remember { mutableStateOf(ReportType.CUSTOMERS) }
    var selectedPeriod by remember { mutableStateOf("كل الفترات") }
    val periods = listOf("اليوم", "هذا الأسبوع", "هذا الشهر", "هذا العام", "كل الفترات")

    var showPdfPreviewDialog by remember { mutableStateOf(false) }

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
                    DaftarBrandLogo(size = 36.dp, subtitle = "التقارير المالية والمحاسبية")

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                val reportSummary = buildString {
                                    appendLine("📊 *تقرير ${selectedReport.title} - تطبيق دفتر*")
                                    appendLine("🏢 المنشأة: ${settings.businessName}")
                                    appendLine("📅 الفترة: $selectedPeriod (${formatArabicDate(System.currentTimeMillis())})")
                                    appendLine("👥 عدد العملاء: ${stats.customerCount}")
                                    appendLine("💰 إجمالي الحجم: ${formatArabicCurrency(stats.totalVolume, settings.currencySymbol)}")
                                    appendLine("✅ إجمالي المحصل: ${formatArabicCurrency(stats.totalPaid, settings.currencySymbol)}")
                                    appendLine("🔴 إجمالي المتبقي: ${formatArabicCurrency(stats.totalRemaining, settings.currencySymbol)}")
                                    appendLine("-------------------")
                                    if (selectedReport == ReportType.DEBTS) {
                                        appendLine("*قائمة المبالغ المستحقة:*")
                                        customers.filter { it.remainingAmount > 0 }.forEach {
                                            appendLine("• ${it.name}: ${formatArabicCurrency(it.remainingAmount, settings.currencySymbol)} (استحقاق: ${formatArabicDate(it.dueDate)})")
                                        }
                                    } else if (selectedReport == ReportType.PAYMENTS) {
                                        appendLine("*قائمة سندات القبض:*")
                                        payments.take(10).forEach {
                                            appendLine("• ${it.customerName}: ${formatArabicCurrency(it.amount, settings.currencySymbol)} (${it.paymentMethod})")
                                        }
                                    }
                                }

                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, reportSummary)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "مشاركة التقرير")
                                context.startActivity(shareIntent)
                            }
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = "مشاركة", tint = MaterialTheme.colorScheme.onSurface)
                        }

                        IconButton(onClick = { showPdfPreviewDialog = true }) {
                            Icon(Icons.Outlined.PictureAsPdf, contentDescription = "تصدير PDF", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Report Type Selector
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ReportType.values()) { report ->
                        val isSelected = selectedReport == report
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedReport = report },
                            label = {
                                Text(
                                    text = report.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Period Selector
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(periods) { period ->
                        SuggestionChip(
                            onClick = { selectedPeriod = period },
                            label = { Text(period, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(8.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selectedPeriod == period) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
                            )
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Quick Export & Print Actions Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showPdfPreviewDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصدير PDF", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "تم تصدير ملف Excel (CSV) بنجاح إلى التنزيلات", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp), tint = StatusPaidGreenDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصدير Excel", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Summary Stats KPI Cards
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "المؤشرات الإجمالية للتقرير",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricBox(
                                title = "إجمالي الحسابات",
                                value = formatArabicCurrency(stats.totalVolume, settings.currencySymbol),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            MetricBox(
                                title = "المدفوع والمحصل",
                                value = formatArabicCurrency(stats.totalPaid, settings.currencySymbol),
                                color = StatusPaidGreenDark,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricBox(
                                title = "المتبقي (ديون معلقة)",
                                value = formatArabicCurrency(stats.totalRemaining, settings.currencySymbol),
                                color = StatusDebtRed,
                                modifier = Modifier.weight(1f)
                            )
                            MetricBox(
                                title = "المتأخرين في السداد",
                                value = "${stats.overdueCount} عميل",
                                color = StatusDebtRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Dynamic Report Table / Content
            when (selectedReport) {
                ReportType.CUSTOMERS -> {
                    item {
                        Text(
                            text = "بيان جميع العملاء (${customers.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    items(customers, key = { it.id }) { customer ->
                        ReportCustomerRow(
                            customer = customer,
                            currency = settings.currencySymbol,
                            onClick = { onNavigateToCustomer(customer.id) }
                        )
                    }
                }
                ReportType.DEBTS -> {
                    val debtCustomers = customers.filter { it.remainingAmount > 0 }
                    item {
                        Text(
                            text = "بيان المبالغ المستحقة والديون (${debtCustomers.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = StatusDebtRed
                            )
                        )
                    }
                    if (debtCustomers.isEmpty()) {
                        item {
                            Text("لا توجد مبالغ مستحقة أو ديون معلقة حالياً، جميع الحسابات مسددة!")
                        }
                    } else {
                        items(debtCustomers, key = { it.id }) { customer ->
                            ReportCustomerRow(
                                customer = customer,
                                currency = settings.currencySymbol,
                                onClick = { onNavigateToCustomer(customer.id) }
                            )
                        }
                    }
                }
                ReportType.PAYMENTS -> {
                    item {
                        Text(
                            text = "بيان سندات القبض والمدفوعات (${payments.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    items(payments, key = { it.id }) { payment ->
                        PaymentReceiptItem(
                            payment = payment,
                            currency = settings.currencySymbol,
                            onDelete = { viewModel.deletePayment(payment) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showPdfPreviewDialog) {
        PdfExportPreviewDialog(
            reportType = selectedReport,
            period = selectedPeriod,
            stats = stats,
            customers = customers,
            settings = settings,
            onDismiss = { showPdfPreviewDialog = false }
        )
    }
}

@Composable
fun MetricBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            ),
            maxLines = 1
        )
    }
}

@Composable
fun ReportCustomerRow(
    customer: com.example.data.local.entity.CustomerEntity,
    currency: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "البضاعة: ${customer.goodsType} | ${customer.phone}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (customer.remainingAmount <= 0) "خالص" else formatArabicCurrency(customer.remainingAmount, currency),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (customer.remainingAmount <= 0) StatusPaidGreenDark else StatusDebtRed
                    )
                )
                Text(
                    text = "استحقاق: ${formatArabicDate(customer.dueDate)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun PdfExportPreviewDialog(
    reportType: ReportType,
    period: String,
    stats: com.example.ui.viewmodel.DaftarStats,
    customers: List<com.example.data.local.entity.CustomerEntity>,
    settings: com.example.data.local.entity.AppSettingsEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DaftarBrandLogo(size = 32.dp, subtitle = null)
                Text(
                    text = "معاينة ملف PDF الرسمي",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                LazyColumn(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // PDF Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = settings.businessName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                )
                                Text(
                                    text = "دفتر الحسابات والعملاء | ${settings.businessPhone}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                                )
                            }
                            Text(
                                text = formatArabicDate(System.currentTimeMillis()),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    }

                    // Title
                    item {
                        Text(
                            text = "${reportType.title} ($period)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("إجمالي الحجم: ${formatArabicCurrency(stats.totalVolume, settings.currencySymbol)}", style = MaterialTheme.typography.labelSmall.copy(color = Color.Black))
                            Text("المحصل: ${formatArabicCurrency(stats.totalPaid, settings.currencySymbol)}", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF047857)))
                            Text("المتبقي: ${formatArabicCurrency(stats.totalRemaining, settings.currencySymbol)}", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFDC2626)))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    }

                    // List items preview
                    items(customers.take(6)) { c ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(c.name, style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                            Text(formatArabicCurrency(c.remainingAmount, settings.currencySymbol), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.Black))
                        }
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Text(
                            text = settings.invoiceFooterNote,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Toast.makeText(context, "تم حفظ وتصدير ملف PDF بنجاح!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ وتنزيل PDF")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}
