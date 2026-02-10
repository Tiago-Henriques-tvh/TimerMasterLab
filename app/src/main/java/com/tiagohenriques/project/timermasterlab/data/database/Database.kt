package com.tiagohenriques.project.timermasterlab.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tiagohenriques.project.timermasterlab.data.dao.AthletesDao
import com.tiagohenriques.project.timermasterlab.data.dao.SessionResultDao
import com.tiagohenriques.project.timermasterlab.data.dao.StartListEntryDao
import com.tiagohenriques.project.timermasterlab.data.dao.TrialSessionDao
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import com.tiagohenriques.project.timermasterlab.data.models.StartListEntry
import com.tiagohenriques.project.timermasterlab.data.models.TrialSession

@Database(
    entities = [Athlete::class, SessionResult::class, StartListEntry::class, TrialSession::class],
    version = 1,
    exportSchema = false,
)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun athletesDao(): AthletesDao
    abstract fun sessionResultDao(): SessionResultDao
    abstract fun startListEntryDao(): StartListEntryDao
    abstract fun trialSessionDao(): TrialSessionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                Room
                    .databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_database",
                    ).build()
                    .also { instance = it }
            }
    }
}
