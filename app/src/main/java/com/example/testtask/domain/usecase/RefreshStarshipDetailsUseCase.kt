package com.example.testtask.domain.usecase

import com.example.testtask.domain.repository.StarshipsRepository
import javax.inject.Inject

class RefreshStarshipDetailsUseCase @Inject constructor(
    private val repository: StarshipsRepository,
) {
    suspend operator fun invoke(starshipId: Int) = repository.refreshStarshipDetails(starshipId)
}

