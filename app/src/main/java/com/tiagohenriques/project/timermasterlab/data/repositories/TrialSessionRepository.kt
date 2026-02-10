package com.tiagohenriques.project.timermasterlab.data.repositories

import androidx.room.Transaction
import com.tiagohenriques.project.timermasterlab.data.dao.SessionResultDao
import com.tiagohenriques.project.timermasterlab.data.dao.StartListEntryDao
import com.tiagohenriques.project.timermasterlab.data.dao.TrialSessionDao
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import com.tiagohenriques.project.timermasterlab.data.models.TrialSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface TrialSessionRepository {
    suspend fun insert(trialSession: TrialSession): Long
    suspend fun update(trialSession: TrialSession)
    suspend fun delete(trialSession: TrialSession)
    fun getTrialSessionById(sessionId: Long): Flow<TrialSession>
    fun getAllTrialSessions(): Flow<List<TrialSession>>
    suspend fun startSession(): Long
}

class OfflineTrialSessionRepository(
    private val trialSessionDao: TrialSessionDao,
    private val sessionResultDao: SessionResultDao,
    private val startListEntryDao: StartListEntryDao,
) : TrialSessionRepository {
    override suspend fun insert(trialSession: TrialSession): Long =
        withContext(Dispatchers.IO) {
            trialSessionDao.insertTrialSession(trialSession)
        }

    override suspend fun update(trialSession: TrialSession) =
        withContext(Dispatchers.IO) {
            trialSessionDao.updateTrialSession(trialSession)
        }

    override suspend fun delete(trialSession: TrialSession) =
        withContext(Dispatchers.IO) {
            trialSessionDao.deleteTrialSession(trialSession)
        }

    override fun getTrialSessionById(sessionId: Long): Flow<TrialSession> =
        trialSessionDao.getTrialSessionById(sessionId)

    override fun getAllTrialSessions(): Flow<List<TrialSession>> =
        trialSessionDao.getAllTrialSessions()

    @Transaction
    override suspend fun startSession(): Long {
        val startList = startListEntryDao.getStartListOnce()
        val sessionId = trialSessionDao.insertTrialSession(
            TrialSession(date = System.currentTimeMillis().toString()),
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
