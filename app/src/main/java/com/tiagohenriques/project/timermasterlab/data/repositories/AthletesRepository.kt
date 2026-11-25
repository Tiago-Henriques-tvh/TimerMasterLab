package com.tiagohenriques.project.timermasterlab.data.repositories

import com.tiagohenriques.project.timermasterlab.data.dao.AthletesDao
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface AthletesRepository {
    suspend fun insert(athlete: Athlete): Long
    suspend fun update(athlete: Athlete)
    suspend fun delete(athlete: Athlete)
    fun getAthleteById(id: Long): Flow<Athlete>
    suspend fun getAllAthletesOnce(): List<Athlete>
}

class OfflineAthletesRepository(
    private val athleteDao: AthletesDao,
) : AthletesRepository {
    override suspend fun insert(athlete: Athlete): Long =
        withContext(Dispatchers.IO) {
            athleteDao.insertAthlete(athlete)
        }

    override suspend fun update(athlete: Athlete) =
        withContext(Dispatchers.IO) {
            athleteDao.updateAthlete(athlete)
        }

    override suspend fun delete(athlete: Athlete) =
        withContext(Dispatchers.IO) {
            athleteDao.deleteAthlete(athlete)
        }

    override fun getAthleteById(id: Long): Flow<Athlete> = athleteDao.getAthleteById(id)

    override suspend fun getAllAthletesOnce(): List<Athlete> =
        withContext(Dispatchers.IO) {
            athleteDao.getAllAthletesOnce()
        }
}
