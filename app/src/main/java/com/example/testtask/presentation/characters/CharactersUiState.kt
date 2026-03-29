package com.example.testtask.presentation.characters

import com.example.testtask.domain.model.Character

data class CharactersUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isAppending: Boolean = false,
    val isOnline: Boolean = true,
    val isRemoteSearchLoading: Boolean = false,
    val query: String = "",
    val items: List<Character> = emptyList(),
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val lastUpdatedLabel: String? = null,
)
