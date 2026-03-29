package com.example.testtask.data.repository

import com.example.testtask.data.local.dao.PagingStateDao
import com.example.testtask.data.local.dao.StarshipDao
import com.example.testtask.data.local.dao.SyncMetadataDao
import com.example.testtask.data.local.entity.PagingStateEntity
import com.example.testtask.data.local.entity.StarshipEntity
import com.example.testtask.data.local.entity.SyncMetadataEntity
import com.example.testtask.data.remote.SwapiApi
import com.example.testtask.data.remote.dto.FilmDto
import com.example.testtask.data.remote.dto.PeopleResponseDto
import com.example.testtask.data.remote.dto.PlanetsResponseDto
import com.example.testtask.data.remote.dto.PersonDto
import com.example.testtask.data.remote.dto.PlanetDto
import com.example.testtask.data.remote.dto.SpeciesDto
import com.example.testtask.data.remote.dto.StarshipDto
import com.example.testtask.data.remote.dto.StarshipsResponseDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StarshipsRepositoryImplTest {
    @Test
    fun `refresh keeps cached starships when network fails`() = runTest {
        val dao = FakeStarshipDao()
        dao.insertStarship(
            StarshipEntity(
                id = 10,
                name = "Cached Falcon",
                model = "YT-1300",
                manufacturer = "Corellian",
                costInCredits = "100000",
                length = "34.75",
                crew = "4",
                passengers = "6",
                starshipClass = "Light freighter",
            ),
        )

        val repo = StarshipsRepositoryImpl(
            api = FakeStarshipsApi(shouldFail = true),
            starshipDao = dao,
            pagingStateDao = StarshipsFakePagingStateDao(),
            syncMetadataDao = StarshipsFakeSyncMetadataDao(),
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

        runCatching { repo.refreshStarships() }
        val cached = repo.observeStarships().first()
        assertEquals(1, cached.size)
        assertEquals("Cached Falcon", cached.first().name)
    }

    @Test
    fun `load next starships page appends data`() = runTest {
        val repo = StarshipsRepositoryImpl(
            api = FakeStarshipsApi(
                pages = listOf(
                    StarshipsResponseDto(
                        next = "https://swapi.dev/api/starships/?page=2",
                        results = listOf(StarshipDto(name = "X-wing", url = "https://swapi.dev/api/starships/12/")),
                    ),
                    StarshipsResponseDto(
                        next = null,
                        results = listOf(StarshipDto(name = "TIE Advanced", url = "https://swapi.dev/api/starships/13/")),
                    ),
                ),
            ),
            starshipDao = FakeStarshipDao(),
            pagingStateDao = StarshipsFakePagingStateDao(),
            syncMetadataDao = StarshipsFakeSyncMetadataDao(),
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

        repo.refreshStarships()
        val hasNext = repo.loadNextStarshipsPage()
        val items = repo.observeStarships().first()

        assertTrue(hasNext)
        assertEquals(2, items.size)
    }
}

private class FakeStarshipDao : StarshipDao {
    private val starships = MutableStateFlow<List<StarshipEntity>>(emptyList())

    override fun observeStarships(): Flow<List<StarshipEntity>> = starships

    override fun observeStarship(starshipId: Int): Flow<StarshipEntity?> =
        MutableStateFlow(starships.value.firstOrNull { it.id == starshipId })

    override suspend fun insertStarships(starships: List<StarshipEntity>) {
        val merged = this.starships.value.associateBy { it.id }.toMutableMap()
        starships.forEach { merged[it.id] = it }
        this.starships.value = merged.values.sortedBy { it.name }
    }

    override suspend fun insertStarship(starship: StarshipEntity) = insertStarships(listOf(starship))

    override suspend fun clearStarships() {
        starships.value = emptyList()
    }

    override suspend fun getStarshipName(starshipId: Int): String? =
        starships.value.firstOrNull { it.id == starshipId }?.name
}

private class FakeStarshipsApi(
    private val pages: List<StarshipsResponseDto> = emptyList(),
    private val shouldFail: Boolean = false,
) : SwapiApi {
    override suspend fun getPeoplePage(page: Int, search: String?): PeopleResponseDto = PeopleResponseDto()
    override suspend fun getPerson(id: Int): PersonDto = PersonDto()
    override suspend fun getPlanet(id: Int): PlanetDto = PlanetDto(name = "", url = "")
    override suspend fun getPlanetsPage(page: Int, search: String?): PlanetsResponseDto = PlanetsResponseDto()
    override suspend fun getStarshipsPage(page: Int, search: String?): StarshipsResponseDto {
        if (shouldFail) error("network")
        if (!search.isNullOrBlank()) return StarshipsResponseDto(next = null, results = emptyList())
        return pages.getOrNull(page - 1) ?: StarshipsResponseDto(next = null, results = emptyList())
    }
    override suspend fun getStarship(id: Int): StarshipDto =
        StarshipDto(name = "Ship$id", url = "https://swapi.dev/api/starships/$id/")
    override suspend fun getFilm(id: Int): FilmDto = FilmDto(title = "", openingCrawl = "", url = "")
    override suspend fun getSpeciesByUrl(url: String): SpeciesDto = SpeciesDto(name = "")
}

private class StarshipsFakePagingStateDao : PagingStateDao {
    private val map = mutableMapOf<String, PagingStateEntity>()
    override suspend fun get(key: String): PagingStateEntity? = map[key]
    override suspend fun upsert(state: PagingStateEntity) {
        map[state.key] = state
    }
    override suspend fun clear(key: String) {
        map.remove(key)
    }
}

private class StarshipsFakeSyncMetadataDao : SyncMetadataDao {
    private val map = mutableMapOf<String, SyncMetadataEntity>()
    private val flows = mutableMapOf<String, MutableStateFlow<SyncMetadataEntity?>>()
    override fun observe(key: String): Flow<SyncMetadataEntity?> =
        flows.getOrPut(key) { MutableStateFlow(map[key]) }
    override suspend fun get(key: String): SyncMetadataEntity? = map[key]
    override suspend fun upsert(entity: SyncMetadataEntity) {
        map[entity.key] = entity
        flows.getOrPut(entity.key) { MutableStateFlow(null) }.value = entity
    }
}

