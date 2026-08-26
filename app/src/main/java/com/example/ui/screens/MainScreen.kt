package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseEntity
import com.example.ui.dialogs.AddExpenseDialog
import com.example.ui.dialogs.DateRangeDialog
import com.example.ui.dialogs.DeleteConfirmDialog
import com.example.ui.dialogs.EditExpenseDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.viewmodel.KhataViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: KhataViewModel) {
    val context = LocalContext.current

    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val filteredExpenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val dailyLimit by viewModel.dailyLimit.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchSummary by viewModel.searchSummary.collectAsStateWithLifecycle()
    val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val selectedPaymentFilter by viewModel.selectedPaymentFilter.collectAsStateWithLifecycle()
    val customStartDate by viewModel.customStartDate.collectAsStateWithLifecycle()
    val customEndDate by viewModel.customEndDate.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()

    // Nav Tab state: 0 = Today Dashboard, 1 = My Khata Timeline, 2 = Monthly Report, 3 = History & Search
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog & Sheet States
    var showAddDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var deletingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showCustomDateDialog by remember { mutableStateOf(false) }

    // Computations
    val todayExpenses = remember(allExpenses) { viewModel.getTodayExpenses(allExpenses) }
    val todayTotal = remember(allExpenses) { viewModel.getTodayTotal(allExpenses) }
    val todayItemsCount = remember(allExpenses) { viewModel.getTodayTotalQuantity(allExpenses) }
    val todayProductsCount = remember(allExpenses) { viewModel.getTodayUniqueProductsCount(allExpenses) }

    val groupedKhata = remember(filteredExpenses) { viewModel.getGroupedExpensesByDate(filteredExpenses) }
    val monthlyReport = remember(allExpenses, selectedMonth) { viewModel.computeMonthlyReport(allExpenses, selectedMonth) }

    fun shareText(title: String, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        context.startActivity(shareIntent)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Khata • Today (आज)"
                            1 -> "My Khata (दैनिक खाता)"
                            2 -> "Monthly Summary (रिपोर्ट)"
                            3 -> "Purchase History (इतिहास)"
                            else -> "Khata"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                // Tab 0: Dashboard (Today)
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Today") },
                    label = { Text("Today (आज)", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_today_tab")
                )

                // Tab 1: My Khata (Timeline)
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "My Khata") },
                    label = { Text("Khata", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_khata_tab")
                )

                // Tab 2: Monthly Summary
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Summary") },
                    label = { Text("Summary", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_summary_tab")
                )

                // Tab 3: History & Search
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_history_tab")
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Emerald600,
                contentColor = androidx.compose.ui.graphics.Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Expense") },
                text = { Text("Add Expense", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("global_fab_add_expense")
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    todayExpenses = todayExpenses,
                    todayTotal = todayTotal,
                    todayItemsCount = todayItemsCount,
                    todayProductsCount = todayProductsCount,
                    dailyLimit = dailyLimit,
                    onAddExpense = { showAddDialog = true },
                    onEditExpense = { editingExpense = it },
                    onDeleteExpense = { deletingExpense = it },
                    onNavigateToKhata = { selectedTab = 1 }
                )

                1 -> KhataTimelineScreen(
                    groupedExpenses = groupedKhata,
                    overallTotal = filteredExpenses.sumOf { it.totalPrice },
                    totalCount = filteredExpenses.size,
                    timeFilter = timeFilter,
                    onTimeFilterSelected = { viewModel.setTimeFilter(it) },
                    onOpenCustomDateRange = { showCustomDateDialog = true },
                    onEditExpense = { editingExpense = it },
                    onDeleteExpense = { deletingExpense = it }
                )

                2 -> MonthlySummaryScreen(
                    monthlyReport = monthlyReport,
                    onPrevMonth = { viewModel.prevMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )

                3 -> HistorySearchScreen(
                    expenses = filteredExpenses,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    searchSummary = searchSummary,
                    timeFilter = timeFilter,
                    onTimeFilterSelected = { viewModel.setTimeFilter(it) },
                    selectedCategory = selectedCategoryFilter,
                    onCategoryFilterSelected = { viewModel.setCategoryFilter(it) },
                    selectedPayment = selectedPaymentFilter,
                    onPaymentFilterSelected = { viewModel.setPaymentFilter(it) },
                    onOpenCustomDateRange = { showCustomDateDialog = true },
                    onExportCsv = {
                        viewModel.exportCsv { csv ->
                            shareText("Share Khata CSV Report", csv)
                        }
                    },
                    onEditExpense = { editingExpense = it },
                    onDeleteExpense = { deletingExpense = it }
                )
            }
        }
    }

    // Modal: Add Expense
    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onSaveSingle = { name, qty, price, date, cat, pay, note ->
                viewModel.addSingleExpense(name, qty, price, date, cat, pay, note)
                showAddDialog = false
                Toast.makeText(context, "Saved $name (₹${(qty * price).toInt()})", Toast.LENGTH_SHORT).show()
            },
            onSaveBatch = { items, date, pay, note ->
                viewModel.addBatchExpenses(items, date, pay, note)
                showAddDialog = false
                val total = items.sumOf { it.totalPrice }
                Toast.makeText(context, "Saved ${items.size} items (₹${total.toInt()})", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Edit Expense
    editingExpense?.let { expense ->
        EditExpenseDialog(
            expense = expense,
            onDismiss = { editingExpense = null },
            onSave = { id, name, qty, price, date, cat, pay, note, createdAt ->
                viewModel.updateExpense(id, name, qty, price, date, cat, pay, note, createdAt)
                editingExpense = null
                Toast.makeText(context, "Updated $name", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Delete Confirm
    deletingExpense?.let { expense ->
        DeleteConfirmDialog(
            expense = expense,
            onDismiss = { deletingExpense = null },
            onConfirm = {
                viewModel.deleteExpense(expense)
                deletingExpense = null
                Toast.makeText(context, "Deleted ${expense.productName}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Settings
    if (showSettingsDialog) {
        SettingsDialog(
            currentDailyLimit = dailyLimit,
            onDismiss = { showSettingsDialog = false },
            onUpdateDailyLimit = { viewModel.setDailySpendingLimit(it) },
            onExportCsv = { onReady -> viewModel.exportCsv(onReady) },
            onExportBackup = { onReady -> viewModel.exportBackup(onReady) },
            onImportBackup = { json, onResult -> viewModel.importBackup(json, onResult) },
            onResetSampleData = { viewModel.resetSampleData() },
            onClearAllData = { viewModel.clearAllData() }
        )
    }

    // Modal: Custom Date Range
    if (showCustomDateDialog) {
        DateRangeDialog(
            initialStartDate = customStartDate,
            initialEndDate = customEndDate,
            onDismiss = { showCustomDateDialog = false },
            onApplyRange = { start, end ->
                viewModel.setCustomDateRange(start, end)
                showCustomDateDialog = false
            }
        )
    }
}
