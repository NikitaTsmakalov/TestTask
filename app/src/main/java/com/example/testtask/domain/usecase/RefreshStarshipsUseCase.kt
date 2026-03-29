package com.example.testtask.domain.usecase

import com.example.testtask.domain.repository.StarshipsRepository
import javax.inject.Inject

class RefreshStarshipsUseCase @Inject constructor(
    private val repository: StarshipsRepository,
) {
    suspend operator fun invoke() = repository.refreshStarships()
}

