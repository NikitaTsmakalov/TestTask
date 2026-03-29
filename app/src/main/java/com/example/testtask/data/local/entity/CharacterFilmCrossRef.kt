package com.example.testtask.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "character_film_cross_ref",
    primaryKeys = ["characterId", "filmId"],
    indices = [Index("filmId")],
)
data class CharacterFilmCrossRef(
    val characterId: Int,
    val filmId: Int,
)
