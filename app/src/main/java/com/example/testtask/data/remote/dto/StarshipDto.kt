package com.example.testtask.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StarshipDto(
    @SerialName("name") val name: String = "",
    @SerialName("model") val model: String = "",
    @SerialName("manufacturer") val manufacturer: String = "",
    @SerialName("cost_in_credits") val costInCredits: String = "",
    @SerialName("length") val length: String = "",
    @SerialName("crew") val crew: String = "",
    @SerialName("passengers") val passengers: String = "",
    @SerialName("starship_class") val starshipClass: String = "",
    @SerialName("url") val url: String = "",
)

