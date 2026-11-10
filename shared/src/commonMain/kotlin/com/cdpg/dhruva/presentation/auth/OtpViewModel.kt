package com.cdpg.dhruva.presentation.auth

import com.cdpg.dhruva.core.common.CFlow
import com.cdpg.dhruva.core.common.CStateFlow
import com.cdpg.dhruva.core.common.DanAiaResult
import com.cdpg.dhruva.core.common.SharedViewModel
import com.cdpg.dhruva.core.common.asCFlow
import com.cdpg.dhruva.core.common.asCStateFlow
import com.cdpg.dhruva.core.strings.StringKey
import com.cdpg.dhruva.core.strings.UiText
import com.cdpg.dhruva.core.strings.toUiText
import com.cdpg.dhruva.domain.auth.AuthRepository
import com.cdpg.dhruva.domain.auth.OtpPolicy
import com.cdpg.dhruva.domain.auth.OtpVerification
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OtpUiState(
    val phoneNumber: String,
    val otp: String = "",
    val isVerifying: Boolean = false,
    val error: UiText? = null,
    val resendSecondsLeft: Int = OtpPolicy.RESEND_COUNTDOWN_SECONDS,
    /** Changes every time an OTP is sent; Android restarts SMS autofill on each change. */
    val smsListenKey: Int = 0,
) {
    val isVerifyEnabled: Boolean get() = otp.length == OtpPolicy.LENGTH && !isVerifying
    val canResend: Boolean get() = resendSecondsLeft == 0 && !isVerifying

    /** "We have sent a verification code to your phone number ending with 5577." */
    val codeSentText: UiText get() = UiText.Res(StringKey.OtpCodeSent, listOf(phoneNumber.takeLast(4)))

    /** "Resend in 12s" while counting down, "Resend" once allowed. */
    val resendText: UiText
        get() = if (resendSecondsLeft > 0) UiText.Res(StringKey.OtpResendIn, listOf(resendSecondsLeft))
        else UiText.Res(StringKey.OtpResend)
}

sealed class OtpEvent {
    /** Signed in. New users go on to "What is your name?", returning users to Home. */
    data class Verified(val isNewUser: Boolean) : OtpEvent()
}

/** OTP verification. The OTP has already been sent by [LoginViewModel]. */
class OtpViewModel(
    phoneNumber: String,
    private val authRepository: AuthRepository,
) : SharedViewModel() {

    private val _state = MutableStateFlow(OtpUiState(phoneNumber = phoneNumber))
    val state: StateFlow<OtpUiState> = _state.asStateFlow()
    val stateForSwift: CStateFlow<OtpUiState> get() = state.asCStateFlow()

    private val _events = Channel<OtpEvent>(Channel.BUFFERED)
    val events: Flow<OtpEvent> = _events.receiveAsFlow()
    val eventsForSwift: CFlow<OtpEvent> get() = events.asCFlow()

    private var countdown: Job? = null

    init {
        startCountdown()
    }

    fun onOtpChange(input: String) {
        // The field stays focused while verifying; edits are ignored until the result is in.
        if (_state.value.isVerifying) return
        // Editing after a wrong OTP clears the error (both apps).
        _state.update { it.copy(otp = OtpPolicy.sanitize(input), error = null) }
    }

    /** Continue button or keyboard Done. Does nothing until all digits are in. */
    fun onVerify() {
        val current = _state.value
        if (!current.isVerifyEnabled) return
        _state.update { it.copy(isVerifying = true, error = null) }
        scope.launch {
            val result = authRepository.verifyOtp(current.phoneNumber, current.otp)
            when (result) {
                is DanAiaResult.Success -> when (val verification = result.data) {
                    is OtpVerification.Verified -> {
                        _state.update { it.copy(isVerifying = false) }
                        _events.send(OtpEvent.Verified(verification.isNewUser))
                    }
                    OtpVerification.Incorrect -> _state.update {
                        it.copy(isVerifying = false, error = UiText.Res(StringKey.OtpErrorIncorrect))
                    }
                }
                is DanAiaResult.Error -> _state.update { it.copy(isVerifying = false, error = result.error.toUiText()) }
            }
        }
    }

    /** OTP read from the SMS (Android SMS User Consent / iOS one-time-code autofill): fill and verify. */
    fun onOtpReceivedFromSms(otp: String) {
        if (_state.value.isVerifying) return
        onOtpChange(otp)
        onVerify()
    }

    /** Enabled only once the countdown has finished. Clears the entered OTP. */
    fun onResend() {
        if (!_state.value.canResend) return
        _state.update { it.copy(otp = "", error = null, smsListenKey = it.smsListenKey + 1) }
        startCountdown()
        scope.launch {
            val result = authRepository.requestOtp(_state.value.phoneNumber)
            if (result is DanAiaResult.Error) {
                // Let the user try again straight away.
                countdown?.cancel()
                _state.update { it.copy(resendSecondsLeft = 0, error = result.error.toUiText()) }
            }
        }
    }

    private fun startCountdown() {
        countdown?.cancel()
        _state.update { it.copy(resendSecondsLeft = OtpPolicy.RESEND_COUNTDOWN_SECONDS) }
        countdown = scope.launch {
            while (_state.value.resendSecondsLeft > 0) {
                delay(1_000)
                _state.update { it.copy(resendSecondsLeft = it.resendSecondsLeft - 1) }
            }
        }
    }
}
