package com.example.testtask.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class OnboardingRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var repository: OnboardingRepositoryImpl

    @Before
    fun setUp() {
        val file = tempFolder.newFile("test.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { file },
        )
        repository = OnboardingRepositoryImpl(dataStore)
    }

    @Test
    fun isOnboardingCompleted_initiallyFalse() = runBlocking {
        assertFalse(repository.isOnboardingCompleted())
    }

    @Test
    fun afterSetOnboardingCompleted_readsTrue() = runBlocking {
        assertFalse(repository.isOnboardingCompleted())
        repository.setOnboardingCompleted()
        assertTrue(repository.isOnboardingCompleted())
    }
}
