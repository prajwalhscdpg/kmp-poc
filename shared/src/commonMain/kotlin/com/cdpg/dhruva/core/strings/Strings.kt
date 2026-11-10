package com.cdpg.dhruva.core.strings

import com.cdpg.dhruva.core.common.DanAiaError
import com.cdpg.dhruva.core.common.ProblemType
import kotlinx.coroutines.flow.StateFlow

/** Languages the app can be shown in. [tag] is the BCP 47 language tag. */
enum class AppLanguage(val tag: String) {
    English("en"),
    Hindi("hi"),
    ;

    companion object {
        /** Matches "hi", "hi-IN"…; anything unknown is English. */
        fun fromTag(tag: String?): AppLanguage =
            entries.firstOrNull { tag != null && tag.lowercase().startsWith(it.tag) } ?: English
    }
}

/**
 * Text a ViewModel wants on screen, resolved to a string only by the UI (so a language change
 * re-renders it). [Res] is a shared string; [Raw] is user data such as a name or an address.
 */
sealed class UiText {
    data class Res(val key: StringKey, val args: List<Any> = emptyList()) : UiText()
    data class Raw(val value: String) : UiText()
}

/** Pure lookup, usable from tests and from both apps. */
object Strings {

    fun get(key: StringKey, language: AppLanguage): String =
        table(language)[key] ?: EnglishStrings[key] ?: key.name

    fun format(key: StringKey, language: AppLanguage, args: List<Any>): String =
        formatTemplate(get(key, language), args)

    private fun table(language: AppLanguage): Map<StringKey, String> = when (language) {
        AppLanguage.English -> EnglishStrings
        AppLanguage.Hindi -> HindiStrings
    }
}

private val PLACEHOLDER = Regex("%(\\d+)\\$[sd]")

/** Replaces `%1$s` / `%1$d` with the matching argument. Unknown positions are left as is. */
internal fun formatTemplate(template: String, args: List<Any>): String =
    PLACEHOLDER.replace(template) { match ->
        val index = match.groupValues[1].toInt() - 1
        args.getOrNull(index)?.toString() ?: match.value
    }

/**
 * The app-wide string resolver, in the user's current language. Both UIs call it:
 * Compose with `localizer.text(StringKey.LoginTitle)`, SwiftUI with `L10n.text(.loginTitle)`.
 */
class Localizer(val language: StateFlow<AppLanguage>) {

    fun text(key: StringKey): String = Strings.get(key, language.value)

    fun format(key: StringKey, args: List<Any>): String = Strings.format(key, language.value, args)

    fun resolve(text: UiText): String = when (text) {
        is UiText.Res -> Strings.format(text.key, language.value, text.args)
        is UiText.Raw -> text.value
    }
}

/** How each typed error reads on screen. One mapping, both platforms. */
fun DanAiaError.toUiText(): UiText = UiText.Res(
    when (this) {
        DanAiaError.Network -> StringKey.ErrorNetwork
        DanAiaError.Timeout -> StringKey.ErrorTimeout
        DanAiaError.Unauthorized -> StringKey.ErrorSessionExpired
        is DanAiaError.Server -> StringKey.ErrorServer
        is DanAiaError.Validation -> when (type) {
            ProblemType.INCORRECT_OTP -> StringKey.OtpErrorIncorrect
            ProblemType.HANDLE_TAKEN -> StringKey.CreateDaHandleTaken
            else -> StringKey.ErrorUnexpected
        }
        is DanAiaError.NotFound, DanAiaError.Unexpected -> StringKey.ErrorUnexpected
    },
)
