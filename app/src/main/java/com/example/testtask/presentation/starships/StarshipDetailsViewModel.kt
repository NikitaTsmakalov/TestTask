package com.example.testtask.presentation.starships

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtask.R
import com.example.testtask.domain.repository.StarshipsRepository
import com.example.testtask.domain.usecase.ObserveStarshipDetailsUseCase
import com.example.testtask.domain.usecase.RefreshStarshipDetailsUseCase
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
class StarshipDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val starshipsRepository: StarshipsRepository,
    private val observeStarshipDetailsUseCase: ObserveStarshipDetailsUseCase,
    private val refreshStarshipDetailsUseCase: RefreshStarshipDetailsUseCase,
    private val saveRecentViewUseCase: SaveRecentViewUseCase,
    private val stringProvider: StringProvider,
) : ViewModel() {

    private val starshipId: Int = checkNotNull(savedStateHandle["starshipId"]).toString().toInt()

    private val _uiState = MutableStateFlow(StarshipDetailsUiState(isLoading = true))
    val uiState: StateFlow<StarshipDetailsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val title = starshipsRepository.getCachedStarshipName(starshipId)
                ?: stringProvider.get(R.string.recent_starship_fallback, starshipId)
            saveRecentViewUseCase(type = "starship", itemId = starshipId, title = title)
        }
        observeDetails()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { refreshStarshipDetailsUseCase(starshipId) }
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
            observeStarshipDetailsUseCase(starshipId).collectLatest { details ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        details = details,
                    )
                }
                details?.let { data ->
                    saveRecentViewUseCase(
                        type = "starship",
                        itemId = data.id,
                        title = data.name,
                    )
                }
            }
        }
    }
}

