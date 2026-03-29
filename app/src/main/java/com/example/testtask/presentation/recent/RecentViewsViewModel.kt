package com.example.testtask.presentation.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtask.domain.usecase.GetRecentViewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RecentViewsViewModel @Inject constructor(
    private val getRecentViewsUseCase: GetRecentViewsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecentViewsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getRecentViewsUseCase().collectLatest { items ->
                _uiState.update { it.copy(items = items) }
            }
        }
    }
}

