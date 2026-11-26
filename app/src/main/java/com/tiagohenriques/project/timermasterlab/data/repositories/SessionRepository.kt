package com.tiagohenriques.project.timermasterlab.data.repositories

import androidx.room.withTransaction
import com.tiagohenriques.project.timermasterlab.data.database.AppDatabase
import com.tiagohenriques.project.timermasterlab.data.dao.SessionDao
import com.tiagohenriques.project.timermasterlab.data.dao.SessionResultDao
import com.tiagohenriques.project.timermasterlab.data.dao.StartListEntryDao
import com.tiagohenriques.project.timermasterlab.data.models.Session
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import com.tiagohenriques.project.timermasterlab.data.models.StartListEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface SessionRepository {
    suspend fun insert(session: Session): Long
    suspend fun update(session: Session)
    suspend fun delete(session: Session)
    fun getTrialSessionById(sessionId: Long): Flow<Session>
    fun getAllTrialSessions(): Flow<List<Session>>
    suspend fun startTimeTrialSession(): Long
    suspend fun startGroupRaceSession(): Long
}

internal class OfflineSessionRepository(
    private val sessionDao: SessionDao,
    private val sessionResultDao: SessionResultDao,
    private val startListEntryDao: StartListEntryDao,
    private val database: AppDatabase,
) : SessionRepository {
    override suspend fun insert(session: Session): Long =
        withContext(Dispatchers.IO) {
            sessionDao.insertTrialSession(session)
        }

    override suspend fun update(session: Session) =
        withContext(Dispatchers.IO) {
            sessionDao.updateTrialSession(session)
        }

    override suspend fun delete(session: Session) =
        withContext(Dispatchers.IO) {
            sessionDao.deleteTrialSession(session)
        }

    override fun getTrialSessionById(sessionId: Long): Flow<Session> =
        sessionDao.getTrialSessionById(sessionId)

    override fun getAllTrialSessions(): Flow<List<Session>> =
        sessionDao.getAllTrialSessions()

    override suspend fun startTimeTrialSession(): Long =
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val startList = startListEntryDao.getTimeTrialStartListOnce()
                createSessionWithResults(startList)
            }
        }

    override suspend fun startGroupRaceSession(): Long =
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val startList = startListEntryDao.getGroupRaceListOnce()
                createSessionWithResults(startList)
            }
        }

    private suspend fun createSessionWithResults(startList: List<StartListEntry>): Long {
        val sessionId = sessionDao.insertTrialSession(
            Session(date = System.currentTimeMillis().toString()),
        )
        if (startList.isNotEmpty()) {
            sessionResultDao.insertSessions(
                startList.map { entry ->
                    SessionResult(
                        sessionId = sessionId,
                        athleteId = entry.athleteId,
                        startOrder = entry.startOrder,
                        distance = entry.distanceMeters.toInt(),
                        startedAt = null,
                        duration = null,
                    )
                },
            )
        }
        return sessionId
    }
}
