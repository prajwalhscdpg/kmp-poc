package com.cdpg.dhruva.domain.auth

/**
 * Indian mobile numbers, without the +91 country code: exactly 10 digits starting with 6–9.
 * One implementation for both apps (Android used a regex in a use case, iOS checked by hand in
 * the ViewModel).
 */
class ValidateMobileNumberUseCase {

    operator fun invoke(number: String): Boolean = MOBILE_NUMBER.matches(number)

    /** Digits only, at most 10 (iOS behaviour: pasted "+91 98450 12345" → "9845012345" is cut to 10). */
    fun sanitize(input: String): String = input.filter { it in '0'..'9' }.take(LENGTH)

    /**
     * The error to show *while typing*: only a wrong first digit (it can never become valid).
     * An incomplete number is reported on Continue instead ([submitError]).
     */
    fun liveError(number: String): MobileNumberError? =
        if (number.isNotEmpty() && number.first() !in '6'..'9') MobileNumberError.InvalidStart else null

    /** The error to show when the user taps Continue, or null if the number can be submitted. */
    fun submitError(number: String): MobileNumberError? = when {
        liveError(number) != null -> MobileNumberError.InvalidStart
        !invoke(number) -> MobileNumberError.Incomplete
        else -> null
    }

    companion object {
        const val LENGTH = 10
        private val MOBILE_NUMBER = Regex("^[6-9][0-9]{9}$")
    }
}

enum class MobileNumberError {
    /** First digit isn't 6–9. */
    InvalidStart,

    /** Fewer than 10 digits. */
    Incomplete,
}

/** OTP rules, shared so both screens behave the same. */
object OtpPolicy {
    const val LENGTH = 6

    /**
     * Seconds before "Resend" is enabled. Android used 30, iOS 10; 30 is kept as the
     * product value (iOS's 10 looked like a development setting). Confirm with product.
     */
    const val RESEND_COUNTDOWN_SECONDS = 30

    fun sanitize(input: String): String = input.filter { it in '0'..'9' }.take(LENGTH)
}

/**
 * Keeps a full name tidy while it is typed and when it is submitted.
 *
 * Merges the two apps' rules: only letters (any script, so Hindi names work), spaces, dots,
 * hyphens and apostrophes (iOS's rule and its error copy); no leading space and never two
 * spaces in a row (both apps); one trailing space allowed while typing, removed on submit
 * (Android). Maximum length: iOS allowed 50, Android 100 — 50 is used here; confirm with product.
 */
class FullNameFormatter {

    fun sanitize(input: String): String = input
        .filter { it.isLetter() || it.isCombiningMark() || it.isWhitespace() || it in ALLOWED_PUNCTUATION }
        .replace(WHITESPACE, " ")
        .trimStart()
        .take(MAX_LENGTH)

    /** The name as it is saved; empty if nothing usable was typed. */
    fun complete(input: String): String = sanitize(input).trimEnd()

    /** Submittable once it contains at least one letter. */
    fun isComplete(input: String): Boolean = complete(input).any { it.isLetter() }

    /** First word of the name, e.g. "Jyotirmoy" for "Jyotirmoy Boral". */
    fun firstName(fullName: String): String = complete(fullName).substringBefore(' ')

    /** Vowel signs and viramas (e.g. "ि", "्" in Devanagari) are marks, not letters, but belong to names. */
    private fun Char.isCombiningMark(): Boolean = category == CharCategory.NON_SPACING_MARK ||
        category == CharCategory.COMBINING_SPACING_MARK ||
        category == CharCategory.ENCLOSING_MARK

    companion object {
        const val MAX_LENGTH = 50
        private const val ALLOWED_PUNCTUATION = ".-'’"
        private val WHITESPACE = Regex("\\s+")
    }
}
