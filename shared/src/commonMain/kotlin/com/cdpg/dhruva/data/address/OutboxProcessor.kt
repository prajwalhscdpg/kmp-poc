package com.cdpg.dhruva.data.address

import kotlinx.serialization.decodeFromString
import io.ktor.client.request.url
import com.cdpg.dhruva.core.common.DanAiaError
import com.cdpg.dhruva.core.common.DanAiaResult
import com.cdpg.dhruva.core.database.DhruvaDatabase
import com.cdpg.dhruva.core.database.OutboxEntity
import com.cdpg.dhruva.core.database.OutboxKind
import com.cdpg.dhruva.core.network.AddressResults
import com.cdpg.dhruva.core.network.ApiEnvelope
import com.cdpg.dhruva.core.network.ApiHeaders
import com.cdpg.dhruva.core.network.ApiPaths
import com.cdpg.dhruva.core.network.CreateAddressRequest
import com.cdpg.dhruva.core.network.DhruvaHttp
import com.cdpg.dhruva.core.network.DhruvaJson
import com.cdpg.dhruva.core.network.apiCall
import com.cdpg.dhruva.core.network.bodyOrThrow
import com.cdpg.dhruva.domain.address.DigitalAddress
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import io.ktor.client.request.header
import com.cdpg.dhruva.core.network.jsonBody
import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Replays writes that couldn't reach the server, in order, each with its original
 * Idempotency-Key. Platform schedulers call [drain]: WorkManager on Android (when the network
 * comes back), app-foreground on iOS. Only one drain runs at a time.
 *
 * - Connectivity failure → stop; the item stays queued for the next drain.
 * - Server rejected it (4xx) → drop the item and its local row; the user can't fix it by retrying.
 * - 5xx → keep it and count the attempt.
 */
class OutboxProcessor internal constructor(
    private val http: DhruvaHttp,
    private val database: DhruvaDatabase,
) {
    private val mutex = Mutex()
    private val outboxDao = database.outboxDao()
    private val addressDao = database.addressDao()

    val pendingCount: Flow<Int> = outboxDao.observeCount()

    /** Delivers every queued write it can. Returns how many were delivered. */
    suspend fun drain(): Int = mutex.withLock {
        var delivered = 0
        for (item in outboxDao.pending()) {
            when (deliver(item)) {
                is Delivery.Delivered -> delivered++
                is Delivery.Rejected -> Unit
                Delivery.Queued -> return@withLock delivered
            }
        }
        delivered
    }

    internal suspend fun deliverCreateAddress(idempotencyKey: String): Delivery = mutex.withLock {
        val item = outboxDao.pending().firstOrNull { it.idempotencyKey == idempotencyKey }
            ?: return@withLock Delivery.Queued
        deliver(item)
    }

    private suspend fun deliver(item: OutboxEntity): Delivery = when (item.kind) {
        OutboxKind.CREATE_ADDRESS -> deliverCreate(item)
        else -> {
            outboxDao.delete(item.id)
            Delivery.Rejected(DanAiaError.Unexpected)
        }
    }

    private suspend fun deliverCreate(item: OutboxEntity): Delivery {
        val request = DhruvaJson.decodeFromString<CreateAddressRequest>(item.payloadJson)
        val result = apiCall {
            http.execute(authenticated = true) {
                method = HttpMethod.Post
                url(ApiPaths.ADDRESSES)
                header(ApiHeaders.IDEMPOTENCY_KEY, item.idempotencyKey)
                jsonBody(request)
            }.bodyOrThrow<ApiEnvelope<AddressResults>>().results?.address
        }
        return when (result) {
            is DanAiaResult.Success -> {
                val dto = result.data ?: return Delivery.Rejected(DanAiaError.Unexpected).also { drop(item) }
                val entity = dto.toEntity()
                database.useWriterConnection { transactor ->
                    transactor.immediateTransaction {
                        addressDao.delete(item.localId)
                        addressDao.upsert(entity)
                        outboxDao.delete(item.id)
                    }
                }
                Delivery.Delivered(entity.toDomain())
            }
            is DanAiaResult.Error -> when {
                result.error.isConnectivityProblem -> Delivery.Queued
                result.error is DanAiaError.Server -> {
                    outboxDao.incrementAttempts(item.id)
                    Delivery.Queued
                }
                else -> {
                    drop(item)
                    Delivery.Rejected(result.error)
                }
            }
        }
    }

    private suspend fun drop(item: OutboxEntity) {
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                addressDao.delete(item.localId)
                outboxDao.delete(item.id)
            }
        }
    }

    internal sealed class Delivery {
        data class Delivered(val address: DigitalAddress) : Delivery()
        data object Queued : Delivery()
        data class Rejected(val error: DanAiaError) : Delivery()
    }
}
