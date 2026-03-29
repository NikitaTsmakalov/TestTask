package com.example.testtask.di

import com.example.testtask.data.repository.OnboardingRepositoryImpl
import com.example.testtask.data.repository.PeopleRepositoryImpl
import com.example.testtask.data.repository.PlanetsRepositoryImpl
import com.example.testtask.data.repository.RecentViewsRepositoryImpl
import com.example.testtask.data.repository.StarshipsRepositoryImpl
import com.example.testtask.domain.repository.OnboardingRepository
import com.example.testtask.domain.repository.PeopleRepository
import com.example.testtask.domain.repository.PlanetsRepository
import com.example.testtask.domain.repository.RecentViewsRepository
import com.example.testtask.domain.repository.StarshipsRepository
import com.example.testtask.presentation.common.AndroidStringProvider
import com.example.testtask.presentation.common.StringProvider
import com.example.testtask.presentation.util.NetworkMonitor
import com.example.testtask.presentation.util.NetworkStatusProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(impl: OnboardingRepositoryImpl): OnboardingRepository

    @Binds
    @Singleton
    abstract fun bindPeopleRepository(impl: PeopleRepositoryImpl): PeopleRepository

    @Binds
    @Singleton
    abstract fun bindPlanetsRepository(impl: PlanetsRepositoryImpl): PlanetsRepository

    @Binds
    @Singleton
    abstract fun bindStarshipsRepository(impl: StarshipsRepositoryImpl): StarshipsRepository

    @Binds
    @Singleton
    abstract fun bindRecentViewsRepository(impl: RecentViewsRepositoryImpl): RecentViewsRepository

    @Binds
    @Singleton
    abstract fun bindNetworkStatusProvider(impl: NetworkMonitor): NetworkStatusProvider

    @Binds
    @Singleton
    abstract fun bindStringProvider(impl: AndroidStringProvider): StringProvider
}
