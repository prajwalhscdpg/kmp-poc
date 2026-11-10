package com.cdpg.dhruva.data.address

import kotlinx.serialization.encodeToString
import io.ktor.client.request.url
import com.cdpg.dhruva.core.common.AppClock
import com.cdpg.dhruva.core.common.DanAiaError
import com.cdpg.dhruva.core.common.DanAiaResult
import com.cdpg.dhruva.core.common.ProblemType
import com.cdpg.dhruva.core.database.AddressEntity
import com.cdpg.dhruva.core.database.DhruvaDatabase
import com.cdpg.dhruva.core.database.OutboxEntity
import com.cdpg.dhruva.core.database.OutboxKind
import com.cdpg.dhruva.core.database.SyncKeys
import com.cdpg.dhruva.core.database.SyncMetaEntity
import com.cdpg.dhruva.core.database.SyncState
import com.cdpg.dhruva.core.network.AddressListResults
import com.cdpg.dhruva.core.network.ApiEnvelope
import com.cdpg.dhruva.core.network.ApiPaths
import com.cdpg.dhruva.core.network.DhruvaHttp
import com.cdpg.dhruva.core.network.DhruvaJson
import com.cdpg.dhruva.core.network.apiCall
import com.cdpg.dhruva.core.network.bodyOrThrow
import com.cdpg.dhruva.domain.address.AddressRepository
import com.cdpg.dhruva.domain.address.CreateAddressOutcome
import com.cdpg.dhruva.domain.address.DigitalAddress
import com.cdpg.dhruva.domain.address.NewDigitalAddress
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

internal class AddressRepositoryImpl(
    private val http: DhruvaHttp,
    private val database: DhruvaDatabase,
    private val outbox: OutboxProcessor,
    private val clock: AppClock,
) : AddressRepository {

    private val addressDao = database.addressDao()
    private val outboxDao = database.outboxDao()
    private val syncMetaDao = database.syncMetaDao()

    override fun observeAddresses(): Flow<List<DigitalAddress>> =
        addressDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun refresh(force: Boolean): DanAiaResult<Unit> {
        if (!force && isFresh()) return DanAiaResult.Success(Unit)

        // Deliver queued writes first, so the list we fetch already contains them.
        outbox.drain()

        val result = apiCall {
            http.execute(authenticated = true) {
                method = HttpMethod.Get
                url(ApiPaths.ADDRESSES)
            }.bodyOrThrow<ApiEnvelope<AddressListResults>>().results?.addresses.orEmpty()
        }
        val addresses = when (result) {
            is DanAiaResult.Success -> result.data
            is DanAiaResult.Error -> {
                val error = result.error
                // 404 NoAddressesFound is a valid empty list, not an error (OpenAPI spec, Dhruva iOS).
                if (error is DanAiaError.NotFound && error.type == ProblemType.NO_ADDRESSES_FOUND) {
                    emptyList()
                } else {
                    return result
                }
            }
        }

        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                addressDao.deleteSynced()
                addressDao.upsertAll(addresses.map { it.toEntity() })
                syncMetaDao.upsert(SyncMetaEntity(SyncKeys.ADDRESSES, clock.now().toEpochMilliseconds()))
            }
        }
        return DanAiaResult.Success(Unit)
    }

    override suspend fun create(address: NewDigitalAddress): DanAiaResult<CreateAddressOutcome> {
        // One key per user action, reused by every retry, so the server never creates it twice.
        val idempotencyKey = Uuid.random().toString()
        val now = clock.now().toEpochMilliseconds()
        val pending = AddressEntity(
            id = "local-$idempotencyKey",
            digitalAddress = address.handle,
            title = address.title,
            type = address.type.apiValue,
            createdAtEpochMs = now,
            updatedAtEpochMs = now,
            syncState = SyncState.PENDING,
        )
        val request = address.toRequest()

        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                addressDao.upsert(pending)
                outboxDao.insert(
                    OutboxEntity(
                        idempotencyKey = idempotencyKey,
                        kind = OutboxKind.CREATE_ADDRESS,
                        payloadJson = DhruvaJson.encodeToString(request),
                        localId = pending.id,
                        createdAtEpochMs = now,
                    ),
                )
            }
        }

        // Try right away; on a connectivity problem the item simply stays queued.
        return when (val delivery = outbox.deliverCreateAddress(idempotencyKey)) {
            is OutboxProcessor.Delivery.Delivered -> DanAiaResult.Success(CreateAddressOutcome.Created(delivery.address))
            OutboxProcessor.Delivery.Queued -> DanAiaResult.Success(CreateAddressOutcome.Queued(pending.toDomain()))
            is OutboxProcessor.Delivery.Rejected -> DanAiaResult.Error(delivery.error)
        }
    }

    private suspend fun isFresh(): Boolean {
        val lastSynced = syncMetaDao.lastSynced(SyncKeys.ADDRESSES) ?: return false
        val age = clock.now().toEpochMilliseconds() - lastSynced
        return age < AddressRepository.STALE_AFTER_MINUTES.minutes.inWholeMilliseconds
    }
}
