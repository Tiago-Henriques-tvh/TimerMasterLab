package com.tiagohenriques.project.timermasterlab.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.tiagohenriques.project.timermasterlab.data.dao.AthletesDao
import com.tiagohenriques.project.timermasterlab.data.dao.SessionDao
import com.tiagohenriques.project.timermasterlab.data.dao.SessionResultDao
import com.tiagohenriques.project.timermasterlab.data.dao.StartListEntryDao
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.Session
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import com.tiagohenriques.project.timermasterlab.data.models.StartListEntry
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [Athlete::class, SessionResult::class, StartListEntry::class, Session::class],
    version = 1,
    exportSchema = false,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun athletesDao(): AthletesDao
    abstract fun sessionResultDao(): SessionResultDao
    abstract fun startListEntryDao(): StartListEntryDao
    abstract fun trialSessionDao(): SessionDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

fun createAppDatabase(): AppDatabase =
    getDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()

suspend fun AppDatabase.withSessionTransaction(block: suspend () -> Unit) {
    useWriterConnection { transactor ->
        transactor.immediateTransaction { block() }
    }
}
