package com.example.testtask.data.mapper

import com.example.testtask.data.local.entity.StarshipEntity
import com.example.testtask.data.remote.dto.StarshipDto
import com.example.testtask.domain.model.Starship
import com.example.testtask.domain.model.StarshipDetails

fun StarshipEntity.toDomainStarship(): Starship =
    Starship(
        id = id,
        name = name,
        model = model,
        starshipClass = starshipClass,
    )

fun StarshipEntity.toDomainStarshipDetails(): StarshipDetails =
    StarshipDetails(
        id = id,
        name = name,
        model = model,
        manufacturer = manufacturer,
        costInCredits = costInCredits,
        length = length,
        crew = crew,
        passengers = passengers,
        starshipClass = starshipClass,
    )

fun StarshipDto.toStarshipEntity(): StarshipEntity? {
    val id = extractIdFromUrl(url)?.takeIf { it > 0 } ?: return null
    return StarshipEntity(
        id = id,
        name = name,
        model = model,
        manufacturer = manufacturer,
        costInCredits = costInCredits,
        length = length,
        crew = crew,
        passengers = passengers,
        starshipClass = starshipClass,
    )
}

fun StarshipDto.toDomainStarshipListItem(): Starship? {
    val id = extractIdFromUrl(url)?.takeIf { it > 0 } ?: return null
    return Starship(
        id = id,
        name = name,
        model = model,
        starshipClass = starshipClass,
    )
}

