package com.example.testtask.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.testtask.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : OnboardingRepository {

    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

    override suspend fun isOnboardingCompleted(): Boolean =
        dataStore.data.map { it[onboardingCompletedKey] ?: false }.first()

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { prefs ->
            prefs[onboardingCompletedKey] = true
        }
    }
}
