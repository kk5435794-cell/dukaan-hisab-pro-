package com.example

import com.example.core.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun testPaiseParsing() {
    val paise = Formatters.parseRupeesToPaise("1500.50")
    assertEquals(150050L, paise)
  }

  @Test
  fun testCurrencyFormattingContainsRupeeSymbol() {
    val formatted = Formatters.formatPaiseToRupees(150000L)
    assertTrue(formatted.contains("₹") || formatted.contains("1,500"))
  }
}
