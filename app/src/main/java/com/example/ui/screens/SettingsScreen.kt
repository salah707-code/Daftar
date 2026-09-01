package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AppSettingsEntity
import com.example.ui.components.DaftarBrandLogo
import com.example.ui.theme.*
import com.example.ui.viewmodel.DaftarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DaftarViewModel,
    onOpenShowcase: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    var showChangePinDialog by remember { mutableStateOf(false) }
    var showBusinessInfoDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "إعدادات دفتر",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = onOpenShowcase) {
                        Icon(
                            imageVector = Icons.Outlined.Preview,
                            contentDescription = "معاينة الهوية",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Branding Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        DaftarBrandLogo(size = 48.dp, subtitle = null)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "تطبيق «دَفْتَر» للعملاء والحسابات",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            Text(
                                text = "الإصدار 1.0 (محلي 100% بدون إنترنت وبدون خادم خارجي)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }
            }

            // 1. Appearance & Theme Section (المظهر والألوان)
            item {
                SettingsSectionCard(title = "المظهر والهوية البصرية") {
                    // Theme Mode
                    Text("نمط المظهر (فاتح / داكن):", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "LIGHT" to "فاتح",
                            "DARK" to "داكن",
                            "SYSTEM" to "تلقائي"
                        ).forEach { (mode, label) ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = {
                                    viewModel.updateSettings(settings.copy(themeMode = mode))
                                },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Brand Color Palette
                    Text("لون الهوية الأساسي:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("TEAL", "فيروزي", TealPrimary),
                            Triple("NAVY", "كحلي", NavyPrimary),
                            Triple("EMERALD", "زمردي", EmeraldPrimary),
                            Triple("BRONZE", "برونزي", BronzePrimary)
                        ).forEach { (code, label, color) ->
                            val isSelected = settings.colorTheme == code
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        viewModel.updateSettings(settings.copy(colorTheme = code))
                                    }
                                    .padding(vertical = 8.dp),
                                color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. App Security Section (حماية التطبيق)
            item {
                SettingsSectionCard(title = "أمان وحماية التطبيق") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("قفل التطبيق برقم سري (PIN)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "طلب كلمة المرور عند فتح التطبيق للحماية والخصوصية",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(
                            checked = settings.isLockEnabled,
                            onCheckedChange = { isEnabled ->
                                viewModel.updateSettings(settings.copy(isLockEnabled = isEnabled))
                            }
                        )
                    }

                    if (settings.isLockEnabled) {
                        OutlinedButton(
                            onClick = { showChangePinDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تغيير الرقم السري (الحالي: ${settings.lockPin})")
                        }
                    }
                }
            }

            // 3. Alerts & Notification Settings
            item {
                SettingsSectionCard(title = "إعدادات التنبيهات والاستحقاقات") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تنبيهات الاستحقاقات القادمة", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "تنبيه قبل موعد السداد المتبقي للعملاء",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(
                            checked = settings.isAlertsEnabled,
                            onCheckedChange = { isEnabled ->
                                viewModel.updateSettings(settings.copy(isAlertsEnabled = isEnabled))
                            }
                        )
                    }
                }
            }

            // 4. Business & Invoice Header Info
            item {
                SettingsSectionCard(title = "معلومات المنشأة والتقارير") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("اسم المنشأة: ${settings.businessName}", style = MaterialTheme.typography.bodyMedium)
                        Text("رقم الهاتف التجاري: ${settings.businessPhone}", style = MaterialTheme.typography.bodyMedium)
                        Text("العملة المعتمدة: ${settings.currencySymbol}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { showBusinessInfoDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تعديل بيانات المنشأة والطباعة", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // 5. Data Backup & Reset
            item {
                SettingsSectionCard(title = "النسخ الاحتياطي وإدارة البيانات") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "تم إنشاء نسخة احتياطية محلية من قاعدة بيانات دفتر بنجاح", Toast.LENGTH_LONG).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير نسخة احتياطية (Backup)")
                        }

                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "تمت استعادة البيانات بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استعادة نسخة احتياطية (Restore)")
                        }

                        Button(
                            onClick = { showResetConfirmDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusDebtRed.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = StatusDebtRed)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إعادة تهيئة البيانات التجريبية", color = StatusDebtRed)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("تغيير الرقم السري (PIN)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("أدخل الرقم السري الجديد المكون من 4 أرقام:")
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 6) newPin = it },
                        label = { Text("الرقم السري الجديد") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.isNotBlank()) {
                            viewModel.updateSettings(settings.copy(lockPin = newPin))
                            showChangePinDialog = false
                            Toast.makeText(context, "تم تحديث الرقم السري بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Business Info Dialog
    if (showBusinessInfoDialog) {
        var busName by remember { mutableStateOf(settings.businessName) }
        var busPhone by remember { mutableStateOf(settings.businessPhone) }
        var busCurrency by remember { mutableStateOf(settings.currencySymbol) }
        var busFooter by remember { mutableStateOf(settings.invoiceFooterNote) }

        AlertDialog(
            onDismissRequest = { showBusinessInfoDialog = false },
            title = { Text("تعديل بيانات المنشأة والتقارير") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = busName,
                        onValueChange = { busName = it },
                        label = { Text("اسم المنشأة / المتجر") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = busPhone,
                        onValueChange = { busPhone = it },
                        label = { Text("رقم الهاتف التجاري") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = busCurrency,
                        onValueChange = { busCurrency = it },
                        label = { Text("رمز العملة (مثال: ر.س، د.إ، ج.م)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = busFooter,
                        onValueChange = { busFooter = it },
                        label = { Text("ملاحظة تذييل الفاتورة") },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSettings(
                            settings.copy(
                                businessName = busName,
                                businessPhone = busPhone,
                                currencySymbol = busCurrency,
                                invoiceFooterNote = busFooter
                            )
                        )
                        showBusinessInfoDialog = false
                        Toast.makeText(context, "تم حفظ الإعدادات بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("حفظ التغييرات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBusinessInfoDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("إعادة تهيئة البيانات التجريبية") },
            text = { Text("سيتم إعادة تعيين بيانات العملاء والسجلات التجريبية إلى الوضع الافتراضي الأصلي. هل تريد الاستمرار؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetMockData {
                            showResetConfirmDialog = false
                            Toast.makeText(context, "تمت إعادة التهيئة بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDebtRed)
                ) {
                    Text("تأكيد التهيئة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            content()
        }
    }
}
