package com.example.testtask.domain.usecase

import com.example.testtask.domain.model.RecentView
import com.example.testtask.domain.repository.RecentViewsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetRecentViewsUseCase @Inject constructor(
    private val repository: RecentViewsRepository,
) {
    operator fun invoke(limit: Int = 30): Flow<List<RecentView>> = repository.observeRecent(limit)
}

