package com.example.testtask.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtask.domain.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RootDestination {
    data object Splash : RootDestination
    data object Onboarding : RootDestination
    data object Main : RootDestination
}

@HiltViewModel
class RootViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val _destination = MutableStateFlow<RootDestination>(RootDestination.Splash)
    val destination: StateFlow<RootDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val completedDeferred = async { onboardingRepository.isOnboardingCompleted() }
            delay(SPLASH_MIN_DURATION_MS)
            _destination.value = if (completedDeferred.await()) {
                RootDestination.Main
            } else {
                RootDestination.Onboarding
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingRepository.setOnboardingCompleted()
            _destination.value = RootDestination.Main
        }
    }

    private companion object {
        const val SPLASH_MIN_DURATION_MS = 1_600L
    }
}
