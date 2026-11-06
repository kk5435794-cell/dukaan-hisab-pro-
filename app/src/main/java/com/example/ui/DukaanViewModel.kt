package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.engine.AccountingEngine
import com.example.core.model.CustomerTransactionType
import com.example.core.model.PaymentMode
import com.example.core.model.SupplierTransactionType
import com.example.database.AppDatabase
import com.example.database.entity.BusinessProfile
import com.example.database.entity.CustomerTransaction
import com.example.database.entity.Expense
import com.example.database.entity.Product
import com.example.database.entity.SalesInvoice
import com.example.database.entity.SupplierTransaction
import com.example.repository.DukaanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DukaanViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = DukaanRepository(database)

    // Language state (default Hindi true as per primary language specification)
    private val _isHindi = MutableStateFlow(true)
    val isHindi: StateFlow<Boolean> = _isHindi.asStateFlow()

    init {
        val prefs = application.getSharedPreferences("dukaan_app_prefs", android.content.Context.MODE_PRIVATE)
        val hasClearedInitialEntries = prefs.getBoolean("has_cleared_initial_entries_v3", false)
        if (!hasClearedInitialEntries) {
            viewModelScope.launch {
                repository.clearAllData()
                prefs.edit().putBoolean("has_cleared_initial_entries_v3", true).apply()
            }
        }
    }

    fun toggleLanguage() {
        _isHindi.value = !_isHindi.value
    }

    // Business Profile
    val profile = repository.businessProfile.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    // Customers
    val customersWithBalances = repository.getCustomersWithBalances().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Suppliers
    val suppliersWithBalances = repository.getSuppliersWithBalances().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Products & Stock
    val productsWithStock = repository.getProductsWithStock().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Invoices
    val invoices = repository.allInvoices.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Expenses
    val expenses = repository.allExpenses.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Purchases
    val purchases = repository.allPurchases.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Cashbook
    val cashbookEntries = repository.allCashbookEntries.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // POS Cart State
    private val _cart = MutableStateFlow<List<DukaanRepository.CartItem>>(emptyList())
    val cart: StateFlow<List<DukaanRepository.CartItem>> = _cart.asStateFlow()

    private val _selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedCustomerId: StateFlow<Long?> = _selectedCustomerId.asStateFlow()

    private val _posCustomerName = MutableStateFlow("")
    val posCustomerName: StateFlow<String> = _posCustomerName.asStateFlow()

    private val _posDiscountPaise = MutableStateFlow(0L)
    val posDiscountPaise: StateFlow<Long> = _posDiscountPaise.asStateFlow()

    private val _posAmountPaidPaise = MutableStateFlow(0L)
    val posAmountPaidPaise: StateFlow<Long> = _posAmountPaidPaise.asStateFlow()

    private val _posPaymentMode = MutableStateFlow(PaymentMode.CASH)
    val posPaymentMode: StateFlow<PaymentMode> = _posPaymentMode.asStateFlow()

    // -------------------------------------------------------------------------
    // DASHBOARD AGGREGATED METRICS
    // -------------------------------------------------------------------------
    data class DashboardStats(
        val totalReceivablesPaise: Long, // Total Udhaar to collect
        val totalPayablesPaise: Long,    // Total owed to suppliers
        val todaySalesPaise: Long,       // Sales made today
        val cashBalancePaise: Long,      // Cash on hand
        val lowStockCount: Int,
        val totalCustomersCount: Int,
        val totalProductsCount: Int
    )

    val dashboardStats: StateFlow<DashboardStats> = combine(
        customersWithBalances,
        suppliersWithBalances,
        invoices,
        cashbookEntries,
        productsWithStock
    ) { customers, suppliers, allInvoices, cashEntries, products ->
        val totalReceivables = customers.sumOf { if (it.netBalancePaise > 0) it.netBalancePaise else 0L }
        val totalPayables = suppliers.sumOf { if (it.netPayablePaise > 0) it.netPayablePaise else 0L }

        // Today's start of day timestamp
        val startOfToday = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todaySales = allInvoices
            .filter { it.dateTimestamp >= startOfToday }
            .sumOf { it.grandTotalPaise }

        val cashTxList = cashEntries.map { AccountingEngine.CashTx(it.isInflow, it.amountPaise) }
        val currentCash = AccountingEngine.calculateCashBalance(0L, cashTxList)
        val lowStock = products.count { it.currentStock <= it.product.minStockThreshold }

        DashboardStats(
            totalReceivablesPaise = totalReceivables,
            totalPayablesPaise = totalPayables,
            todaySalesPaise = todaySales,
            cashBalancePaise = currentCash,
            lowStockCount = lowStock,
            totalCustomersCount = customers.size,
            totalProductsCount = products.size
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardStats(0, 0, 0, 0, 0, 0, 0)
    )

    // -------------------------------------------------------------------------
    // ACTIONS
    // -------------------------------------------------------------------------
    fun saveBusinessProfile(
        name: String,
        type: String,
        owner: String,
        phone: String,
        address: String,
        openingCashPaise: Long
    ) {
        viewModelScope.launch {
            repository.saveProfile(
                BusinessProfile(
                    id = 1,
                    businessName = name,
                    businessType = type,
                    ownerName = owner,
                    phone = phone,
                    address = address,
                    openingCashPaise = openingCashPaise,
                    isSetupCompleted = true
                )
            )
        }
    }

    fun addCustomer(
        name: String,
        phone: String,
        address: String,
        notes: String,
        openingBalancePaise: Long
    ) {
        viewModelScope.launch {
            repository.addCustomer(name, phone, address, notes, openingBalancePaise)
        }
    }

    fun addCustomerTransaction(
        customerId: Long,
        type: CustomerTransactionType,
        amountPaise: Long,
        paymentMode: PaymentMode,
        note: String
    ) {
        viewModelScope.launch {
            repository.addCustomerTransaction(customerId, type, amountPaise, paymentMode, note)
        }
    }

    fun reverseCustomerTransaction(transaction: CustomerTransaction, reason: String) {
        viewModelScope.launch {
            repository.reverseCustomerTransaction(transaction, reason)
        }
    }

    fun addSupplier(name: String, phone: String, address: String, notes: String, openingBalance: Long) {
        viewModelScope.launch {
            repository.addSupplier(name, phone, address, notes, openingBalance)
        }
    }

    fun addSupplierTransaction(supplierId: Long, type: SupplierTransactionType, amountPaise: Long, paymentMode: PaymentMode, note: String) {
        viewModelScope.launch {
            repository.addSupplierTransaction(supplierId, type, amountPaise, paymentMode, note)
        }
    }

    fun addProduct(name: String, sku: String, category: String, costPaise: Long, sellingPaise: Long, openingStock: Double, minThreshold: Double, unit: String) {
        viewModelScope.launch {
            repository.addProduct(name, sku, category, costPaise, sellingPaise, openingStock, minThreshold, unit)
        }
    }

    fun adjustStock(productId: Long, adjustmentQty: Double, reason: String) {
        viewModelScope.launch {
            repository.adjustStock(productId, adjustmentQty, reason)
        }
    }

    // Cart Management
    fun addToCart(product: Product) {
        val existing = _cart.value.find { it.product.id == product.id }
        if (existing != null) {
            _cart.value = _cart.value.map {
                if (it.product.id == product.id) it.copy(quantity = it.quantity + 1.0) else it
            }
        } else {
            _cart.value = _cart.value + DukaanRepository.CartItem(
                product = product,
                quantity = 1.0,
                unitPricePaise = product.sellingPricePaise
            )
        }
    }

    fun updateCartItemQuantity(productId: Long, newQty: Double) {
        if (newQty <= 0) {
            _cart.value = _cart.value.filter { it.product.id != productId }
        } else {
            _cart.value = _cart.value.map {
                if (it.product.id == productId) it.copy(quantity = newQty) else it
            }
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _selectedCustomerId.value = null
        _posCustomerName.value = ""
        _posDiscountPaise.value = 0L
        _posAmountPaidPaise.value = 0L
    }

    fun selectPosCustomer(customerId: Long?, customerName: String) {
        _selectedCustomerId.value = customerId
        _posCustomerName.value = customerName
    }

    fun setPosDiscount(discountPaise: Long) {
        _posDiscountPaise.value = discountPaise
    }

    fun setPosAmountPaid(paidPaise: Long) {
        _posAmountPaidPaise.value = paidPaise
    }

    fun setPosPaymentMode(mode: PaymentMode) {
        _posPaymentMode.value = mode
    }

    fun completeSale(onSuccess: (Long) -> Unit) {
        val currentItems = _cart.value
        if (currentItems.isEmpty()) return

        val custName = _posCustomerName.value.ifBlank { "नकद ग्राहक (Cash Walk-in)" }

        viewModelScope.launch {
            val invoiceId = repository.postSale(
                customerId = _selectedCustomerId.value,
                customerName = custName,
                items = currentItems,
                invoiceDiscountPaise = _posDiscountPaise.value,
                amountPaidPaise = _posAmountPaidPaise.value,
                paymentMode = _posPaymentMode.value
            )
            clearCart()
            onSuccess(invoiceId)
        }
    }

    fun addExpense(category: String, amountPaise: Long, paymentMode: PaymentMode, payee: String, note: String) {
        viewModelScope.launch {
            repository.addExpense(category, amountPaise, paymentMode, payee, note)
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpense(id)
        }
    }

    fun addCashEntry(isInflow: Boolean, category: String, amountPaise: Long, description: String) {
        viewModelScope.launch {
            repository.addManualCashEntry(isInflow, category, amountPaise, description)
        }
    }

    fun deleteCashEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteCashEntry(id)
        }
    }

    fun addPurchase(
        supplierName: String,
        supplierPhone: String,
        invoiceNumber: String,
        totalPaise: Long,
        amountPaidPaise: Long,
        paymentMode: PaymentMode,
        note: String
    ) {
        viewModelScope.launch {
            repository.addPurchaseInvoice(
                supplierName = supplierName,
                supplierPhone = supplierPhone,
                invoiceNumber = invoiceNumber,
                totalPaise = totalPaise,
                amountPaidPaise = amountPaidPaise,
                paymentMode = paymentMode,
                note = note
            )
        }
    }

    fun deletePurchase(id: Long) {
        viewModelScope.launch {
            repository.deletePurchase(id)
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedDemoData()
        }
    }
}
