package com.example.testtask.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testtask.data.local.entity.PagingStateEntity

@Dao
interface PagingStateDao {
    @Query("SELECT * FROM paging_state WHERE `key` = :key")
    suspend fun get(key: String): PagingStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: PagingStateEntity)

    @Query("DELETE FROM paging_state WHERE `key` = :key")
    suspend fun clear(key: String)
}

