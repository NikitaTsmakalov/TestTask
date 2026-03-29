package com.example.testtask.domain.usecase

import com.example.testtask.domain.repository.PeopleRepository
import javax.inject.Inject

class RefreshCharactersUseCase @Inject constructor(
    private val repository: PeopleRepository,
) {
    suspend operator fun invoke() = repository.refreshCharacters()
}
