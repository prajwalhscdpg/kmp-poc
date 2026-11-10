package com.cdpg.dhruva.domain.address

import com.cdpg.dhruva.core.common.DanAiaResult
import kotlinx.coroutines.flow.Flow

/**
 * The user's Digital Addresses, offline-first: [observeAddresses] reads the local cache (instant,
 * works offline) and [refresh] updates it from the server. Writes carry an idempotency key and
 * fall back to the outbox when there is no connection.
 */
interface AddressRepository {

    /** Newest first, including addresses still waiting to sync. */
    fun observeAddresses(): Flow<List<DigitalAddress>>

    /**
     * Refreshes the cache from the server. Without [force] it does nothing if the cache is
     * fresher than [STALE_AFTER_MINUTES] (Dhruva Android's caching policy).
     */
    suspend fun refresh(force: Boolean): DanAiaResult<Unit>

    suspend fun create(address: NewDigitalAddress): DanAiaResult<CreateAddressOutcome>

    companion object {
        const val STALE_AFTER_MINUTES = 15
    }
}
