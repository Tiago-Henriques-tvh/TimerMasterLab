package com.tiagohenriques.project.timermasterlab.data

import com.tiagohenriques.project.timermasterlab.data.database.createAppDatabase
import com.tiagohenriques.project.timermasterlab.data.preferences.UserPreferencesRepository
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
    val userPreferencesRepository: UserPreferencesRepository
}

class AppDataContainer : AppContainer {
    private val db by lazy { createAppDatabase() }

    override val athletesRepository: AthletesRepository by lazy {
        OfflineAthletesRepository(db.athletesDao())
    }

    override val sessionResultRepository: SessionResultRepository by lazy {
        OfflineSessionResultRepository(db.sessionResultDao())
    }

    override val sessionRepository: SessionRepository by lazy {
        OfflineSessionRepository(
            db.trialSessionDao(),
            db.sessionResultDao(),
            db.startListEntryDao(),
            db,
        )
    }

    override val startListEntryRepository: StartListEntryRepository by lazy {
        OfflineStartListEntryRepository(db.startListEntryDao(), db)
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository()
    }
}
