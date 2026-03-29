package com.example.testtask.domain.usecase

import com.example.testtask.domain.repository.PlanetsRepository
import javax.inject.Inject

class LoadNextPlanetsPageUseCase @Inject constructor(
    private val repository: PlanetsRepository,
) {
    suspend operator fun invoke(): Boolean = repository.loadNextPlanetsPage()
}

