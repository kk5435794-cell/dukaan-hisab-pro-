package com.example.core.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Precision formatting utilities for INR currency, Indian number format, and timestamps.
 */
object Formatters {

    private val inrLocale = Locale("en", "IN")
    private val numberFormat = NumberFormat.getCurrencyInstance(inrLocale).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    /**
     * Converts paise (e.g. 150000) to formatted currency string (e.g. "₹1,500.00")
     */
    fun formatPaiseToRupees(paise: Long, showDecimals: Boolean = true): String {
        val rupees = paise.toDouble() / 100.0
        val formatted = numberFormat.format(rupees)
        // If system puts standard currency code or symbol, ensure ₹ prefix
        val clean = formatted.replace("INR", "₹").trim()
        return if (!showDecimals && clean.endsWith(".00")) {
            clean.substring(0, clean.length - 3)
        } else {
            clean
        }
    }

    /**
     * Formats plain number of rupees with ₹ symbol
     */
    fun formatRupeeDouble(rupees: Double): String {
        val paise = (rupees * 100).toLong()
        return formatPaiseToRupees(paise)
    }

    /**
     * Parses user rupee input (e.g. "1250.50" or "1250") safely to Long paise (125050L)
     */
    fun parseRupeesToPaise(input: String): Long {
        val cleaned = input.trim().replace(",", "").replace("₹", "")
        if (cleaned.isEmpty()) return 0L
        val amount = cleaned.toDoubleOrNull() ?: return 0L
        if (amount < 0) return 0L
        return Math.round(amount * 100)
    }

    fun formatDate(timestamp: Long, pattern: String = "dd MMM yyyy, hh:mm a"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatQuantity(qty: Double, unit: String): String {
        val isWhole = qty % 1.0 == 0.0
        val formattedQty = if (isWhole) qty.toInt().toString() else String.format(Locale.US, "%.2f", qty)
        return "$formattedQty $unit"
    }
}
