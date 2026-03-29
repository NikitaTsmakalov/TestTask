package com.example.testtask.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FilmDto(
    @SerialName("title") val title: String = "",
    @SerialName("opening_crawl") val openingCrawl: String = "",
    @SerialName("url") val url: String = "",
)
