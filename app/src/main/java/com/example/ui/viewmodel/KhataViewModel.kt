package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.KhataDatabase
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.PaymentMethod
import com.example.data.repository.KhataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

enum class TimeFilter(val displayName: String) {
    ALL("All (सब)"),
    TODAY("Today (आज)"),
    YESTERDAY("Yesterday (कल)"),
    THIS_WEEK("This Week (इस सप्ताह)"),
    THIS_MONTH("This Month (इस महीने)"),
    CUSTOM("Custom (कस्टम)")
}

data class CartItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val productName: String = "",
    val quantity: Double = 1.0,
    val pricePerUnit: Double = 0.0,
    val category: ExpenseCategory = ExpenseCategory.GROCERY
) {
    val totalPrice: Double
        get() = quantity * pricePerUnit
}

data class CategorySummary(
    val category: ExpenseCategory,
    val totalAmount: Double,
    val count: Int,
    val percentage: Float
)

data class MonthlyReport(
    val yearMonth: YearMonth,
    val totalSpent: Double,
    val totalQuantity: Double,
    val totalPurchases: Int,
    val highestSpendDay: LocalDate?,
    val highestSpendAmount: Double,
    val topItems: List<Pair<String, Double>>, // ProductName to TotalAmount
    val categorySummaries: List<CategorySummary>
)

class KhataViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KhataRepository

    init {
        val db = KhataDatabase.getDatabase(application)
        repository = KhataRepository(db.expenseDao(), application)
        viewModelScope.launch {
            repository.ensureSampleDataLoaded()
        }
    }

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyLimit: StateFlow<Double> = repository.dailyLimit

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _timeFilter = MutableStateFlow(TimeFilter.ALL)
    val timeFilter = _timeFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<ExpenseCategory?>(null)
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    private val _selectedPaymentFilter = MutableStateFlow<PaymentMethod?>(null)
    val selectedPaymentFilter = _selectedPaymentFilter.asStateFlow()

    private val _customStartDate = MutableStateFlow(LocalDate.now().minusDays(7))
    val customStartDate = _customStartDate.asStateFlow()

    private val _customEndDate = MutableStateFlow(LocalDate.now())
    val customEndDate = _customEndDate.asStateFlow()

    // Monthly Report Selected Month
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth = _selectedMonth.asStateFlow()

    // Filtered Expenses
    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
        allExpenses,
        _searchQuery,
        _timeFilter,
        _selectedCategoryFilter,
        _selectedPaymentFilter,
        _customStartDate,
        _customEndDate
    ) { params ->
        val list = params[0] as List<ExpenseEntity>
        val query = (params[1] as String).trim().lowercase()
        val filter = params[2] as TimeFilter
        val catFilter = params[3] as ExpenseCategory?
        val payFilter = params[4] as PaymentMethod?
        val startCustom = params[5] as LocalDate
        val endCustom = params[6] as LocalDate

        val today = LocalDate.now()

        list.filter { item ->
            val matchesQuery = query.isEmpty() ||
                    item.productName.lowercase().contains(query) ||
                    item.note.lowercase().contains(query)

            val itemDate = item.localDate
            val matchesTime = when (filter) {
                TimeFilter.ALL -> true
                TimeFilter.TODAY -> itemDate == today
                TimeFilter.YESTERDAY -> itemDate == today.minusDays(1)
                TimeFilter.THIS_WEEK -> {
                    val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                    !itemDate.isBefore(startOfWeek) && !itemDate.isAfter(endOfWeek)
                }
                TimeFilter.THIS_MONTH -> itemDate.year == today.year && itemDate.month == today.month
                TimeFilter.CUSTOM -> !itemDate.isBefore(startCustom) && !itemDate.isAfter(endCustom)
            }

            val matchesCat = catFilter == null || item.category == catFilter.name
            val matchesPay = payFilter == null || item.paymentMethod == payFilter.name

            matchesQuery && matchesTime && matchesCat && matchesPay
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search stats for currently searched term
    val searchSummary = combine(allExpenses, _searchQuery) { list, query ->
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            null
        } else {
            val matching = list.filter {
                it.productName.lowercase().contains(q) || it.note.lowercase().contains(q)
            }
            val totalSpend = matching.sumOf { it.totalPrice }
            val count = matching.size
            val totalQty = matching.sumOf { it.quantity }
            Triple(totalSpend, count, totalQty)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTimeFilter(filter: TimeFilter) {
        _timeFilter.value = filter
    }

    fun setCategoryFilter(category: ExpenseCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setPaymentFilter(payment: PaymentMethod?) {
        _selectedPaymentFilter.value = payment
    }

    fun setCustomDateRange(start: LocalDate, end: LocalDate) {
        _customStartDate.value = start
        _customEndDate.value = end
        _timeFilter.value = TimeFilter.CUSTOM
    }

    fun setSelectedMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
    }

    fun prevMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    // Today's specific computations
    fun getTodayExpenses(list: List<ExpenseEntity>): List<ExpenseEntity> {
        val today = LocalDate.now()
        return list.filter { it.localDate == today }
    }

    fun getTodayTotal(list: List<ExpenseEntity>): Double {
        return getTodayExpenses(list).sumOf { it.totalPrice }
    }

    fun getTodayTotalQuantity(list: List<ExpenseEntity>): Double {
        return getTodayExpenses(list).sumOf { it.quantity }
    }

    fun getTodayUniqueProductsCount(list: List<ExpenseEntity>): Int {
        return getTodayExpenses(list).map { it.productName.trim().lowercase() }.distinct().size
    }

    // Group expenses by Date
    fun getGroupedExpensesByDate(list: List<ExpenseEntity>): Map<LocalDate, List<ExpenseEntity>> {
        return list.groupBy { it.localDate }
            .toSortedMap(compareByDescending { it.toEpochDay() })
    }

    // Monthly Report Computation
    fun computeMonthlyReport(list: List<ExpenseEntity>, yearMonth: YearMonth): MonthlyReport {
        val monthExpenses = list.filter {
            val d = it.localDate
            d.year == yearMonth.year && d.month == yearMonth.month
        }

        val totalSpent = monthExpenses.sumOf { it.totalPrice }
        val totalQuantity = monthExpenses.sumOf { it.quantity }
        val totalPurchases = monthExpenses.size

        // Highest spending day
        val daySpendMap = monthExpenses.groupBy { it.localDate }
            .mapValues { entry -> entry.value.sumOf { it.totalPrice } }
        val highestEntry = daySpendMap.maxByOrNull { it.value }
        val highestDay = highestEntry?.key
        val highestAmount = highestEntry?.value ?: 0.0

        // Top purchased items
        val itemSpendMap = monthExpenses.groupBy { it.productName.trim() }
            .mapValues { entry -> entry.value.sumOf { it.totalPrice } }
            .toList()
            .sortedByDescending { it.second }
            .take(6)

        // Category breakdown
        val catMap = monthExpenses.groupBy { ExpenseCategory.fromString(it.category) }
        val categorySummaries = ExpenseCategory.values().mapNotNull { cat ->
            val items = catMap[cat]
            if (items != null && items.isNotEmpty()) {
                val sum = items.sumOf { it.totalPrice }
                val count = items.size
                val percentage = if (totalSpent > 0) (sum / totalSpent).toFloat() else 0f
                CategorySummary(cat, sum, count, percentage)
            } else null
        }.sortedByDescending { it.totalAmount }

        return MonthlyReport(
            yearMonth = yearMonth,
            totalSpent = totalSpent,
            totalQuantity = totalQuantity,
            totalPurchases = totalPurchases,
            highestSpendDay = highestDay,
            highestSpendAmount = highestAmount,
            topItems = itemSpendMap,
            categorySummaries = categorySummaries
        )
    }

    // Expense CRUD
    fun addSingleExpense(
        productName: String,
        quantity: Double,
        pricePerUnit: Double,
        date: LocalDate,
        category: ExpenseCategory,
        paymentMethod: PaymentMethod,
        note: String
    ) {
        val expense = ExpenseEntity(
            productName = productName.trim(),
            quantity = quantity,
            pricePerUnit = pricePerUnit,
            totalPrice = quantity * pricePerUnit,
            dateEpochDay = date.toEpochDay(),
            category = category.name,
            paymentMethod = paymentMethod.name,
            note = note.trim(),
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.insertExpense(expense)
        }
    }

    fun addBatchExpenses(
        items: List<CartItem>,
        date: LocalDate,
        paymentMethod: PaymentMethod,
        note: String
    ) {
        val validItems = items.filter { it.productName.isNotBlank() && it.pricePerUnit > 0 }
        if (validItems.isEmpty()) return

        val expenses = validItems.map { item ->
            ExpenseEntity(
                productName = item.productName.trim(),
                quantity = item.quantity,
                pricePerUnit = item.pricePerUnit,
                totalPrice = item.totalPrice,
                dateEpochDay = date.toEpochDay(),
                category = item.category.name,
                paymentMethod = paymentMethod.name,
                note = note.trim(),
                createdAt = System.currentTimeMillis()
            )
        }

        viewModelScope.launch {
            repository.insertBatchExpenses(expenses)
        }
    }

    fun updateExpense(
        id: Long,
        productName: String,
        quantity: Double,
        pricePerUnit: Double,
        date: LocalDate,
        category: ExpenseCategory,
        paymentMethod: PaymentMethod,
        note: String,
        createdAt: Long
    ) {
        val expense = ExpenseEntity(
            id = id,
            productName = productName.trim(),
            quantity = quantity,
            pricePerUnit = pricePerUnit,
            totalPrice = quantity * pricePerUnit,
            dateEpochDay = date.toEpochDay(),
            category = category.name,
            paymentMethod = paymentMethod.name,
            note = note.trim(),
            createdAt = createdAt
        )
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun setDailySpendingLimit(limit: Double) {
        repository.setDailyLimit(limit)
    }

    fun resetSampleData() {
        viewModelScope.launch {
            repository.clearAllExpenses()
            repository.seedSampleData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllExpenses()
        }
    }

    fun exportCsv(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val csv = repository.generateCsvString()
            onReady(csv)
        }
    }

    fun exportBackup(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportBackupJson()
            onReady(json)
        }
    }

    fun importBackup(json: String, onResult: (Boolean, Int) -> Unit) {
        viewModelScope.launch {
            val result = repository.importBackupJson(json)
            if (result.isSuccess) {
                onResult(true, result.getOrDefault(0))
            } else {
                onResult(false, 0)
            }
        }
    }
}
