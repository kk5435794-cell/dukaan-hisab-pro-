package com.example.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.model.CustomerTransactionType
import com.example.core.model.InvoiceStatus
import com.example.core.model.PaymentMode
import com.example.core.model.StockMovementType
import com.example.core.model.SupplierTransactionType

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey val id: Int = 1,
    val businessName: String = "मेरी दुकान (Dukaan Hisab)",
    val businessType: String = "किराना व जनरल स्टोर",
    val ownerName: String = "",
    val phone: String = "",
    val address: String = "",
    val defaultInvoicePrefix: String = "INV-",
    val language: String = "hi",
    val openingCashPaise: Long = 0L,
    val isSetupCompleted: Boolean = false
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val openingBalancePaise: Long = 0L,
    val category: String = "Regular",
    val createdTimestamp: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

@Entity(tableName = "customer_transactions")
data class CustomerTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val type: CustomerTransactionType,
    val amountPaise: Long,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val invoiceId: Long? = null,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val isReversed: Boolean = false,
    val reversalReason: String = ""
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val openingBalancePaise: Long = 0L,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

@Entity(tableName = "supplier_transactions")
data class SupplierTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val type: SupplierTransactionType,
    val amountPaise: Long,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val purchaseId: Long? = null,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val isReversed: Boolean = false,
    val reversalReason: String = ""
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val skuOrBarcode: String = "",
    val category: String = "General",
    val brand: String = "",
    val costPricePaise: Long = 0L,
    val sellingPricePaise: Long = 0L,
    val openingStock: Double = 0.0,
    val minStockThreshold: Double = 5.0,
    val unit: String = "Pcs",
    val supplierId: Long? = null,
    val description: String = "",
    val isArchived: Boolean = false
)

@Entity(tableName = "stock_movements")
data class StockMovement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val type: StockMovementType,
    val quantity: Double,
    val reason: String = "",
    val dateTimestamp: Long = System.currentTimeMillis(),
    val referenceId: String = ""
)

@Entity(tableName = "sales_invoices")
data class SalesInvoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "नकद ग्राहक (Cash Walk-in)",
    val dateTimestamp: Long = System.currentTimeMillis(),
    val subtotalPaise: Long,
    val discountPaise: Long = 0L,
    val grandTotalPaise: Long,
    val amountPaidPaise: Long,
    val balanceDuePaise: Long,
    val paymentMode: PaymentMode,
    val status: InvoiceStatus,
    val notes: String = ""
)

@Entity(tableName = "sales_invoice_items")
data class SalesInvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unit: String,
    val unitPricePaise: Long,
    val discountPaise: Long = 0L,
    val lineTotalPaise: Long
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val amountPaise: Long,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val payee: String = "",
    val note: String = "",
    val isRecurring: Boolean = false
)

@Entity(tableName = "cashbook_entries")
data class CashbookEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isInflow: Boolean,
    val category: String,
    val amountPaise: Long,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val description: String,
    val referenceType: String = "",
    val referenceId: Long? = null
)
