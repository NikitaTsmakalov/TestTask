package com.example.testtask.data.mapper

import com.example.testtask.data.local.entity.CharacterEntity
import com.example.testtask.data.local.entity.CharacterWithFilms
import com.example.testtask.data.local.entity.FilmEntity
import com.example.testtask.data.remote.dto.PersonDto
import com.example.testtask.domain.model.Character
import com.example.testtask.domain.model.CharacterDetails
import com.example.testtask.domain.model.Film

fun PersonDto.toCharacterEntity(
    homeworldName: String? = null,
    speciesText: String? = null,
): CharacterEntity? {
    val id = extractIdFromUrl(url) ?: return null
    return CharacterEntity(
        id = id,
        name = name,
        birthYear = birthYear,
        height = height,
        mass = mass,
        gender = gender,
        hairColor = hairColor,
        eyeColor = eyeColor,
        homeworldName = homeworldName,
        speciesText = speciesText,
    )
}

fun CharacterEntity.toDomainCharacter(): Character = Character(
    id = id,
    name = name,
    height = height,
    mass = mass,
    hairColor = hairColor,
    eyeColor = eyeColor,
)

fun PersonDto.toDomainCharacterListItem(): Character? {
    val id = extractIdFromUrl(url) ?: return null
    return Character(
        id = id,
        name = name,
        height = height,
        mass = mass,
        hairColor = hairColor,
        eyeColor = eyeColor,
    )
}

fun FilmEntity.toDomainFilm(): Film = Film(
    id = id,
    title = title,
    openingCrawl = openingCrawl,
)

fun CharacterWithFilms.toDomainDetails(): CharacterDetails = CharacterDetails(
    id = character.id,
    name = character.name,
    birthYear = character.birthYear,
    height = character.height,
    mass = character.mass,
    gender = character.gender,
    hairColor = character.hairColor,
    eyeColor = character.eyeColor,
    homeworldName = character.homeworldName,
    speciesText = character.speciesText,
    films = films.map { it.toDomainFilm() },
)
