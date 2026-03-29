package com.example.testtask.data.repository

import com.example.testtask.data.local.dao.RecentViewDao
import com.example.testtask.data.local.entity.RecentViewEntity
import com.example.testtask.domain.model.RecentView
import com.example.testtask.domain.repository.RecentViewsRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RecentViewsRepositoryImpl @Inject constructor(
    private val dao: RecentViewDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : RecentViewsRepository {
    override fun observeRecent(limit: Int): Flow<List<RecentView>> =
        dao.observeRecent(limit).map { items ->
            items.map {
                RecentView(
                    type = it.type,
                    itemId = it.itemId,
                    title = it.title,
                    viewedAtMs = it.viewedAtMs,
                )
            }
        }

    override suspend fun save(type: String, itemId: Int, title: String) {
        withContext(ioDispatcher) {
            dao.upsert(
                RecentViewEntity(
                    key = "$type:$itemId",
                    type = type,
                    itemId = itemId,
                    title = title,
                    viewedAtMs = System.currentTimeMillis(),
                ),
            )
        }
    }
}

