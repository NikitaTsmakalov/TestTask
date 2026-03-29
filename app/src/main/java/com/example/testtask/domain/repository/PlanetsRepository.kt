package com.example.testtask.domain.repository

import com.example.testtask.domain.model.Planet
import com.example.testtask.domain.model.PlanetDetails
import kotlinx.coroutines.flow.Flow

interface PlanetsRepository {
    fun observePlanets(): Flow<List<Planet>>
    fun observePlanetDetails(planetId: Int): Flow<PlanetDetails?>
    fun observeLastSuccessSyncAtMs(): Flow<Long?>
    suspend fun refreshPlanets()
    suspend fun refreshPlanetDetails(planetId: Int)
    suspend fun loadNextPlanetsPage(): Boolean
    suspend fun isBackendReachable(): Boolean
    suspend fun getCachedPlanetName(planetId: Int): String?
    suspend fun searchPlanets(query: String): List<Planet>
}

