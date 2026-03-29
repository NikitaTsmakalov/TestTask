package com.example.testtask.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val birthYear: String,
    val height: String,
    val mass: String,
    val gender: String,
    val hairColor: String,
    val eyeColor: String,
    val homeworldName: String?,
    val speciesText: String?,
)
