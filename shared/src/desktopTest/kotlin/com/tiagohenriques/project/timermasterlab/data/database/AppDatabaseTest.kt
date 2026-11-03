package com.tiagohenriques.project.timermasterlab.data.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.Session
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import com.tiagohenriques.project.timermasterlab.data.models.StartListEntry
import com.tiagohenriques.project.timermasterlab.data.models.Type
import com.tiagohenriques.project.timermasterlab.data.repositories.OfflineSessionRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.OfflineStartListEntryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppDatabaseTest {
    @Test
    fun athleteDaoPersistsAndReadsRecords() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()

        try {
            database.athletesDao().insertAthlete(
                Athlete(
                    name = "Test athlete",
                    age = 20,
                    gender = "Other",
                    category = 1,
                ),
            )

            val athletes = database.athletesDao().getAllAthletesOnce()
            assertEquals(1, athletes.size)
            assertEquals("Test athlete", athletes.single().name)
        } finally {
            database.close()
        }
    }

    @Test
    fun startListReplacementIsPersisted() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()

        try {
            val athleteId = database.athletesDao().insertAthlete(
                Athlete(
                    name = "Start-list athlete",
                    age = 20,
                    gender = "Other",
                    category = 1,
                ),
            )
            val repository = OfflineStartListEntryRepository(database.startListEntryDao(), database)

            repository.replaceStartList(
                Type.TIME_TRIAL,
                listOf(
                    StartListEntry(
                        athleteId = athleteId,
                        startOrder = 1,
                        distanceMeters = 1000f,
                        type = Type.TIME_TRIAL.displayName,
                    ),
                ),
            )

            val savedEntries = repository.getTimeTrialStartList().first()
            assertEquals(1, savedEntries.size)
            assertEquals(athleteId, savedEntries.single().athleteId)
            assertEquals(1000f, savedEntries.single().distanceMeters)
        } finally {
            database.close()
        }
    }

    @Test
    fun startingTimeTrialCopiesSavedEntriesIntoSession() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()

        try {
            val athleteId = database.athletesDao().insertAthlete(
                Athlete(
                    name = "Race athlete",
                    age = 18,
                    gender = "Other",
                    category = 2,
                ),
            )
            val startListRepository =
                OfflineStartListEntryRepository(database.startListEntryDao(), database)
            startListRepository.replaceStartList(
                Type.TIME_TRIAL,
                listOf(
                    StartListEntry(
                        athleteId = athleteId,
                        startOrder = 1,
                        distanceMeters = 2000f,
                        type = Type.TIME_TRIAL.displayName,
                    ),
                ),
            )
            val sessionRepository = OfflineSessionRepository(
                sessionDao = database.trialSessionDao(),
                sessionResultDao = database.sessionResultDao(),
                startListEntryDao = database.startListEntryDao(),
                database = database,
            )

            val sessionId = sessionRepository.startTimeTrialSession()
            val results = database.sessionResultDao()
                .getSessionResultsBySessionId(sessionId)
                .first()

            assertEquals(1, results.size)
            assertEquals(athleteId, results.single().athleteId)
            assertEquals(2000, results.single().distance)
            assertEquals(null, results.single().startedAt)
            assertEquals(null, results.single().duration)
        } finally {
            database.close()
        }
    }

    @Test
    fun startingGroupRacePreservesStartGroupsUntilEachGroupStarts() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()

        try {
            val athleteIds = listOf("First", "Second", "Third").map { name ->
                database.athletesDao().insertAthlete(
                    Athlete(name = name, age = 20, gender = "Other", category = 1),
                )
            }
            val startListRepository =
                OfflineStartListEntryRepository(database.startListEntryDao(), database)
            startListRepository.replaceStartList(
                Type.GROUP_RACE,
                athleteIds.mapIndexed { index, athleteId ->
                    StartListEntry(
                        athleteId = athleteId,
                        startOrder = if (index < 2) 1 else 2,
                        distanceMeters = 1000f,
                        type = Type.GROUP_RACE.displayName,
                    )
                },
            )
            val sessionRepository = OfflineSessionRepository(
                sessionDao = database.trialSessionDao(),
                sessionResultDao = database.sessionResultDao(),
                startListEntryDao = database.startListEntryDao(),
                database = database,
            )

            val sessionId = sessionRepository.startGroupRaceSession()
            val results = database.sessionResultDao()
                .getSessionResultsBySessionId(sessionId)
                .first()
                .sortedBy { it.startOrder }

            assertEquals(listOf(1, 1, 2), results.map { it.startOrder })
            assertTrue(results.all { it.startedAt == null })
            assertTrue(results.all { it.duration == null })
        } finally {
            database.close()
        }
    }

    @Test
    fun pruningCompletedSessionsKeepsNewestAndLeavesIncompleteSessions() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()

        try {
            val athleteId = database.athletesDao().insertAthlete(
                Athlete(name = "History athlete", age = 20, gender = "Other", category = 1),
            )
            val olderCompletedId = database.trialSessionDao()
                .insertTrialSession(Session(date = "1000"))
            val newerCompletedId = database.trialSessionDao()
                .insertTrialSession(Session(date = "3000"))
            val incompleteId = database.trialSessionDao()
                .insertTrialSession(Session(date = "2000"))
            val results = listOf(
                SessionResult(olderCompletedId, athleteId, 1, 1000, 1000L, 500L),
                SessionResult(newerCompletedId, athleteId, 1, 1000, 3000L, 700L),
                SessionResult(incompleteId, athleteId, 1, 1000, null, null),
            )
            for (result in results) {
                database.sessionResultDao().insertSession(result)
            }
            val repository = OfflineSessionRepository(
                sessionDao = database.trialSessionDao(),
                sessionResultDao = database.sessionResultDao(),
                startListEntryDao = database.startListEntryDao(),
                database = database,
            )

            repository.pruneCompletedSessions(maxSessions = 1)

            val remainingSessions = database.trialSessionDao().getAllTrialSessions().first()
            val remainingResults = database.sessionResultDao().getAllSessionResults().first()
            assertEquals(setOf(newerCompletedId, incompleteId), remainingSessions.map { it.sessionId }.toSet())
            assertEquals(setOf(newerCompletedId, incompleteId), remainingResults.map { it.sessionId }.toSet())
        } finally {
            database.close()
        }
    }
}
