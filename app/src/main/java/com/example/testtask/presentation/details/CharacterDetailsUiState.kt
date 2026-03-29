package com.example.testtask.presentation.details

import com.example.testtask.domain.model.CharacterDetails

data class CharacterDetailsUiState(
    val isLoading: Boolean = false,
    val details: CharacterDetails? = null,
    val errorMessage: String? = null,
)
