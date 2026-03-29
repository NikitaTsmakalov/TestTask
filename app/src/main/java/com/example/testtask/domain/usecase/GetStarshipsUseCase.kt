package com.example.testtask.domain.usecase

import com.example.testtask.domain.model.Starship
import com.example.testtask.domain.repository.StarshipsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetStarshipsUseCase @Inject constructor(
    private val repository: StarshipsRepository,
) {
    operator fun invoke(): Flow<List<Starship>> = repository.observeStarships()
}

