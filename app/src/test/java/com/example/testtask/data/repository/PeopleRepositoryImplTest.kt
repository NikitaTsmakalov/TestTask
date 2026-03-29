package com.example.testtask.data.repository

import com.example.testtask.data.local.dao.CharacterDao
import com.example.testtask.data.local.dao.FilmDao
import com.example.testtask.data.local.dao.PagingStateDao
import com.example.testtask.data.local.dao.SyncMetadataDao
import com.example.testtask.data.local.entity.CharacterEntity
import com.example.testtask.data.local.entity.CharacterFilmCrossRef
import com.example.testtask.data.local.entity.CharacterWithFilms
import com.example.testtask.data.local.entity.FilmEntity
import com.example.testtask.data.local.entity.PagingStateEntity
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

class PeopleRepositoryImplTest {

    @Test
    fun `refreshCharacters updates cache when network available`() = runTest {
        val dao = FakeCharacterDao()
        val repo = PeopleRepositoryImpl(
            api = FakeSwapiApi(
                peoplePages = listOf(
                    PeopleResponseDto(
                        next = null,
                        results = listOf(
                            PersonDto(
                                name = "Luke Skywalker",
                                birthYear = "19BBY",
                                height = "172",
                                mass = "77",
                                gender = "male",
                                hairColor = "blond",
                                eyeColor = "blue",
                                url = "https://swapi.dev/api/people/1/",
                            ),
                        ),
                    ),
                ),
            ),
            characterDao = dao,
            filmDao = FakeFilmDao(),
            pagingStateDao = FakePagingStateDao(),
            syncMetadataDao = FakeSyncMetadataDao(),
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

        repo.refreshCharacters()

        val cached = repo.observeCharacters().first()
        assertEquals(1, cached.size)
        assertEquals("Luke Skywalker", cached.first().name)
    }

    @Test
    fun `refreshCharacters keeps cached data when network fails`() = runTest {
        val dao = FakeCharacterDao()
        dao.insertCharacter(
            CharacterEntity(
                id = 1,
                name = "Cached Luke",
                birthYear = "19BBY",
                height = "172",
                mass = "77",
                gender = "male",
                hairColor = "blond",
                eyeColor = "blue",
                homeworldName = null,
                speciesText = null,
            ),
        )
        val repo = PeopleRepositoryImpl(
            api = FakeSwapiApi(shouldFailPeople = true),
            characterDao = dao,
            filmDao = FakeFilmDao(),
            pagingStateDao = FakePagingStateDao(),
            syncMetadataDao = FakeSyncMetadataDao(),
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

        runCatching { repo.refreshCharacters() }

        val cached = repo.observeCharacters().first()
        assertEquals(1, cached.size)
        assertEquals("Cached Luke", cached.first().name)
    }

    @Test
    fun `empty api list results in empty cache`() = runTest {
        val dao = FakeCharacterDao()
        val repo = PeopleRepositoryImpl(
            api = FakeSwapiApi(peoplePages = listOf(PeopleResponseDto(next = null, results = emptyList()))),
            characterDao = dao,
            filmDao = FakeFilmDao(),
            pagingStateDao = FakePagingStateDao(),
            syncMetadataDao = FakeSyncMetadataDao(),
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

        repo.refreshCharacters()

        assertTrue(repo.observeCharacters().first().isEmpty())
    }
}

private class FakeCharacterDao : CharacterDao {
    private val characters = MutableStateFlow<List<CharacterEntity>>(emptyList())

    override fun observeCharacters(): Flow<List<CharacterEntity>> = characters

    override fun observeCharacterWithFilms(characterId: Int): Flow<CharacterWithFilms?> =
        MutableStateFlow(null)

    override suspend fun insertCharacters(characters: List<CharacterEntity>) {
        this.characters.value = merge(this.characters.value, characters)
    }

    override suspend fun insertCharacter(character: CharacterEntity) {
        this.characters.value = merge(this.characters.value, listOf(character))
    }

    override suspend fun clearCharacters() {
        characters.value = emptyList()
    }

    override suspend fun insertCharacterFilmRefs(refs: List<CharacterFilmCrossRef>) = Unit

    override suspend fun deleteCharacterFilmRefs(characterId: Int) = Unit

    override suspend fun getCharacterName(characterId: Int): String? =
        characters.value.firstOrNull { it.id == characterId }?.name

    private fun merge(old: List<CharacterEntity>, new: List<CharacterEntity>): List<CharacterEntity> {
        val map = old.associateBy { it.id }.toMutableMap()
        new.forEach { map[it.id] = it }
        return map.values.sortedBy { it.name }
    }
}

private class FakeFilmDao : FilmDao {
    override suspend fun insertFilms(films: List<FilmEntity>) = Unit
}

private class FakePagingStateDao : PagingStateDao {
    private val map = mutableMapOf<String, PagingStateEntity>()

    override suspend fun get(key: String): PagingStateEntity? = map[key]

    override suspend fun upsert(state: PagingStateEntity) {
        map[state.key] = state
    }

    override suspend fun clear(key: String) {
        map.remove(key)
    }
}

private class FakeSyncMetadataDao : SyncMetadataDao {
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

private class FakeSwapiApi(
    private val peoplePages: List<PeopleResponseDto> = emptyList(),
    private val shouldFailPeople: Boolean = false,
) : SwapiApi {
    override suspend fun getPeoplePage(page: Int, search: String?): PeopleResponseDto {
        if (shouldFailPeople) error("No network")
        if (!search.isNullOrBlank()) {
            return PeopleResponseDto(next = null, results = emptyList())
        }
        return peoplePages.getOrNull(page - 1) ?: PeopleResponseDto(next = null, results = emptyList())
    }

    override suspend fun getPerson(id: Int): PersonDto = error("Not used in these tests")

    override suspend fun getPlanet(id: Int): PlanetDto = PlanetDto(name = "")

    override suspend fun getPlanetsPage(page: Int, search: String?): PlanetsResponseDto =
        PlanetsResponseDto(next = null, results = emptyList())

    override suspend fun getStarshipsPage(page: Int, search: String?): StarshipsResponseDto =
        StarshipsResponseDto(next = null, results = emptyList())

    override suspend fun getStarship(id: Int): StarshipDto = StarshipDto()

    override suspend fun getFilm(id: Int): FilmDto = FilmDto(title = "", openingCrawl = "", url = "")

    override suspend fun getSpeciesByUrl(url: String): SpeciesDto = SpeciesDto(name = "")
}
