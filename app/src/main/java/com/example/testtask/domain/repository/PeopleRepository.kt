package com.example.testtask.domain.repository

import com.example.testtask.domain.model.Character
import com.example.testtask.domain.model.CharacterDetails
import kotlinx.coroutines.flow.Flow

interface PeopleRepository {
    fun observeCharacters(): Flow<List<Character>>
    fun observeCharacterDetails(characterId: Int): Flow<CharacterDetails?>
    fun observeLastSuccessSyncAtMs(): Flow<Long?>
    suspend fun refreshCharacters()
    suspend fun loadNextCharactersPage(): Boolean
    suspend fun isBackendReachable(): Boolean
    suspend fun refreshCharacterDetails(characterId: Int)
    suspend fun getCachedCharacterName(characterId: Int): String?
    suspend fun searchCharacters(query: String): List<Character>
}
