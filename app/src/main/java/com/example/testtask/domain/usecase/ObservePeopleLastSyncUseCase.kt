package com.example.testtask.domain.usecase

import com.example.testtask.domain.repository.PeopleRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObservePeopleLastSyncUseCase @Inject constructor(
    private val repository: PeopleRepository,
) {
    operator fun invoke(): Flow<Long?> = repository.observeLastSuccessSyncAtMs()
}

