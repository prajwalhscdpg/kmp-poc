package com.cdpg.dhruva.data.address

import com.cdpg.dhruva.core.database.AddressEntity
import com.cdpg.dhruva.core.database.SyncState
import com.cdpg.dhruva.core.network.AddressDto
import com.cdpg.dhruva.core.network.CreateAddressRequest
import com.cdpg.dhruva.domain.address.AddressType
import com.cdpg.dhruva.domain.address.DigitalAddress
import com.cdpg.dhruva.domain.address.NewDigitalAddress
import kotlin.time.Instant

internal fun AddressDto.toEntity(): AddressEntity = AddressEntity(
    id = id,
    digitalAddress = digitalAddress,
    title = title,
    type = type,
    createdAtEpochMs = parseEpochMs(createdAt),
    updatedAtEpochMs = parseEpochMs(updatedAt),
    syncState = SyncState.SYNCED,
)

internal fun AddressEntity.toDomain(): DigitalAddress = DigitalAddress(
    id = id,
    handle = digitalAddress,
    title = title,
    type = AddressType.fromApi(type),
    createdAtEpochMs = createdAtEpochMs,
    isPendingSync = syncState == SyncState.PENDING,
)

internal fun NewDigitalAddress.toRequest(): CreateAddressRequest = CreateAddressRequest(
    title = title,
    type = type.apiValue,
    prefix = prefix,
    suffix = suffix,
    digipin = digipin,
    latitude = latitude,
    longitude = longitude,
    unit = unit.ifBlank { null },
    floor = floor.ifBlank { null },
    building = building.ifBlank { null },
    city = details.city,
    district = details.district,
    state = details.state,
    pincode = details.pincode,
)

private fun parseEpochMs(iso: String): Long =
    runCatching { Instant.parse(iso).toEpochMilliseconds() }.getOrDefault(0L)
