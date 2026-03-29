package com.example.testtask.presentation.characters

import com.example.testtask.domain.model.Character
import com.example.testtask.domain.model.CharacterDetails
import com.example.testtask.domain.repository.PeopleRepository
import com.example.testtask.domain.usecase.GetCharactersUseCase
import com.example.testtask.domain.usecase.IsBackendReachableUseCase
import com.example.testtask.domain.usecase.LoadNextCharactersPageUseCase
import com.example.testtask.domain.usecase.ObservePeopleLastSyncUseCase
import com.example.testtask.domain.usecase.RefreshCharactersUseCase
import com.example.testtask.domain.usecase.SearchCharactersUseCase
import com.example.testtask.presentation.common.StringProvider
import com.example.testtask.presentation.util.NetworkStatusProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharactersViewModelTest {
    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shows network restored info when online returns`() = runTest(dispatcher) {
        val repo = FakePeopleRepository()
        val network = FakeNetworkStatusProvider(initial = false)
        val strings = FakeStringProvider()
        val vm = CharactersViewModel(
            getCharactersUseCase = GetCharactersUseCase(repo),
            refreshCharactersUseCase = RefreshCharactersUseCase(repo),
            loadNextCharactersPageUseCase = LoadNextCharactersPageUseCase(repo),
            searchCharactersUseCase = SearchCharactersUseCase(repo),
            isBackendReachableUseCase = IsBackendReachableUseCase(repo),
            observePeopleLastSyncUseCase = ObservePeopleLastSyncUseCase(repo),
            stringProvider = strings,
            networkStatusProvider = network,
        )

        runCurrent()
        advanceTimeBy(1_501)
        runCurrent()
        network.update(true)
        runCurrent()
        advanceTimeBy(1_001)
        runCurrent()

        assertTrue(vm.uiState.value.infoMessage?.contains("Снова в сети") == true)
        assertNull(vm.uiState.value.errorMessage)

        advanceTimeBy(4_001)
        runCurrent()
        assertNull(vm.uiState.value.infoMessage)
    }

    @Test
    fun `refresh loads all pages and updates full list`() = runTest(dispatcher) {
        val repo = FakePeopleRepository()
        val network = FakeNetworkStatusProvider(initial = true)
        val strings = FakeStringProvider()
        val vm = CharactersViewModel(
            getCharactersUseCase = GetCharactersUseCase(repo),
            refreshCharactersUseCase = RefreshCharactersUseCase(repo),
            loadNextCharactersPageUseCase = LoadNextCharactersPageUseCase(repo),
            searchCharactersUseCase = SearchCharactersUseCase(repo),
            isBackendReachableUseCase = IsBackendReachableUseCase(repo),
            observePeopleLastSyncUseCase = ObservePeopleLastSyncUseCase(repo),
            stringProvider = strings,
            networkStatusProvider = network,
        )

        runCurrent()

        assertEquals(3, vm.uiState.value.items.size)
        assertEquals(3, repo.loadNextCalls)
        assertEquals("Данные обновлены", vm.uiState.value.infoMessage)

        advanceTimeBy(4_001)
        runCurrent()
        assertNull(vm.uiState.value.infoMessage)
    }

    @Test
    fun `failed refresh then recovery probe shows restored banner`() = runTest(dispatcher) {
        val repo = FakePeopleRepository()
        val network = FakeNetworkStatusProvider(initial = true)
        val strings = FakeStringProvider()
        val vm = CharactersViewModel(
            getCharactersUseCase = GetCharactersUseCase(repo),
            refreshCharactersUseCase = RefreshCharactersUseCase(repo),
            loadNextCharactersPageUseCase = LoadNextCharactersPageUseCase(repo),
            searchCharactersUseCase = SearchCharactersUseCase(repo),
            isBackendReachableUseCase = IsBackendReachableUseCase(repo),
            observePeopleLastSyncUseCase = ObservePeopleLastSyncUseCase(repo),
            stringProvider = strings,
            networkStatusProvider = network,
        )

        runCurrent()
        assertNull(vm.uiState.value.errorMessage)

        repo.refreshShouldFail = true
        repo.backendReachable = false
        vm.refresh()
        runCurrent()
        assertTrue(vm.uiState.value.errorMessage?.contains("Показан кеш") == true)

        repo.backendReachable = true
        advanceTimeBy(3_100)
        runCurrent()

        assertNull(vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.infoMessage?.contains("Снова в сети") == true)
    }

    @Test
    fun `repeated online event clears stale cache error`() = runTest(dispatcher) {
        val repo = FakePeopleRepository()
        val network = FakeNetworkStatusProvider(initial = true)
        val strings = FakeStringProvider()
        val vm = CharactersViewModel(
            getCharactersUseCase = GetCharactersUseCase(repo),
            refreshCharactersUseCase = RefreshCharactersUseCase(repo),
            loadNextCharactersPageUseCase = LoadNextCharactersPageUseCase(repo),
            searchCharactersUseCase = SearchCharactersUseCase(repo),
            isBackendReachableUseCase = IsBackendReachableUseCase(repo),
            observePeopleLastSyncUseCase = ObservePeopleLastSyncUseCase(repo),
            stringProvider = strings,
            networkStatusProvider = network,
        )

        runCurrent()
        repo.refreshShouldFail = true
        repo.backendReachable = false
        vm.refresh()
        runCurrent()
        assertTrue(vm.uiState.value.errorMessage?.contains("Показан кеш") == true)

        repo.backendReachable = true
        network.update(true)
        runCurrent()
        advanceTimeBy(3_100)
        runCurrent()

        assertNull(vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.infoMessage?.contains("Снова в сети") == true)
    }

    @Test
    fun `offline then online without refresh shows restored banner`() = runTest(dispatcher) {
        val repo = FakePeopleRepository()
        val network = FakeNetworkStatusProvider(initial = true)
        val strings = FakeStringProvider()
        val vm = CharactersViewModel(
            getCharactersUseCase = GetCharactersUseCase(repo),
            refreshCharactersUseCase = RefreshCharactersUseCase(repo),
            loadNextCharactersPageUseCase = LoadNextCharactersPageUseCase(repo),
            searchCharactersUseCase = SearchCharactersUseCase(repo),
            isBackendReachableUseCase = IsBackendReachableUseCase(repo),
            observePeopleLastSyncUseCase = ObservePeopleLastSyncUseCase(repo),
            stringProvider = strings,
            networkStatusProvider = network,
        )

        runCurrent()
        network.update(false)
        runCurrent()
        advanceTimeBy(1_501)
        runCurrent()
        assertTrue(vm.uiState.value.infoMessage?.contains("Оффлайн") == true)

        network.update(true)
        runCurrent()
        advanceTimeBy(1_001)
        runCurrent()
        assertTrue(vm.uiState.value.infoMessage?.contains("Снова в сети") == true)
    }

}

private class FakeStringProvider : StringProvider {
    override fun get(resId: Int): String = when (resId) {
        com.example.testtask.R.string.banner_offline_cached -> "Оффлайн: показаны сохранённые данные"
        com.example.testtask.R.string.banner_restored -> "Снова в сети. Обновите список, чтобы получить актуальные данные."
        com.example.testtask.R.string.banner_data_updated -> "Данные обновлены"
        com.example.testtask.R.string.banner_refresh_failed_cached -> "Не удалось обновить данные. Показан кеш."
        com.example.testtask.R.string.banner_next_page_failed -> "Не удалось загрузить следующую страницу"
        else -> "unknown"
    }

    override fun get(resId: Int, vararg formatArgs: Any): String = get(resId)
}

private class FakeNetworkStatusProvider(initial: Boolean) : NetworkStatusProvider {
    private val flow = MutableSharedFlow<Boolean>(replay = 1)
    override val isOnline: Flow<Boolean> = flow

    init {
        flow.tryEmit(initial)
    }

    fun update(value: Boolean) {
        flow.tryEmit(value)
    }
}

private class FakePeopleRepository : PeopleRepository {
    private val charactersFlow = MutableStateFlow<List<Character>>(emptyList())
    var loadNextCalls: Int = 0
        private set
    private var page: Int = 0

    override fun observeCharacters(): Flow<List<Character>> = charactersFlow

    override fun observeCharacterDetails(characterId: Int): Flow<CharacterDetails?> = flowOf(null)
    override fun observeLastSuccessSyncAtMs(): Flow<Long?> = flowOf(null)

    var refreshShouldFail: Boolean = false
    var backendReachable: Boolean = true

    override suspend fun refreshCharacters() {
        if (refreshShouldFail) error("offline")
        page = 1
        charactersFlow.value = listOf(
            Character(1, "Luke", "172", "77", "blond", "blue"),
        )
    }

    override suspend fun loadNextCharactersPage(): Boolean {
        loadNextCalls++
        return when (page) {
            1 -> {
                page = 2
                charactersFlow.value = charactersFlow.value + Character(2, "Leia", "150", "49", "brown", "brown")
                true
            }
            2 -> {
                page = 3
                charactersFlow.value = charactersFlow.value + Character(3, "Han", "180", "80", "brown", "brown")
                true
            }
            else -> false
        }
    }

    override suspend fun isBackendReachable(): Boolean = backendReachable

    override suspend fun refreshCharacterDetails(characterId: Int) = Unit

    override suspend fun getCachedCharacterName(characterId: Int): String? = null

    override suspend fun searchCharacters(query: String): List<Character> = emptyList()
}

