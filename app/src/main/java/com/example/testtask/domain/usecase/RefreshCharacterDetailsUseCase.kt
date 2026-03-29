package com.example.testtask.domain.usecase

import com.example.testtask.domain.repository.PeopleRepository
import javax.inject.Inject

class RefreshCharacterDetailsUseCase @Inject constructor(
    private val repository: PeopleRepository,
) {
    suspend operator fun invoke(characterId: Int) = repository.refreshCharacterDetails(characterId)
}
