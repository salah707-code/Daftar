package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DaftarDatabase
import com.example.data.local.entity.*
import com.example.data.repository.DaftarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class CustomerFilter(val title: String) {
    ALL("الكل"),
    WITH_DEBT("عليهم مبالغ"),
    SETTLED("مسددين بالكامل"),
    OVERDUE("متأخرين بالسداد")
}

enum class SortOption(val title: String) {
    DATE_DESC("الأحدث تعاملاً"),
    REMAINING_DESC("الأعلى متبقي"),
    NAME_ASC("الاسم أبجدياً"),
    DUE_DATE_ASC("الأقرب استحقاقاً")
}

enum class HistoryFilter(val title: String) {
    TODAY("اليوم"),
    THIS_WEEK("هذا الأسبوع"),
    THIS_MONTH("هذا الشهر"),
    ALL("الكل")
}

class DaftarViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DaftarDatabase.getDatabase(application, viewModelScope)
    val repository = DaftarRepository(database)

    // Raw Flows
    val allCustomers = repository.allCustomers
    val allPayments = repository.allPayments
    val allLogs = repository.allLogs
    val allAlerts = repository.allAlerts
    val unreadAlertsCount = repository.unreadAlertsCount
    val settings = repository.settings.map { it ?: AppSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettingsEntity())

    // Search and Filter States
    val customerSearchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow(CustomerFilter.ALL)
    val selectedSort = MutableStateFlow(SortOption.DATE_DESC)

    // History filter
    val historyFilter = MutableStateFlow(HistoryFilter.ALL)
    val historySearchQuery = MutableStateFlow("")

    // Security Lock State
    val isAppUnlocked = MutableStateFlow(false)
    val lockError = MutableStateFlow<String?>(null)

    // Selected customer for detail
    val selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = selectedCustomerId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else repository.getCustomerById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedCustomerPayments: StateFlow<List<PaymentEntity>> = selectedCustomerId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getPaymentsForCustomer(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Customers Flow
    val filteredCustomers: StateFlow<List<CustomerEntity>> = combine(
        allCustomers,
        customerSearchQuery,
        selectedFilter,
        selectedSort
    ) { list, query, filter, sort ->
        var result = list

        // Search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.name.lowercase().contains(q) ||
                it.phone.contains(q) ||
                it.goodsType.lowercase().contains(q) ||
                it.address.lowercase().contains(q)
            }
        }

        // Filter
        result = when (filter) {
            CustomerFilter.ALL -> result
            CustomerFilter.WITH_DEBT -> result.filter { it.remainingAmount > 0 }
            CustomerFilter.SETTLED -> result.filter { it.remainingAmount <= 0 }
            CustomerFilter.OVERDUE -> result.filter { it.isOverdue }
        }

        // Sort
        when (sort) {
            SortOption.DATE_DESC -> result.sortedByDescending { it.dealDate }
            SortOption.REMAINING_DESC -> result.sortedByDescending { it.remainingAmount }
            SortOption.NAME_ASC -> result.sortedBy { it.name }
            SortOption.DUE_DATE_ASC -> result.sortedBy { it.dueDate }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered History Flow
    val filteredLogs: StateFlow<List<AuditLogEntity>> = combine(
        allLogs,
        historyFilter,
        historySearchQuery
    ) { logs, filter, query ->
        val now = System.currentTimeMillis()
        val oneDay = 24L * 60 * 60 * 1000
        var filtered = when (filter) {
            HistoryFilter.TODAY -> logs.filter { it.timestamp >= now - oneDay }
            HistoryFilter.THIS_WEEK -> logs.filter { it.timestamp >= now - 7 * oneDay }
            HistoryFilter.THIS_MONTH -> logs.filter { it.timestamp >= now - 30 * oneDay }
            HistoryFilter.ALL -> logs
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q)
            }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary Statistics
    val summaryStats = combine(
        allCustomers,
        allPayments
    ) { customers, payments ->
        val totalCustomersCount = customers.size
        val totalVolume = customers.sumOf { it.totalAmount }
        val totalPaid = customers.sumOf { it.paidAmount }
        val totalRemaining = customers.sumOf { it.remainingAmount }
        val overdueCustomersCount = customers.count { it.isOverdue }
        val fullySettledCount = customers.count { it.isPaidInFull }

        DaftarStats(
            customerCount = totalCustomersCount,
            totalVolume = totalVolume,
            totalPaid = totalPaid,
            totalRemaining = totalRemaining,
            overdueCount = overdueCustomersCount,
            settledCount = fullySettledCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DaftarStats())

    init {
        // Check initial lock status
        viewModelScope.launch {
            val s = repository.getSettingsDirect()
            if (!s.isLockEnabled) {
                isAppUnlocked.value = true
            }
        }
    }

    // Security Unlock
    fun unlockApp(pin: String): Boolean {
        val currentPin = settings.value.lockPin
        return if (pin == currentPin) {
            isAppUnlocked.value = true
            lockError.value = null
            true
        } else {
            lockError.value = "رمز الدخول غير صحيح، يرجى المحاولة ثانية"
            false
        }
    }

    fun lockApp() {
        if (settings.value.isLockEnabled) {
            isAppUnlocked.value = false
        }
    }

    // Customer Actions
    fun saveCustomer(
        id: Long = 0,
        name: String,
        phone: String,
        address: String,
        goodsType: String,
        totalAmount: Double,
        paidAmount: Double,
        dealDate: Long,
        dueDate: Long,
        notes: String,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val remaining = (totalAmount - paidAmount).coerceAtLeast(0.0)
            val customer = CustomerEntity(
                id = id,
                name = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                goodsType = goodsType.trim(),
                totalAmount = totalAmount,
                paidAmount = paidAmount,
                remainingAmount = remaining,
                dealDate = dealDate,
                dueDate = dueDate,
                notes = notes.trim(),
                updatedAt = System.currentTimeMillis()
            )

            val savedId = if (id == 0L) {
                repository.insertCustomer(customer)
            } else {
                repository.updateCustomer(customer)
                id
            }
            onSuccess(savedId)
        }
    }

    fun deleteCustomer(id: Long, name: String, onDeleted: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCustomer(id, name)
            if (selectedCustomerId.value == id) {
                selectedCustomerId.value = null
            }
            onDeleted()
        }
    }

    // Payment Actions
    fun addPayment(
        customerId: Long,
        customerName: String,
        amount: Double,
        method: String,
        notes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val payment = PaymentEntity(
                customerId = customerId,
                customerName = customerName,
                amount = amount,
                paymentDate = System.currentTimeMillis(),
                paymentMethod = method,
                notes = notes.trim()
            )
            repository.addPayment(payment)
            onComplete()
        }
    }

    fun deletePayment(payment: PaymentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePayment(payment)
        }
    }

    // Alert Actions
    fun markAlertAsRead(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markAlertAsRead(id)
        }
    }

    fun markAllAlertsAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markAllAlertsAsRead()
        }
    }

    fun deleteAlert(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAlert(id)
        }
    }

    fun clearAllAlerts() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAlerts()
        }
    }

    // History Actions
    fun clearLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearLogs()
        }
    }

    // Settings Actions
    fun updateSettings(newSettings: AppSettingsEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(newSettings)
            if (!newSettings.isLockEnabled) {
                isAppUnlocked.value = true
            }
        }
    }

    fun resetMockData(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetMockData()
            onComplete()
        }
    }
}

data class DaftarStats(
    val customerCount: Int = 0,
    val totalVolume: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val overdueCount: Int = 0,
    val settledCount: Int = 0
)
