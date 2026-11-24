package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TallyDao {
    @Query("SELECT * FROM tally_entries ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<TallyEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: TallyEntryEntity)

    @Query("DELETE FROM tally_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tally_entries")
    suspend fun clearAll()
}
