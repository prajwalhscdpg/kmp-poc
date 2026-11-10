package com.cdpg.dhruva.data.location

import com.cdpg.dhruva.core.network.bodyOrThrow

import com.cdpg.dhruva.domain.location.AddressDetails
import com.cdpg.dhruva.domain.location.GeocodingResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.encodeURLPathPart
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * MapTiler geocoding (Create DA location search), ported from Dhruva iOS's
 * MapTilerGeocodingClient. Mapbox-compatible GeoJSON: features with `place_name` and
 * `center: [longitude, latitude]`; reverse geocoding's `context` carries City / District /
 * State / Pincode under the `municipality` / `subregion` / `region` / `postal_code` id prefixes.
 *
 * With no API key (the mock config) it returns a few fixed Bengaluru places so the flow works.
 */
class MapTilerGeocodingClient(
    private val client: HttpClient,
    private val apiKey: String,
) {
    suspend fun search(query: String): List<GeocodingResult> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        if (apiKey.isBlank()) return OFFLINE_PLACES.filter { it.name.contains(trimmed, ignoreCase = true) }

        val response = client.get("$BASE_URL/${trimmed.encodeURLPathPart()}.json") {
            parameter("key", apiKey)
            parameter("country", "in")
            parameter("limit", 8)
        }.bodyOrThrow<FeatureCollection>()

        return response.features.mapNotNull { feature ->
            if (feature.center.size != 2) return@mapNotNull null
            GeocodingResult(
                id = feature.id,
                name = feature.placeName,
                latitude = feature.center[1],
                longitude = feature.center[0],
            )
        }
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): AddressDetails {
        if (apiKey.isBlank()) return AddressDetails(city = "Bengaluru", district = "Bengaluru Urban", state = "Karnataka", pincode = "560003")

        val response = client.get("$BASE_URL/$longitude,$latitude.json") {
            parameter("key", apiKey)
        }.bodyOrThrow<FeatureCollection>()

        val context = response.features.firstOrNull()?.context ?: return AddressDetails.Empty
        fun text(prefix: String) = context.firstOrNull { it.id.startsWith(prefix) }?.text
        return AddressDetails(
            city = text("municipality"),
            district = text("subregion"),
            state = text("region"),
            pincode = text("postal_code")?.replace(" ", ""),
        )
    }

    @Serializable
    private data class FeatureCollection(val features: List<Feature>)

    @Serializable
    private data class Feature(
        val id: String,
        @SerialName("place_name") val placeName: String,
        val center: List<Double>,
        val context: List<ContextEntry>? = null,
    )

    @Serializable
    private data class ContextEntry(val id: String, val text: String)

    private companion object {
        const val BASE_URL = "https://api.maptiler.com/geocoding"

        val OFFLINE_PLACES = listOf(
            GeocodingResult("malleshwaram", "Malleshwaram, Bengaluru, Karnataka", 13.0033, 77.5700),
            GeocodingResult("indiranagar", "Indiranagar, Bengaluru, Karnataka", 12.9784, 77.6408),
            GeocodingResult("hsr-layout", "HSR Layout, Bengaluru, Karnataka", 12.9116, 77.6474),
            GeocodingResult("koramangala", "Koramangala, Bengaluru, Karnataka", 12.9352, 77.6245),
        )
    }
}
