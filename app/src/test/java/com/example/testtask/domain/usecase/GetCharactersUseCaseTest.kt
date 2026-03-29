package com.example.testtask.domain.usecase

import com.example.testtask.domain.model.Character
import com.example.testtask.domain.model.CharacterDetails
import com.example.testtask.domain.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCharactersUseCaseTest {
    @Test
    fun `returns characters from repository`() = runTest {
        val expected = listOf(
            Character(1, "Luke Skywalker", "172", "77", "blond", "blue"),
        )
        val repository = FakePeopleRepository(expected)
        val useCase = GetCharactersUseCase(repository)

        val actual = useCase().first()

        assertEquals(expected, actual)
    }
}

private class FakePeopleRepository(
    private val characters: List<Character>,
) : PeopleRepository {
    override fun observeCharacters(): Flow<List<Character>> = flowOf(characters)

    override fun observeCharacterDetails(characterId: Int): Flow<CharacterDetails?> = flowOf(null)

    override fun observeLastSuccessSyncAtMs(): Flow<Long?> = flowOf(null)

    override suspend fun refreshCharacters() = Unit

    override suspend fun loadNextCharactersPage(): Boolean = false

    override suspend fun isBackendReachable(): Boolean = true

    override suspend fun refreshCharacterDetails(characterId: Int) = Unit

    override suspend fun getCachedCharacterName(characterId: Int): String? = null

    override suspend fun searchCharacters(query: String): List<Character> = emptyList()
}
