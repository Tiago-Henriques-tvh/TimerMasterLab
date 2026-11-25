package com.tiagohenriques.project.timermasterlab.data.repositories

import com.tiagohenriques.project.timermasterlab.data.dao.SessionResultDao
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import kotlinx.coroutines.flow.Flow

interface SessionResultRepository {
    suspend fun insert(sessionResult: SessionResult): Long
    suspend fun update(sessionResult: SessionResult)
    suspend fun delete(sessionResult: SessionResult)
    fun getSessionResultsBySessionId(sessionId: Long): Flow<List<SessionResult>>
    fun getAllSessionResults(): Flow<List<SessionResult>>
}

class OfflineSessionResultRepository(
    private val sessionResultDao: SessionResultDao,
) : SessionResultRepository {
    override suspend fun insert(sessionResult: SessionResult): Long =
        sessionResultDao.insertSession(sessionResult)

    override suspend fun update(sessionResult: SessionResult) =
        sessionResultDao.upsertSession(sessionResult)

    override suspend fun delete(sessionResult: SessionResult) =
        sessionResultDao.deleteSession(sessionResult)

    override fun getSessionResultsBySessionId(sessionId: Long): Flow<List<SessionResult>> =
        sessionResultDao.getSessionResultsBySessionId(sessionId)

    override fun getAllSessionResults(): Flow<List<SessionResult>> =
        sessionResultDao.getAllSessionResults()
}
