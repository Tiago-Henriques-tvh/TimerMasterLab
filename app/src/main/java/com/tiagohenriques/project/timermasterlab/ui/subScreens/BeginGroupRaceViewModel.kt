package com.tiagohenriques.project.timermasterlab.ui.subScreens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import com.tiagohenriques.project.timermasterlab.data.repositories.AthletesRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.SessionRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.SessionResultRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.StartListEntryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

data class GroupRaceUiState(
    val draft: List<DraftGroupRaceItem> = emptyList(),
    val hasEnded: Boolean = false,
)

data class DraftGroupRaceItem(
    val sessionId: Long = 0L,
    val athlete: Athlete,
    val result: SessionResult?,
    val startOrder: Int,
    val distance: Float
)

class BeginGroupRaceViewModel(
    private val sessionResultRepo: SessionResultRepository,
    private val sessionRepo: SessionRepository,
    private val athletesRepo: AthletesRepository,
    private val startListRepo: StartListEntryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupRaceUiState())
    val uiState: StateFlow<GroupRaceUiState> = _uiState.asStateFlow()

    private val _sessionTime = MutableStateFlow(0L)
    val sessionTime: StateFlow<Long> = _sessionTime.asStateFlow()

    private val _nowMillis = MutableStateFlow(System.currentTimeMillis())
    val nowMillis: StateFlow<Long> = _nowMillis.asStateFlow()

    private var globalTimerJob: Job? = null
    private var sessionStartTime: Long = 0L

    init {
        viewModelScope.launch {
            val startListEntries = startListRepo.getGroupRaceStartList().first()
            val allAthletes = athletesRepo.getAllAthletesOnce()
            val athletesMap = allAthletes.associateBy { it.id }
            val sessionId = sessionRepo.startGroupRaceSession()

            val draftItems = startListEntries.map { entry ->
                val athlete = athletesMap[entry.athleteId]!!
                DraftGroupRaceItem(
                    sessionId = sessionId,
                    athlete = athlete,
                    result = null,
                    startOrder = entry.startOrder,
                    distance = entry.distanceMeters
                )
            }.sortedBy { it.startOrder }

            _uiState.value = GroupRaceUiState(draft = draftItems)
        }
    }

    fun startAthletes(group: Int) {
        val now = System.currentTimeMillis()

        if (globalTimerJob == null) {
            sessionStartTime = now
            globalTimerJob = viewModelScope.launch {
                while (true) {
                    delay(100L)
                    val tick = System.currentTimeMillis()
                    _nowMillis.value = tick
                    _sessionTime.value = tick - sessionStartTime
                }
            }
        }

        val currentDraft = _uiState.value.draft
        val groupItems = currentDraft.filter { it.startOrder == group }

        groupItems.forEach { item ->

            val updatedResult = item.result ?: SessionResult(
                sessionId = item.sessionId,
                athleteId = item.athlete.id,
                startOrder = item.startOrder,
                distance = item.distance.toInt(),
                startedAt = now,
                duration = 0L
            )

            viewModelScope.launch {
                sessionResultRepo.update(updatedResult)
            }
        }

        _uiState.update { state ->
            val updatedDraft = state.draft.map { draftItem ->
                if (draftItem.startOrder == group) {
                    val updatedResult = draftItem.result ?: SessionResult(
                        sessionId = draftItem.sessionId,
                        athleteId = draftItem.athlete.id,
                        startOrder = draftItem.startOrder,
                        distance = draftItem.distance.toInt(),
                        startedAt = now,
                        duration = 0L
                    )
                    draftItem.copy(result = updatedResult)
                } else {
                    draftItem
                }
            }
            state.copy(draft = updatedDraft)
        }
    }

    fun stopAthlete(item: DraftGroupRaceItem) {
        val now = System.currentTimeMillis()
        val result = item.result ?: return
        val duration = now - (result.startedAt ?: now)

        viewModelScope.launch {
            sessionResultRepo.update(result.copy(duration = duration))
        }

        _uiState.update { state ->
            val updatedDraft = state.draft.map {
                if (it.athlete.id == item.athlete.id) it.copy(result = it.result?.copy(duration = duration))
                else it
            }
            val finishedAll = updatedDraft.all { dr ->
                val r = dr.result
                val duration = r?.duration
                r != null && r.startedAt != null && duration != null && duration > 0L
            }

            if (finishedAll) {
                globalTimerJob?.cancel()
                globalTimerJob = null
                viewModelScope.launch {
                    enforceMaxSessions()
                }
            }

            state.copy(draft = updatedDraft, hasEnded = finishedAll)
        }
    }

    fun abortTrial() {
        viewModelScope.launch {
            val sessionId = _uiState.value.draft.firstOrNull()?.sessionId
            if (sessionId != null) {
                val results = sessionResultRepo.getSessionResultsBySessionId(sessionId).first()
                results.forEach { sessionResultRepo.delete(it) }

                val trialSession = sessionRepo.getTrialSessionById(sessionId).first()
                sessionRepo.delete(trialSession)
            }

            _uiState.update { state ->
                val clearedDraft = state.draft.map { it.copy(result = null) }
                state.copy(draft = clearedDraft, hasEnded = false)
            }

            globalTimerJob?.cancel()
            globalTimerJob = null
            _sessionTime.value = 0L
        }
    }

    private suspend fun enforceMaxSessions(maxSessions: Int = 20) {
        fun parseDateToEpoch(dateStr: String): Long {
            if (dateStr.isBlank()) return 0L
            return try {
                dateStr.toLong()
            } catch (_: Throwable) {
                try {
                    Instant.parse(dateStr).toEpochMilli()
                } catch (_: Throwable) {
                    0L
                }
            }
        }

        val allSessions = sessionRepo.getAllTrialSessions().first()

        val completedSessions = allSessions.filter { session ->
            val results = sessionResultRepo.getSessionResultsBySessionId(session.sessionId).first()
            results.any { result ->
                val duration = result.duration
                duration != null && duration > 0L
            }
        }

        if (completedSessions.size <= maxSessions) return
        val sortedByDateDesc = completedSessions.sortedByDescending { parseDateToEpoch(it.date) }

        val toDelete = sortedByDateDesc.drop(maxSessions)

        toDelete.forEach { session ->
            val results = sessionResultRepo.getSessionResultsBySessionId(session.sessionId).first()
            results.forEach { sessionResultRepo.delete(it) }
            sessionRepo.delete(session)
        }
    }
}
