package com.example.testtask.domain.usecase

import com.example.testtask.domain.repository.PlanetsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObservePlanetsLastSyncUseCase @Inject constructor(
    private val repository: PlanetsRepository,
) {
    operator fun invoke(): Flow<Long?> = repository.observeLastSuccessSyncAtMs()
}

