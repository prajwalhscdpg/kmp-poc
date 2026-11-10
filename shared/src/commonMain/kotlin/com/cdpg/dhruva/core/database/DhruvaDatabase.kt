package com.cdpg.dhruva.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * The offline store, shared by Android and iOS (Room KMP + bundled SQLite, so both platforms
 * run the same SQLite version).
 *
 * Every schema change ships with a migration and a migration test; never
 * fallbackToDestructiveMigration in release builds (Dhruva engineering rules).
 * Schemas are exported to shared/schemas.
 */
@Database(
    entities = [AddressEntity::class, OutboxEntity::class, SyncMetaEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(DhruvaDatabaseConstructor::class)
abstract class DhruvaDatabase : RoomDatabase() {
    abstract fun addressDao(): AddressDao
    abstract fun outboxDao(): OutboxDao
    abstract fun syncMetaDao(): SyncMetaDao
}

// Room's compiler generates the actual implementations for each platform.
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object DhruvaDatabaseConstructor : RoomDatabaseConstructor<DhruvaDatabase> {
    override fun initialize(): DhruvaDatabase
}

/** Finishes the platform builder the same way on both platforms. */
fun buildDhruvaDatabase(builder: RoomDatabase.Builder<DhruvaDatabase>): DhruvaDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()

/** Logout: user data never outlives the session. */
suspend fun DhruvaDatabase.wipeUserData() {
    addressDao().deleteAll()
    outboxDao().deleteAll()
    syncMetaDao().deleteAll()
}
