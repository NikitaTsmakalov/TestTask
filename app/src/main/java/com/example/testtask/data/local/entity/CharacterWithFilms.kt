package com.example.testtask.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CharacterWithFilms(
    @Embedded val character: CharacterEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = CharacterFilmCrossRef::class,
            parentColumn = "characterId",
            entityColumn = "filmId",
        ),
    )
    val films: List<FilmEntity>,
)
