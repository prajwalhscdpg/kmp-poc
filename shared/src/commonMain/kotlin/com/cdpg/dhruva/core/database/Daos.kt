package com.cdpg.dhruva.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {

    /** Newest first; pending (not yet synced) addresses included. */
    @Query("SELECT * FROM addresses ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<AddressEntity>>

    @Upsert
    suspend fun upsertAll(addresses: List<AddressEntity>)

    @Upsert
    suspend fun upsert(address: AddressEntity)

    /** Drops server copies before a full refresh; pending local ones survive. */
    @Query("DELETE FROM addresses WHERE syncState = 0")
    suspend fun deleteSynced()

    @Query("DELETE FROM addresses WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM addresses")
    suspend fun deleteAll()
}

@Dao
interface OutboxDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: OutboxEntity): Long

    /** Oldest first, so writes replay in the order the user made them. */
    @Query("SELECT * FROM outbox ORDER BY id ASC")
    suspend fun pending(): List<OutboxEntity>

    @Query("SELECT COUNT(*) FROM outbox")
    fun observeCount(): Flow<Int>

    @Query("UPDATE outbox SET attempts = attempts + 1 WHERE id = :id")
    suspend fun incrementAttempts(id: Long)

    @Query("DELETE FROM outbox WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM outbox")
    suspend fun deleteAll()
}

@Dao
interface SyncMetaDao {

    @Query("SELECT lastSyncedEpochMs FROM sync_meta WHERE `key` = :key")
    suspend fun lastSynced(key: String): Long?

    @Upsert
    suspend fun upsert(meta: SyncMetaEntity)

    @Query("DELETE FROM sync_meta")
    suspend fun deleteAll()
}
