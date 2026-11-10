package com.cdpg.dhruva.core

import com.cdpg.dhruva.core.common.DanAiaError
import com.cdpg.dhruva.core.common.ProblemType
import com.cdpg.dhruva.core.strings.AppLanguage
import com.cdpg.dhruva.core.strings.StringKey
import com.cdpg.dhruva.core.strings.Strings
import com.cdpg.dhruva.core.strings.UiText
import com.cdpg.dhruva.core.strings.toUiText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class StringsTest {

    @Test fun everyKeyHasEnglishText() {
        StringKey.entries.forEach { key ->
            assertNotEquals(key.name, Strings.get(key, AppLanguage.English), "Missing English for $key")
        }
    }

    @Test fun hindiFallsBackToEnglishNeverToTheKey() {
        StringKey.entries.forEach { key ->
            assertNotEquals(key.name, Strings.get(key, AppLanguage.Hindi), "No text at all for $key")
        }
    }

    @Test fun formatsPositionalArguments() {
        assertEquals("Resend in 12s", Strings.format(StringKey.OTP_RESEND_IN, AppLanguage.English, listOf(12)))
        assertEquals("12 सेकंड में फिर से भेजें", Strings.format(StringKey.OTP_RESEND_IN, AppLanguage.Hindi, listOf(12)))
        assertEquals("Welcome Ravi.", Strings.format(StringKey.WELCOME_USER_GREETING, AppLanguage.English, listOf("Ravi")))
    }

    @Test fun languageFromTag() {
        assertEquals(AppLanguage.Hindi, AppLanguage.fromTag("hi-IN"))
        assertEquals(AppLanguage.English, AppLanguage.fromTag("ta-IN"))
        assertEquals(AppLanguage.English, AppLanguage.fromTag(null))
    }

    @Test fun errorsMapToSharedText() {
        assertEquals(UiText.Res(StringKey.ERROR_NETWORK), DanAiaError.Network.toUiText())
        assertEquals(UiText.Res(StringKey.ERROR_SESSION_EXPIRED), DanAiaError.Unauthorized.toUiText())
        assertEquals(UiText.Res(StringKey.CREATE_DA_HANDLE_TAKEN), DanAiaError.Validation(ProblemType.HANDLE_TAKEN).toUiText())
        assertEquals(UiText.Res(StringKey.ERROR_SERVER), DanAiaError.Server(503).toUiText())
    }
}
