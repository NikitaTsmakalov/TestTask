package com.example.testtask.presentation.starships

import com.example.testtask.domain.model.Starship

data class StarshipsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isAppending: Boolean = false,
    val isOnline: Boolean = true,
    val isRemoteSearchLoading: Boolean = false,
    val query: String = "",
    val items: List<Starship> = emptyList(),
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val lastUpdatedLabel: String? = null,
)

