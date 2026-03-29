package com.example.testtask.data.repository

import com.example.testtask.data.local.dao.PagingStateDao
import com.example.testtask.data.local.dao.PlanetDao
import com.example.testtask.data.local.dao.SyncMetadataDao
import com.example.testtask.data.local.entity.PagingStateEntity
import com.example.testtask.data.local.entity.SyncMetadataEntity
import com.example.testtask.data.mapper.toDomainPlanet
import com.example.testtask.data.mapper.toDomainPlanetDetails
import com.example.testtask.data.mapper.toDomainPlanetListItem
import com.example.testtask.data.mapper.toPlanetEntity
import com.example.testtask.data.remote.SwapiApi
import com.example.testtask.domain.model.Planet
import com.example.testtask.domain.model.PlanetDetails
import com.example.testtask.domain.repository.PlanetsRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PlanetsRepositoryImpl @Inject constructor(
    private val api: SwapiApi,
    private val planetDao: PlanetDao,
    private val pagingStateDao: PagingStateDao,
    private val syncMetadataDao: SyncMetadataDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PlanetsRepository {

    private companion object {
        const val PLANETS_KEY = "planets"
        const val SEARCH_MAX_PAGES = 50
    }

    private val refreshMutex = Mutex()
    private val appendMutex = Mutex()

    override fun observePlanets(): Flow<List<Planet>> =
        planetDao.observePlanets().map { entities -> entities.map { it.toDomainPlanet() } }

    override fun observePlanetDetails(planetId: Int): Flow<PlanetDetails?> =
        planetDao.observePlanet(planetId).map { it?.toDomainPlanetDetails() }

    override fun observeLastSuccessSyncAtMs(): Flow<Long?> =
        syncMetadataDao.observe(PLANETS_KEY).map { it?.lastSuccessSyncAtMs }

    override suspend fun refreshPlanets() {
        withContext(ioDispatcher) {
            refreshMutex.withLock {
                runCatching {
                    val response = retryWithBackoff { api.getPlanetsPage(page = 1, search = null) }
                    val entities = response.results.mapNotNull { it.toPlanetEntity() }

                    pagingStateDao.clear(PLANETS_KEY)
                    if (entities.isNotEmpty()) {
                        planetDao.insertPlanets(entities)
                    }
                    val nextPage = if (response.next != null) 2 else null
                    pagingStateDao.upsert(PagingStateEntity(key = PLANETS_KEY, nextPage = nextPage))
                    syncMetadataDao.upsert(
                        SyncMetadataEntity(
                            key = PLANETS_KEY,
                            lastSuccessSyncAtMs = System.currentTimeMillis(),
                            lastFailureSyncAtMs = null,
                            lastFailureReason = null,
                            lastSource = "network",
                        ),
                    )
                }.onFailure { error ->
                    syncMetadataDao.upsert(
                        SyncMetadataEntity(
                            key = PLANETS_KEY,
                            lastSuccessSyncAtMs = syncMetadataDao.get(PLANETS_KEY)?.lastSuccessSyncAtMs,
                            lastFailureSyncAtMs = System.currentTimeMillis(),
                            lastFailureReason = error::class.java.simpleName,
                            lastSource = "cache",
                        ),
                    )
                    throw error
                }
            }
        } 
    }

    override suspend fun loadNextPlanetsPage(): Boolean = withContext(ioDispatcher) {
        appendMutex.withLock {
            val state = pagingStateDao.get(PLANETS_KEY)
            val nextPage = state?.nextPage ?: return@withContext false
            loadPlanetsPage(page = nextPage)
            true
        }
    }

    override suspend fun refreshPlanetDetails(planetId: Int) {
        withContext(ioDispatcher) {
            val planet = retryWithBackoff { api.getPlanet(planetId) }
            val entity = planet.toPlanetEntity()
            if (entity != null) {
                planetDao.insertPlanet(entity)
            }
        }
    }

    override suspend fun isBackendReachable(): Boolean = withContext(ioDispatcher) {
        runCatching {
            retryWithBackoff(attempts = 2) { api.getPlanetsPage(page = 1, search = null) }
            true
        }.getOrDefault(false)
    }

    override suspend fun getCachedPlanetName(planetId: Int): String? =
        withContext(ioDispatcher) {
            planetDao.getPlanetName(planetId)
        }

    override suspend fun searchPlanets(query: String): List<Planet> = withContext(ioDispatcher) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()
        buildList {
            var page = 1
            repeat(SEARCH_MAX_PAGES) {
                val response = retryWithBackoff { api.getPlanetsPage(page = page, search = trimmed) }
                addAll(response.results.mapNotNull { it.toDomainPlanetListItem() })
                if (response.next == null) return@buildList
                page++
            }
        }.distinctBy { it.id }
    }

    private suspend fun loadPlanetsPage(page: Int) {
        val response = retryWithBackoff { api.getPlanetsPage(page = page, search = null) }
        val entities = response.results.mapNotNull { it.toPlanetEntity() }
        if (entities.isNotEmpty()) {
            planetDao.insertPlanets(entities)
        }
        val nextPage = if (response.next != null) page + 1 else null
        pagingStateDao.upsert(PagingStateEntity(key = PLANETS_KEY, nextPage = nextPage))
    }
}

