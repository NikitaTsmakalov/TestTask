package com.example.testtask.domain.usecase

import com.example.testtask.domain.model.Character
import com.example.testtask.domain.repository.PeopleRepository
import javax.inject.Inject

class SearchCharactersUseCase @Inject constructor(
    private val repository: PeopleRepository,
) {
    suspend operator fun invoke(query: String): List<Character> = repository.searchCharacters(query)
}
