package com.cdpg.dhruva.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A Digital Address as cached on the device. Room is the source of truth for screens: they
 * read from here (works offline) and the network refreshes it in the background.
 *
 * [syncState]: [SyncState.SYNCED] came from the server; [SyncState.PENDING] was created on this
 * device and is waiting in the outbox (its [id] is a local id until the server assigns one).
 */
@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val digitalAddress: String,
    val title: String,
    val type: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val syncState: Int,
)

object SyncState {
    const val SYNCED = 0
    const val PENDING = 1
}

/**
 * A write that hasn't reached the server yet. Replayed by OutboxProcessor with the same
 * [idempotencyKey], so a retry can never create a duplicate.
 */
@Entity(
    tableName = "outbox",
    indices = [Index(value = ["idempotencyKey"], unique = true)],
)
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idempotencyKey: String,
    /** What to replay, e.g. [OutboxKind.CREATE_ADDRESS]. */
    val kind: String,
    /** The request body, as JSON. */
    val payloadJson: String,
    /** The local row this write belongs to (e.g. the pending AddressEntity id). */
    val localId: String,
    val createdAtEpochMs: Long,
    val attempts: Int = 0,
)

object OutboxKind {
    const val CREATE_ADDRESS = "create_address"
}

/** When each cached collection was last refreshed from the server. */
@Entity(tableName = "sync_meta")
data class SyncMetaEntity(
    @PrimaryKey val key: String,
    val lastSyncedEpochMs: Long,
)

object SyncKeys {
    const val ADDRESSES = "addresses"
}
