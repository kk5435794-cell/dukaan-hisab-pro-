package com.example.core.model

/**
 * Monetary representation helper.
 * 1 Rupee = 100 Paise.
 * Uses integer arithmetic (Long) to eliminate floating point rounding errors.
 */
object CurrencyConstants {
    const val PAISE_PER_RUPEE = 100L
}

enum class CustomerTransactionType(val hindiLabel: String, val englishLabel: String, val isCreditToCustomer: Boolean) {
    SALE_CREDIT("उधार दिया (Sale)", "Gave Credit / Sale", true),
    PAYMENT_RECEIVED("रुपये मिले (Payment)", "Got Payment", false),
    DEBIT_ADJUSTMENT("डेबिट जोड़ (Debit Adj)", "Debit Adjustment", true),
    RETURN("माल वापसी (Return)", "Sales Return", false),
    REVERSAL("प्रविष्टि रद्द (Reversal)", "Entry Reversal", false)
}

enum class SupplierTransactionType(val hindiLabel: String, val englishLabel: String) {
    PURCHASE_CREDIT("उधार माल खरीदा (Purchase)", "Purchase on Credit"),
    PAYMENT_MADE("भुगतान किया (Payment)", "Payment Made"),
    RETURN("माल वापसी (Purchase Return)", "Purchase Return"),
    ADJUSTMENT("समायोजन (Adjustment)", "Adjustment"),
    REVERSAL("रद्द (Reversal)", "Reversal")
}

enum class PaymentMode(val hindiLabel: String, val englishLabel: String) {
    CASH("नकद (Cash)", "Cash"),
    UPI("यूपीआई (UPI)", "UPI / QR"),
    BANK_TRANSFER("बैंक ट्रांसफर (Bank)", "Bank Transfer"),
    CHEQUE("चेक (Cheque)", "Cheque"),
    CREDIT("उधार (Khata / Credit)", "Credit / Udhar")
}

enum class InvoiceStatus {
    PAID,
    PARTIALLY_PAID,
    UNPAID,
    CANCELLED
}

enum class StockMovementType(val hindiLabel: String, val englishLabel: String) {
    OPENING("प्रारंभिक स्टॉक (Opening)", "Opening Stock"),
    PURCHASE("खरीद आवक (Purchase In)", "Purchase In"),
    SALE("बिक्री निकासी (Sale Out)", "Sale Out"),
    CUSTOMER_RETURN("ग्राहक वापसी (Customer Return)", "Customer Return In"),
    SUPPLIER_RETURN("सप्लायर वापसी (Supplier Return)", "Supplier Return Out"),
    DAMAGE("नुकसान (Damage)", "Damaged / Broken"),
    LOSS("कमी (Loss)", "Loss / Expired"),
    MANUAL_ADJUSTMENT("मैनुअल सुधार (Adjustment)", "Manual Adjustment")
}

enum class BusinessType(val hindiTitle: String, val englishTitle: String) {
    KIRANA("किराना व जनरल स्टोर", "Grocery & General Store"),
    CLOTHING("कपड़ा व वस्त्र", "Clothing & Garments"),
    ELECTRICAL("इलेक्ट्रिकल व इलेक्ट्रॉनिक्स", "Electrical & Electronics"),
    HARDWARE("हार्डवेयर व सेनेटरी", "Hardware & Sanitary"),
    MOBILE("मोबाइल व एक्सेसरीज", "Mobile & Accessories"),
    COSMETICS("कॉस्मेटिक्स व गिफ्ट", "Cosmetics & Gifts"),
    STATIONERY("स्टेशनरी व बुक स्टोर", "Stationery & Books"),
    WHOLESALE("थोक व्यापार", "Wholesale & Trading"),
    OTHER("अन्य व्यापार", "Other Business")
}

enum class AppLanguage(val code: String, val displayName: String) {
    HINDI("hi", "हिंदी (Hindi)"),
    ENGLISH("en", "English")
}
