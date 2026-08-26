package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpenseCategory
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {
    private val currencyFormatter = DecimalFormat("#,##,##0.##")
    private val integerFormatter = DecimalFormat("#,##,##0")
    val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
    val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
    val monthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

    fun formatRupee(amount: Double): String {
        return "₹${currencyFormatter.format(amount)}"
    }

    fun formatQuantity(quantity: Double): String {
        return if (quantity % 1.0 == 0.0) {
            quantity.toInt().toString()
        } else {
            String.format(Locale.ENGLISH, "%.1f", quantity)
        }
    }

    fun formatFriendlyDate(date: LocalDate): String {
        val today = LocalDate.now()
        return when {
            date == today -> "Today, ${date.format(displayDateFormatter)}"
            date == today.minusDays(1) -> "Yesterday, ${date.format(displayDateFormatter)}"
            date.year == today.year -> date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH))
            else -> date.format(displayDateFormatter)
        }
    }

    fun formatKhataHeaderDate(date: LocalDate): String {
        val today = LocalDate.now()
        return when {
            date == today -> "Today (${date.format(DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH))})"
            date == today.minusDays(1) -> "Yesterday (${date.format(DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH))})"
            else -> date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH))
        }
    }
}

@Composable
fun CategoryIconBox(
    category: ExpenseCategory,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    val (icon, bgColor, iconColor) = getCategoryVisuals(category)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.displayName,
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

fun getCategoryVisuals(category: ExpenseCategory): Triple<ImageVector, Color, Color> {
    return when (category) {
        ExpenseCategory.GROCERY -> Triple(Icons.Default.ShoppingCart, Color(0xFFE0F2FE), Color(0xFF0369A1))
        ExpenseCategory.FOOD -> Triple(Icons.Default.Restaurant, Color(0xFFFEF3C7), Color(0xFFB45309))
        ExpenseCategory.VEGETABLES -> Triple(Icons.Default.Eco, Color(0xFFDCFCE7), Color(0xFF15803D))
        ExpenseCategory.MILK_DAIRY -> Triple(Icons.Default.LocalDrink, Color(0xFFEFF6FF), Color(0xFF1D4ED8))
        ExpenseCategory.TRANSPORT -> Triple(Icons.Default.DirectionsCar, Color(0xFFEDE9FE), Color(0xFF6D28D9))
        ExpenseCategory.EDUCATION -> Triple(Icons.Default.MenuBook, Color(0xFFFCE7F3), Color(0xFFBE185D))
        ExpenseCategory.BILLS -> Triple(Icons.Default.Receipt, Color(0xFFFFEDD5), Color(0xFFC2410C))
        ExpenseCategory.HEALTH -> Triple(Icons.Default.LocalHospital, Color(0xFFFFE4E6), Color(0xFFE11D48))
        ExpenseCategory.SHOPPING -> Triple(Icons.Default.ShoppingBag, Color(0xFFF3E8FF), Color(0xFF7E22CE))
        ExpenseCategory.OTHER -> Triple(Icons.Default.Category, Color(0xFFF1F5F9), Color(0xFF475569))
    }
}
