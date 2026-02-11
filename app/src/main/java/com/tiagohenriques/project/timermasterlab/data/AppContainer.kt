package com.tiagohenriques.project.timermasterlab.data

import android.content.Context
import com.tiagohenriques.project.timermasterlab.data.database.AppDatabase
import com.tiagohenriques.project.timermasterlab.data.repositories.AthletesRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.OfflineAthletesRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.OfflineSessionRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.OfflineSessionResultRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.OfflineStartListEntryRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.SessionRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.SessionResultRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.StartListEntryRepository

interface AppContainer {
    val athletesRepository: AthletesRepository
    val sessionResultRepository: SessionResultRepository
    val startListEntryRepository: StartListEntryRepository
    val sessionRepository: SessionRepository
}

class AppDataContainer(
    private val context: Context,
) : AppContainer {
    private val db by lazy { AppDatabase.getDatabase(context) }

    override val athletesRepository: AthletesRepository by lazy {
        OfflineAthletesRepository(db.athletesDao())
    }

    override val sessionResultRepository: SessionResultRepository by lazy {
        OfflineSessionResultRepository(db.sessionResultDao())
    }

    override val sessionRepository: SessionRepository by lazy {
        OfflineSessionRepository(
            db.trialSessionDao(),
            sessionResultDao = db.sessionResultDao(),
            startListEntryDao = db.startListEntryDao()
        )
    }

    override val startListEntryRepository: StartListEntryRepository by lazy {
        OfflineStartListEntryRepository(db.startListEntryDao())
    }

}
