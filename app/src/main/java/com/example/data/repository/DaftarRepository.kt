package com.example.data.repository

import com.example.data.local.DaftarDatabase
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

class DaftarRepository(private val database: DaftarDatabase) {
    private val customerDao = database.customerDao()
    private val paymentDao = database.paymentDao()
    private val auditLogDao = database.auditLogDao()
    private val alertDao = database.alertDao()
    private val settingsDao = database.appSettingsDao()

    // Customers
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val totalVolume: Flow<Double?> = customerDao.getTotalVolume()
    val totalRemainingDebts: Flow<Double?> = customerDao.getTotalRemainingDebts()
    val totalPaid: Flow<Double?> = customerDao.getTotalPaid()
    val customerCount: Flow<Int> = customerDao.getCustomerCount()

    fun getCustomerById(id: Long): Flow<CustomerEntity?> = customerDao.getCustomerById(id)
    suspend fun getCustomerByIdDirect(id: Long): CustomerEntity? = customerDao.getCustomerByIdDirect(id)

    suspend fun insertCustomer(customer: CustomerEntity): Long {
        val id = customerDao.insertCustomer(customer)
        auditLogDao.insertLog(
            AuditLogEntity(
                title = "إضافة عميل جديد",
                description = "تمت إضافة العميل ${customer.name} بإجمالي ${customer.totalAmount}",
                actionType = "CREATE",
                amount = customer.totalAmount,
                customerId = id
            )
        )
        // Check if alert needed
        if (customer.remainingAmount > 0) {
            val now = System.currentTimeMillis()
            val isOverdue = customer.dueDate < now
            val title = if (isOverdue) "دفعة متأخرة" else "استحقاق سداد قادم"
            val message = "العميل ${customer.name} - المبلغ المتبقي: ${customer.remainingAmount}"
            alertDao.insertAlert(
                AlertEntity(
                    customerId = id,
                    customerName = customer.name,
                    title = title,
                    message = message,
                    amount = customer.remainingAmount,
                    dueDate = customer.dueDate,
                    isOverdue = isOverdue
                )
            )
        }
        return id
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        customerDao.updateCustomer(customer)
        auditLogDao.insertLog(
            AuditLogEntity(
                title = "تعديل بيانات العميل",
                description = "تم تحديث بيانات العميل ${customer.name} (المتبقي: ${customer.remainingAmount})",
                actionType = "UPDATE",
                amount = customer.remainingAmount,
                customerId = customer.id
            )
        )
    }

    suspend fun deleteCustomer(id: Long, customerName: String) {
        customerDao.deleteCustomerById(id)
        auditLogDao.insertLog(
            AuditLogEntity(
                title = "حذف العميل",
                description = "تم حذف سجل العميل $customerName",
                actionType = "DELETE",
                amount = null,
                customerId = id
            )
        )
    }

    // Payments
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    fun getPaymentsForCustomer(customerId: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsForCustomer(customerId)

    suspend fun addPayment(payment: PaymentEntity): Long {
        val paymentId = paymentDao.insertPayment(payment)
        // Also update customer paid and remaining
        val currentCustomer = customerDao.getCustomerByIdDirect(payment.customerId)
        if (currentCustomer != null) {
            val newPaid = currentCustomer.paidAmount + payment.amount
            val newRemaining = (currentCustomer.totalAmount - newPaid).coerceAtLeast(0.0)
            val updatedCustomer = currentCustomer.copy(
                paidAmount = newPaid,
                remainingAmount = newRemaining,
                updatedAt = System.currentTimeMillis()
            )
            customerDao.updateCustomer(updatedCustomer)

            auditLogDao.insertLog(
                AuditLogEntity(
                    title = "تسجيل سند دفع/قبض",
                    description = "تم استلام مبلغ ${payment.amount} من ${currentCustomer.name} (${payment.paymentMethod})",
                    actionType = "PAYMENT",
                    amount = payment.amount,
                    customerId = currentCustomer.id
                )
            )
        }
        return paymentId
    }

    suspend fun deletePayment(payment: PaymentEntity) {
        paymentDao.deletePayment(payment)
        val customer = customerDao.getCustomerByIdDirect(payment.customerId)
        if (customer != null) {
            val newPaid = (customer.paidAmount - payment.amount).coerceAtLeast(0.0)
            val newRemaining = (customer.totalAmount - newPaid).coerceAtLeast(0.0)
            customerDao.updateCustomer(
                customer.copy(
                    paidAmount = newPaid,
                    remainingAmount = newRemaining,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // Audit logs
    val allLogs: Flow<List<AuditLogEntity>> = auditLogDao.getAllLogs()
    fun getLogsSince(since: Long): Flow<List<AuditLogEntity>> = auditLogDao.getLogsSince(since)
    suspend fun clearLogs() = auditLogDao.clearLogs()

    // Alerts
    val allAlerts: Flow<List<AlertEntity>> = alertDao.getAllAlerts()
    val unreadAlertsCount: Flow<Int> = alertDao.getUnreadAlertsCount()
    suspend fun markAlertAsRead(id: Long) = alertDao.markAsRead(id)
    suspend fun markAllAlertsAsRead() = alertDao.markAllAsRead()
    suspend fun deleteAlert(id: Long) = alertDao.deleteAlert(id)
    suspend fun clearAlerts() = alertDao.clearAlerts()

    // Settings
    val settings: Flow<AppSettingsEntity?> = settingsDao.getSettings()
    suspend fun getSettingsDirect(): AppSettingsEntity = settingsDao.getSettingsDirect() ?: AppSettingsEntity()
    suspend fun saveSettings(settings: AppSettingsEntity) = settingsDao.saveSettings(settings)

    // Reset with mock data
    suspend fun resetMockData() {
        DaftarDatabase.populateInitialArabicData(database)
    }
}
