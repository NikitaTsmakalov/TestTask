package com.example.testtask.presentation.characters

import com.example.testtask.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testtask.domain.usecase.GetCharactersUseCase
import com.example.testtask.domain.usecase.IsBackendReachableUseCase
import com.example.testtask.domain.usecase.LoadNextCharactersPageUseCase
import com.example.testtask.domain.usecase.ObservePeopleLastSyncUseCase
import com.example.testtask.domain.usecase.RefreshCharactersUseCase
import com.example.testtask.domain.model.Character
import com.example.testtask.domain.usecase.SearchCharactersUseCase
import com.example.testtask.presentation.common.formatLastUpdated
import com.example.testtask.presentation.common.StringProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.testtask.presentation.util.NetworkStatusProvider

@HiltViewModel
class CharactersViewModel @Inject constructor(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val refreshCharactersUseCase: RefreshCharactersUseCase,
    private val loadNextCharactersPageUseCase: LoadNextCharactersPageUseCase,
    private val searchCharactersUseCase: SearchCharactersUseCase,
    private val isBackendReachableUseCase: IsBackendReachableUseCase,
    private val observePeopleLastSyncUseCase: ObservePeopleLastSyncUseCase,
    private val stringProvider: StringProvider,
    networkStatusProvider: NetworkStatusProvider,
) : ViewModel() {
    private companion object {
        const val BANNER_DURATION_MS = 4_000L
        const val SEARCH_DEBOUNCE_MS = 400L
    }

    private val allItems = MutableStateFlow<List<Character>>(emptyList())
    private val _uiState = MutableStateFlow(CharactersUiState(isLoading = true))
    val uiState: StateFlow<CharactersUiState> = _uiState.asStateFlow()

    private var bannerJob: Job? = null
    private var stabilizeJob: Job? = null
    private var recoveryProbeJob: Job? = null
    private var searchJob: Job? = null
    private var remoteSearchResults: List<Character> = emptyList()
    private val networkRecovery = NetworkRecoveryStateMachine()

    init {
        viewModelScope.launch {
            networkStatusProvider.isOnline.collectLatest { online ->
                val currentState = _uiState.value
                _uiState.update { it.copy(isOnline = online) }

                scheduleStableNetworkTransition(rawOnline = online, cacheErrorPresent = currentState.errorMessage != null)
            }
        }
        observeCharacters()
        observeLastSync()
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
            val results = runCatching { searchCharactersUseCase(trimmed) }.getOrElse { emptyList() }
            if (_uiState.value.query.trim() != trimmed) return@launch
            remoteSearchResults = results
            _uiState.update { it.copy(isRemoteSearchLoading = false) }
            mergeDisplayedItems()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.items.isEmpty(),
                    isRefreshing = it.items.isNotEmpty(),
                    isAppending = false,
                    errorMessage = null,
                    infoMessage = null,
                )
            }
            runCatching {
                refreshCharactersUseCase()
                while (loadNextCharactersPageUseCase()) {
                }
            }
                .onFailure {
                    networkRecovery.markRefreshFailed()
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isAppending = false,
                            errorMessage = stringProvider.get(R.string.banner_refresh_failed_cached),
                            infoMessage = null,
                        )
                    }
                    scheduleBannerClear()
                    startRecoveryProbe()
                }
                .onSuccess {
                    val effect = networkRecovery.onRefreshSuccess(isOnline = _uiState.value.isOnline)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isAppending = false,
                            errorMessage = if (effect.clearError) null else it.errorMessage,
                            infoMessage = effect.bannerResId?.let(stringProvider::get) ?: it.infoMessage,
                        )
                    }
                    if (_uiState.value.infoMessage != null) {
                        scheduleBannerClear()
                    }
                }
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isRefreshing || state.isAppending) return
        if (!state.isOnline) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAppending = true) }
            runCatching { loadNextCharactersPageUseCase() }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isAppending = false,
                            infoMessage = stringProvider.get(R.string.banner_next_page_failed),
                        )
                    }
                }
                .onSuccess {
                    _uiState.update { it.copy(isAppending = false) }
                }
        }
    }

    private fun observeCharacters() {
        viewModelScope.launch {
            getCharactersUseCase().collectLatest { list ->
                allItems.value = list
                mergeDisplayedItems()
            }
        }
    }

    private fun observeLastSync() {
        viewModelScope.launch {
            observePeopleLastSyncUseCase().collectLatest { timestamp ->
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

    private fun mergeDisplayedItems() {
        val query = _uiState.value.query.trim()
        val items = if (query.isBlank()) {
            allItems.value
        } else {
            val local = allItems.value.filter { it.name.contains(query, ignoreCase = true) }
            val merged = LinkedHashMap<Int, Character>()
            remoteSearchResults.forEach { merged[it.id] = it }
            local.forEach { if (!merged.containsKey(it.id)) merged[it.id] = it }
            merged.values.toList()
        }
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                isAppending = false,
                items = items,
            )
        }
    }

    private fun showInfoBanner(message: String) {
        _uiState.update { it.copy(errorMessage = null, infoMessage = message) }
        scheduleBannerClear()
    }

    private fun scheduleStableNetworkTransition(
        rawOnline: Boolean,
        cacheErrorPresent: Boolean,
    ) {
        stabilizeJob?.cancel()
        val delayMs = networkRecovery.stabilizeDelayMs(rawOnline)
        stabilizeJob = viewModelScope.launch {
            delay(delayMs)

            val stillRawOnline = _uiState.value.isOnline
            if (stillRawOnline != rawOnline) return@launch

            val effect = networkRecovery.onStableNetwork(
                rawOnline = rawOnline,
                cacheErrorPresent = cacheErrorPresent,
            )
            if (effect.bannerResId != null) {
                if (effect.clearError) {
                    _uiState.update { it.copy(errorMessage = null) }
                }
                showInfoBanner(stringProvider.get(effect.bannerResId))
            }
            if (!rawOnline) {
                startRecoveryProbe()
            }
        }
    }

    private fun scheduleBannerClear() {
        bannerJob?.cancel()
        bannerJob = viewModelScope.launch {
            delay(BANNER_DURATION_MS)
            _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
        }
    }

    private fun startRecoveryProbe() {
        if (recoveryProbeJob?.isActive == true) return
        recoveryProbeJob = viewModelScope.launch {
            while (networkRecovery.hasRecoveryPending()) {
                delay(3000)
                val reachable = runCatching { isBackendReachableUseCase() }.getOrDefault(false)
                if (reachable) {
                    val effect = networkRecovery.onRecoveryProbeSuccess()
                    if (effect.clearError) {
                        _uiState.update { it.copy(errorMessage = null) }
                    }
                    if (effect.bannerResId != null) {
                        showInfoBanner(stringProvider.get(effect.bannerResId))
                    }
                    break
                }
            }
        }
    }
}
