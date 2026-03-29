package com.example.testtask.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testtask.data.local.entity.RecentViewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentViewDao {
    @Query("SELECT * FROM recent_views ORDER BY viewedAtMs DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<RecentViewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: RecentViewEntity)
}

