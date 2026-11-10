package com.cdpg.dhruva.presentation.createaddress

import com.cdpg.dhruva.core.common.CFlow
import com.cdpg.dhruva.core.common.CStateFlow
import com.cdpg.dhruva.core.common.DanAiaResult
import com.cdpg.dhruva.core.common.SharedViewModel
import com.cdpg.dhruva.core.common.asCFlow
import com.cdpg.dhruva.core.common.asCStateFlow
import com.cdpg.dhruva.core.strings.StringKey
import com.cdpg.dhruva.core.strings.UiText
import com.cdpg.dhruva.core.strings.toUiText
import com.cdpg.dhruva.domain.address.AddressRepository
import com.cdpg.dhruva.domain.address.AddressSuffixes
import com.cdpg.dhruva.domain.address.AddressType
import com.cdpg.dhruva.domain.address.CreateAddressOutcome
import com.cdpg.dhruva.domain.address.FloorOption
import com.cdpg.dhruva.domain.address.NewDigitalAddress
import com.cdpg.dhruva.domain.address.ValidateAddressPrefixUseCase
import com.cdpg.dhruva.domain.digipin.Digipin
import com.cdpg.dhruva.domain.location.AddressDetails
import com.cdpg.dhruva.domain.location.GeocodingRepository
import com.cdpg.dhruva.domain.location.GeocodingResult
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

data class CreateAddressUiState(
    // Step 1: map + DIGIPIN. The native map moves; the pin stays centred and reports here.
    val latitude: Double = DEFAULT_LATITUDE,
    val longitude: Double = DEFAULT_LONGITUDE,
    val searchText: String = "",
    val searchResults: List<GeocodingResult> = emptyList(),
    /** One-shot: the native map recenters here after a search pick or "Current Location". */
    val recenterRequest: GeocodingResult? = null,
    val locationError: UiText? = null,

    // Step 2: additional details.
    val details: AddressDetails? = null,
    val unit: String = "",
    val floor: FloorOption = FloorOption.Ground,
    val building: String = "",

    // Step 3: nickname, type and handle.
    val type: AddressType = AddressType.Home,
    val nickname: String = "",
    val prefix: String = "",
    val suffix: String = AddressSuffixes.DEFAULT,
    val nicknameError: UiText? = null,
    val prefixError: UiText? = null,

    // Step 4: confirm.
    val isSubmitting: Boolean = false,
    val submitError: UiText? = null,
) {
    /** Raw 10-character DIGIPIN, or null outside coverage. */
    val digipin: String? get() = Digipin.encode(latitude, longitude)

    /** "39J-438-TJC7", or "Outside DIGIPIN coverage area". */
    val digipinDisplay: UiText
        get() = digipin?.let { UiText.Raw(Digipin.formatForDisplay(it)) }
            ?: UiText.Res(StringKey.CREATE_DA_DIGIPIN_UNAVAILABLE)

    val canProceedFromMap: Boolean get() = digipin != null

    val previewNickname: UiText
        get() = if (nickname.isBlank()) UiText.Res(StringKey.CREATE_DA_PREVIEW_NICKNAME_PLACEHOLDER) else UiText.Raw(nickname)

    val previewHandle: UiText
        get() = if (prefix.isEmpty()) UiText.Res(StringKey.CREATE_DA_PREVIEW_HANDLE_PLACEHOLDER) else UiText.Raw(prefix + suffix)

    val suffixOptions: List<String> get() = AddressSuffixes.options
    val floorOptions: List<FloorOption> get() = FloorOption.entries
    val typeOptions: List<AddressType> get() = AddressType.entries

    companion object {
        /** Malleshwaram, Bengaluru — the Figma mock's default pin (Dhruva iOS). */
        const val DEFAULT_LATITUDE = 13.0033
        const val DEFAULT_LONGITUDE = 77.5700
    }
}

sealed class CreateAddressEvent {
    data object GoToDetails : CreateAddressEvent()
    data object GoToNickname : CreateAddressEvent()
    data object GoToConfirm : CreateAddressEvent()

    /** Finished. [message] is shown when it was saved offline and will sync later. */
    data class Finished(val message: UiText?) : CreateAddressEvent()
}

/**
 * The Create DA wizard's logic (map → details → nickname/handle → confirm), from Dhruva iOS's
 * CreateDAViewModel. The map itself, current-location permission and step navigation are
 * native; DIGIPIN, search, reverse geocoding, validation and the offline-capable submit are here.
 */
class CreateAddressViewModel(
    private val geocoding: GeocodingRepository,
    private val repository: AddressRepository,
    private val validatePrefix: ValidateAddressPrefixUseCase,
) : SharedViewModel() {

    private val _state = MutableStateFlow(CreateAddressUiState())
    val state: StateFlow<CreateAddressUiState> = _state.asStateFlow()
    val stateForSwift: CStateFlow<CreateAddressUiState> get() = state.asCStateFlow()

    private val _events = Channel<CreateAddressEvent>(Channel.BUFFERED)
    val events: Flow<CreateAddressEvent> = _events.receiveAsFlow()
    val eventsForSwift: CFlow<CreateAddressEvent> get() = events.asCFlow()

    private var searchJob: Job? = null
    private var detailsJob: Job? = null

    // Step 1

    /** The map was panned; the centre pin is now here. */
    fun onMapMoved(latitude: Double, longitude: Double) {
        _state.update { it.copy(latitude = latitude, longitude = longitude) }
    }

    /** Debounced search, at least 3 characters (Dhruva iOS behaviour). */
    fun onSearchTextChange(text: String) {
        _state.update { it.copy(searchText = text) }
        searchJob?.cancel()
        if (text.trim().length < MIN_SEARCH_LENGTH) {
            _state.update { it.copy(searchResults = emptyList()) }
            return
        }
        searchJob = scope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            val results = (geocoding.search(text) as? DanAiaResult.Success)?.data.orEmpty()
            _state.update { it.copy(searchResults = results) }
        }
    }

    fun onSearchResultSelected(result: GeocodingResult) {
        searchJob?.cancel()
        _state.update {
            it.copy(
                latitude = result.latitude,
                longitude = result.longitude,
                recenterRequest = result,
                searchText = "",
                searchResults = emptyList(),
            )
        }
    }

    /** The native layer got a fix for "Current Location". */
    fun onCurrentLocation(latitude: Double, longitude: Double) {
        onSearchResultSelected(GeocodingResult("current", "", latitude, longitude))
    }

    fun onCurrentLocationFailed(permissionDenied: Boolean) {
        val key = if (permissionDenied) StringKey.CREATE_DA_LOCATION_PERMISSION_DENIED else StringKey.CREATE_DA_LOCATION_UNAVAILABLE
        _state.update { it.copy(locationError = UiText.Res(key)) }
    }

    fun onLocationErrorShown() = _state.update { it.copy(locationError = null) }

    fun onRecenterHandled() = _state.update { it.copy(recenterRequest = null) }

    fun onMapProceed() {
        if (!_state.value.canProceedFromMap) return
        loadDetails()
        scope.launch { _events.send(CreateAddressEvent.GoToDetails) }
    }

    // Step 2

    fun onUnitChange(value: String) = _state.update { it.copy(unit = value) }
    fun onBuildingChange(value: String) = _state.update { it.copy(building = value) }
    fun onFloorSelected(floor: FloorOption) = _state.update { it.copy(floor = floor) }

    fun onDetailsProceed() {
        scope.launch { _events.send(CreateAddressEvent.GoToNickname) }
    }

    // Step 3

    fun onTypeSelected(type: AddressType) = _state.update { it.copy(type = type) }
    fun onNicknameChange(value: String) = _state.update { it.copy(nickname = value, nicknameError = null) }
    fun onPrefixChange(value: String) = _state.update { it.copy(prefix = validatePrefix.sanitize(value), prefixError = null) }
    fun onSuffixSelected(suffix: String) = _state.update { it.copy(suffix = suffix) }

    fun onNicknameProceed() {
        val current = _state.value
        val nicknameError = if (current.nickname.isBlank()) UiText.Res(StringKey.CREATE_DA_NICKNAME_REQUIRED) else null
        val prefixError = if (!validatePrefix(current.prefix)) UiText.Res(StringKey.CREATE_DA_PREFIX_INVALID) else null
        _state.update { it.copy(nicknameError = nicknameError, prefixError = prefixError) }
        if (nicknameError == null && prefixError == null) {
            scope.launch { _events.send(CreateAddressEvent.GoToConfirm) }
        }
    }

    // Step 4

    fun onConfirm() {
        val current = _state.value
        if (current.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, submitError = null) }
        scope.launch {
            val address = NewDigitalAddress(
                title = current.nickname.trim(),
                type = current.type,
                prefix = current.prefix,
                suffix = current.suffix,
                digipin = current.digipin,
                latitude = current.latitude,
                longitude = current.longitude,
                unit = current.unit.trim(),
                floor = current.floor.name,
                building = current.building.trim(),
                details = current.details ?: AddressDetails.Empty,
            )
            when (val result = repository.create(address)) {
                is DanAiaResult.Success -> {
                    _state.update { it.copy(isSubmitting = false) }
                    val message = when (result.data) {
                        is CreateAddressOutcome.Created -> null
                        is CreateAddressOutcome.Queued -> UiText.Res(StringKey.CREATE_DA_QUEUED_OFFLINE)
                    }
                    _events.send(CreateAddressEvent.Finished(message))
                }
                is DanAiaResult.Error -> _state.update { it.copy(isSubmitting = false, submitError = result.error.toUiText()) }
            }
        }
    }

    private fun loadDetails() {
        detailsJob?.cancel()
        val current = _state.value
        detailsJob = scope.launch {
            val details = (geocoding.reverseGeocode(current.latitude, current.longitude) as? DanAiaResult.Success)?.data
            _state.update { it.copy(details = details ?: AddressDetails.Empty) }
        }
    }

    private companion object {
        const val MIN_SEARCH_LENGTH = 3
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}
