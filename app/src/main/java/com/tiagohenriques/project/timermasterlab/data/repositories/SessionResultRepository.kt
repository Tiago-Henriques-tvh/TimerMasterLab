package com.tiagohenriques.project.timermasterlab.data.repositories

import com.tiagohenriques.project.timermasterlab.data.dao.SessionResultDao
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface SessionResultRepository {
    suspend fun insert(sessionResult: SessionResult): Long
    suspend fun update(sessionResult: SessionResult)
    suspend fun delete(sessionResult: SessionResult)
    fun getSessionResultsBySessionId(sessionId: Long): Flow<List<SessionResult>>
}

class OfflineSessionResultRepository(
    private val sessionResultDao: SessionResultDao,
) : SessionResultRepository {
    override suspend fun insert(sessionResult: SessionResult): Long =
        withContext(Dispatchers.IO) {
            sessionResultDao.insertSession(sessionResult)
        }

    override suspend fun update(sessionResult: SessionResult) =
        withContext(Dispatchers.IO) {
            sessionResultDao.upsertSession(sessionResult)
        }

    override suspend fun delete(sessionResult: SessionResult) =
        withContext(Dispatchers.IO) {
            sessionResultDao.deleteSession(sessionResult)
        }

    override fun getSessionResultsBySessionId(sessionId: Long): Flow<List<SessionResult>> =
        sessionResultDao.getSessionResultsBySessionId(sessionId)
}
