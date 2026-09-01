package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CustomerEntity
import com.example.ui.components.formatArabicCurrency
import com.example.ui.components.formatArabicDate
import com.example.ui.theme.StatusDebtRed
import com.example.ui.theme.StatusDebtRedLight
import com.example.ui.theme.StatusPaidGreenDark
import com.example.ui.theme.StatusPaidGreenLight
import com.example.ui.viewmodel.DaftarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerScreen(
    customerId: Long?,
    viewModel: DaftarViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
                totalAmountText = if (customer.totalAmount > 0) customer.totalAmount.toInt().toString() else ""
                paidAmountText = if (customer.paidAmount > 0) customer.paidAmount.toInt().toString() else ""
                dealDate = customer.dealDate
                dueDate = customer.dueDate
                notes = customer.notes
            }
        }
        isLoaded = true
    }

    val totalAmount = totalAmountText.toDoubleOrNull() ?: 0.0
    val paidAmount = paidAmountText.toDoubleOrNull() ?: 0.0
    val remainingAmount = (totalAmount - paidAmount).coerceAtLeast(0.0)

    val goodsSuggestions = listOf(
        "أجهزة كهربائية",
        "أقمشة وملابس",
        "مواد بناء",
        "مواد غذائية",
        "قطع غيار",
        "خدمات ومقاولات",
        "مفروشات وأثاث",
        "عطور ومستحضرات"
    )

    val oneDay = 24L * 60 * 60 * 1000

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "تعديل بيانات العميل" else "إضافة عميل جديد",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
            // Basic Information Card
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
                    Text(
                        text = "البيانات الأساسية للعميل",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) nameError = false
                        },
                        label = { Text("اسم العميل أو المؤسسة *") },
                        placeholder = { Text("مثال: عبد الله الشمري") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        isError = nameError,
                        supportingText = {
                            if (nameError) {
                                Text("اسم العميل مطلوب", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_name_input")
                    )

                    // Phone Field
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف") },
                        placeholder = { Text("05xxxxxxxx") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                        label = { Text("العنوان أو الحي / المدينة") },
                        placeholder = { Text("مثال: الرياض - حي النسيم") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Goods Type with suggestions
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = goodsType,
                            onValueChange = { goodsType = it },
                            label = { Text("نوع البضاعة / النشاط") },
                            placeholder = { Text("مثال: أقمشة بالجملة") },
                            leadingIcon = {
                                Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(goodsSuggestions) { suggestion ->
                                SuggestionChip(
                                    onClick = { goodsType = suggestion },
                                    label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Financial Balance Card
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
                    Text(
                        text = "المعاملة المالية (${settings.currencySymbol})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Amount
                        OutlinedTextField(
                            value = totalAmountText,
                            onValueChange = { totalAmountText = it },
                            label = { Text("إجمالي المبلغ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            label = { Text("المبلغ المدفوع") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("paid_amount_input")
                        )
                    }

                    // Auto-calculated Remaining Amount banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (remainingAmount <= 0) StatusPaidGreenLight else StatusDebtRedLight
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المتبقي المستحق (يحسب تلقائياً):",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = formatArabicCurrency(remainingAmount, settings.currencySymbol),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (remainingAmount <= 0) StatusPaidGreenDark else StatusDebtRed
                                )
                            )
                        }
                    }
                }
            }

            // Dates Card (Deal Date & Due Date)
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
                    Text(
                        text = "التواريخ والاستحقاق",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("تاريخ التعامل:", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = formatArabicDate(dealDate),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("تاريخ الاستحقاق:", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = formatArabicDate(dueDate),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    Text("تحديد موعد الاستحقاق السريع:", style = MaterialTheme.typography.labelSmall)
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
                            FilterChip(
                                selected = dueDate == dealDate + days * oneDay,
                                onClick = { dueDate = dealDate + days * oneDay },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات إضافية عن العميل أو المعاملة") },
                        placeholder = { Text("أدخل أي تفاصيل أو اتفاقيات...") },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        Toast.makeText(context, "يرجى كتابة اسم العميل أولاً", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.saveCustomer(
                            id = if (isEditMode) customerId!! else 0L,
                            name = name,
                            phone = phone,
                            address = address,
                            goodsType = goodsType,
                            totalAmount = totalAmount,
                            paidAmount = paidAmount,
                            dealDate = dealDate,
                            dueDate = dueDate,
                            notes = notes
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
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
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
