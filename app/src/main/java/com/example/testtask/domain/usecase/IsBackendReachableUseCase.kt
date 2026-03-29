package com.example.testtask.domain.usecase

import com.example.testtask.domain.repository.PeopleRepository
import javax.inject.Inject

class IsBackendReachableUseCase @Inject constructor(
    private val repository: PeopleRepository,
) {
    suspend operator fun invoke(): Boolean = repository.isBackendReachable()
}

