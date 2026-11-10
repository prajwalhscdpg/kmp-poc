package com.cdpg.dhruva.domain.location

import com.cdpg.dhruva.core.common.DanAiaResult

data class GeocodingResult(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

data class AddressDetails(
    val city: String?,
    val district: String?,
    val state: String?,
    val pincode: String?,
) {
    companion object {
        val Empty = AddressDetails(city = null, district = null, state = null, pincode = null)
    }
}

interface GeocodingRepository {
    suspend fun search(query: String): DanAiaResult<List<GeocodingResult>>
    suspend fun reverseGeocode(latitude: Double, longitude: Double): DanAiaResult<AddressDetails>
}
