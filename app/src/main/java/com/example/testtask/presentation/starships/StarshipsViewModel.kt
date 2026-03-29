package com.example.testtask.presentation.starships

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtask.R
import com.example.testtask.domain.model.Starship
import com.example.testtask.domain.usecase.GetStarshipsUseCase
import com.example.testtask.domain.usecase.IsStarshipsBackendReachableUseCase
import com.example.testtask.domain.usecase.LoadNextStarshipsPageUseCase
import com.example.testtask.domain.usecase.ObserveStarshipsLastSyncUseCase
import com.example.testtask.domain.usecase.RefreshStarshipsUseCase
import com.example.testtask.domain.usecase.SearchStarshipsUseCase
import com.example.testtask.presentation.characters.NetworkRecoveryEffect
import com.example.testtask.presentation.characters.NetworkRecoveryStateMachine
import com.example.testtask.presentation.common.formatLastUpdated
import com.example.testtask.presentation.common.StringProvider
import com.example.testtask.presentation.util.NetworkStatusProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class StarshipsViewModel @Inject constructor(
    private val getStarshipsUseCase: GetStarshipsUseCase,
    private val refreshStarshipsUseCase: RefreshStarshipsUseCase,
    private val searchStarshipsUseCase: SearchStarshipsUseCase,
    private val loadNextStarshipsPageUseCase: LoadNextStarshipsPageUseCase,
    private val isBackendReachableUseCase: IsStarshipsBackendReachableUseCase,
    private val observeStarshipsLastSyncUseCase: ObserveStarshipsLastSyncUseCase,
    private val networkStatusProvider: NetworkStatusProvider,
    private val stringProvider: StringProvider,
) : ViewModel() {

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }

    private val machine = NetworkRecoveryStateMachine()

    private val _uiState = MutableStateFlow(StarshipsUiState())
    val uiState = _uiState.asStateFlow()

    private var allStarshipsCache: List<Starship> = emptyList()
    private var remoteSearchResults: List<Starship> = emptyList()

    private var bannerClearJob: Job? = null
    private var recoveryProbeJob: Job? = null
    private var searchJob: Job? = null

    init {
        observeStarships()
        observeLastSync()
        observeNetwork()
        refresh()
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            remoteSearchResults = emptyList()
            _uiState.update { it.copy(isRemoteSearchLoading = false) }
            mergeDisplayedItems()
            return
        }
        remoteSearchResults = emptyList()
        mergeDisplayedItems()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            if (_uiState.value.query.trim() != trimmed) return@launch
            if (!_uiState.value.isOnline) {
                _uiState.update { it.copy(isRemoteSearchLoading = false) }
                return@launch
            }
            _uiState.update { it.copy(isRemoteSearchLoading = true) }
            val results = runCatching { searchStarshipsUseCase(trimmed) }.getOrElse { emptyList() }
            if (_uiState.value.query.trim() != trimmed) return@launch
            remoteSearchResults = results
            _uiState.update { it.copy(isRemoteSearchLoading = false) }
            mergeDisplayedItems()
        }
    }

    private fun mergeDisplayedItems() {
        val q = _uiState.value.query.trim()
        val items = if (q.isBlank()) {
            allStarshipsCache
        } else {
            val local = allStarshipsCache.filter { ship ->
                ship.name.contains(q, ignoreCase = true) ||
                    ship.model.contains(q, ignoreCase = true) ||
                    ship.starshipClass.contains(q, ignoreCase = true)
            }
            val merged = LinkedHashMap<Int, Starship>()
            remoteSearchResults.forEach { merged[it.id] = it }
            local.forEach { if (!merged.containsKey(it.id)) merged[it.id] = it }
            merged.values.toList()
        }
        _uiState.update { it.copy(items = items, isLoading = false) }
    }

    fun refresh() {
        recoveryProbeJob?.cancel()
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            runCatching {
                refreshStarshipsUseCase()
                while (loadNextStarshipsPageUseCase()) {
                }
            }.onSuccess {
                val isOnline = _uiState.value.isOnline
                val effect = machine.onRefreshSuccess(isOnline = isOnline)
                applyEffect(effect)
            }.onFailure {
                machine.markRefreshFailed()
                _uiState.update {
                    it.copy(
                        errorMessage = stringProvider.get(R.string.banner_refresh_failed_cached),
                    )
                }
                startRecoveryProbe()
            }

            _uiState.update { it.copy(isRefreshing = false) }
            scheduleBannerClear()
        }
    }

    fun loadNextPage() {
        if (_uiState.value.isAppending || _uiState.value.isRefreshing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAppending = true) }
            runCatching { loadNextStarshipsPageUseCase() }
                .onFailure {
                    _uiState.update {
                        it.copy(errorMessage = stringProvider.get(R.string.banner_next_page_failed))
                    }
                    scheduleBannerClear()
                }
            _uiState.update { it.copy(isAppending = false) }
        }
    }

    private fun observeStarships() {
        viewModelScope.launch {
            getStarshipsUseCase().collectLatest { list ->
                allStarshipsCache = list
                mergeDisplayedItems()
            }
        }
    }

    private fun observeLastSync() {
        viewModelScope.launch {
            observeStarshipsLastSyncUseCase().collectLatest { timestamp ->
                _uiState.update {
                    it.copy(
                        lastUpdatedLabel = timestamp?.let { ts ->
                            formatLastUpdated(
                                template = stringProvider.get(R.string.last_updated_template),
                                timestampMs = ts,
                            )
                        },
                    )
                }
            }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkStatusProvider.isOnline
                .distinctUntilChanged()
                .collectLatest { rawOnline ->
                    delay(machine.stabilizeDelayMs(rawOnline))
                    val effect = machine.onStableNetwork(
                        rawOnline = rawOnline,
                        cacheErrorPresent = _uiState.value.errorMessage != null,
                    )
                    _uiState.update { it.copy(isOnline = rawOnline) }
                    applyEffect(effect)
                    if (!rawOnline) {
                        startRecoveryProbe()
                    }
                    scheduleBannerClear()
                }
        }
    }

    private fun startRecoveryProbe() {
        if (!machine.hasRecoveryPending()) return
        recoveryProbeJob?.cancel()
        recoveryProbeJob = viewModelScope.launch {
            while (machine.hasRecoveryPending()) {
                delay(2_000)
                val reachable = isBackendReachableUseCase()
                if (reachable) {
                    val effect = machine.onRecoveryProbeSuccess()
                    applyEffect(effect)
                    scheduleBannerClear()
                    return@launch
                }
            }
        }
    }

    private fun applyEffect(effect: NetworkRecoveryEffect) {
        if (effect.clearError) {
            _uiState.update { it.copy(errorMessage = null) }
        }
        effect.bannerResId?.let { resId ->
            _uiState.update { it.copy(infoMessage = stringProvider.get(resId)) }
        }
    }

    private fun scheduleBannerClear() {
        bannerClearJob?.cancel()
        val hasBanner = _uiState.value.errorMessage != null || _uiState.value.infoMessage != null
        if (!hasBanner) return
        bannerClearJob = viewModelScope.launch {
            delay(4_000)
            _uiState.update { it.copy(infoMessage = null) }
        }
    }
}

