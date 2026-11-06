package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.database.dao.BusinessProfileDao
import com.example.database.dao.CashbookDao
import com.example.database.dao.CustomerDao
import com.example.database.dao.CustomerTransactionDao
import com.example.database.dao.ExpenseDao
import com.example.database.dao.ProductDao
import com.example.database.dao.SalesInvoiceDao
import com.example.database.dao.StockMovementDao
import com.example.database.dao.SupplierDao
import com.example.database.dao.SupplierTransactionDao
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

@Database(
    entities = [
        BusinessProfile::class,
        Customer::class,
        CustomerTransaction::class,
        Supplier::class,
        SupplierTransaction::class,
        Product::class,
        StockMovement::class,
        SalesInvoice::class,
        SalesInvoiceItem::class,
        Expense::class,
        CashbookEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerTransactionDao(): CustomerTransactionDao
    abstract fun supplierDao(): SupplierDao
    abstract fun supplierTransactionDao(): SupplierTransactionDao
    abstract fun productDao(): ProductDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun salesInvoiceDao(): SalesInvoiceDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun cashbookDao(): CashbookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dukaan_hisab_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
