package com.example.testtask.domain.repository

import com.example.testtask.domain.model.Starship
import com.example.testtask.domain.model.StarshipDetails
import kotlinx.coroutines.flow.Flow

interface StarshipsRepository {
    fun observeStarships(): Flow<List<Starship>>
    fun observeStarshipDetails(starshipId: Int): Flow<StarshipDetails?>
    fun observeLastSuccessSyncAtMs(): Flow<Long?>
    suspend fun refreshStarships()
    suspend fun refreshStarshipDetails(starshipId: Int)
    suspend fun loadNextStarshipsPage(): Boolean
    suspend fun isBackendReachable(): Boolean
    suspend fun getCachedStarshipName(starshipId: Int): String?
    suspend fun searchStarships(query: String): List<Starship>
}

