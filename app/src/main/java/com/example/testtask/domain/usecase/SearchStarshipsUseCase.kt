package com.example.testtask.domain.usecase

import com.example.testtask.domain.model.Starship
import com.example.testtask.domain.repository.StarshipsRepository
import javax.inject.Inject

class SearchStarshipsUseCase @Inject constructor(
    private val repository: StarshipsRepository,
) {
    suspend operator fun invoke(query: String): List<Starship> = repository.searchStarships(query)
}
