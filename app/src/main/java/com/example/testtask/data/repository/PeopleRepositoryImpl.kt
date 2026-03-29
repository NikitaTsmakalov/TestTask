package com.example.testtask.data.repository

import com.example.testtask.data.local.dao.CharacterDao
import com.example.testtask.data.local.dao.FilmDao
import com.example.testtask.data.local.dao.PagingStateDao
import com.example.testtask.data.local.dao.SyncMetadataDao
import com.example.testtask.data.local.entity.CharacterFilmCrossRef
import com.example.testtask.data.local.entity.FilmEntity
import com.example.testtask.data.local.entity.PagingStateEntity
import com.example.testtask.data.local.entity.SyncMetadataEntity
import com.example.testtask.data.mapper.extractIdFromUrl
import com.example.testtask.data.mapper.toCharacterEntity
import com.example.testtask.data.mapper.toDomainCharacter
import com.example.testtask.data.mapper.toDomainCharacterListItem
import com.example.testtask.data.mapper.toDomainDetails
import com.example.testtask.data.remote.SwapiApi
import com.example.testtask.domain.model.Character
import com.example.testtask.domain.model.CharacterDetails
import com.example.testtask.domain.repository.PeopleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PeopleRepositoryImpl @Inject constructor(
    private val api: SwapiApi,
    private val characterDao: CharacterDao,
    private val filmDao: FilmDao,
    private val pagingStateDao: PagingStateDao,
    private val syncMetadataDao: SyncMetadataDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PeopleRepository {

    private companion object {
        const val PEOPLE_KEY = "people"
        const val SEARCH_MAX_PAGES = 50
    }

    private val refreshMutex = Mutex()
    private val appendMutex = Mutex()

    override fun observeCharacters(): Flow<List<Character>> =
        characterDao.observeCharacters().map { entities ->
            entities.map { it.toDomainCharacter() }
        }

    override fun observeCharacterDetails(characterId: Int): Flow<CharacterDetails?> =
        characterDao.observeCharacterWithFilms(characterId).map { entity ->
            entity?.toDomainDetails()
        }

    override fun observeLastSuccessSyncAtMs(): Flow<Long?> =
        syncMetadataDao.observe(PEOPLE_KEY).map { it?.lastSuccessSyncAtMs }

    override suspend fun refreshCharacters() {
        withContext(ioDispatcher) {
            refreshMutex.withLock {
                runCatching {
                    val response = retryWithBackoff { api.getPeoplePage(page = 1, search = null) }
                    val entities = response.results.mapNotNull { it.toCharacterEntity() }

                    pagingStateDao.clear(PEOPLE_KEY)
                    if (entities.isNotEmpty()) {
                        characterDao.insertCharacters(entities)
                    }
                    val nextPage = if (response.next != null) 2 else null
                    pagingStateDao.upsert(PagingStateEntity(key = PEOPLE_KEY, nextPage = nextPage))

                    syncMetadataDao.upsert(
                        SyncMetadataEntity(
                            key = PEOPLE_KEY,
                            lastSuccessSyncAtMs = System.currentTimeMillis(),
                            lastFailureSyncAtMs = null,
                            lastFailureReason = null,
                            lastSource = "network",
                        ),
                    )
                }.onFailure { error ->
                    syncMetadataDao.upsert(
                        SyncMetadataEntity(
                            key = PEOPLE_KEY,
                            lastSuccessSyncAtMs = syncMetadataDao.get(PEOPLE_KEY)?.lastSuccessSyncAtMs,
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

    override suspend fun loadNextCharactersPage(): Boolean = withContext(ioDispatcher) {
        appendMutex.withLock {
            val state = pagingStateDao.get(PEOPLE_KEY)
            val nextPage = state?.nextPage ?: return@withContext false
            loadPeoplePage(page = nextPage)
            true
        }
    }

    override suspend fun isBackendReachable(): Boolean = withContext(ioDispatcher) {
        runCatching {
            retryWithBackoff(attempts = 2) { api.getPeoplePage(page = 1, search = null) }
            true
        }.getOrDefault(false)
    }

    private suspend fun loadPeoplePage(page: Int) {
        val response = retryWithBackoff { api.getPeoplePage(page = page, search = null) }
        val entities = response.results.mapNotNull { it.toCharacterEntity() }
        if (entities.isNotEmpty()) {
            characterDao.insertCharacters(entities)
        }
        val nextPage = if (response.next != null) page + 1 else null
        pagingStateDao.upsert(PagingStateEntity(key = PEOPLE_KEY, nextPage = nextPage))
    }

    override suspend fun refreshCharacterDetails(characterId: Int) = withContext(ioDispatcher) {
        val person = retryWithBackoff { api.getPerson(characterId) }

        val homeworldName = extractIdFromUrl(person.homeworldUrl)?.let { planetId ->
            runCatching { retryWithBackoff { api.getPlanet(planetId).name } }.getOrNull()
        }

        val speciesText = if (person.speciesUrls.isEmpty()) {
            null
        } else {
            val names = coroutineScope {
                person.speciesUrls.map { url ->
                    async { runCatching { retryWithBackoff { api.getSpeciesByUrl(url).name } }.getOrNull() }
                }.awaitAll().filterNotNull()
            }
            names.takeIf { it.isNotEmpty() }?.joinToString(", ")
        }

        val character = person.toCharacterEntity(
            homeworldName = homeworldName,
            speciesText = speciesText,
        ) ?: return@withContext

        characterDao.insertCharacter(character)

        val films = coroutineScope {
            person.filmUrls.map { filmUrl ->
                async {
                    val filmId = extractIdFromUrl(filmUrl) ?: return@async null
                    runCatching {
                        val film = retryWithBackoff { api.getFilm(filmId) }
                        FilmEntity(
                            id = filmId,
                            title = film.title,
                            openingCrawl = film.openingCrawl,
                        )
                    }.getOrNull()
                }
            }.awaitAll().filterNotNull()
        }

        if (films.isNotEmpty()) {
            filmDao.insertFilms(films)
        }
        characterDao.deleteCharacterFilmRefs(characterId)
        characterDao.insertCharacterFilmRefs(
            refs = films.map { CharacterFilmCrossRef(characterId = characterId, filmId = it.id) },
        )
    }

    override suspend fun getCachedCharacterName(characterId: Int): String? =
        withContext(ioDispatcher) {
            characterDao.getCharacterName(characterId)
        }

    override suspend fun searchCharacters(query: String): List<Character> = withContext(ioDispatcher) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()
        buildList {
            var page = 1
            repeat(SEARCH_MAX_PAGES) {
                val response = retryWithBackoff { api.getPeoplePage(page = page, search = trimmed) }
                addAll(response.results.mapNotNull { it.toDomainCharacterListItem() })
                if (response.next == null) return@buildList
                page++
            }
        }.distinctBy { it.id }
    }
}
