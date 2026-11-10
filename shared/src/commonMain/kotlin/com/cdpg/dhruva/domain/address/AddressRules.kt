package com.cdpg.dhruva.domain.address

import com.cdpg.dhruva.core.strings.StringKey
import com.cdpg.dhruva.core.strings.UiText

/** My DAs search: matches the title or the handle, ignoring case (both apps did this). */
class SearchAddressesUseCase {
    operator fun invoke(addresses: List<DigitalAddress>, query: String): List<DigitalAddress> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return addresses
        return addresses.filter {
            it.title.contains(trimmed, ignoreCase = true) || it.handle.contains(trimmed, ignoreCase = true)
        }
    }
}

/**
 * The handle prefix ("bandra.104" in "bandra.104@dap.dop").
 * Neither app validated it yet; this is a placeholder rule (letters, digits, single dots, no dot
 * at either end, at most 30 characters) until the backend publishes the real one.
 */
class ValidateAddressPrefixUseCase {
    operator fun invoke(prefix: String): Boolean = prefix.length <= MAX_LENGTH && PREFIX.matches(prefix)

    /** Lower-case, no spaces or "@" while typing. */
    fun sanitize(input: String): String =
        input.lowercase().filter { it.isLetterOrDigit() || it == '.' }.take(MAX_LENGTH)

    companion object {
        const val MAX_LENGTH = 30
        private val PREFIX = Regex("^[a-z0-9]+(\\.[a-z0-9]+)*$")
    }
}

/** Suffix options. Mock values from Dhruva iOS until an API lists them; "@dap.dop" is the default. */
object AddressSuffixes {
    val options: List<String> = listOf(
        "@home", "@work", "@dap.dop", "@somewhere", "@nowhere", "@anywhere",
        "@everywhere", "@someplace", "@anyplace", "@someday", "@nowandthen",
    )
    const val DEFAULT = "@dap.dop"
}

/** Ground plus 1st–8th, exactly the Figma "Select floor number" sheet (Dhruva iOS). */
enum class FloorOption(val number: Int) {
    Ground(0), First(1), Second(2), Third(3), Fourth(4), Fifth(5), Sixth(6), Seventh(7), Eighth(8);

    val label: UiText
        get() = if (this == Ground) UiText.Res(StringKey.CREATE_DA_FLOOR_GROUND)
        else UiText.Res(StringKey.CREATE_DA_FLOOR_NUMBERED, listOf(number))
}
