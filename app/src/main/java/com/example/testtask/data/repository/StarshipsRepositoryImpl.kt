package com.example.testtask.data.repository

import com.example.testtask.data.local.dao.PagingStateDao
import com.example.testtask.data.local.dao.StarshipDao
import com.example.testtask.data.local.dao.SyncMetadataDao
import com.example.testtask.data.local.entity.PagingStateEntity
import com.example.testtask.data.local.entity.SyncMetadataEntity
import com.example.testtask.data.mapper.toDomainStarship
import com.example.testtask.data.mapper.toDomainStarshipDetails
import com.example.testtask.data.mapper.toDomainStarshipListItem
import com.example.testtask.data.mapper.toStarshipEntity
import com.example.testtask.data.remote.SwapiApi
import com.example.testtask.domain.model.Starship
import com.example.testtask.domain.model.StarshipDetails
import com.example.testtask.domain.repository.StarshipsRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StarshipsRepositoryImpl @Inject constructor(
    private val api: SwapiApi,
    private val starshipDao: StarshipDao,
    private val pagingStateDao: PagingStateDao,
    private val syncMetadataDao: SyncMetadataDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : StarshipsRepository {

    private companion object {
        const val STARSHIPS_KEY = "starships"
        const val SEARCH_MAX_PAGES = 50
    }

    private val refreshMutex = Mutex()
    private val appendMutex = Mutex()

    override fun observeStarships(): Flow<List<Starship>> =
        starshipDao.observeStarships().map { entities -> entities.map { it.toDomainStarship() } }

    override fun observeStarshipDetails(starshipId: Int): Flow<StarshipDetails?> =
        starshipDao.observeStarship(starshipId).map { it?.toDomainStarshipDetails() }

    override fun observeLastSuccessSyncAtMs(): Flow<Long?> =
        syncMetadataDao.observe(STARSHIPS_KEY).map { it?.lastSuccessSyncAtMs }

    override suspend fun refreshStarships() {
        withContext(ioDispatcher) {
            refreshMutex.withLock {
                runCatching {
                    val response = retryWithBackoff { api.getStarshipsPage(page = 1, search = null) }
                    val entities = response.results.mapNotNull { it.toStarshipEntity() }

                    pagingStateDao.clear(STARSHIPS_KEY)
                    if (entities.isNotEmpty()) {
                        starshipDao.insertStarships(entities)
                    }
                    val nextPage = if (response.next != null) 2 else null
                    pagingStateDao.upsert(PagingStateEntity(key = STARSHIPS_KEY, nextPage = nextPage))
                    syncMetadataDao.upsert(
                        SyncMetadataEntity(
                            key = STARSHIPS_KEY,
                            lastSuccessSyncAtMs = System.currentTimeMillis(),
                            lastFailureSyncAtMs = null,
                            lastFailureReason = null,
                            lastSource = "network",
                        ),
                    )
                }.onFailure { error ->
                    syncMetadataDao.upsert(
                        SyncMetadataEntity(
                            key = STARSHIPS_KEY,
                            lastSuccessSyncAtMs = syncMetadataDao.get(STARSHIPS_KEY)?.lastSuccessSyncAtMs,
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

    override suspend fun loadNextStarshipsPage(): Boolean = withContext(ioDispatcher) {
        appendMutex.withLock {
            val state = pagingStateDao.get(STARSHIPS_KEY)
            val nextPage = state?.nextPage ?: return@withContext false
            loadStarshipsPage(page = nextPage)
            true
        }
    }

    override suspend fun refreshStarshipDetails(starshipId: Int) {
        withContext(ioDispatcher) {
            val starship = retryWithBackoff { api.getStarship(starshipId) }
            val entity = starship.toStarshipEntity()
            if (entity != null) {
                starshipDao.insertStarship(entity)
            }
        }
    }

    override suspend fun isBackendReachable(): Boolean = withContext(ioDispatcher) {
        runCatching {
            retryWithBackoff(attempts = 2) { api.getStarshipsPage(page = 1, search = null) }
            true
        }.getOrDefault(false)
    }

    override suspend fun getCachedStarshipName(starshipId: Int): String? =
        withContext(ioDispatcher) {
            starshipDao.getStarshipName(starshipId)
        }

    override suspend fun searchStarships(query: String): List<Starship> = withContext(ioDispatcher) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()
        buildList {
            var page = 1
            repeat(SEARCH_MAX_PAGES) {
                val response = retryWithBackoff { api.getStarshipsPage(page = page, search = trimmed) }
                addAll(response.results.mapNotNull { it.toDomainStarshipListItem() })
                if (response.next == null) return@buildList
                page++
            }
        }.distinctBy { it.id }
    }

    private suspend fun loadStarshipsPage(page: Int) {
        val response = retryWithBackoff { api.getStarshipsPage(page = page, search = null) }
        val entities = response.results.mapNotNull { it.toStarshipEntity() }
        if (entities.isNotEmpty()) {
            starshipDao.insertStarships(entities)
        }
        val nextPage = if (response.next != null) page + 1 else null
        pagingStateDao.upsert(PagingStateEntity(key = STARSHIPS_KEY, nextPage = nextPage))
    }
}

