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
import com.cdpg.dhruva.domain.auth.MobileNumberError
import com.cdpg.dhruva.domain.auth.ValidateMobileNumberUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    /** Digits only, at most 10. */
    val phoneNumber: String = "",
    val error: UiText? = null,
    val isSendingOtp: Boolean = false,
) {
    /** Figma (both apps): enabled as soon as a digit is typed; a wrong number is reported on tap. */
    val isContinueEnabled: Boolean get() = phoneNumber.isNotEmpty() && !isSendingOtp
}

sealed class LoginEvent {
    /** The OTP was sent; show OTP verification for [phoneNumber]. */
    data class OtpSent(val phoneNumber: String) : LoginEvent()
}

/**
 * "Log in or Sign up". Error timing follows both apps: a wrong first digit is shown live; a
 * too-short number only on Continue, and it clears on the next edit.
 *
 * Unlike Android (which sent the OTP from the OTP screen), the OTP is requested here so a send
 * failure is shown next to the number — the flow iOS's TODO asked for.
 */
class LoginViewModel(
    private val validate: ValidateMobileNumberUseCase,
    private val authRepository: AuthRepository,
) : SharedViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()
    val stateForSwift: CStateFlow<LoginUiState> get() = state.asCStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()
    val eventsForSwift: CFlow<LoginEvent> get() = events.asCFlow()

    fun onPhoneNumberChange(input: String) {
        val number = validate.sanitize(input)
        _state.update { it.copy(phoneNumber = number, error = validate.liveError(number)?.toUiText()) }
    }

    /** Continue button or keyboard Done. */
    fun onContinue() {
        val current = _state.value
        if (!current.isContinueEnabled) return
        validate.submitError(current.phoneNumber)?.let { error ->
            _state.update { it.copy(error = error.toUiText()) }
            return
        }
        _state.update { it.copy(isSendingOtp = true, error = null) }
        scope.launch {
            when (val result = authRepository.requestOtp(current.phoneNumber)) {
                is DanAiaResult.Success -> {
                    _state.update { it.copy(isSendingOtp = false) }
                    _events.send(LoginEvent.OtpSent(current.phoneNumber))
                }
                is DanAiaResult.Error -> _state.update { it.copy(isSendingOtp = false, error = result.error.toUiText()) }
            }
        }
    }
}

private fun MobileNumberError.toUiText(): UiText = UiText.Res(
    when (this) {
        MobileNumberError.InvalidStart -> StringKey.LOGIN_ERROR_INVALID_START
        MobileNumberError.Incomplete -> StringKey.LOGIN_ERROR_INVALID_NUMBER
    },
)
