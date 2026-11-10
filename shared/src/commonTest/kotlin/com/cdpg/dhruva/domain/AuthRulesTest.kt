package com.cdpg.dhruva.domain

import com.cdpg.dhruva.domain.auth.FullNameFormatter
import com.cdpg.dhruva.domain.auth.MobileNumberError
import com.cdpg.dhruva.domain.auth.OtpPolicy
import com.cdpg.dhruva.domain.auth.ValidateMobileNumberUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidateMobileNumberUseCaseTest {
    private val validate = ValidateMobileNumberUseCase()

    @Test fun acceptsTenDigitsStartingSixToNine() {
        listOf("6000000000", "7123456789", "8999999999", "9900195577").forEach { assertTrue(validate(it), it) }
    }

    @Test fun rejectsWrongStartOrLength() {
        listOf("", "5123456789", "0123456789", "912345678", "91234567890").forEach { assertFalse(validate(it), it) }
    }

    @Test fun sanitizeKeepsDigitsAndCapsAtTen() {
        assertEquals("9845012345", validate.sanitize("+91 98450 12345"))
        assertEquals("9900195577", validate.sanitize("99001955771234"))
    }

    @Test fun wrongFirstDigitIsALiveError() {
        assertEquals(MobileNumberError.InvalidStart, validate.liveError("5"))
        assertNull(validate.liveError("98"))
        assertNull(validate.liveError(""))
    }

    @Test fun incompleteNumberIsOnlyReportedOnSubmit() {
        assertNull(validate.liveError("98450"))
        assertEquals(MobileNumberError.Incomplete, validate.submitError("98450"))
        assertNull(validate.submitError("9845012345"))
    }
}

class FullNameFormatterTest {
    private val formatter = FullNameFormatter()

    @Test fun collapsesSpacesAndDropsLeadingOnes() {
        assertEquals("Jyotirmoy Boral ", formatter.sanitize("  Jyotirmoy   Boral "))
    }

    @Test fun completeRemovesTrailingSpace() {
        assertEquals("Jyotirmoy Boral", formatter.complete("Jyotirmoy Boral "))
    }

    @Test fun keepsAllowedPunctuationAndOtherScripts() {
        assertEquals("Mary-Jane O'Neil Jr.", formatter.sanitize("Mary-Jane O'Neil Jr."))
        // Devanagari vowel signs and viramas are combining marks and must survive.
        assertEquals("प्रज्वल शर्मा", formatter.sanitize("प्रज्वल शर्मा"))
    }

    @Test fun dropsDigitsAndSymbols() {
        assertEquals("Ravi", formatter.sanitize("R4av1i!@#"))
    }

    @Test fun capsLength() {
        assertEquals(FullNameFormatter.MAX_LENGTH, formatter.sanitize("a".repeat(80)).length)
    }

    @Test fun needsALetter() {
        assertFalse(formatter.isComplete("  .- "))
        assertTrue(formatter.isComplete("A"))
    }

    @Test fun firstName() {
        assertEquals("Jyotirmoy", formatter.firstName("Jyotirmoy Boral"))
    }

    @Test fun otpSanitize() {
        assertEquals("123456", OtpPolicy.sanitize("12a3 4567"))
    }
}
