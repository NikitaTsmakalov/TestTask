package com.example.testtask.presentation.planets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtask.R
import com.example.testtask.domain.repository.PlanetsRepository
import com.example.testtask.domain.usecase.ObservePlanetDetailsUseCase
import com.example.testtask.domain.usecase.RefreshPlanetDetailsUseCase
import com.example.testtask.domain.usecase.SaveRecentViewUseCase
import com.example.testtask.presentation.common.StringProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PlanetDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val planetsRepository: PlanetsRepository,
    private val observePlanetDetailsUseCase: ObservePlanetDetailsUseCase,
    private val refreshPlanetDetailsUseCase: RefreshPlanetDetailsUseCase,
    private val saveRecentViewUseCase: SaveRecentViewUseCase,
    private val stringProvider: StringProvider,
) : ViewModel() {

    private val planetId: Int = checkNotNull(savedStateHandle["planetId"]).toString().toInt()

    private val _uiState = MutableStateFlow(PlanetDetailsUiState(isLoading = true))
    val uiState: StateFlow<PlanetDetailsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val title = planetsRepository.getCachedPlanetName(planetId)
                ?: stringProvider.get(R.string.recent_planet_fallback, planetId)
            saveRecentViewUseCase(type = "planet", itemId = planetId, title = title)
        }
        observeDetails()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { refreshPlanetDetailsUseCase(planetId) }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = stringProvider.get(R.string.banner_refresh_failed_cached),
                        )
                    }
                }
        }
    }

    private fun observeDetails() {
        viewModelScope.launch {
            observePlanetDetailsUseCase(planetId).collectLatest { details ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        details = details,
                    )
                }
                details?.let { data ->
                    saveRecentViewUseCase(
                        type = "planet",
                        itemId = data.id,
                        title = data.name,
                    )
                }
            }
        }
    }
}

