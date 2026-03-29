package com.example.testtask.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_views")
data class RecentViewEntity(
    @PrimaryKey val key: String,
    val type: String,
    val itemId: Int,
    val title: String,
    val viewedAtMs: Long,
)

