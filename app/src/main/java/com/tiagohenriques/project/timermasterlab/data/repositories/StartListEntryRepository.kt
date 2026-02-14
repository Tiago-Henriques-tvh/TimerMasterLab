package com.tiagohenriques.project.timermasterlab.data.repositories

import com.tiagohenriques.project.timermasterlab.data.dao.StartListEntryDao
import com.tiagohenriques.project.timermasterlab.data.models.StartListEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface StartListEntryRepository {
    suspend fun insert(startList: StartListEntry): Long
    suspend fun update(startList: StartListEntry)
    suspend fun delete(startList: StartListEntry)
    suspend fun clearTimeTrialStartList()
    suspend fun clearGroupRaceStartList()
    fun getTimeTrialStartList(): Flow<List<StartListEntry>>
    fun getGroupRaceStartList(): Flow<List<StartListEntry>>
}

class OfflineStartListEntryRepository(
    private val starListEntryDao: StartListEntryDao,
) : StartListEntryRepository {
    override suspend fun insert(startList: StartListEntry): Long =
        withContext(Dispatchers.IO) {
            starListEntryDao.insertListEntry(startList)
        }

    override suspend fun update(startList: StartListEntry) =
        withContext(Dispatchers.IO) {
            starListEntryDao.updateListEntry(startList)
        }

    override suspend fun delete(startList: StartListEntry) =
        withContext(Dispatchers.IO) {
            starListEntryDao.deleteListEntry(startList)
        }

    override suspend fun clearTimeTrialStartList() =
        withContext(Dispatchers.IO) {
            starListEntryDao.clearTimeTrialStartList()
        }

    override suspend fun clearGroupRaceStartList() =
        withContext(Dispatchers.IO) {
            starListEntryDao.clearGroupRaceStartList()
        }

    override fun getTimeTrialStartList(): Flow<List<StartListEntry>> =
        starListEntryDao.getTimeTrialStartList()

    override fun getGroupRaceStartList(): Flow<List<StartListEntry>> =
        starListEntryDao.getGroupRaceStartList()
}
