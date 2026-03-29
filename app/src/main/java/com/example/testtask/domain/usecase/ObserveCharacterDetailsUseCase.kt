package com.example.testtask.domain.usecase

import com.example.testtask.domain.model.CharacterDetails
import com.example.testtask.domain.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCharacterDetailsUseCase @Inject constructor(
    private val repository: PeopleRepository,
) {
    operator fun invoke(characterId: Int): Flow<CharacterDetails?> =
        repository.observeCharacterDetails(characterId)
}
