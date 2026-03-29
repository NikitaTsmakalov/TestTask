package com.example.testtask.presentation.planets

import com.example.testtask.domain.model.PlanetDetails

data class PlanetDetailsUiState(
    val isLoading: Boolean = true,
    val details: PlanetDetails? = null,
    val errorMessage: String? = null,
)

