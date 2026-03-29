package com.example.testtask.domain.usecase

import com.example.testtask.domain.model.Character
import com.example.testtask.domain.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCharactersUseCase @Inject constructor(
    private val repository: PeopleRepository,
) {
    operator fun invoke(): Flow<List<Character>> = repository.observeCharacters()
}
