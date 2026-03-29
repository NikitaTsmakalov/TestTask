package com.example.testtask.presentation.planets

import com.example.testtask.domain.model.Planet

data class PlanetsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isAppending: Boolean = false,
    val isOnline: Boolean = true,
    val isRemoteSearchLoading: Boolean = false,
    val query: String = "",
    val items: List<Planet> = emptyList(),
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val lastUpdatedLabel: String? = null,
)

