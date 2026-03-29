package com.example.testtask.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testtask.data.local.entity.StarshipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StarshipDao {
    @Query("SELECT * FROM starships ORDER BY name ASC")
    fun observeStarships(): Flow<List<StarshipEntity>>

    @Query("SELECT * FROM starships WHERE id = :starshipId")
    fun observeStarship(starshipId: Int): Flow<StarshipEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStarships(starships: List<StarshipEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStarship(starship: StarshipEntity)

    @Query("DELETE FROM starships")
    suspend fun clearStarships()

    @Query("SELECT name FROM starships WHERE id = :starshipId LIMIT 1")
    suspend fun getStarshipName(starshipId: Int): String?
}

