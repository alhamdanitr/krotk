package com.example

import com.example.utils.SmsParser
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testSmsParser_Jawali() {
    val message = "استلمت مبلغ 5100 YER من 700070034 رصيدك هو 5300"
    val parsed = SmsParser.parse(message)
    assertNotNull("Parsed result should not be null", parsed)
    assertEquals(5100, parsed!!.amount)
    assertEquals("700070034", parsed.phone)
    assertEquals("جوالي", parsed.walletType)
    assertFalse(parsed.isAccountCode)
  }

  @Test
  fun testSmsParser_Jeeb() {
    val message = "500ر.ي تم استلامها من المحفظة - 777777777"
    val parsed = SmsParser.parse(message)
    assertNotNull("Parsed result should not be null for Jeeb", parsed)
    assertEquals(500, parsed!!.amount)
    assertEquals("777777777", parsed.phone)
    assertEquals("جيب", parsed.walletType)
    assertFalse(parsed.isAccountCode)
  }
}
