package com.example.testtask.domain.usecase

import com.example.testtask.domain.model.StarshipDetails
import com.example.testtask.domain.repository.StarshipsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveStarshipDetailsUseCase @Inject constructor(
    private val repository: StarshipsRepository,
) {
    operator fun invoke(starshipId: Int): Flow<StarshipDetails?> =
        repository.observeStarshipDetails(starshipId)
}

