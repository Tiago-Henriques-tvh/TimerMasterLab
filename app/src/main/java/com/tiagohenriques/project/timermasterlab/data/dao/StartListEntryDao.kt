package com.tiagohenriques.project.timermasterlab.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tiagohenriques.project.timermasterlab.data.models.StartListEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface StartListEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListEntry(entry: StartListEntry): Long

    @Update
    suspend fun updateListEntry(entry: StartListEntry)

    @Delete
    suspend fun deleteListEntry(entry: StartListEntry)

    @Query("DELETE FROM start_list_entry")
    suspend fun clearStartList()

    @Query("SELECT * FROM start_list_entry ORDER BY start_order ASC")
    fun getStartList(): Flow<List<StartListEntry>>

    @Query("SELECT * FROM start_list_entry ORDER BY start_order ASC")
    suspend fun getStartListOnce(): List<StartListEntry>
}
