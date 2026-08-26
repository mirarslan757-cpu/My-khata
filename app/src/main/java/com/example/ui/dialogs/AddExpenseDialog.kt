package com.example.ui.dialogs

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMethod
import com.example.ui.components.CategoryIconBox
import com.example.ui.components.Formatters
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.viewmodel.CartItem
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSaveSingle: (
        productName: String,
        quantity: Double,
        pricePerUnit: Double,
        date: LocalDate,
        category: ExpenseCategory,
        paymentMethod: PaymentMethod,
        note: String
    ) -> Unit,
    onSaveBatch: (
        items: List<CartItem>,
        date: LocalDate,
        paymentMethod: PaymentMethod,
        note: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun dismissSafely() {
        focusManager.clearFocus()
        keyboardController?.hide()
        onDismiss()
    }

    // Mode: 0 = Quick Single Item, 1 = Multi-Item Calculator / Basket
    var entryMode by remember { mutableStateOf(0) }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedPayment by remember { mutableStateOf(PaymentMethod.CASH) }
    var generalNote by remember { mutableStateOf("") }

    // Single item fields
    var productName by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var pricePerUnitText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.GROCERY) }

    // Multi item fields
    val multiItems = remember {
        mutableStateListOf(
            CartItem(productName = "Milk", quantity = 2.0, pricePerUnit = 60.0, category = ExpenseCategory.MILK_DAIRY),
            CartItem(productName = "Bread", quantity = 1.0, pricePerUnit = 40.0, category = ExpenseCategory.GROCERY)
        )
    }

    // New item adder for multi-mode
    var multiNewName by remember { mutableStateOf("") }
    var multiNewQtyText by remember { mutableStateOf("1") }
    var multiNewPriceText by remember { mutableStateOf("") }
    var multiNewCat by remember { mutableStateOf(ExpenseCategory.GROCERY) }

    val singleQuantity = quantityText.toDoubleOrNull() ?: 1.0
    val singlePrice = pricePerUnitText.toDoubleOrNull() ?: 0.0
    val singleCalculatedTotal = singleQuantity * singlePrice

    val multiTotal = multiItems.sumOf { it.totalPrice }

    val quickSuggestions = listOf(
        "Milk" to ExpenseCategory.MILK_DAIRY,
        "Bread" to ExpenseCategory.GROCERY,
        "Eggs" to ExpenseCategory.GROCERY,
        "Vegetables" to ExpenseCategory.VEGETABLES,
        "Chai & Snacks" to ExpenseCategory.FOOD,
        "Petrol" to ExpenseCategory.TRANSPORT,
        "Notebook" to ExpenseCategory.EDUCATION,
        "Pen" to ExpenseCategory.EDUCATION,
        "Medicine" to ExpenseCategory.HEALTH,
        "Recharge" to ExpenseCategory.BILLS
    )

    fun showDatePicker() {
        val dpd = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
        dpd.show()
    }

    ModalBottomSheet(
        onDismissRequest = { dismissSafely() },
        sheetState = sheetState,
        dragHandle = null,
        modifier = Modifier.testTag("add_expense_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Add Expense / खर्च जोड़ें",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Quickly record your daily purchase",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { dismissSafely() },
                    modifier = Modifier.testTag("close_add_expense_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mode Selector: Quick Single vs Multi-Item Transaction
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = entryMode == 0,
                    onClick = { entryMode = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { Icon(Icons.Default.FlashOn, contentDescription = null) },
                    modifier = Modifier.testTag("mode_single_button")
                ) {
                    Text("Single Item")
                }
                SegmentedButton(
                    selected = entryMode == 1,
                    onClick = { entryMode = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                    modifier = Modifier.testTag("mode_multi_button")
                ) {
                    Text("Multi-Item Basket (${multiItems.size})")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Date and Payment Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Date Picker Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker() }
                        .testTag("date_picker_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Formatters.shortDateFormatter.format(selectedDate),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Payment Method Chips Scroll
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1.2f)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PaymentMethod.values().forEach { method ->
                            val selected = selectedPayment == method
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    )
                                    .clickable { selectedPayment = method }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = method.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (entryMode == 0) {
                // ================= SINGLE ITEM MODE =================

                // Quick item suggestions
                Text(
                    text = "Quick Suggestions:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickSuggestions.forEach { (item, cat) ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (productName.equals(item, ignoreCase = true))
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                productName = item
                                selectedCategory = cat
                            }
                        ) {
                            Text(
                                text = item,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Product Name Field
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product / Item Name *") },
                    placeholder = { Text("e.g. Milk, Bread, Notebook") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quantity & Price Stepper/Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quantity
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity") },
                        placeholder = { Text("1") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quantity_input")
                    )

                    // Price Per Unit
                    OutlinedTextField(
                        value = pricePerUnitText,
                        onValueChange = { pricePerUnitText = it },
                        label = { Text("Price per Item (₹) *") },
                        placeholder = { Text("e.g. 60") },
                        singleLine = true,
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("price_input")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Calculation Card (Calculator Feature)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (singleCalculatedTotal > 0)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Auto Calculated Total:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${Formatters.formatQuantity(singleQuantity)} × ₹${if (pricePerUnitText.isNotBlank()) pricePerUnitText else "0"} = ${Formatters.formatRupee(singleCalculatedTotal)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = Formatters.formatRupee(singleCalculatedTotal),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selector
                Text(
                    text = "Category (वर्ग):",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpenseCategory.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text("${cat.displayName} (${cat.hindiName})") },
                            leadingIcon = {
                                CategoryIconBox(category = cat, size = 24.dp, iconSize = 14.dp)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Note
                OutlinedTextField(
                    value = generalNote,
                    onValueChange = { generalNote = it },
                    label = { Text("Note / Remark (Optional)") },
                    placeholder = { Text("e.g. 2L packet from Sharma store") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_input")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Save Single Button
                Button(
                    onClick = {
                        if (productName.isNotBlank() && singlePrice > 0) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onSaveSingle(
                                productName.trim(),
                                singleQuantity,
                                singlePrice,
                                selectedDate,
                                selectedCategory,
                                selectedPayment,
                                generalNote
                            )
                        }
                    },
                    enabled = productName.isNotBlank() && singlePrice > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_single_expense_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text(
                        text = "Save Purchase • ${Formatters.formatRupee(singleCalculatedTotal)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

            } else {
                // ================= MULTI-ITEM TRANSACTION MODE =================

                Text(
                    text = "Transaction Items (${multiItems.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // List of items in current basket
                multiItems.forEachIndexed { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                CategoryIconBox(category = item.category, size = 32.dp, iconSize = 18.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = item.productName,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${Formatters.formatQuantity(item.quantity)} × ₹${item.pricePerUnit} = ${Formatters.formatRupee(item.totalPrice)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { multiItems.removeAt(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Add Form for Multi-Items
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "+ Add Item to Transaction:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = multiNewName,
                            onValueChange = { multiNewName = it },
                            label = { Text("Item Name") },
                            placeholder = { Text("e.g. Eggs") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = multiNewQtyText,
                                onValueChange = { multiNewQtyText = it },
                                label = { Text("Qty") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = multiNewPriceText,
                                onValueChange = { multiNewPriceText = it },
                                label = { Text("Price/Unit (₹)") },
                                singleLine = true,
                                prefix = { Text("₹") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1.2f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category selector for multi-item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ExpenseCategory.values().forEach { cat ->
                                FilterChip(
                                    selected = multiNewCat == cat,
                                    onClick = { multiNewCat = cat },
                                    label = { Text(cat.displayName, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                val qty = multiNewQtyText.toDoubleOrNull() ?: 1.0
                                val price = multiNewPriceText.toDoubleOrNull() ?: 0.0
                                if (multiNewName.isNotBlank() && price > 0) {
                                    multiItems.add(
                                        CartItem(
                                            productName = multiNewName.trim(),
                                            quantity = qty,
                                            pricePerUnit = price,
                                            category = multiNewCat
                                        )
                                    )
                                    multiNewName = ""
                                    multiNewQtyText = "1"
                                    multiNewPriceText = ""
                                }
                            },
                            enabled = multiNewName.isNotBlank() && (multiNewPriceText.toDoubleOrNull() ?: 0.0) > 0,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Item to Basket")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Optional Note
                OutlinedTextField(
                    value = generalNote,
                    onValueChange = { generalNote = it },
                    label = { Text("Transaction Note / Vendor (Optional)") },
                    placeholder = { Text("e.g. Weekly Mart Grocery Run") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Transaction Total Summary & Save
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Transaction Total (${multiItems.size} items):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = Formatters.formatRupee(multiTotal),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (multiItems.isNotEmpty()) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onSaveBatch(multiItems.toList(), selectedDate, selectedPayment, generalNote)
                        }
                    },
                    enabled = multiItems.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_batch_expenses_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text(
                        text = "Save All ${multiItems.size} Items • ${Formatters.formatRupee(multiTotal)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
