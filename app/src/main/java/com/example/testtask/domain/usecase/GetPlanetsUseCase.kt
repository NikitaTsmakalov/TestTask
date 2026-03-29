package com.example.testtask.domain.usecase

import com.example.testtask.domain.model.Planet
import com.example.testtask.domain.repository.PlanetsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetPlanetsUseCase @Inject constructor(
    private val repository: PlanetsRepository,
) {
    operator fun invoke(): Flow<List<Planet>> = repository.observePlanets()
}

