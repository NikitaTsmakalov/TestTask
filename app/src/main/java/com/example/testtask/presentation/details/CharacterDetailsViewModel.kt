package com.example.testtask.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtask.R
import com.example.testtask.domain.repository.PeopleRepository
import com.example.testtask.domain.usecase.ObserveCharacterDetailsUseCase
import com.example.testtask.domain.usecase.RefreshCharacterDetailsUseCase
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
class CharacterDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val peopleRepository: PeopleRepository,
    private val observeCharacterDetailsUseCase: ObserveCharacterDetailsUseCase,
    private val refreshCharacterDetailsUseCase: RefreshCharacterDetailsUseCase,
    private val saveRecentViewUseCase: SaveRecentViewUseCase,
    private val stringProvider: StringProvider,
) : ViewModel() {

    private val characterId: Int =
        checkNotNull(savedStateHandle["characterId"]).toString().toInt()

    private val _uiState = MutableStateFlow(CharacterDetailsUiState(isLoading = true))
    val uiState: StateFlow<CharacterDetailsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val title = peopleRepository.getCachedCharacterName(characterId)
                ?: stringProvider.get(R.string.recent_character_fallback, characterId)
            saveRecentViewUseCase(type = "character", itemId = characterId, title = title)
        }
        observeDetails()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { refreshCharacterDetailsUseCase(characterId) }
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
            observeCharacterDetailsUseCase(characterId).collectLatest { details ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        details = details,
                    )
                }
                details?.let { data ->
                    saveRecentViewUseCase(
                        type = "character",
                        itemId = data.id,
                        title = data.name,
                    )
                }
            }
        }
    }
}
