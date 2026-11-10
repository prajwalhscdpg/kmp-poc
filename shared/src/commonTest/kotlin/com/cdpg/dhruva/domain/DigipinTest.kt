package com.cdpg.dhruva.domain

import com.cdpg.dhruva.domain.digipin.Digipin
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DigipinTest {

    private val samples = listOf(
        13.0033 to 77.5700, // Malleshwaram, Bengaluru (Create DA default pin)
        28.622788 to 77.213033, // Dak Bhawan, New Delhi
        19.0760 to 72.8777, // Mumbai
        8.5241 to 76.9366, // Thiruvananthapuram
        34.0837 to 74.7973, // Srinagar
    )

    @Test fun codesUseOnlyTheGridSymbolsAndAreTenLong() {
        val alphabet = "FC98J327K456LMPT".toSet()
        samples.forEach { (lat, lon) ->
            val code = assertNotNull(Digipin.encode(lat, lon))
            assertEquals(10, code.length)
            assertTrue(code.all { it in alphabet }, code)
        }
    }

    @Test fun decodeReturnsAPointInsideTheSameCell() {
        samples.forEach { (lat, lon) ->
            val code = Digipin.encode(lat, lon)!!
            val (cLat, cLon) = assertNotNull(Digipin.decode(code))
            assertEquals(code, Digipin.encode(cLat, cLon))
            // A level-10 cell is about 4 m wide.
            assertTrue(abs(cLat - lat) < 0.0001 && abs(cLon - lon) < 0.0001, "$code → $cLat,$cLon")
        }
    }

    @Test fun outsideCoverageIsNull() {
        assertNull(Digipin.encode(51.5, -0.12)) // London
        assertNull(Digipin.encode(2.0, 80.0))
    }

    @Test fun displayFormatIsThreeThreeFour() {
        assertEquals("39J-438-TJC7", Digipin.formatForDisplay("39J438TJC7"))
        assertNotNull(Digipin.decode("39J-438-TJC7"))
        assertNull(Digipin.decode("39J-438-TJC")) // too short
        assertNull(Digipin.decode("39J-438-TJCA")) // 'A' isn't a DIGIPIN symbol
    }
}
