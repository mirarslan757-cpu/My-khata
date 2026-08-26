package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class ExpenseCategory(val displayName: String, val hindiName: String, val iconName: String) {
    GROCERY("Grocery", "किराना", "ShoppingCart"),
    FOOD("Food & Dining", "खाना", "Restaurant"),
    VEGETABLES("Vegetables & Fruits", "सब्जी-फल", "Eco"),
    MILK_DAIRY("Milk & Dairy", "दूध-डेयरी", "LocalDrink"),
    TRANSPORT("Transport & Fuel", "यातायात", "DirectionsCar"),
    EDUCATION("Education & Books", "शिक्षा", "MenuBook"),
    BILLS("Bills & Utilities", "बिल", "Receipt"),
    HEALTH("Health & Medicine", "दवा", "LocalHospital"),
    SHOPPING("Shopping", "खरीदारी", "ShoppingBag"),
    OTHER("Other", "अन्य", "Category");

    companion object {
        fun fromString(value: String): ExpenseCategory {
            return try {
                valueOf(value)
            } catch (e: Exception) {
                OTHER
            }
        }
    }
}

enum class PaymentMethod(val displayName: String) {
    CASH("Cash (नकद)"),
    UPI("UPI (GPay/PhonePe/Paytm)"),
    CARD("Card (कार्ड)"),
    OTHER("Other (अन्य)");

    companion object {
        fun fromString(value: String): PaymentMethod {
            return try {
                valueOf(value)
            } catch (e: Exception) {
                CASH
            }
        }
    }
}

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,
    val quantity: Double,
    val pricePerUnit: Double,
    val totalPrice: Double,
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val category: String = ExpenseCategory.OTHER.name,
    val paymentMethod: String = PaymentMethod.CASH.name,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val localDate: LocalDate
        get() = LocalDate.ofEpochDay(dateEpochDay)
}
