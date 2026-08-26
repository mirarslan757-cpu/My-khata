package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.dao.ExpenseDao
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.PaymentMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class KhataRepository(
    private val expenseDao: ExpenseDao,
    private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("khata_preferences", Context.MODE_PRIVATE)

    private val _dailyLimit = MutableStateFlow(prefs.getFloat(KEY_DAILY_LIMIT, 1000f).toDouble())
    val dailyLimit = _dailyLimit.asStateFlow()

    companion object {
        private const val KEY_DAILY_LIMIT = "daily_spending_limit"
        private const val KEY_SAMPLE_INITIALIZED = "sample_data_initialized"
    }

    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpensesFlow()

    fun getExpensesForDate(date: LocalDate): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByDateFlow(date.toEpochDay())
    }

    fun getExpensesForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByDateRangeFlow(startDate.toEpochDay(), endDate.toEpochDay())
    }

    fun searchExpenses(query: String): Flow<List<ExpenseEntity>> {
        return expenseDao.searchExpensesFlow(query)
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long {
        return withContext(Dispatchers.IO) {
            expenseDao.insert(expense)
        }
    }

    suspend fun insertBatchExpenses(expenses: List<ExpenseEntity>): List<Long> {
        return withContext(Dispatchers.IO) {
            expenseDao.insertAll(expenses)
        }
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        withContext(Dispatchers.IO) {
            expenseDao.update(expense)
        }
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        withContext(Dispatchers.IO) {
            expenseDao.delete(expense)
        }
    }

    suspend fun deleteExpenseById(id: Long) {
        withContext(Dispatchers.IO) {
            expenseDao.deleteById(id)
        }
    }

    suspend fun clearAllExpenses() {
        withContext(Dispatchers.IO) {
            expenseDao.clearAll()
        }
    }

    fun setDailyLimit(limit: Double) {
        prefs.edit().putFloat(KEY_DAILY_LIMIT, limit.toFloat()).apply()
        _dailyLimit.value = limit
    }

    suspend fun ensureSampleDataLoaded() {
        withContext(Dispatchers.IO) {
            val count = expenseDao.getCount()
            val hasInitialized = prefs.getBoolean(KEY_SAMPLE_INITIALIZED, false)
            if (count == 0 && !hasInitialized) {
                seedSampleData()
                prefs.edit().putBoolean(KEY_SAMPLE_INITIALIZED, true).apply()
            }
        }
    }

    suspend fun seedSampleData() {
        withContext(Dispatchers.IO) {
            val today = LocalDate.now()
            val sampleItems = listOf(
                // Today items (from prompt examples)
                ExpenseEntity(
                    productName = "Milk",
                    quantity = 2.0,
                    pricePerUnit = 60.0,
                    totalPrice = 120.0,
                    dateEpochDay = today.toEpochDay(),
                    category = ExpenseCategory.MILK_DAIRY.name,
                    paymentMethod = PaymentMethod.UPI.name,
                    note = "Amul Taaza 1L packets",
                    createdAt = System.currentTimeMillis() - 3600000 * 4
                ),
                ExpenseEntity(
                    productName = "Bread",
                    quantity = 1.0,
                    pricePerUnit = 40.0,
                    totalPrice = 40.0,
                    dateEpochDay = today.toEpochDay(),
                    category = ExpenseCategory.GROCERY.name,
                    paymentMethod = PaymentMethod.CASH.name,
                    note = "Brown bread",
                    createdAt = System.currentTimeMillis() - 3600000 * 3
                ),
                ExpenseEntity(
                    productName = "Notebook",
                    quantity = 2.0,
                    pricePerUnit = 50.0,
                    totalPrice = 100.0,
                    dateEpochDay = today.toEpochDay(),
                    category = ExpenseCategory.EDUCATION.name,
                    paymentMethod = PaymentMethod.UPI.name,
                    note = "Classmate long notebook",
                    createdAt = System.currentTimeMillis() - 3600000 * 2
                ),
                ExpenseEntity(
                    productName = "Snacks",
                    quantity = 3.0,
                    pricePerUnit = 30.0,
                    totalPrice = 90.0,
                    dateEpochDay = today.toEpochDay(),
                    category = ExpenseCategory.FOOD.name,
                    paymentMethod = PaymentMethod.CASH.name,
                    note = "Chips & biscuits for evening",
                    createdAt = System.currentTimeMillis() - 3600000
                ),
                ExpenseEntity(
                    productName = "Pen",
                    quantity = 2.0,
                    pricePerUnit = 50.0,
                    totalPrice = 100.0,
                    dateEpochDay = today.toEpochDay(),
                    category = ExpenseCategory.EDUCATION.name,
                    paymentMethod = PaymentMethod.UPI.name,
                    note = "Gel pens pack",
                    createdAt = System.currentTimeMillis()
                ),

                // Yesterday items
                ExpenseEntity(
                    productName = "Vegetables",
                    quantity = 1.0,
                    pricePerUnit = 180.0,
                    totalPrice = 180.0,
                    dateEpochDay = today.minusDays(1).toEpochDay(),
                    category = ExpenseCategory.VEGETABLES.name,
                    paymentMethod = PaymentMethod.CASH.name,
                    note = "Potatoes, Tomatoes, Onion",
                    createdAt = System.currentTimeMillis() - 86400000
                ),
                ExpenseEntity(
                    productName = "Tea & Samosa",
                    quantity = 2.0,
                    pricePerUnit = 35.0,
                    totalPrice = 70.0,
                    dateEpochDay = today.minusDays(1).toEpochDay(),
                    category = ExpenseCategory.FOOD.name,
                    paymentMethod = PaymentMethod.UPI.name,
                    note = "Evening chai with friends",
                    createdAt = System.currentTimeMillis() - 86400000 + 3600000
                ),
                ExpenseEntity(
                    productName = "Petrol",
                    quantity = 2.5,
                    pricePerUnit = 104.0,
                    totalPrice = 260.0,
                    dateEpochDay = today.minusDays(1).toEpochDay(),
                    category = ExpenseCategory.TRANSPORT.name,
                    paymentMethod = PaymentMethod.CARD.name,
                    note = "Scooty tank refill",
                    createdAt = System.currentTimeMillis() - 86400000 + 7200000
                ),

                // 2 days ago
                ExpenseEntity(
                    productName = "Eggs",
                    quantity = 12.0,
                    pricePerUnit = 8.0,
                    totalPrice = 96.0,
                    dateEpochDay = today.minusDays(2).toEpochDay(),
                    category = ExpenseCategory.GROCERY.name,
                    paymentMethod = PaymentMethod.UPI.name,
                    note = "Tray of fresh eggs",
                    createdAt = System.currentTimeMillis() - 86400000 * 2
                ),
                ExpenseEntity(
                    productName = "Mobile Recharge",
                    quantity = 1.0,
                    pricePerUnit = 299.0,
                    totalPrice = 299.0,
                    dateEpochDay = today.minusDays(2).toEpochDay(),
                    category = ExpenseCategory.BILLS.name,
                    paymentMethod = PaymentMethod.UPI.name,
                    note = "Monthly 28 days plan",
                    createdAt = System.currentTimeMillis() - 86400000 * 2 + 3600000
                ),

                // 3 days ago
                ExpenseEntity(
                    productName = "Medicine (Paracetamol)",
                    quantity = 1.0,
                    pricePerUnit = 65.0,
                    totalPrice = 65.0,
                    dateEpochDay = today.minusDays(3).toEpochDay(),
                    category = ExpenseCategory.HEALTH.name,
                    paymentMethod = PaymentMethod.CASH.name,
                    note = "First aid strip",
                    createdAt = System.currentTimeMillis() - 86400000 * 3
                ),
                ExpenseEntity(
                    productName = "Fruits (Bananas & Apples)",
                    quantity = 1.0,
                    pricePerUnit = 140.0,
                    totalPrice = 140.0,
                    dateEpochDay = today.minusDays(3).toEpochDay(),
                    category = ExpenseCategory.VEGETABLES.name,
                    paymentMethod = PaymentMethod.UPI.name,
                    note = "1 dozen bananas + 500g apples",
                    createdAt = System.currentTimeMillis() - 86400000 * 3 + 3600000
                ),

                // 5 days ago
                ExpenseEntity(
                    productName = "Groceries (Atta & Rice)",
                    quantity = 1.0,
                    pricePerUnit = 650.0,
                    totalPrice = 650.0,
                    dateEpochDay = today.minusDays(5).toEpochDay(),
                    category = ExpenseCategory.GROCERY.name,
                    paymentMethod = PaymentMethod.UPI.name,
                    note = "5kg Aashirvaad Atta + 5kg Rice",
                    createdAt = System.currentTimeMillis() - 86400000 * 5
                ),
                ExpenseEntity(
                    productName = "Cotton T-Shirt",
                    quantity = 1.0,
                    pricePerUnit = 499.0,
                    totalPrice = 499.0,
                    dateEpochDay = today.minusDays(6).toEpochDay(),
                    category = ExpenseCategory.SHOPPING.name,
                    paymentMethod = PaymentMethod.CARD.name,
                    note = "Casual summer tee",
                    createdAt = System.currentTimeMillis() - 86400000 * 6
                )
            )
            expenseDao.insertAll(sampleItems)
        }
    }

    suspend fun generateCsvString(): String {
        return withContext(Dispatchers.IO) {
            val list = expenseDao.getAllExpensesList()
            val sb = StringBuilder()
            sb.append("ID,Date,Product Name,Quantity,Price Per Unit,Total Price,Category,Payment Method,Note\n")
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            for (item in list) {
                val dateStr = item.localDate.format(formatter)
                val safeName = "\"${item.productName.replace("\"", "\"\"")}\""
                val safeNote = "\"${item.note.replace("\"", "\"\"")}\""
                val categoryName = ExpenseCategory.fromString(item.category).displayName
                val payment = PaymentMethod.fromString(item.paymentMethod).displayName
                sb.append("${item.id},$dateStr,$safeName,${item.quantity},${item.pricePerUnit},${item.totalPrice},\"$categoryName\",\"$payment\",$safeNote\n")
            }
            sb.toString()
        }
    }

    suspend fun exportBackupJson(): String {
        return withContext(Dispatchers.IO) {
            val list = expenseDao.getAllExpensesList()
            val jsonArray = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("productName", item.productName)
                    put("quantity", item.quantity)
                    put("pricePerUnit", item.pricePerUnit)
                    put("totalPrice", item.totalPrice)
                    put("dateEpochDay", item.dateEpochDay)
                    put("category", item.category)
                    put("paymentMethod", item.paymentMethod)
                    put("note", item.note)
                    put("createdAt", item.createdAt)
                }
                jsonArray.put(obj)
            }
            val root = JSONObject().apply {
                put("app", "Khata")
                put("version", 1)
                put("exportedAt", System.currentTimeMillis())
                put("expenses", jsonArray)
            }
            root.toString(2)
        }
    }

    suspend fun importBackupJson(jsonString: String): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val root = JSONObject(jsonString)
                val array = root.getJSONArray("expenses")
                val importedList = mutableListOf<ExpenseEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val expense = ExpenseEntity(
                        productName = obj.optString("productName", "Item"),
                        quantity = obj.optDouble("quantity", 1.0),
                        pricePerUnit = obj.optDouble("pricePerUnit", 0.0),
                        totalPrice = obj.optDouble("totalPrice", 0.0),
                        dateEpochDay = obj.optLong("dateEpochDay", LocalDate.now().toEpochDay()),
                        category = obj.optString("category", ExpenseCategory.OTHER.name),
                        paymentMethod = obj.optString("paymentMethod", PaymentMethod.CASH.name),
                        note = obj.optString("note", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                    importedList.add(expense)
                }
                if (importedList.isNotEmpty()) {
                    expenseDao.insertAll(importedList)
                }
                Result.success(importedList.size)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
