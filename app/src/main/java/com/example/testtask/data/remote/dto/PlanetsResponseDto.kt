package com.example.testtask.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlanetsResponseDto(
    @SerialName("next") val next: String? = null,
    @SerialName("results") val results: List<PlanetDto> = emptyList(),
)

