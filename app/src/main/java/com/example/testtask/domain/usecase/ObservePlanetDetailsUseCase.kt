package com.example.testtask.domain.usecase

import com.example.testtask.domain.model.PlanetDetails
import com.example.testtask.domain.repository.PlanetsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObservePlanetDetailsUseCase @Inject constructor(
    private val repository: PlanetsRepository,
) {
    operator fun invoke(planetId: Int): Flow<PlanetDetails?> = repository.observePlanetDetails(planetId)
}

