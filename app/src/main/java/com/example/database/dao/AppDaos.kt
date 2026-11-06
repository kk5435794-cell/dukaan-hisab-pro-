package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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

@Dao
interface BusinessProfileDao {
    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<BusinessProfile?>

    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): BusinessProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: BusinessProfile)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerFlow(id: Long): Flow<Customer?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("UPDATE customers SET isArchived = 1 WHERE id = :id")
    suspend fun archiveCustomer(id: Long)
}

@Dao
interface CustomerTransactionDao {
    @Query("SELECT * FROM customer_transactions WHERE customerId = :customerId ORDER BY dateTimestamp DESC")
    fun getTransactionsForCustomer(customerId: Long): Flow<List<CustomerTransaction>>

    @Query("SELECT * FROM customer_transactions WHERE customerId = :customerId ORDER BY dateTimestamp ASC")
    suspend fun getTransactionsForCustomerSync(customerId: Long): List<CustomerTransaction>

    @Query("SELECT * FROM customer_transactions ORDER BY dateTimestamp DESC")
    fun getAllTransactions(): Flow<List<CustomerTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CustomerTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: CustomerTransaction)
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    suspend fun getSupplierById(id: Long): Supplier?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier): Long

    @Update
    suspend fun updateSupplier(supplier: Supplier)
}

@Dao
interface SupplierTransactionDao {
    @Query("SELECT * FROM supplier_transactions WHERE supplierId = :supplierId ORDER BY dateTimestamp DESC")
    fun getTransactionsForSupplier(supplierId: Long): Flow<List<SupplierTransaction>>

    @Query("SELECT * FROM supplier_transactions WHERE supplierId = :supplierId ORDER BY dateTimestamp ASC")
    suspend fun getTransactionsForSupplierSync(supplierId: Long): List<SupplierTransaction>

    @Query("SELECT * FROM supplier_transactions ORDER BY dateTimestamp DESC")
    fun getAllTransactions(): Flow<List<SupplierTransaction>>

    @Query("SELECT * FROM supplier_transactions WHERE type = 'PURCHASE_CREDIT' ORDER BY dateTimestamp DESC")
    fun getAllPurchases(): Flow<List<SupplierTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: SupplierTransaction): Long

    @Query("DELETE FROM supplier_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET isArchived = 1 WHERE id = :id")
    suspend fun archiveProduct(id: Long)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)
}

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY dateTimestamp DESC")
    fun getMovementsForProduct(productId: Long): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY dateTimestamp ASC")
    suspend fun getMovementsForProductSync(productId: Long): List<StockMovement>

    @Query("SELECT * FROM stock_movements ORDER BY dateTimestamp DESC")
    fun getAllMovements(): Flow<List<StockMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovement): Long
}

@Dao
interface SalesInvoiceDao {
    @Query("SELECT * FROM sales_invoices ORDER BY dateTimestamp DESC")
    fun getAllInvoices(): Flow<List<SalesInvoice>>

    @Query("SELECT * FROM sales_invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Long): SalesInvoice?

    @Query("SELECT * FROM sales_invoice_items WHERE invoiceId = :invoiceId")
    fun getInvoiceItems(invoiceId: Long): Flow<List<SalesInvoiceItem>>

    @Query("SELECT * FROM sales_invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getInvoiceItemsSync(invoiceId: Long): List<SalesInvoiceItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: SalesInvoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<SalesInvoiceItem>)

    @Update
    suspend fun updateInvoice(invoice: SalesInvoice)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateTimestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)
}

@Dao
interface CashbookDao {
    @Query("SELECT * FROM cashbook_entries ORDER BY dateTimestamp DESC")
    fun getAllEntries(): Flow<List<CashbookEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: CashbookEntry): Long

    @Query("DELETE FROM cashbook_entries WHERE id = :id")
    suspend fun deleteEntry(id: Long)
}
