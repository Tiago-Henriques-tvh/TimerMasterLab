package com.tiagohenriques.project.timermasterlab.data.repositories

import com.tiagohenriques.project.timermasterlab.data.dao.SessionDao
import com.tiagohenriques.project.timermasterlab.data.dao.SessionResultDao
import com.tiagohenriques.project.timermasterlab.data.dao.StartListEntryDao
import com.tiagohenriques.project.timermasterlab.data.database.AppDatabase
import com.tiagohenriques.project.timermasterlab.data.database.withSessionTransaction
import com.tiagohenriques.project.timermasterlab.data.models.Session
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import com.tiagohenriques.project.timermasterlab.platform.currentTimeMillis
import kotlinx.datetime.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface SessionRepository {
    suspend fun insert(session: Session): Long
    suspend fun update(session: Session)
    suspend fun delete(session: Session)
    fun getTrialSessionById(sessionId: Long): Flow<Session>
    fun getAllTrialSessions(): Flow<List<Session>>
    suspend fun startTimeTrialSession(): Long
    suspend fun startGroupRaceSession(): Long
    suspend fun pruneCompletedSessions(maxSessions: Int = 20)
}

class OfflineSessionRepository(
    private val sessionDao: SessionDao,
    private val sessionResultDao: SessionResultDao,
    private val startListEntryDao: StartListEntryDao,
    private val database: AppDatabase,
) : SessionRepository {
    override suspend fun insert(session: Session): Long = sessionDao.insertTrialSession(session)

    override suspend fun update(session: Session) = sessionDao.updateTrialSession(session)

    override suspend fun delete(session: Session) = sessionDao.deleteTrialSession(session)

    override fun getTrialSessionById(sessionId: Long): Flow<Session> =
        sessionDao.getTrialSessionById(sessionId)

    override fun getAllTrialSessions(): Flow<List<Session>> =
        sessionDao.getAllTrialSessions()

    override suspend fun pruneCompletedSessions(maxSessions: Int) {
        require(maxSessions >= 0) { "Maximum completed sessions cannot be negative." }
        val sessions = sessionDao.getAllTrialSessions().first()
        val resultsBySession = sessionResultDao.getAllSessionResults().first().groupBy { it.sessionId }
        val sessionsToDelete = sessions
            .filter { session ->
                resultsBySession[session.sessionId].orEmpty().any { (it.duration ?: 0L) > 0L }
            }
            .sortedByDescending { parseSessionDate(it.date) }
            .drop(maxSessions)
        if (sessionsToDelete.isEmpty()) return

        database.withSessionTransaction {
            for (session in sessionsToDelete) {
                for (result in resultsBySession[session.sessionId].orEmpty()) {
                    sessionResultDao.deleteSession(result)
                }
                sessionDao.deleteTrialSession(session)
            }
        }
    }

    override suspend fun startTimeTrialSession(): Long {
        var sessionId = 0L
        database.withSessionTransaction {
            val startList = startListEntryDao.getTimeTrialStartListOnce()
            sessionId = sessionDao.insertTrialSession(
                Session(date = currentTimeMillis().toString()),
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
        }
        return sessionId
    }

    override suspend fun startGroupRaceSession(): Long {
        var sessionId = 0L
        database.withSessionTransaction {
            val startList = startListEntryDao.getGroupRaceListOnce()
            sessionId = sessionDao.insertTrialSession(
                Session(date = currentTimeMillis().toString()),
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
        }
        return sessionId
    }

    private fun parseSessionDate(date: String): Long {
        if (date.isBlank()) return 0L
        return date.toLongOrNull() ?: try {
            Instant.parse(date).toEpochMilliseconds()
        } catch (_: IllegalArgumentException) {
            0L
        }
    }
}
