package com.cdpg.dhruva.domain.address

import com.cdpg.dhruva.core.strings.StringKey
import com.cdpg.dhruva.domain.location.AddressDetails

enum class AddressType(val apiValue: String, val label: StringKey) {
    Home("home", StringKey.AddressTypeHome),
    Work("work", StringKey.AddressTypeWork),
    Other("other", StringKey.AddressTypeOther),
    ;

    companion object {
        /** `type` is free text in the API; matched ignoring case, anything unknown is Other. */
        fun fromApi(value: String): AddressType =
            entries.firstOrNull { it.apiValue.equals(value.trim(), ignoreCase = true) } ?: Other
    }
}

/**
 * A Digital Address in the list ("Yeli Home", home.yeli@dap.dop).
 * [isPendingSync] is true for one created offline that the outbox hasn't delivered yet.
 */
data class DigitalAddress(
    val id: String,
    val handle: String,
    val title: String,
    val type: AddressType,
    val createdAtEpochMs: Long,
    val isPendingSync: Boolean,
)

/** Everything the Create DA wizard collects. */
data class NewDigitalAddress(
    val title: String,
    val type: AddressType,
    val prefix: String,
    /** Including the "@", e.g. "@dap.dop". */
    val suffix: String,
    val digipin: String?,
    val latitude: Double,
    val longitude: Double,
    val unit: String,
    val floor: String,
    val building: String,
    val details: AddressDetails,
) {
    val handle: String get() = prefix + suffix
}

sealed class CreateAddressOutcome {
    /** The server created it. */
    data class Created(val address: DigitalAddress) : CreateAddressOutcome()

    /** No connection: saved locally and queued; the outbox will create it later. */
    data class Queued(val address: DigitalAddress) : CreateAddressOutcome()
}
