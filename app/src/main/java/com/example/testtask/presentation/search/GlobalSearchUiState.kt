package com.example.testtask.presentation.search

data class GlobalSearchItem(
    val type: String,
    val id: Int,
    val title: String,
    val subtitle: String,
)

data class GlobalSearchUiState(
    val query: String = "",
    val items: List<GlobalSearchItem> = emptyList(),
    val isSearching: Boolean = false,
)
