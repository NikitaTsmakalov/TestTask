package com.example.testtask.presentation.planets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtask.R
import com.example.testtask.domain.model.Planet
import com.example.testtask.domain.usecase.GetPlanetsUseCase
import com.example.testtask.domain.usecase.IsPlanetsBackendReachableUseCase
import com.example.testtask.domain.usecase.LoadNextPlanetsPageUseCase
import com.example.testtask.domain.usecase.ObservePlanetsLastSyncUseCase
import com.example.testtask.domain.usecase.RefreshPlanetsUseCase
import com.example.testtask.domain.usecase.SearchPlanetsUseCase
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
class PlanetsViewModel @Inject constructor(
    private val getPlanetsUseCase: GetPlanetsUseCase,
    private val refreshPlanetsUseCase: RefreshPlanetsUseCase,
    private val searchPlanetsUseCase: SearchPlanetsUseCase,
    private val loadNextPlanetsPageUseCase: LoadNextPlanetsPageUseCase,
    private val isBackendReachableUseCase: IsPlanetsBackendReachableUseCase,
    private val observePlanetsLastSyncUseCase: ObservePlanetsLastSyncUseCase,
    private val networkStatusProvider: NetworkStatusProvider,
    private val stringProvider: StringProvider,
) : ViewModel() {

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }

    private val machine = NetworkRecoveryStateMachine()

    private val _uiState = MutableStateFlow(PlanetsUiState())
    val uiState = _uiState.asStateFlow()

    private var allPlanetsCache: List<Planet> = emptyList()
    private var remoteSearchResults: List<Planet> = emptyList()

    private var bannerClearJob: Job? = null
    private var recoveryProbeJob: Job? = null
    private var searchJob: Job? = null

    init {
        observePlanets()
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
            val results = runCatching { searchPlanetsUseCase(trimmed) }.getOrElse { emptyList() }
            if (_uiState.value.query.trim() != trimmed) return@launch
            remoteSearchResults = results
            _uiState.update { it.copy(isRemoteSearchLoading = false) }
            mergeDisplayedItems()
        }
    }

    private fun mergeDisplayedItems() {
        val q = _uiState.value.query.trim()
        val items = if (q.isBlank()) {
            allPlanetsCache
        } else {
            val local = allPlanetsCache.filter { planet ->
                planet.name.contains(q, ignoreCase = true) ||
                    planet.climate.contains(q, ignoreCase = true) ||
                    planet.terrain.contains(q, ignoreCase = true)
            }
            val merged = LinkedHashMap<Int, Planet>()
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
                refreshPlanetsUseCase()
                while (loadNextPlanetsPageUseCase()) {
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
            runCatching { loadNextPlanetsPageUseCase() }
                .onFailure {
                    _uiState.update {
                        it.copy(errorMessage = stringProvider.get(R.string.banner_next_page_failed))
                    }
                    scheduleBannerClear()
                }
            _uiState.update { it.copy(isAppending = false) }
        }
    }

    private fun observePlanets() {
        viewModelScope.launch {
            getPlanetsUseCase().collectLatest { list ->
                allPlanetsCache = list
                mergeDisplayedItems()
            }
        }
    }

    private fun observeLastSync() {
        viewModelScope.launch {
            observePlanetsLastSyncUseCase().collectLatest { timestamp ->
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

    private fun applyEffect(effect: com.example.testtask.presentation.characters.NetworkRecoveryEffect) {
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

