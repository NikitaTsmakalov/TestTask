package com.example.testtask.domain.model

data class CharacterDetails(
    val id: Int,
    val name: String,
    val birthYear: String,
    val height: String,
    val mass: String,
    val gender: String,
    val hairColor: String,
    val eyeColor: String,
    val homeworldName: String?,
    val speciesText: String?,
    val films: List<Film>,
)
