package com.example.testtask.domain.model

data class RecentView(
    val type: String,
    val itemId: Int,
    val title: String,
    val viewedAtMs: Long,
)

