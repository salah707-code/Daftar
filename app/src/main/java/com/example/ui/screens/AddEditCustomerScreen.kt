package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CustomerEntity
import com.example.ui.components.formatArabicCurrency
import com.example.ui.components.formatArabicDate
import com.example.ui.theme.StatusDebtRed
import com.example.ui.theme.StatusDebtRedLight
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPaidGreenDark
import com.example.ui.theme.StatusPaidGreenLight
import com.example.ui.theme.StatusWarningOrange
import com.example.ui.theme.StatusWarningOrangeLight
import com.example.ui.viewmodel.DaftarViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerScreen(
    customerId: Long?,
    viewModel: DaftarViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val settings by viewModel.settings.collectAsState()
    val isEditMode = customerId != null && customerId > 0

    var existingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var isLoaded by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var goodsType by remember { mutableStateOf("") }
    var totalAmountText by remember { mutableStateOf("") }
    var paidAmountText by remember { mutableStateOf("") }
    var dealDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var dueDate by remember { mutableStateOf(System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000) }
    var notes by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }

    LaunchedEffect(customerId) {
        if (isEditMode) {
            val customer = viewModel.repository.getCustomerByIdDirect(customerId!!)
            if (customer != null) {
                existingCustomer = customer
                name = customer.name
                phone = customer.phone
                address = customer.address
                goodsType = customer.goodsType
                totalAmountText = if (customer.totalAmount > 0) {
                    if (customer.totalAmount % 1.0 == 0.0) customer.totalAmount.toLong().toString() else customer.totalAmount.toString()
                } else ""
                paidAmountText = if (customer.paidAmount > 0) {
                    if (customer.paidAmount % 1.0 == 0.0) customer.paidAmount.toLong().toString() else customer.paidAmount.toString()
                } else ""
                dealDate = customer.dealDate
                dueDate = customer.dueDate
                notes = customer.notes
            }
        }
        isLoaded = true
    }

    // Instant automatic recalculation of remaining balance
    val totalAmount = totalAmountText.toDoubleOrNull() ?: 0.0
    val paidAmount = paidAmountText.toDoubleOrNull() ?: 0.0
    val remainingAmount = (totalAmount - paidAmount).coerceAtLeast(0.0)
    val isFullyPaid = totalAmount > 0 && remainingAmount == 0.0
    val isOverPaid = paidAmount > totalAmount && totalAmount > 0

    val goodsSuggestions = listOf(
        "أجهزة كهربائية",
        "أقمشة وملابس",
        "مواد بناء ومقاولات",
        "مواد غذائية وتموينات",
        "قطع غيار سيارات",
        "أثاث ومفروشات",
        "خدمات وصيانة",
        "مجوهرات وذهب",
        "أجهزة ذكية وإلكترونيات"
    )

    val oneDay = 24L * 60 * 60 * 1000

    fun showDatePicker(initialDate: Long, onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance().apply { timeInMillis = initialDate }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                onDateSelected(selectedCal.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEditMode) "تعديل حساب العميل" else "إضافة عميل جديد",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isEditMode) "تحديث السجلات والمعاملات" else "تسجيل حساب ودين جديد في دفترك",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.EditNote else Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEditMode) "تعديل بيانات: ${name.ifBlank { "عميل" }}" else "حساب عميل جديد",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "أدخل بيانات المعاملة والمبلغ، وسيتم احتساب المتبقي تلقائياً فوراً.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        )
                    }
                }
            }

            // Card 1: Basic Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "بيانات العميل الشخصية",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Customer Name Field (Mandatory)
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) nameError = false
                        },
                        label = { Text("اسم العميل أو المنشأة *") },
                        placeholder = { Text("مثال: عبد الله أحمد السعيد") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (nameError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (name.isNotBlank()) {
                                IconButton(onClick = { name = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        isError = nameError,
                        supportingText = {
                            if (nameError) {
                                Text("اسم العميل إلزامي لحفظ السجل", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_name_input")
                    )

                    // Phone Field
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الجوال / الهاتف") },
                        placeholder = { Text("05xxxxxxxx") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            if (phone.isNotBlank()) {
                                IconButton(onClick = { phone = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_phone_input")
                    )

                    // Address Field
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("العنوان / المدينة والحي") },
                        placeholder = { Text("مثال: الرياض - حي الملز") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Goods Type with Quick Selector Chips
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = goodsType,
                            onValueChange = { goodsType = it },
                            label = { Text("نوع البضاعة أو الخدمة") },
                            placeholder = { Text("مثال: أقمشة وملابس، أجهزة كهربائية...") },
                            leadingIcon = {
                                Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "اقتراحات سريعة لنوع البضاعة:",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(goodsSuggestions) { suggestion ->
                                val isSelected = goodsType == suggestion
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { goodsType = if (isSelected) "" else suggestion },
                                    label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Card 2: Financial Calculation Card (M3 styled with Real-Time Auto Calculation)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "الحساب المالي والمبالغ",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "العملة: ${settings.currencySymbol}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Total and Paid Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Amount
                        OutlinedTextField(
                            value = totalAmountText,
                            onValueChange = { totalAmountText = it },
                            label = { Text("المبلغ الإجمالي *") },
                            placeholder = { Text("0") },
                            leadingIcon = {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            suffix = { Text(settings.currencySymbol, style = MaterialTheme.typography.labelSmall) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("total_amount_input")
                        )

                        // Paid Amount
                        OutlinedTextField(
                            value = paidAmountText,
                            onValueChange = { paidAmountText = it },
                            label = { Text("المدفوع الآن") },
                            placeholder = { Text("0") },
                            leadingIcon = {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = StatusPaidGreenDark)
                            },
                            suffix = { Text(settings.currencySymbol, style = MaterialTheme.typography.labelSmall) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("paid_amount_input")
                        )
                    }

                    // Quick helpers for Paid Amount
                    if (totalAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "اختصارات الدفع:",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            SuggestionChip(
                                onClick = { paidAmountText = "0" },
                                label = { Text("لم يدفع شيء (0)", style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(8.dp)
                            )
                            SuggestionChip(
                                onClick = {
                                    val half = totalAmount / 2
                                    paidAmountText = if (half % 1.0 == 0.0) half.toLong().toString() else half.toString()
                                },
                                label = { Text("النصف (50%)", style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(8.dp)
                            )
                            SuggestionChip(
                                onClick = { paidAmountText = totalAmountText },
                                label = { Text("دفع كامل (100%)", style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Dynamic Real-time Calculation Result Banner
                    val bannerBackground by animateColorAsState(
                        targetValue = when {
                            isOverPaid -> StatusWarningOrangeLight
                            isFullyPaid -> StatusPaidGreenLight
                            remainingAmount > 0 -> StatusDebtRedLight
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        label = "bannerColor"
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = 1.5.dp,
                                color = when {
                                    isOverPaid -> StatusWarningOrange.copy(alpha = 0.5f)
                                    isFullyPaid -> StatusPaidGreenDark.copy(alpha = 0.5f)
                                    remainingAmount > 0 -> StatusDebtRed.copy(alpha = 0.4f)
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = RoundedCornerShape(14.dp)
                            ),
                        color = bannerBackground
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isFullyPaid -> Icons.Default.CheckCircle
                                            isOverPaid -> Icons.Default.Warning
                                            remainingAmount > 0 -> Icons.Default.AccountBalanceWallet
                                            else -> Icons.Default.Info
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            isFullyPaid -> StatusPaidGreenDark
                                            isOverPaid -> StatusWarningOrange
                                            remainingAmount > 0 -> StatusDebtRed
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "المبلغ المتبقي (محسوب تلقائياً)",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "المتبقي = الإجمالي ($totalAmount) - المدفوع ($paidAmount)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = formatArabicCurrency(remainingAmount, settings.currencySymbol),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = when {
                                            isFullyPaid -> StatusPaidGreenDark
                                            remainingAmount > 0 -> StatusDebtRed
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                )
                            }

                            // Dynamic Helper Status Text
                            if (isFullyPaid) {
                                Text(
                                    text = "✓ تم سداد كامل المبلغ، الحساب مسوى وخالص.",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StatusPaidGreenDark
                                    )
                                )
                            } else if (isOverPaid) {
                                Text(
                                    text = "تنبيه: المبلغ المدفوع أكبر من الإجمالي بمقدار ${formatArabicCurrency(paidAmount - totalAmount, settings.currencySymbol)} (فائض للعميل).",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StatusWarningOrange
                                    )
                                )
                            } else if (remainingAmount > 0) {
                                Text(
                                    text = "دين مستحق على العميل سيتم تسجيله في كشف الحساب.",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = StatusDebtRed
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Card 3: Transaction Dates & Due Date Planning
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "التواريخ والاستحقاق",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Deal Date Selector
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .clickable {
                                    showDatePicker(dealDate) { selected ->
                                        dealDate = selected
                                        if (dueDate < selected) {
                                            dueDate = selected + 14L * oneDay
                                        }
                                    }
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("تاريخ المعاملة", style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatArabicDate(dealDate),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Due Date Selector
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable {
                                    showDatePicker(dueDate) { selected ->
                                        dueDate = selected
                                    }
                                },
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.EventBusy,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "تاريخ الاستحقاق",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatArabicDate(dueDate),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }

                    Text("تحديد موعد الاستحقاق بضغطة واحدة:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "بعد أسبوع" to 7,
                            "بعد أسبوعين" to 14,
                            "بعد شهر" to 30,
                            "بعد شهرين" to 60
                        ).forEach { (label, days) ->
                            val targetDate = dealDate + days * oneDay
                            val isSelected = Math.abs(dueDate - targetDate) < 12 * 60 * 60 * 1000
                            FilterChip(
                                selected = isSelected,
                                onClick = { dueDate = targetDate },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Notes Field
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات إضافية / شروط الاتفاق") },
                        placeholder = { Text("أدخل أي تفاصيل إضافية عن الصفقة أو الضمان...") },
                        leadingIcon = {
                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Save Action Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        Toast.makeText(context, "يرجى كتابة اسم العميل أولاً للمتابعة", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.saveCustomer(
                            id = if (isEditMode) customerId!! else 0L,
                            name = name.trim(),
                            phone = phone.trim(),
                            address = address.trim(),
                            goodsType = goodsType.trim(),
                            totalAmount = totalAmount,
                            paidAmount = paidAmount,
                            dealDate = dealDate,
                            dueDate = dueDate,
                            notes = notes.trim()
                        ) {
                            Toast.makeText(
                                context,
                                if (isEditMode) "تم تحديث بيانات العميل بنجاح" else "تمت إضافة العميل إلى دفترك بنجاح",
                                Toast.LENGTH_SHORT
                            ).show()
                            onNavigateBack()
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_customer_button")
            ) {
                Icon(
                    imageVector = if (isEditMode) Icons.Default.CheckCircle else Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) "حفظ التعديلات" else "حفظ العميل في الدفتر",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
