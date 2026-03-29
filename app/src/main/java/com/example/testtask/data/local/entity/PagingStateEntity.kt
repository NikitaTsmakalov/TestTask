package com.example.testtask.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paging_state")
data class PagingStateEntity(
    @PrimaryKey val key: String,
    val nextPage: Int?,
)

