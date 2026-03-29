package com.example.testtask.domain.usecase

import com.example.testtask.domain.repository.StarshipsRepository
import javax.inject.Inject

class IsStarshipsBackendReachableUseCase @Inject constructor(
    private val repository: StarshipsRepository,
) {
    suspend operator fun invoke(): Boolean = repository.isBackendReachable()
}

