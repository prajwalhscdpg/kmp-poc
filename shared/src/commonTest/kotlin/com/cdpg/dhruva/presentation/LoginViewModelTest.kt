package com.cdpg.dhruva.presentation

import app.cash.turbine.test
import com.cdpg.dhruva.core.common.DanAiaError
import com.cdpg.dhruva.core.common.DanAiaResult
import com.cdpg.dhruva.core.strings.StringKey
import com.cdpg.dhruva.core.strings.UiText
import com.cdpg.dhruva.domain.auth.AuthRepository
import com.cdpg.dhruva.domain.auth.OtpVerification
import com.cdpg.dhruva.domain.auth.ValidateMobileNumberUseCase
import com.cdpg.dhruva.presentation.auth.LoginEvent
import com.cdpg.dhruva.presentation.auth.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private class FakeAuth(var sendResult: DanAiaResult<Unit> = DanAiaResult.Success(Unit)) : AuthRepository {
        val sentTo = mutableListOf<String>()
        override suspend fun requestOtp(phoneNumber: String): DanAiaResult<Unit> {
            sentTo += phoneNumber
            return sendResult
        }
        override suspend fun verifyOtp(phoneNumber: String, otp: String): DanAiaResult<OtpVerification> =
            DanAiaResult.Success(OtpVerification.Verified(isNewUser = false))
    }

    private val auth = FakeAuth()
    private lateinit var viewModel: LoginViewModel

    @BeforeTest fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = LoginViewModel(ValidateMobileNumberUseCase(), auth)
    }

    @AfterTest fun tearDown() {
        viewModel.clear()
        Dispatchers.resetMain()
    }

    @Test fun wrongFirstDigitShowsTheErrorWhileTyping() {
        viewModel.onPhoneNumberChange("5")
        assertEquals(UiText.Res(StringKey.LoginErrorInvalidStart), viewModel.state.value.error)
    }

    @Test fun incompleteNumberErrorAppearsOnContinueAndClearsOnEdit() {
        viewModel.onPhoneNumberChange("98450")
        assertNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.isContinueEnabled)

        viewModel.onContinue()
        assertEquals(UiText.Res(StringKey.LoginErrorInvalidNumber), viewModel.state.value.error)
        assertTrue(auth.sentTo.isEmpty())

        viewModel.onPhoneNumberChange("984501")
        assertNull(viewModel.state.value.error)
    }

    @Test fun validNumberSendsTheOtpAndNavigates() = runTest {
        viewModel.events.test {
            viewModel.onPhoneNumberChange("98450 12345")
            viewModel.onContinue()
            assertEquals(LoginEvent.OtpSent("9845012345"), awaitItem())
        }
        assertEquals(listOf("9845012345"), auth.sentTo)
        assertFalse(viewModel.state.value.isSendingOtp)
    }

    @Test fun sendFailureIsShownNextToTheNumber() {
        auth.sendResult = DanAiaResult.Error(DanAiaError.Network)
        viewModel.onPhoneNumberChange("9845012345")
        viewModel.onContinue()
        assertEquals(UiText.Res(StringKey.ErrorNetwork), viewModel.state.value.error)
    }
}
