package com.cdpg.dhruva.presentation.addresses

import com.cdpg.dhruva.core.common.CStateFlow
import com.cdpg.dhruva.core.common.DanAiaError
import com.cdpg.dhruva.core.common.DanAiaResult
import com.cdpg.dhruva.core.common.SharedViewModel
import com.cdpg.dhruva.core.common.asCStateFlow
import com.cdpg.dhruva.core.network.MockDhruvaBackend
import com.cdpg.dhruva.core.session.SessionManager
import com.cdpg.dhruva.core.strings.StringKey
import com.cdpg.dhruva.core.strings.UiText
import com.cdpg.dhruva.core.strings.toUiText
import com.cdpg.dhruva.domain.address.AddressRepository
import com.cdpg.dhruva.domain.address.DigitalAddress
import com.cdpg.dhruva.domain.address.SearchAddressesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyAddressesUiState(
    val query: String = "",
    /** Already filtered by [query]. */
    val addresses: List<DigitalAddress> = emptyList(),
    /** First load with nothing cached yet: show a spinner instead of the empty state. */
    val isLoading: Boolean = true,
    /** Pull-to-refresh / background refresh with cached rows on screen. */
    val isRefreshing: Boolean = false,
    /** The last refresh failed for lack of connection but cached rows are shown. */
    val isOffline: Boolean = false,
    /** Nothing cached and the refresh failed: full-screen error with Retry. */
    val error: UiText? = null,
    /** No addresses at all (not a search miss). */
    val isEmpty: Boolean = false,
    /** Search typed but nothing matches. */
    val noResults: UiText? = null,
    /** Mock builds only: lets testers cut the "network" to see offline behaviour. */
    val canSimulateOffline: Boolean = false,
    val isSimulatingOffline: Boolean = false,
)

/**
 * My Digital Addresses, offline-first: the list comes from the local cache (instant, works with
 * no connection) and a refresh updates it in the background. Replaces iOS's MyDAViewModel
 * (which fetched on every open and showed nothing offline) and Android's empty placeholder.
 *
 * 404 NoAddressesFound is an empty list, not an error; 401 ends the session (handled by the
 * network layer, both apps then route to Login).
 */
class MyAddressesViewModel(
    private val repository: AddressRepository,
    private val search: SearchAddressesUseCase,
    private val session: SessionManager,
    private val mockBackend: MockDhruvaBackend?,
) : SharedViewModel() {

    private val query = MutableStateFlow("")
    private val refreshStatus = MutableStateFlow(RefreshStatus())
    private val simulatingOffline = mockBackend?.simulateOffline ?: MutableStateFlow(false)

    val state: StateFlow<MyAddressesUiState> = combine(
        repository.observeAddresses(),
        query,
        refreshStatus,
        simulatingOffline,
    ) { all, query, status, offline ->
        val filtered = search(all, query)
        val nothingCached = all.isEmpty()
        val failure = status.lastError
        MyAddressesUiState(
            query = query,
            addresses = filtered,
            isLoading = nothingCached && !status.hasCompletedOnce,
            isRefreshing = status.isRefreshing && !nothingCached,
            isOffline = !nothingCached && failure?.isConnectivityProblem == true,
            error = if (nothingCached && failure != null) failure.toUiText() else null,
            isEmpty = nothingCached && status.hasCompletedOnce && failure == null,
            noResults = if (!nothingCached && filtered.isEmpty() && query.isNotBlank()) {
                UiText.Res(StringKey.MY_DA_NO_SEARCH_RESULTS, listOf(query.trim()))
            } else {
                null
            },
            canSimulateOffline = mockBackend != null,
            isSimulatingOffline = offline,
        )
    }.stateIn(scope, SharingStarted.Eagerly, MyAddressesUiState(canSimulateOffline = mockBackend != null))

    val stateForSwift: CStateFlow<MyAddressesUiState> get() = state.asCStateFlow()

    /** On screen appear: refreshes only if the cache is stale (15 min). */
    fun onAppear() = refresh(force = false)

    fun onRefresh() = refresh(force = true)

    fun onRetry() = refresh(force = true)

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onSimulateOfflineChange(enabled: Boolean) {
        mockBackend?.simulateOffline?.value = enabled
        if (!enabled) refresh(force = true)
    }

    fun onSignOut() {
        scope.launch { session.signOut() }
    }

    private fun refresh(force: Boolean) {
        if (refreshStatus.value.isRefreshing) return
        refreshStatus.update { it.copy(isRefreshing = true) }
        scope.launch {
            val result = repository.refresh(force)
            val error = (result as? DanAiaResult.Error)?.error
            refreshStatus.value = RefreshStatus(
                isRefreshing = false,
                hasCompletedOnce = true,
                // Unauthorized is handled by the session (back to Login); don't flash an error.
                lastError = error.takeUnless { it == DanAiaError.Unauthorized },
            )
        }
    }

    private data class RefreshStatus(
        val isRefreshing: Boolean = false,
        val hasCompletedOnce: Boolean = false,
        val lastError: DanAiaError? = null,
    )
}
