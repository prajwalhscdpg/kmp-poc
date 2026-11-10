package com.cdpg.dhruva.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Wire format of the Dhruva API, shared by both apps. The envelope matches the published
 * OpenAPI schema used by Dhruva iOS (`GET /app/v1/address`):
 *   200 → { type: "urn:dhruva:app:Success", title, results: { … } }
 *   4xx → { type: "urn:dhruva:app:<Problem>", title, detail }
 * Endpoints other than /app/v1/address are placeholders until the backend publishes them.
 */

@Serializable
data class ApiEnvelope<T>(
    val type: String? = null,
    val title: String? = null,
    val detail: String? = null,
    val results: T? = null,
)

@Serializable
data class ProblemEnvelope(
    val type: String? = null,
    val title: String? = null,
    val detail: String? = null,
)

// Auth

@Serializable
data class SendOtpRequest(@SerialName("phone_number") val phoneNumber: String)

@Serializable
data class VerifyOtpRequest(
    @SerialName("phone_number") val phoneNumber: String,
    val otp: String,
)

@Serializable
data class TokenResults(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("is_new_user") val isNewUser: Boolean = false,
)

@Serializable
data class RefreshTokenRequest(@SerialName("refresh_token") val refreshToken: String)

@Serializable
data class UpdateProfileRequest(@SerialName("full_name") val fullName: String)

// Addresses

@Serializable
data class AddressDto(
    val id: String,
    @SerialName("digital_address") val digitalAddress: String,
    val title: String,
    /** Free text per the OpenAPI spec ("home", "work"…), not a closed enum. */
    val type: String,
    /** ISO-8601. */
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class AddressListResults(val addresses: List<AddressDto>)

@Serializable
data class AddressResults(val address: AddressDto)

@Serializable
data class CreateAddressRequest(
    val title: String,
    val type: String,
    val prefix: String,
    /** Including the "@", e.g. "@dap.dop". */
    val suffix: String,
    val digipin: String?,
    val latitude: Double,
    val longitude: Double,
    val unit: String? = null,
    val floor: String? = null,
    val building: String? = null,
    val city: String? = null,
    val district: String? = null,
    val state: String? = null,
    val pincode: String? = null,
)

object ApiPaths {
    const val SEND_OTP = "auth/otp"
    const val VERIFY_OTP = "auth/verify"
    const val REFRESH = "auth/refresh"
    const val PROFILE = "app/v1/profile"
    const val ADDRESSES = "app/v1/address"
}

object ApiHeaders {
    const val IDEMPOTENCY_KEY = "Idempotency-Key"
    const val APP_VERSION = "X-App-Version"
    const val PLATFORM = "X-Platform"
}
