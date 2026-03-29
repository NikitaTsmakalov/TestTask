package com.example.testtask.data.repository

import com.example.testtask.data.local.dao.PagingStateDao
import com.example.testtask.data.local.dao.PlanetDao
import com.example.testtask.data.local.dao.SyncMetadataDao
import com.example.testtask.data.local.entity.PagingStateEntity
import com.example.testtask.data.local.entity.PlanetEntity
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

class PlanetsRepositoryImplTest {
    @Test
    fun `refresh keeps cached planets when network fails`() = runTest {
        val dao = FakePlanetDao()
        dao.insertPlanet(
            PlanetEntity(
                id = 1,
                name = "Cached Tatooine",
                climate = "arid",
                terrain = "desert",
                population = "200000",
                diameter = "10465",
            ),
        )

        val repo = PlanetsRepositoryImpl(
            api = FakePlanetsApi(shouldFail = true),
            planetDao = dao,
            pagingStateDao = PlanetsFakePagingStateDao(),
            syncMetadataDao = PlanetsFakeSyncMetadataDao(),
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

        runCatching { repo.refreshPlanets() }
        val cached = repo.observePlanets().first()
        assertEquals(1, cached.size)
        assertEquals("Cached Tatooine", cached.first().name)
    }

    @Test
    fun `load next planets page appends data`() = runTest {
        val repo = PlanetsRepositoryImpl(
            api = FakePlanetsApi(
                pages = listOf(
                    PlanetsResponseDto(
                        next = "https://swapi.dev/api/planets/?page=2",
                        results = listOf(PlanetDto(name = "Alderaan", url = "https://swapi.dev/api/planets/2/")),
                    ),
                    PlanetsResponseDto(
                        next = null,
                        results = listOf(PlanetDto(name = "Yavin IV", url = "https://swapi.dev/api/planets/3/")),
                    ),
                ),
            ),
            planetDao = FakePlanetDao(),
            pagingStateDao = PlanetsFakePagingStateDao(),
            syncMetadataDao = PlanetsFakeSyncMetadataDao(),
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

        repo.refreshPlanets()
        val hasNext = repo.loadNextPlanetsPage()
        val items = repo.observePlanets().first()

        assertTrue(hasNext)
        assertEquals(2, items.size)
    }
}

private class FakePlanetDao : PlanetDao {
    private val planets = MutableStateFlow<List<PlanetEntity>>(emptyList())

    override fun observePlanets(): Flow<List<PlanetEntity>> = planets

    override fun observePlanet(planetId: Int): Flow<PlanetEntity?> =
        MutableStateFlow(planets.value.firstOrNull { it.id == planetId })

    override suspend fun insertPlanets(planets: List<PlanetEntity>) {
        val merged = this.planets.value.associateBy { it.id }.toMutableMap()
        planets.forEach { merged[it.id] = it }
        this.planets.value = merged.values.sortedBy { it.name }
    }

    override suspend fun insertPlanet(planet: PlanetEntity) = insertPlanets(listOf(planet))

    override suspend fun clearPlanets() {
        planets.value = emptyList()
    }

    override suspend fun getPlanetName(planetId: Int): String? =
        planets.value.firstOrNull { it.id == planetId }?.name
}

private class FakePlanetsApi(
    private val pages: List<PlanetsResponseDto> = emptyList(),
    private val shouldFail: Boolean = false,
) : SwapiApi {
    override suspend fun getPeoplePage(page: Int, search: String?): PeopleResponseDto = PeopleResponseDto()
    override suspend fun getPerson(id: Int): PersonDto = PersonDto()
    override suspend fun getPlanet(id: Int): PlanetDto = PlanetDto(name = "Planet$id", url = "https://swapi.dev/api/planets/$id/")
    override suspend fun getPlanetsPage(page: Int, search: String?): PlanetsResponseDto {
        if (shouldFail) error("network")
        if (!search.isNullOrBlank()) return PlanetsResponseDto(next = null, results = emptyList())
        return pages.getOrNull(page - 1) ?: PlanetsResponseDto(next = null, results = emptyList())
    }
    override suspend fun getStarshipsPage(page: Int, search: String?): StarshipsResponseDto = StarshipsResponseDto()
    override suspend fun getStarship(id: Int): StarshipDto = StarshipDto()
    override suspend fun getFilm(id: Int): FilmDto = FilmDto(title = "", openingCrawl = "", url = "")
    override suspend fun getSpeciesByUrl(url: String): SpeciesDto = SpeciesDto(name = "")
}

private class PlanetsFakePagingStateDao : PagingStateDao {
    private val map = mutableMapOf<String, PagingStateEntity>()
    override suspend fun get(key: String): PagingStateEntity? = map[key]
    override suspend fun upsert(state: PagingStateEntity) {
        map[state.key] = state
    }
    override suspend fun clear(key: String) {
        map.remove(key)
    }
}

private class PlanetsFakeSyncMetadataDao : SyncMetadataDao {
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

