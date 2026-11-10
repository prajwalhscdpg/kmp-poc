package com.cdpg.dhruva.presentation.auth

import com.cdpg.dhruva.core.common.CFlow
import com.cdpg.dhruva.core.common.CStateFlow
import com.cdpg.dhruva.core.common.DanAiaResult
import com.cdpg.dhruva.core.common.SharedViewModel
import com.cdpg.dhruva.core.common.asCFlow
import com.cdpg.dhruva.core.common.asCStateFlow
import com.cdpg.dhruva.core.strings.UiText
import com.cdpg.dhruva.core.strings.toUiText
import com.cdpg.dhruva.domain.auth.FullNameFormatter
import com.cdpg.dhruva.domain.profile.ProfileRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FullNameUiState(
    val fullName: String = "",
    val isContinueEnabled: Boolean = false,
    val isSaving: Boolean = false,
    val error: UiText? = null,
)

sealed class FullNameEvent {
    /** Saved. [firstName] is used for "Welcome Jyotirmoy." */
    data class Submitted(val fullName: String, val firstName: String) : FullNameEvent()
}

/** "What is your name?" for new users. Spaces and characters are cleaned up as the user types. */
class FullNameViewModel(
    private val formatter: FullNameFormatter,
    private val profileRepository: ProfileRepository,
) : SharedViewModel() {

    private val _state = MutableStateFlow(FullNameUiState())
    val state: StateFlow<FullNameUiState> = _state.asStateFlow()
    val stateForSwift: CStateFlow<FullNameUiState> get() = state.asCStateFlow()

    private val _events = Channel<FullNameEvent>(Channel.BUFFERED)
    val events: Flow<FullNameEvent> = _events.receiveAsFlow()
    val eventsForSwift: CFlow<FullNameEvent> get() = events.asCFlow()

    fun onFullNameChange(input: String) {
        val name = formatter.sanitize(input)
        _state.update {
            it.copy(fullName = name, isContinueEnabled = formatter.isComplete(name) && !it.isSaving, error = null)
        }
    }

    /** Continue button or keyboard Done. Saves the name without the trailing space. */
    fun onContinue() {
        val current = _state.value
        if (!current.isContinueEnabled) return
        val fullName = formatter.complete(current.fullName)
        _state.update { it.copy(fullName = fullName, isSaving = true, isContinueEnabled = false) }
        scope.launch {
            when (val result = profileRepository.updateFullName(fullName)) {
                is DanAiaResult.Success -> {
                    _state.update { it.copy(isSaving = false, isContinueEnabled = true) }
                    _events.send(FullNameEvent.Submitted(fullName, formatter.firstName(fullName)))
                }
                is DanAiaResult.Error -> _state.update {
                    it.copy(isSaving = false, isContinueEnabled = true, error = result.error.toUiText())
                }
            }
        }
    }
}
