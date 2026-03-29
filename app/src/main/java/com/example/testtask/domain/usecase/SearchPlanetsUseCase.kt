package com.example.testtask.domain.usecase

import com.example.testtask.domain.model.Planet
import com.example.testtask.domain.repository.PlanetsRepository
import javax.inject.Inject

class SearchPlanetsUseCase @Inject constructor(
    private val repository: PlanetsRepository,
) {
    suspend operator fun invoke(query: String): List<Planet> = repository.searchPlanets(query)
}
