package com.cdpg.dhruva.data.location

import com.cdpg.dhruva.core.common.DanAiaResult
import com.cdpg.dhruva.core.network.apiCall
import com.cdpg.dhruva.domain.location.AddressDetails
import com.cdpg.dhruva.domain.location.GeocodingRepository
import com.cdpg.dhruva.domain.location.GeocodingResult

internal class GeocodingRepositoryImpl(private val client: MapTilerGeocodingClient) : GeocodingRepository {

    override suspend fun search(query: String): DanAiaResult<List<GeocodingResult>> =
        apiCall { client.search(query) }

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): DanAiaResult<AddressDetails> =
        apiCall { client.reverseGeocode(latitude, longitude) }
}
