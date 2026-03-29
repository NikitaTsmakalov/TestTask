package com.example.testtask.presentation.starships

import com.example.testtask.domain.model.StarshipDetails

data class StarshipDetailsUiState(
    val isLoading: Boolean = true,
    val details: StarshipDetails? = null,
    val errorMessage: String? = null,
)

