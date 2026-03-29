package com.example.testtask.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlanetDto(
    @SerialName("name") val name: String = "",
    @SerialName("climate") val climate: String = "",
    @SerialName("terrain") val terrain: String = "",
    @SerialName("population") val population: String = "",
    @SerialName("diameter") val diameter: String = "",
    @SerialName("url") val url: String = "",
)
