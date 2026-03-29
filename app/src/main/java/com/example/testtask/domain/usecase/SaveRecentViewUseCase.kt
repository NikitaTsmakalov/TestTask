package com.example.testtask.domain.usecase

import com.example.testtask.domain.repository.RecentViewsRepository
import javax.inject.Inject

class SaveRecentViewUseCase @Inject constructor(
    private val repository: RecentViewsRepository,
) {
    suspend operator fun invoke(type: String, itemId: Int, title: String) =
        repository.save(type = type, itemId = itemId, title = title)
}

