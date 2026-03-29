package com.example.testtask.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testtask.data.local.entity.PlanetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanetDao {
    @Query("SELECT * FROM planets ORDER BY name ASC")
    fun observePlanets(): Flow<List<PlanetEntity>>

    @Query("SELECT * FROM planets WHERE id = :planetId")
    fun observePlanet(planetId: Int): Flow<PlanetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanets(planets: List<PlanetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanet(planet: PlanetEntity)

    @Query("DELETE FROM planets")
    suspend fun clearPlanets()

    @Query("SELECT name FROM planets WHERE id = :planetId LIMIT 1")
    suspend fun getPlanetName(planetId: Int): String?
}

