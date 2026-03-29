package com.example.testtask.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonDto(
    @SerialName("name") val name: String = "",
    @SerialName("birth_year") val birthYear: String = "",
    @SerialName("height") val height: String = "",
    @SerialName("mass") val mass: String = "",
    @SerialName("gender") val gender: String = "",
    @SerialName("hair_color") val hairColor: String = "",
    @SerialName("eye_color") val eyeColor: String = "",
    @SerialName("homeworld") val homeworldUrl: String = "",
    @SerialName("species") val speciesUrls: List<String> = emptyList(),
    @SerialName("films") val filmUrls: List<String> = emptyList(),
    @SerialName("url") val url: String = "",
)
