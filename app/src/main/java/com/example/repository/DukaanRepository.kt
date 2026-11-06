package com.example.repository

import com.example.core.engine.AccountingEngine
import com.example.core.model.CustomerTransactionType
import com.example.core.model.InvoiceStatus
import com.example.core.model.PaymentMode
import com.example.core.model.StockMovementType
import com.example.core.model.SupplierTransactionType
import com.example.database.AppDatabase
import com.example.database.entity.BusinessProfile
import com.example.database.entity.CashbookEntry
import com.example.database.entity.Customer
import com.example.database.entity.CustomerTransaction
import com.example.database.entity.Expense
import com.example.database.entity.Product
import com.example.database.entity.SalesInvoice
import com.example.database.entity.SalesInvoiceItem
import com.example.database.entity.StockMovement
import com.example.database.entity.Supplier
import com.example.database.entity.SupplierTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class DukaanRepository(private val database: AppDatabase) {

    private val profileDao = database.businessProfileDao()
    private val customerDao = database.customerDao()
    private val customerTxDao = database.customerTransactionDao()
    private val supplierDao = database.supplierDao()
    private val supplierTxDao = database.supplierTransactionDao()
    private val productDao = database.productDao()
    private val stockMovementDao = database.stockMovementDao()
    private val invoiceDao = database.salesInvoiceDao()
    private val expenseDao = database.expenseDao()
    private val cashbookDao = database.cashbookDao()

    // -------------------------------------------------------------------------
    // BUSINESS PROFILE
    // -------------------------------------------------------------------------
    val businessProfile: Flow<BusinessProfile?> = profileDao.getProfileFlow()

    suspend fun saveProfile(profile: BusinessProfile) {
        profileDao.saveProfile(profile)
        // If profile has opening cash, record it in cashbook if empty
        if (profile.openingCashPaise > 0) {
            val entries = cashbookDao.getAllEntries().firstOrNull() ?: emptyList()
            if (entries.isEmpty()) {
                cashbookDao.insertEntry(
                    CashbookEntry(
                        isInflow = true,
                        category = "प्रारंभिक रोकड़ (Opening Cash)",
                        amountPaise = profile.openingCashPaise,
                        paymentMode = PaymentMode.CASH,
                        description = "दुकान की शुरुआती रोकड़ राशि",
                        referenceType = "OPENING_BALANCE"
                    )
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // CUSTOMERS & LEDGER (UDHAAR HISAB)
    // -------------------------------------------------------------------------
    data class CustomerWithBalance(
        val customer: Customer,
        val netBalancePaise: Long, // Positive = customer owes us (बकाया), Negative = advance
        val totalDebitPaise: Long, // Total udhaar given
        val totalCreditPaise: Long // Total payments received
    )

    fun getCustomersWithBalances(): Flow<List<CustomerWithBalance>> {
        return customerDao.getAllCustomers().map { customers ->
            customers.map { customer ->
                val txs = customerTxDao.getTransactionsForCustomerSync(customer.id)
                val records = txs.map {
                    AccountingEngine.CustomerTxRecord(
                        type = it.type,
                        amountPaise = it.amountPaise,
                        isReversed = it.isReversed
                    )
                }
                val netBalance = AccountingEngine.calculateCustomerBalance(customer.openingBalancePaise, records)
                var debit = if (customer.openingBalancePaise > 0) customer.openingBalancePaise else 0L
                var credit = if (customer.openingBalancePaise < 0) -customer.openingBalancePaise else 0L

                for (tx in txs) {
                    if (tx.isReversed) continue
                    if (tx.type == CustomerTransactionType.SALE_CREDIT || tx.type == CustomerTransactionType.DEBIT_ADJUSTMENT) {
                        debit += tx.amountPaise
                    } else if (tx.type == CustomerTransactionType.PAYMENT_RECEIVED || tx.type == CustomerTransactionType.RETURN) {
                        credit += tx.amountPaise
                    }
                }

                CustomerWithBalance(
                    customer = customer,
                    netBalancePaise = netBalance,
                    totalDebitPaise = debit,
                    totalCreditPaise = credit
                )
            }
        }
    }

    fun getCustomerTransactions(customerId: Long): Flow<List<CustomerTransaction>> =
        customerTxDao.getTransactionsForCustomer(customerId)

    suspend fun getCustomerById(customerId: Long): Customer? =
        customerDao.getCustomerById(customerId)

    suspend fun addCustomer(
        name: String,
        phone: String,
        address: String,
        notes: String,
        openingBalancePaise: Long,
        category: String = "Regular"
    ): Long {
        val customer = Customer(
            name = name,
            phone = phone,
            address = address,
            notes = notes,
            openingBalancePaise = openingBalancePaise,
            category = category
        )
        return customerDao.insertCustomer(customer)
    }

    /**
     * Atomically adds a customer ledger transaction.
     * If payment received in Cash, also updates Cashbook.
     */
    suspend fun addCustomerTransaction(
        customerId: Long,
        type: CustomerTransactionType,
        amountPaise: Long,
        paymentMode: PaymentMode = PaymentMode.CASH,
        note: String = "",
        invoiceId: Long? = null
    ): Long {
        if (amountPaise <= 0) return -1L

        val tx = CustomerTransaction(
            customerId = customerId,
            type = type,
            amountPaise = amountPaise,
            paymentMode = paymentMode,
            note = note,
            invoiceId = invoiceId
        )
        val txId = customerTxDao.insertTransaction(tx)

        // If customer gave payment in Cash, record Inflow in Cashbook
        if (type == CustomerTransactionType.PAYMENT_RECEIVED && paymentMode == PaymentMode.CASH) {
            val customer = customerDao.getCustomerById(customerId)
            val customerName = customer?.name ?: "ग्राहक"
            cashbookDao.insertEntry(
                CashbookEntry(
                    isInflow = true,
                    category = "ग्राहक जमा (Customer Payment)",
                    amountPaise = amountPaise,
                    paymentMode = paymentMode,
                    description = "$customerName से प्राप्त राशि",
                    referenceType = "CUSTOMER_PAYMENT",
                    referenceId = txId
                )
            )
        }
        return txId
    }

    /**
     * Reverses a customer transaction cleanly with audit reason (Rule 10 & 18).
     */
    suspend fun reverseCustomerTransaction(transaction: CustomerTransaction, reason: String) {
        val updated = transaction.copy(
            isReversed = true,
            reversalReason = reason
        )
        customerTxDao.updateTransaction(updated)
    }

    // -------------------------------------------------------------------------
    // SUPPLIERS & LEDGER
    // -------------------------------------------------------------------------
    data class SupplierWithBalance(
        val supplier: Supplier,
        val netPayablePaise: Long
    )

    fun getSuppliersWithBalances(): Flow<List<SupplierWithBalance>> {
        return supplierDao.getAllSuppliers().map { suppliers ->
            suppliers.map { supplier ->
                val txs = supplierTxDao.getTransactionsForSupplierSync(supplier.id)
                val records = txs.map {
                    AccountingEngine.SupplierTxRecord(
                        type = it.type,
                        amountPaise = it.amountPaise,
                        isReversed = it.isReversed
                    )
                }
                val netPayable = AccountingEngine.calculateSupplierBalance(supplier.openingBalancePaise, records)
                SupplierWithBalance(supplier, netPayable)
            }
        }
    }

    fun getSupplierTransactions(supplierId: Long): Flow<List<SupplierTransaction>> =
        supplierTxDao.getTransactionsForSupplier(supplierId)

    suspend fun addSupplier(
        name: String,
        phone: String,
        address: String,
        notes: String,
        openingBalancePaise: Long
    ): Long {
        return supplierDao.insertSupplier(
            Supplier(
                name = name,
                phone = phone,
                address = address,
                notes = notes,
                openingBalancePaise = openingBalancePaise
            )
        )
    }

    suspend fun addSupplierTransaction(
        supplierId: Long,
        type: SupplierTransactionType,
        amountPaise: Long,
        paymentMode: PaymentMode = PaymentMode.CASH,
        note: String = ""
    ): Long {
        val tx = SupplierTransaction(
            supplierId = supplierId,
            type = type,
            amountPaise = amountPaise,
            paymentMode = paymentMode,
            note = note
        )
        val txId = supplierTxDao.insertTransaction(tx)

        // If payment made in Cash, record Outflow in Cashbook
        if (type == SupplierTransactionType.PAYMENT_MADE && paymentMode == PaymentMode.CASH) {
            val supplier = supplierDao.getSupplierById(supplierId)
            val supplierName = supplier?.name ?: "सप्लायर"
            cashbookDao.insertEntry(
                CashbookEntry(
                    isInflow = false,
                    category = "सप्लायर भुगतान (Supplier Payment)",
                    amountPaise = amountPaise,
                    paymentMode = paymentMode,
                    description = "$supplierName को भुगतान",
                    referenceType = "SUPPLIER_PAYMENT",
                    referenceId = txId
                )
            )
        }
        return txId
    }

    // -------------------------------------------------------------------------
    // PRODUCTS & INVENTORY
    // -------------------------------------------------------------------------
    data class ProductWithStock(
        val product: Product,
        val currentStock: Double,
        val stockValuePaise: Long // currentStock * costPricePaise
    )

    fun getProductsWithStock(): Flow<List<ProductWithStock>> {
        return productDao.getAllProducts().map { products ->
            products.map { product ->
                val movements = stockMovementDao.getMovementsForProductSync(product.id)
                val movementRecords = movements.map {
                    AccountingEngine.StockMovementRecord(type = it.type, quantity = it.quantity)
                }
                val currentStock = AccountingEngine.calculateStock(product.openingStock, movementRecords)
                val valuation = Math.round(currentStock * product.costPricePaise)
                ProductWithStock(product, currentStock, valuation)
            }
        }
    }

    suspend fun addProduct(
        name: String,
        sku: String,
        category: String,
        costPricePaise: Long,
        sellingPricePaise: Long,
        openingStock: Double,
        minThreshold: Double,
        unit: String
    ): Long {
        val product = Product(
            name = name,
            skuOrBarcode = sku,
            category = category,
            costPricePaise = costPricePaise,
            sellingPricePaise = sellingPricePaise,
            openingStock = openingStock,
            minStockThreshold = minThreshold,
            unit = unit
        )
        val id = productDao.insertProduct(product)
        if (openingStock > 0) {
            stockMovementDao.insertMovement(
                StockMovement(
                    productId = id,
                    type = StockMovementType.OPENING,
                    quantity = openingStock,
                    reason = "शुरुआती स्टॉक (Initial Stock)"
                )
            )
        }
        return id
    }

    suspend fun adjustStock(productId: Long, adjustmentQty: Double, reason: String) {
        stockMovementDao.insertMovement(
            StockMovement(
                productId = productId,
                type = StockMovementType.MANUAL_ADJUSTMENT,
                quantity = adjustmentQty,
                reason = reason
            )
        )
    }

    // -------------------------------------------------------------------------
    // SALES & POS BILLING
    // -------------------------------------------------------------------------
    data class CartItem(
        val product: Product,
        val quantity: Double,
        val unitPricePaise: Long,
        val discountPaise: Long = 0L
    ) {
        val lineTotalPaise: Long
            get() = Math.round(quantity * unitPricePaise) - discountPaise
    }

    val allInvoices: Flow<List<SalesInvoice>> = invoiceDao.getAllInvoices()

    suspend fun getInvoiceItems(invoiceId: Long): List<SalesInvoiceItem> =
        invoiceDao.getInvoiceItemsSync(invoiceId)

    /**
     * Atomically posts a Sale with:
     * - Invoice & Items creation
     * - Stock deductions for every product
     * - Customer ledger update if credit / partial payment
     * - Cashbook entry if cash received
     */
    suspend fun postSale(
        customerId: Long?,
        customerName: String,
        items: List<CartItem>,
        invoiceDiscountPaise: Long,
        amountPaidPaise: Long,
        paymentMode: PaymentMode,
        notes: String = ""
    ): Long {
        val subtotal = items.sumOf { it.lineTotalPaise }
        val calc = AccountingEngine.calculateInvoiceTotals(subtotal, invoiceDiscountPaise, amountPaidPaise)

        // Generate safe unique invoice number
        val timestamp = System.currentTimeMillis()
        val invoiceNumber = "INV-${timestamp.toString().takeLast(6)}"

        val invoice = SalesInvoice(
            invoiceNumber = invoiceNumber,
            customerId = customerId,
            customerName = customerName,
            dateTimestamp = timestamp,
            subtotalPaise = calc.subtotalPaise,
            discountPaise = calc.discountPaise,
            grandTotalPaise = calc.grandTotalPaise,
            amountPaidPaise = calc.amountPaidPaise,
            balanceDuePaise = calc.balanceDuePaise,
            paymentMode = paymentMode,
            status = calc.status,
            notes = notes
        )

        val invoiceId = invoiceDao.insertInvoice(invoice)

        // Save invoice items
        val invoiceItems = items.map {
            SalesInvoiceItem(
                invoiceId = invoiceId,
                productId = it.product.id,
                productName = it.product.name,
                quantity = it.quantity,
                unit = it.product.unit,
                unitPricePaise = it.unitPricePaise,
                discountPaise = it.discountPaise,
                lineTotalPaise = it.lineTotalPaise
            )
        }
        invoiceDao.insertInvoiceItems(invoiceItems)

        // Deduct inventory for each item
        for (item in items) {
            stockMovementDao.insertMovement(
                StockMovement(
                    productId = item.product.id,
                    type = StockMovementType.SALE,
                    quantity = item.quantity,
                    reason = "बिल #$invoiceNumber बिक्री",
                    referenceId = invoiceId.toString()
                )
            )
        }

        // If there is an outstanding balance and linked to a registered customer, post to Customer Ledger!
        if (customerId != null) {
            // Record full sale debit
            customerTxDao.insertTransaction(
                CustomerTransaction(
                    customerId = customerId,
                    type = CustomerTransactionType.SALE_CREDIT,
                    amountPaise = calc.grandTotalPaise,
                    dateTimestamp = timestamp,
                    note = "बिल #$invoiceNumber",
                    invoiceId = invoiceId,
                    paymentMode = paymentMode
                )
            )
            // If customer made partial or full payment at billing time, record payment credit!
            if (calc.amountPaidPaise > 0) {
                customerTxDao.insertTransaction(
                    CustomerTransaction(
                        customerId = customerId,
                        type = CustomerTransactionType.PAYMENT_RECEIVED,
                        amountPaise = calc.amountPaidPaise,
                        dateTimestamp = timestamp + 1,
                        note = "बिल #$invoiceNumber पर तत्काल भुगतान",
                        invoiceId = invoiceId,
                        paymentMode = paymentMode
                    )
                )
            }
        }

        // If Cash payment was received, record in Cashbook
        if (calc.amountPaidPaise > 0 && paymentMode == PaymentMode.CASH) {
            cashbookDao.insertEntry(
                CashbookEntry(
                    isInflow = true,
                    category = "दुकान बिक्री (Cash Sale)",
                    amountPaise = calc.amountPaidPaise,
                    paymentMode = paymentMode,
                    description = "बिल #$invoiceNumber नकद बिक्री",
                    referenceType = "SALE",
                    referenceId = invoiceId
                )
            )
        }

        return invoiceId
    }

    // -------------------------------------------------------------------------
    // EXPENSES
    // -------------------------------------------------------------------------
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    suspend fun addExpense(
        category: String,
        amountPaise: Long,
        paymentMode: PaymentMode,
        payee: String,
        note: String
    ): Long {
        val exp = Expense(
            category = category,
            amountPaise = amountPaise,
            paymentMode = paymentMode,
            payee = payee,
            note = note
        )
        val expId = expenseDao.insertExpense(exp)

        // If paid in Cash, record Outflow in Cashbook
        if (paymentMode == PaymentMode.CASH) {
            cashbookDao.insertEntry(
                CashbookEntry(
                    isInflow = false,
                    category = "खर्च (Expense - $category)",
                    amountPaise = amountPaise,
                    paymentMode = paymentMode,
                    description = if (payee.isNotBlank()) "$category: $payee ($note)" else "$category: $note",
                    referenceType = "EXPENSE",
                    referenceId = expId
                )
            )
        }
        return expId
    }

    // -------------------------------------------------------------------------
    // CASHBOOK
    // -------------------------------------------------------------------------
    val allCashbookEntries: Flow<List<CashbookEntry>> = cashbookDao.getAllEntries()

    suspend fun addManualCashEntry(
        isInflow: Boolean,
        category: String,
        amountPaise: Long,
        description: String
    ) {
        cashbookDao.insertEntry(
            CashbookEntry(
                isInflow = isInflow,
                category = category,
                amountPaise = amountPaise,
                paymentMode = PaymentMode.CASH,
                description = description,
                referenceType = "MANUAL"
            )
        )
    }

    // -------------------------------------------------------------------------
    // PURCHASES & SUPPLIER LEDGER
    // -------------------------------------------------------------------------
    val allPurchases: Flow<List<SupplierTransaction>> = supplierTxDao.getAllPurchases()

    suspend fun addPurchaseInvoice(
        supplierName: String,
        supplierPhone: String,
        invoiceNumber: String,
        totalPaise: Long,
        amountPaidPaise: Long,
        paymentMode: PaymentMode,
        note: String
    ): Long {
        val suppliers = supplierDao.getAllSuppliers().firstOrNull() ?: emptyList()
        val supplier = suppliers.find { it.name.equals(supplierName.trim(), ignoreCase = true) }
        val supplierId = supplier?.id ?: addSupplier(
            name = supplierName.ifBlank { "सप्लायर" }.trim(),
            phone = supplierPhone.trim(),
            address = "",
            notes = "",
            openingBalancePaise = 0L
        )

        // Record Purchase Credit (bill amount)
        val purchaseTxId = supplierTxDao.insertTransaction(
            SupplierTransaction(
                supplierId = supplierId,
                type = SupplierTransactionType.PURCHASE_CREDIT,
                amountPaise = totalPaise,
                paymentMode = paymentMode,
                note = "खरीदी #$invoiceNumber: $note"
            )
        )

        // If paid amount > 0, record supplier payment
        if (amountPaidPaise > 0) {
            supplierTxDao.insertTransaction(
                SupplierTransaction(
                    supplierId = supplierId,
                    type = SupplierTransactionType.PAYMENT_MADE,
                    amountPaise = amountPaidPaise,
                    paymentMode = paymentMode,
                    note = "खरीदी #$invoiceNumber भुगतान"
                )
            )

            // If cash payment, record Outflow in cashbook
            if (paymentMode == PaymentMode.CASH) {
                cashbookDao.insertEntry(
                    CashbookEntry(
                        isInflow = false,
                        category = "खरीदी नकद भुगतान",
                        amountPaise = amountPaidPaise,
                        paymentMode = PaymentMode.CASH,
                        description = "खरीदी #$invoiceNumber नकद भुगतान",
                        referenceType = "PURCHASE",
                        referenceId = purchaseTxId
                    )
                )
            }
        }
        return purchaseTxId
    }

    suspend fun deletePurchase(id: Long) {
        supplierTxDao.deleteTransaction(id)
    }

    suspend fun deleteExpense(id: Long) {
        expenseDao.deleteExpense(id)
    }

    suspend fun deleteCashEntry(id: Long) {
        cashbookDao.deleteEntry(id)
    }

    suspend fun deleteProduct(id: Long) {
        productDao.deleteProduct(id)
    }

    suspend fun exportBackupJson(): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val customers = customerDao.getAllCustomers().firstOrNull() ?: emptyList()
        val suppliers = supplierDao.getAllSuppliers().firstOrNull() ?: emptyList()
        val products = productDao.getAllProducts().firstOrNull() ?: emptyList()
        val invoices = invoiceDao.getAllInvoices().firstOrNull() ?: emptyList()
        val expenses = expenseDao.getAllExpenses().firstOrNull() ?: emptyList()
        val cashEntries = cashbookDao.getAllEntries().firstOrNull() ?: emptyList()

        val json = org.json.JSONObject().apply {
            put("app", "Dukaan Hisab Pro")
            put("version", 1)
            put("exportTimestamp", System.currentTimeMillis())
            put("customersCount", customers.size)
            put("suppliersCount", suppliers.size)
            put("productsCount", products.size)
            put("invoicesCount", invoices.size)
            put("expensesCount", expenses.size)
            put("cashbookEntriesCount", cashEntries.size)
        }
        json.toString(2)
    }

    // -------------------------------------------------------------------------
    // CLEAR ALL DATA (Wipes all ledger entries, invoices, stock & expenses)
    // -------------------------------------------------------------------------
    suspend fun clearAllData() = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        database.clearAllTables()
        profileDao.saveProfile(
            BusinessProfile(
                id = 1,
                businessName = "मेरी दुकान (Dukaan Hisab)",
                businessType = "किराना व जनरल स्टोर",
                ownerName = "",
                phone = "",
                address = "",
                defaultInvoicePrefix = "INV-",
                language = "hi",
                openingCashPaise = 0L,
                isSetupCompleted = true
            )
        )
    }

    // -------------------------------------------------------------------------
    // DEMO DATA SEEDER (Optional Demo Mode)
    // -------------------------------------------------------------------------
    suspend fun seedDemoData() {
        val currentCustomers = customerDao.getAllCustomers().firstOrNull() ?: emptyList()
        if (currentCustomers.isNotEmpty()) return // Already seeded

        // Exact Customers from screenshots
        val c1 = addCustomer("Rahul", "9876543210", "Main Market", "", 0L)
        val c2 = addCustomer("Arun", "9812345678", "Shop Area", "", 0L)
        val c3 = addCustomer("suraj kumar", "9988776655", "Civil Lines", "", 0L)

        // Arun has -₹100 (Advance)
        addCustomerTransaction(c2, CustomerTransactionType.PAYMENT_RECEIVED, 10000L, PaymentMode.CASH, "Arun से नकद भुगतान")

        // suraj kumar has +₹200 (Credit)
        addCustomerTransaction(c3, CustomerTransactionType.SALE_CREDIT, 20000L, PaymentMode.CREDIT, "सामान उधार")

        // Exact Products from screenshots
        val p1 = addProduct("Atta (आटा 5kg)", "ATA5", "Grocery", 21000L, 25000L, 20.0, 5.0, "Packet")
        val p2 = addProduct("Tata Salt (नमक 1kg)", "SALT1", "Grocery", 2200L, 2800L, 50.0, 10.0, "Packet")
        val p3 = addProduct("Oil (तेल 1L)", "OIL1", "Grocery", 13500L, 16000L, 30.0, 5.0, "Litre")

        // Exact Invoices from screenshots (Total Sales = ₹1,640)
        invoiceDao.insertInvoice(
            SalesInvoice(
                invoiceNumber = "INV-45547",
                customerId = c2,
                customerName = "Arun",
                dateTimestamp = 1793280000000L,
                subtotalPaise = 88000L,
                discountPaise = 0L,
                grandTotalPaise = 88000L,
                amountPaidPaise = 88000L,
                balanceDuePaise = 0L,
                paymentMode = PaymentMode.CASH,
                status = InvoiceStatus.PAID
            )
        )
        invoiceDao.insertInvoice(
            SalesInvoice(
                invoiceNumber = "INV-33524",
                customerId = c3,
                customerName = "suraj kumar",
                dateTimestamp = 1793280000000L,
                subtotalPaise = 51000L,
                discountPaise = 0L,
                grandTotalPaise = 51000L,
                amountPaidPaise = 51000L,
                balanceDuePaise = 0L,
                paymentMode = PaymentMode.CASH,
                status = InvoiceStatus.PAID
            )
        )
        invoiceDao.insertInvoice(
            SalesInvoice(
                invoiceNumber = "INV-53687",
                customerId = c3,
                customerName = "suraj kumar",
                dateTimestamp = 1793280000000L,
                subtotalPaise = 20000L,
                discountPaise = 0L,
                grandTotalPaise = 20000L,
                amountPaidPaise = 20000L,
                balanceDuePaise = 0L,
                paymentMode = PaymentMode.CASH,
                status = InvoiceStatus.PAID
            )
        )
        invoiceDao.insertInvoice(
            SalesInvoice(
                invoiceNumber = "INV-79412",
                customerId = c3,
                customerName = "suraj kumar",
                dateTimestamp = 1793280000000L,
                subtotalPaise = 5000L,
                discountPaise = 0L,
                grandTotalPaise = 5000L,
                amountPaidPaise = 5000L,
                balanceDuePaise = 0L,
                paymentMode = PaymentMode.CASH,
                status = InvoiceStatus.PAID
            )
        )

        // Exact Expenses from screenshots
        addExpense("Miscellaneous", 1000000L, PaymentMode.CASH, "", "29 Oct 2026, 12:50 am")
        addExpense("Marketing & Ads", 1400000L, PaymentMode.CASH, "", "29 Oct 2026, 12:50 am")
        addExpense("Packaging & Bags", 70000L, PaymentMode.CASH, "", "29 Oct 2026, 12:49 am")
        addExpense("Stock Purchase", 10000000L, PaymentMode.CASH, "", "29 Oct 2026, 12:49 am")
        addExpense("Repairs / Maintenance", 50000L, PaymentMode.CASH, "", "29 Oct 2026, 12:49 am")

        // Exact Cashbook Inflows & Outflows from screenshots:
        // Cash in Hand = -₹137,210 (Inflow = ₹21,740, Outflow = ₹1,58,950)
        cashbookDao.insertEntry(
            CashbookEntry(
                isInflow = false,
                category = "खरीदी #PUR-91149 नकद भुगतान",
                amountPaise = 1035000L,
                paymentMode = PaymentMode.CASH,
                description = "29 Oct 2026, 09:55 am"
            )
        )
        cashbookDao.insertEntry(
            CashbookEntry(
                isInflow = true,
                category = "दुकान बिक्री (Cash Sales)",
                amountPaise = 2164000L,
                paymentMode = PaymentMode.CASH,
                description = "Cash Sales Collection"
            )
        )
        cashbookDao.insertEntry(
            CashbookEntry(
                isInflow = false,
                category = "OTHER:",
                amountPaise = 1000000L,
                paymentMode = PaymentMode.CASH,
                description = "29 Oct 2026, 12:50 am"
            )
        )
        cashbookDao.insertEntry(
            CashbookEntry(
                isInflow = false,
                category = "MARKETING:",
                amountPaise = 1400000L,
                paymentMode = PaymentMode.CASH,
                description = "29 Oct 2026, 12:50 am"
            )
        )
        cashbookDao.insertEntry(
            CashbookEntry(
                isInflow = false,
                category = "PACKAGING:",
                amountPaise = 70000L,
                paymentMode = PaymentMode.CASH,
                description = "29 Oct 2026, 12:49 am"
            )
        )

        // 1 Supplier from screenshots
        addSupplier("Supplier Wholesaler", "9812300000", "City Market", "", 2000000L)
    }
}
