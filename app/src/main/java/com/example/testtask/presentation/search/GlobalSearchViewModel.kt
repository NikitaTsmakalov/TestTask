package com.example.testtask.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtask.domain.usecase.SearchCharactersUseCase
import com.example.testtask.domain.usecase.SearchPlanetsUseCase
import com.example.testtask.domain.usecase.SearchStarshipsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class GlobalSearchViewModel @Inject constructor(
    private val searchCharactersUseCase: SearchCharactersUseCase,
    private val searchPlanetsUseCase: SearchPlanetsUseCase,
    private val searchStarshipsUseCase: SearchStarshipsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GlobalSearchUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    private companion object {
        const val DEBOUNCE_MS = 400L
    }

    fun onQueryChanged(value: String) {
        _uiState.update { it.copy(query = value) }
        searchJob?.cancel()
        if (value.trim().isEmpty()) {
            _uiState.update { it.copy(items = emptyList(), isSearching = false) }
            return
        }
        val trimmed = value.trim()
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            if (_uiState.value.query.trim() != trimmed) return@launch
            _uiState.update { it.copy(isSearching = true) }
            val items = coroutineScope {
                val chars = async { runCatching { searchCharactersUseCase(trimmed) }.getOrElse { emptyList() } }
                val planets = async { runCatching { searchPlanetsUseCase(trimmed) }.getOrElse { emptyList() } }
                val ships = async { runCatching { searchStarshipsUseCase(trimmed) }.getOrElse { emptyList() } }
                buildList {
                    addAll(
                        chars.await().map {
                            GlobalSearchItem(
                                type = "character",
                                id = it.id,
                                title = it.name,
                                subtitle = "Character",
                            )
                        },
                    )
                    addAll(
                        planets.await().map {
                            GlobalSearchItem(
                                type = "planet",
                                id = it.id,
                                title = it.name,
                                subtitle = "Planet",
                            )
                        },
                    )
                    addAll(
                        ships.await().map {
                            GlobalSearchItem(
                                type = "starship",
                                id = it.id,
                                title = it.name,
                                subtitle = "Starship",
                            )
                        },
                    )
                }
            }
            if (_uiState.value.query.trim() != trimmed) return@launch
            val sorted = items.sortedBySearchRelevance(trimmed)
            _uiState.update { it.copy(items = sorted, isSearching = false) }
        }
    }
}
