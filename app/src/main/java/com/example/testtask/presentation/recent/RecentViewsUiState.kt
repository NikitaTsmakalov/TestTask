package com.example.testtask.presentation.recent

import com.example.testtask.domain.model.RecentView

data class RecentViewsUiState(
    val items: List<RecentView> = emptyList(),
)

