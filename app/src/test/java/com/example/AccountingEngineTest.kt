package com.example

import com.example.core.engine.AccountingEngine
import com.example.core.model.CustomerTransactionType
import com.example.core.model.InvoiceStatus
import com.example.core.model.StockMovementType
import com.example.core.model.SupplierTransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Automated verification of accounting formulas, ledger balances,
 * invoice totals, stock calculations, and profit estimation.
 */
class AccountingEngineTest {

    // -------------------------------------------------------------------------
    // 1. CUSTOMER LEDGER (UDHAAR HISAB)
    // -------------------------------------------------------------------------
    @Test
    fun testCustomerLedger_OpeningBalance_And_CreditSale() {
        val openingBalance = 50000L // ₹500.00
        val txs = listOf(
            AccountingEngine.CustomerTxRecord(CustomerTransactionType.SALE_CREDIT, 100000L) // ₹1000.00
        )
        val balance = AccountingEngine.calculateCustomerBalance(openingBalance, txs)
        assertEquals(150000L, balance) // ₹1500.00 owed
    }

    @Test
    fun testCustomerLedger_PartialPayment_And_FullPayment() {
        val openingBalance = 100000L // ₹1,000.00
        // Customer pays ₹300 (per example in prompt)
        val partialPaymentTx = listOf(
            AccountingEngine.CustomerTxRecord(CustomerTransactionType.PAYMENT_RECEIVED, 30000L)
        )
        val balanceAfterPartial = AccountingEngine.calculateCustomerBalance(openingBalance, partialPaymentTx)
        assertEquals(70000L, balanceAfterPartial) // ₹700.00

        // Customer pays remaining ₹700 in full
        val fullPaymentTxs = listOf(
            AccountingEngine.CustomerTxRecord(CustomerTransactionType.PAYMENT_RECEIVED, 30000L),
            AccountingEngine.CustomerTxRecord(CustomerTransactionType.PAYMENT_RECEIVED, 70000L)
        )
        val balanceAfterFull = AccountingEngine.calculateCustomerBalance(openingBalance, fullPaymentTxs)
        assertEquals(0L, balanceAfterFull) // Account settled
    }

    @Test
    fun testCustomerLedger_SalesReturn_ReducesBalance() {
        val openingBalance = 100000L
        val txs = listOf(
            AccountingEngine.CustomerTxRecord(CustomerTransactionType.RETURN, 25000L) // ₹250 return
        )
        val balance = AccountingEngine.calculateCustomerBalance(openingBalance, txs)
        assertEquals(75000L, balance)
    }

    @Test
    fun testCustomerLedger_Reversal_DoesNotAffectBalance() {
        val openingBalance = 100000L
        val txs = listOf(
            AccountingEngine.CustomerTxRecord(
                CustomerTransactionType.PAYMENT_RECEIVED,
                50000L,
                isReversed = true // Reversed entry with audit trail
            )
        )
        val balance = AccountingEngine.calculateCustomerBalance(openingBalance, txs)
        assertEquals(100000L, balance) // Balance remains ₹1,000 without reversal impact
    }

    // -------------------------------------------------------------------------
    // 2. SUPPLIER LEDGER
    // -------------------------------------------------------------------------
    @Test
    fun testSupplierLedger_PurchaseAndPayment() {
        val opening = 200000L // ₹2,000 owed
        val txs = listOf(
            AccountingEngine.SupplierTxRecord(SupplierTransactionType.PURCHASE_CREDIT, 300000L), // +₹3,000
            AccountingEngine.SupplierTxRecord(SupplierTransactionType.PAYMENT_MADE, 400000L)     // -₹4,000
        )
        val payable = AccountingEngine.calculateSupplierBalance(opening, txs)
        assertEquals(100000L, payable) // ₹1,000 remaining payable
    }

    // -------------------------------------------------------------------------
    // 3. INVENTORY STOCK TRACKING
    // -------------------------------------------------------------------------
    @Test
    fun testStockCalculation_Movements() {
        val openingStock = 50.0
        val movements = listOf(
            AccountingEngine.StockMovementRecord(StockMovementType.PURCHASE, 30.0),       // +30 = 80
            AccountingEngine.StockMovementRecord(StockMovementType.SALE, 15.0),           // -15 = 65
            AccountingEngine.StockMovementRecord(StockMovementType.CUSTOMER_RETURN, 2.0), // +2 = 67
            AccountingEngine.StockMovementRecord(StockMovementType.DAMAGE, 3.0),          // -3 = 64
            AccountingEngine.StockMovementRecord(StockMovementType.MANUAL_ADJUSTMENT, -4.0) // -4 = 60
        )
        val finalStock = AccountingEngine.calculateStock(openingStock, movements)
        assertEquals(60.0, finalStock, 0.001)
    }

    // -------------------------------------------------------------------------
    // 4. CASHBOOK & INFLOW/OUTFLOW
    // -------------------------------------------------------------------------
    @Test
    fun testCashbook_InflowAndOutflow() {
        val openingCash = 500000L // ₹5,000 opening cash
        val entries = listOf(
            AccountingEngine.CashTx(isInflow = true, amountPaise = 250000L),  // Cash sale +₹2,500
            AccountingEngine.CashTx(isInflow = true, amountPaise = 100000L),  // Customer collection +₹1,000
            AccountingEngine.CashTx(isInflow = false, amountPaise = 150000L), // Rent expense -₹1,500
            AccountingEngine.CashTx(isInflow = false, amountPaise = 200000L)  // Supplier payment -₹2,000
        )
        val balance = AccountingEngine.calculateCashBalance(openingCash, entries)
        assertEquals(500000L + 250000L + 100000L - 150000L - 200000L, balance) // ₹5,000
    }

    // -------------------------------------------------------------------------
    // 5. INVOICE TOTALS, DISCOUNT & PAYMENT STATUS
    // -------------------------------------------------------------------------
    @Test
    fun testInvoiceTotals_FullPayment() {
        val result = AccountingEngine.calculateInvoiceTotals(
            itemsTotalPaise = 100000L, // ₹1,000
            invoiceDiscountPaise = 10000L, // ₹100 discount
            amountPaidPaise = 90000L // Paid full ₹900
        )
        assertEquals(90000L, result.grandTotalPaise)
        assertEquals(90000L, result.amountPaidPaise)
        assertEquals(0L, result.balanceDuePaise)
        assertEquals(InvoiceStatus.PAID, result.status)
    }

    @Test
    fun testInvoiceTotals_PartialPayment() {
        val result = AccountingEngine.calculateInvoiceTotals(
            itemsTotalPaise = 200000L, // ₹2,000
            invoiceDiscountPaise = 0L,
            amountPaidPaise = 50000L // Paid ₹500
        )
        assertEquals(200000L, result.grandTotalPaise)
        assertEquals(50000L, result.amountPaidPaise)
        assertEquals(150000L, result.balanceDuePaise)
        assertEquals(InvoiceStatus.PARTIALLY_PAID, result.status)
    }

    @Test
    fun testInvoiceTotals_UnpaidCreditSale() {
        val result = AccountingEngine.calculateInvoiceTotals(
            itemsTotalPaise = 150000L,
            invoiceDiscountPaise = 0L,
            amountPaidPaise = 0L
        )
        assertEquals(150000L, result.grandTotalPaise)
        assertEquals(0L, result.amountPaidPaise)
        assertEquals(150000L, result.balanceDuePaise)
        assertEquals(InvoiceStatus.UNPAID, result.status)
    }

    // -------------------------------------------------------------------------
    // 6. PROFIT CALCULATION ENGINE
    // -------------------------------------------------------------------------
    @Test
    fun testProfitCalculation() {
        val netSales = 500000L // ₹5,000 sales
        val cogs = 350000L     // ₹3,500 cost of goods
        val expenses = 50000L  // ₹500 shop rent/bills

        val report = AccountingEngine.calculateProfit(netSales, cogs, expenses)
        assertEquals(150000L, report.grossProfitPaise) // ₹1,500 gross margin
        assertEquals(100000L, report.operatingProfitPaise) // ₹1,000 net operating profit
        assertTrue(report.isCostDataComplete)
    }
}
