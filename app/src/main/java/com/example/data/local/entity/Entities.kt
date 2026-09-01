package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String = "",
    val goodsType: String = "",
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val dealDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000,
    val notes: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isPaidInFull: Boolean get() = remainingAmount <= 0.0
    val isOverdue: Boolean get() = !isPaidInFull && dueDate < System.currentTimeMillis()
}

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMethod: String = "نقداً", // نقداً، تحويل بنكي، شيك، شبكة
    val notes: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val actionType: String, // CREATE, UPDATE, PAYMENT, DELETE, SYSTEM
    val amount: Double? = null,
    val customerId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val title: String,
    val message: String,
    val amount: Double,
    val dueDate: Long,
    val isOverdue: Boolean,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val businessName: String = "مؤسسة الأمانة للتجارة",
    val businessPhone: String = "0501234567",
    val currencySymbol: String = "ر.س",
    val themeMode: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    val colorTheme: String = "TEAL", // TEAL, NAVY, EMERALD, BRONZE
    val isLockEnabled: Boolean = false,
    val lockPin: String = "1234",
    val isAlertsEnabled: Boolean = true,
    val alertDaysBefore: Int = 3,
    val invoiceFooterNote: String = "شكراً لتعاملكم الراقي معنا - دفتر المحاسبة السريعة"
)
