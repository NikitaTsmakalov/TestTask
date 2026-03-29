package com.example.testtask.data.mapper

import com.example.testtask.data.local.entity.PlanetEntity
import com.example.testtask.data.remote.dto.PlanetDto
import com.example.testtask.domain.model.Planet
import com.example.testtask.domain.model.PlanetDetails

fun PlanetEntity.toDomainPlanet(): Planet =
    Planet(
        id = id,
        name = name,
        climate = climate,
        terrain = terrain,
    )

fun PlanetEntity.toDomainPlanetDetails(): PlanetDetails =
    PlanetDetails(
        id = id,
        name = name,
        climate = climate,
        terrain = terrain,
        population = population,
        diameter = diameter,
    )

fun PlanetDto.toPlanetEntity(): PlanetEntity? {
    val id = extractIdFromUrl(url)?.takeIf { it > 0 } ?: return null
    return PlanetEntity(
        id = id,
        name = name,
        climate = climate,
        terrain = terrain,
        population = population,
        diameter = diameter,
    )
}

fun PlanetDto.toDomainPlanetListItem(): Planet? {
    val id = extractIdFromUrl(url)?.takeIf { it > 0 } ?: return null
    return Planet(
        id = id,
        name = name,
        climate = climate,
        terrain = terrain,
    )
}

