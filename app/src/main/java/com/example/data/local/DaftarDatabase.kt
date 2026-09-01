package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CustomerEntity::class,
        PaymentEntity::class,
        AuditLogEntity::class,
        AlertEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DaftarDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun paymentDao(): PaymentDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun alertDao(): AlertDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: DaftarDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): DaftarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DaftarDatabase::class.java,
                    "daftar_database.db"
                )
                .addCallback(DaftarDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DaftarDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialArabicData(database)
                    }
                }
            }
        }

        suspend fun populateInitialArabicData(database: DaftarDatabase) {
            val customerDao = database.customerDao()
            val paymentDao = database.paymentDao()
            val auditLogDao = database.auditLogDao()
            val alertDao = database.alertDao()
            val settingsDao = database.appSettingsDao()

            // 1. Initial settings
            settingsDao.saveSettings(AppSettingsEntity())

            val now = System.currentTimeMillis()
            val oneDay = 24L * 60 * 60 * 1000

            // 2. Realistic Arabic Customers
            val customers = listOf(
                CustomerEntity(
                    id = 1,
                    name = "مؤسسة الأمل للتجارة (أحمد الدوسري)",
                    phone = "0501234567",
                    address = "الرياض - حي الملز",
                    goodsType = "أجهزة كهربائية وإلكترونيات",
                    totalAmount = 14500.0,
                    paidAmount = 8500.0,
                    remainingAmount = 6000.0,
                    dealDate = now - 10 * oneDay,
                    dueDate = now - 2 * oneDay, // Overdue
                    notes = "عميل مميز، وعد بسداد المتبقي نهاية الأسبوع.",
                    isFavorite = true,
                    createdAt = now - 10 * oneDay,
                    updatedAt = now - 1 * oneDay
                ),
                CustomerEntity(
                    id = 2,
                    name = "سارة عبد الله القحطاني",
                    phone = "0559876543",
                    address = "جدة - حي الروضة",
                    goodsType = "أقمشة وملابس جاهزة",
                    totalAmount = 5200.0,
                    paidAmount = 5200.0,
                    remainingAmount = 0.0,
                    dealDate = now - 15 * oneDay,
                    dueDate = now + 5 * oneDay,
                    notes = "تم سداد كامل الفاتورة نقداً مع خصم العرض.",
                    isFavorite = true,
                    createdAt = now - 15 * oneDay,
                    updatedAt = now - 3 * oneDay
                ),
                CustomerEntity(
                    id = 3,
                    name = "شركة الصفا للمقاولات (خالد العتيبي)",
                    phone = "0543219876",
                    address = "الدمام - حي الشاطئ",
                    goodsType = "مواد بناء وأدوات صحية",
                    totalAmount = 38000.0,
                    paidAmount = 15000.0,
                    remainingAmount = 23000.0,
                    dealDate = now - 20 * oneDay,
                    dueDate = now + 4 * oneDay, // Upcoming
                    notes = "توريد الدفعة الثانية للمشروع السكني، استحقاق بعد 4 أيام.",
                    isFavorite = false,
                    createdAt = now - 20 * oneDay,
                    updatedAt = now - 2 * oneDay
                ),
                CustomerEntity(
                    id = 4,
                    name = "متجر النخيل للمواد الغذائية (فاطمة الشهري)",
                    phone = "0564567890",
                    address = "المدينة المنورة - حي سلطانة",
                    goodsType = "مواد تموينية وغذائية",
                    totalAmount = 9800.0,
                    paidAmount = 4000.0,
                    remainingAmount = 5800.0,
                    dealDate = now - 5 * oneDay,
                    dueDate = now + 12 * oneDay,
                    notes = "طلبية زيوت ومواد تموينية، دفعة أولى تحويل بنكي.",
                    isFavorite = false,
                    createdAt = now - 5 * oneDay,
                    updatedAt = now - 5 * oneDay
                ),
                CustomerEntity(
                    id = 5,
                    name = "ورشة السعادة لقطع الغيار (محمود رضوان)",
                    phone = "0531122334",
                    address = "مكة المكرمة - المعابدة",
                    goodsType = "قطع غيار وزيوت سيارات",
                    totalAmount = 7400.0,
                    paidAmount = 2000.0,
                    remainingAmount = 5400.0,
                    dealDate = now - 18 * oneDay,
                    dueDate = now - 5 * oneDay, // Overdue
                    notes = "تم التواصل هاتفياً وطلب تأجيل الموعد ليوم الأحد.",
                    isFavorite = false,
                    createdAt = now - 18 * oneDay,
                    updatedAt = now - 4 * oneDay
                )
            )

            for (c in customers) {
                customerDao.insertCustomer(c)
            }

            // 3. Sample payments
            val payments = listOf(
                PaymentEntity(
                    customerId = 1,
                    customerName = "مؤسسة الأمل للتجارة (أحمد الدوسري)",
                    amount = 5000.0,
                    paymentDate = now - 9 * oneDay,
                    paymentMethod = "تحويل بنكي",
                    notes = "دفعة تعاقدية أولى"
                ),
                PaymentEntity(
                    customerId = 1,
                    customerName = "مؤسسة الأمل للتجارة (أحمد الدوسري)",
                    amount = 3500.0,
                    paymentDate = now - 1 * oneDay,
                    paymentMethod = "نقداً",
                    notes = "سداد نقدي في المعرض"
                ),
                PaymentEntity(
                    customerId = 2,
                    customerName = "سارة عبد الله القحطاني",
                    amount = 5200.0,
                    paymentDate = now - 3 * oneDay,
                    paymentMethod = "شبكة (مدى)",
                    notes = "سداد كامل المبلغ دفعة واحدة"
                ),
                PaymentEntity(
                    customerId = 3,
                    customerName = "شركة الصفا للمقاولات (خالد العتيبي)",
                    amount = 15000.0,
                    paymentDate = now - 19 * oneDay,
                    paymentMethod = "شيك مصرفي",
                    notes = "شيك دفعة أولى رقم 4489"
                ),
                PaymentEntity(
                    customerId = 4,
                    customerName = "متجر النخيل للمواد الغذائية (فاطمة الشهري)",
                    amount = 4000.0,
                    paymentDate = now - 5 * oneDay,
                    paymentMethod = "تحويل بنكي",
                    notes = "تحويل حساب مؤسسي"
                ),
                PaymentEntity(
                    customerId = 5,
                    customerName = "ورشة السعادة لقطع الغيار (محمود رضوان)",
                    amount = 2000.0,
                    paymentDate = now - 17 * oneDay,
                    paymentMethod = "نقداً",
                    notes = "عربون حجز قطع الغيار"
                )
            )

            for (p in payments) {
                paymentDao.insertPayment(p)
            }

            // 4. Sample Alerts
            val alerts = listOf(
                AlertEntity(
                    customerId = 1,
                    customerName = "مؤسسة الأمل للتجارة (أحمد الدوسري)",
                    title = "دفعة متأخرة في السداد",
                    message = "استحقاق سداد مبلغ 6,000 ر.س كان قبل يومين.",
                    amount = 6000.0,
                    dueDate = now - 2 * oneDay,
                    isOverdue = true,
                    isRead = false,
                    createdAt = now - 2 * oneDay
                ),
                AlertEntity(
                    customerId = 5,
                    customerName = "ورشة السعادة لقطع الغيار (محمود رضوان)",
                    title = "دفعة متأخرة في السداد",
                    message = "استحقاق سداد مبلغ 5,400 ر.س متأخر منذ 5 أيام.",
                    amount = 5400.0,
                    dueDate = now - 5 * oneDay,
                    isOverdue = true,
                    isRead = false,
                    createdAt = now - 5 * oneDay
                ),
                AlertEntity(
                    customerId = 3,
                    customerName = "شركة الصفا للمقاولات (خالد العتيبي)",
                    title = "استحقاق قادم قريب",
                    message = "استحقاق سداد مبلغ 23,000 ر.س متبقي بعد 4 أيام.",
                    amount = 23000.0,
                    dueDate = now + 4 * oneDay,
                    isOverdue = false,
                    isRead = true,
                    createdAt = now - 1 * oneDay
                )
            )

            alertDao.insertAlerts(alerts)

            // 5. Activity Logs
            val logs = listOf(
                AuditLogEntity(
                    title = "إضافة عميل جديد",
                    description = "تم تسجيل العميل: مؤسسة الأمل للتجارة بمبلغ 14,500 ر.س",
                    actionType = "CREATE",
                    amount = 14500.0,
                    customerId = 1,
                    timestamp = now - 10 * oneDay
                ),
                AuditLogEntity(
                    title = "تسجيل سند قبض",
                    description = "تم استلام دفعة 3,500 ر.س من العميل أحمد الدوسري نقداً",
                    actionType = "PAYMENT",
                    amount = 3500.0,
                    customerId = 1,
                    timestamp = now - 1 * oneDay
                ),
                AuditLogEntity(
                    title = "تسوية حساب بالكامل",
                    description = "تم تسوية كامل حساب العميلة سارة القحطاني 5,200 ر.س",
                    actionType = "PAYMENT",
                    amount = 5200.0,
                    customerId = 2,
                    timestamp = now - 3 * oneDay
                ),
                AuditLogEntity(
                    title = "تعديل بيانات العميل",
                    description = "تحديث ملاحظات ورشة السعادة وتأجيل الموعد",
                    actionType = "UPDATE",
                    amount = null,
                    customerId = 5,
                    timestamp = now - 4 * oneDay
                )
            )

            for (l in logs) {
                auditLogDao.insertLog(l)
            }
        }
    }
}
