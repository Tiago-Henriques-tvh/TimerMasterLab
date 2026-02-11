package com.tiagohenriques.project.timermasterlab.data.repositories

import androidx.room.Transaction
import com.tiagohenriques.project.timermasterlab.data.dao.SessionDao
import com.tiagohenriques.project.timermasterlab.data.dao.SessionResultDao
import com.tiagohenriques.project.timermasterlab.data.dao.StartListEntryDao
import com.tiagohenriques.project.timermasterlab.data.models.Session
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface SessionRepository {
    suspend fun insert(session: Session): Long
    suspend fun update(session: Session)
    suspend fun delete(session: Session)
    fun getTrialSessionById(sessionId: Long): Flow<Session>
    fun getAllTrialSessions(): Flow<List<Session>>
    suspend fun startSession(): Long
}

class OfflineSessionRepository(
    private val sessionDao: SessionDao,
    private val sessionResultDao: SessionResultDao,
    private val startListEntryDao: StartListEntryDao,
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

    @Transaction
    override suspend fun startSession(): Long {
        val startList = startListEntryDao.getStartListOnce()
        val sessionId = sessionDao.insertTrialSession(
            Session(date = System.currentTimeMillis().toString()),
        )

        startList.forEach { entry ->
            sessionResultDao.insertSession(
                SessionResult(
                    sessionId = sessionId,
                    athleteId = entry.athleteId,
                    startOrder = entry.startOrder,
                    distance = entry.distanceMeters.toInt(),
                    startedAt = null,
                    duration = null,
                ),
            )
        }
        return sessionId
    }
}
