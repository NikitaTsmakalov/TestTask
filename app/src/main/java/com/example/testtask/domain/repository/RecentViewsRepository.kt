package com.example.testtask.domain.repository

import com.example.testtask.domain.model.RecentView
import kotlinx.coroutines.flow.Flow

interface RecentViewsRepository {
    fun observeRecent(limit: Int = 30): Flow<List<RecentView>>
    suspend fun save(type: String, itemId: Int, title: String)
}

