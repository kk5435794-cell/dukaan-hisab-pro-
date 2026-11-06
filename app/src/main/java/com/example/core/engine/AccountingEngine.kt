package com.example.core.engine

import com.example.core.model.CustomerTransactionType
import com.example.core.model.InvoiceStatus
import com.example.core.model.StockMovementType
import com.example.core.model.SupplierTransactionType

/**
 * Centralized, verifiable accounting and calculation engine.
 * All financial amounts are represented in integer Paise (1 INR = 100 Paise).
 * Follows double-entry and ledger consistency principles.
 */
object AccountingEngine {

    /**
     * Data holder representing a customer transaction input for calculation.
     */
    data class CustomerTxRecord(
        val type: CustomerTransactionType,
        val amountPaise: Long,
        val isReversed: Boolean = false
    )

    /**
     * Calculates the net customer balance.
     * Convention:
     * - Positive balance (> 0): Customer owes money to the shop (Udhaar / Receivable / बक़ाया).
     * - Negative balance (< 0): Shop owes customer / customer has advance deposit (Advance / अग्रिम).
     * - Zero balance (== 0): Fully settled (हिसाब बराबर).
     */
    fun calculateCustomerBalance(
        openingBalancePaise: Long,
        transactions: List<CustomerTxRecord>
    ): Long {
        var balance = openingBalancePaise
        for (tx in transactions) {
            if (tx.isReversed) continue // Reversed transactions have no effect
            when (tx.type) {
                CustomerTransactionType.SALE_CREDIT -> balance += tx.amountPaise
                CustomerTransactionType.DEBIT_ADJUSTMENT -> balance += tx.amountPaise
                CustomerTransactionType.PAYMENT_RECEIVED -> balance -= tx.amountPaise
                CustomerTransactionType.RETURN -> balance -= tx.amountPaise
                CustomerTransactionType.REVERSAL -> {
                    // Handled via isReversed flag or explicit reversal line
                }
            }
        }
        return balance
    }

    /**
     * Supplier transaction record for calculation.
     */
    data class SupplierTxRecord(
        val type: SupplierTransactionType,
        val amountPaise: Long,
        val isReversed: Boolean = false
    )

    /**
     * Calculates the net supplier balance.
     * Convention:
     * - Positive balance (> 0): Shop owes money to supplier (Payable / देनदारी).
     * - Negative balance (< 0): Supplier has shop's advance deposit (Advance given).
     */
    fun calculateSupplierBalance(
        openingBalancePaise: Long,
        transactions: List<SupplierTxRecord>
    ): Long {
        var balance = openingBalancePaise
        for (tx in transactions) {
            if (tx.isReversed) continue
            when (tx.type) {
                SupplierTransactionType.PURCHASE_CREDIT -> balance += tx.amountPaise
                SupplierTransactionType.ADJUSTMENT -> balance += tx.amountPaise
                SupplierTransactionType.PAYMENT_MADE -> balance -= tx.amountPaise
                SupplierTransactionType.RETURN -> balance -= tx.amountPaise
                SupplierTransactionType.REVERSAL -> {}
            }
        }
        return balance
    }

    /**
     * Stock movement record for inventory quantity tracking.
     */
    data class StockMovementRecord(
        val type: StockMovementType,
        val quantity: Double
    )

    /**
     * Single source of truth for product stock level.
     */
    fun calculateStock(
        openingStock: Double,
        movements: List<StockMovementRecord>
    ): Double {
        var stock = openingStock
        for (m in movements) {
            when (m.type) {
                StockMovementType.OPENING -> { /* Already in openingStock */ }
                StockMovementType.PURCHASE -> stock += m.quantity
                StockMovementType.CUSTOMER_RETURN -> stock += m.quantity
                StockMovementType.SALE -> stock -= m.quantity
                StockMovementType.SUPPLIER_RETURN -> stock -= m.quantity
                StockMovementType.DAMAGE -> stock -= m.quantity
                StockMovementType.LOSS -> stock -= m.quantity
                StockMovementType.MANUAL_ADJUSTMENT -> stock += m.quantity // can be positive or negative
            }
        }
        return stock
    }

    /**
     * Cashbook calculation.
     * Total cash on hand = Opening Cash + Cash Inflows - Cash Outflows
     */
    data class CashTx(
        val isInflow: Boolean,
        val amountPaise: Long
    )

    fun calculateCashBalance(
        openingCashPaise: Long,
        cashEntries: List<CashTx>
    ): Long {
        var balance = openingCashPaise
        for (entry in cashEntries) {
            if (entry.isInflow) {
                balance += entry.amountPaise
            } else {
                balance -= entry.amountPaise
            }
        }
        return balance
    }

    /**
     * Calculates invoice totals with item-level and invoice-level discounts.
     * Prevents negative balances or discounts exceeding subtotal.
     */
    data class InvoiceCalculationResult(
        val subtotalPaise: Long,
        val discountPaise: Long,
        val grandTotalPaise: Long,
        val amountPaidPaise: Long,
        val balanceDuePaise: Long,
        val status: InvoiceStatus
    )

    fun calculateInvoiceTotals(
        itemsTotalPaise: Long,
        invoiceDiscountPaise: Long,
        amountPaidPaise: Long
    ): InvoiceCalculationResult {
        val safeItemsTotal = maxOf(0L, itemsTotalPaise)
        val safeDiscount = maxOf(0L, minOf(safeItemsTotal, invoiceDiscountPaise))
        val grandTotal = maxOf(0L, safeItemsTotal - safeDiscount)
        val safePaid = maxOf(0L, minOf(grandTotal, amountPaidPaise))
        val balanceDue = maxOf(0L, grandTotal - safePaid)

        val status = when {
            balanceDue == 0L && grandTotal > 0L -> InvoiceStatus.PAID
            safePaid > 0L && balanceDue > 0L -> InvoiceStatus.PARTIALLY_PAID
            safePaid == 0L && grandTotal > 0L -> InvoiceStatus.UNPAID
            else -> InvoiceStatus.PAID // Zero amount bill
        }

        return InvoiceCalculationResult(
            subtotalPaise = safeItemsTotal,
            discountPaise = safeDiscount,
            grandTotalPaise = grandTotal,
            amountPaidPaise = safePaid,
            balanceDuePaise = balanceDue,
            status = status
        )
    }

    /**
     * Business Analytics calculations: Net Sales, COGS, Gross Profit, Operating Profit
     */
    data class ProfitReport(
        val netSalesPaise: Long,
        val totalCostPaise: Long,
        val grossProfitPaise: Long,
        val totalExpensesPaise: Long,
        val operatingProfitPaise: Long,
        val isCostDataComplete: Boolean
    )

    fun calculateProfit(
        netSalesPaise: Long,
        costOfGoodsSoldPaise: Long,
        operatingExpensesPaise: Long,
        isCostDataComplete: Boolean = true
    ): ProfitReport {
        val grossProfit = netSalesPaise - costOfGoodsSoldPaise
        val operatingProfit = grossProfit - operatingExpensesPaise
        return ProfitReport(
            netSalesPaise = netSalesPaise,
            totalCostPaise = costOfGoodsSoldPaise,
            grossProfitPaise = grossProfit,
            totalExpensesPaise = operatingExpensesPaise,
            operatingProfitPaise = operatingProfit,
            isCostDataComplete = isCostDataComplete
        )
    }
}
