package com.example.testtask.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testtask.data.local.entity.CharacterEntity
import com.example.testtask.data.local.entity.CharacterFilmCrossRef
import com.example.testtask.data.local.entity.CharacterWithFilms
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters ORDER BY name ASC")
    fun observeCharacters(): Flow<List<CharacterEntity>>

    @Transaction
    @Query("SELECT * FROM characters WHERE id = :characterId")
    fun observeCharacterWithFilms(characterId: Int): Flow<CharacterWithFilms?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    @Query("DELETE FROM characters")
    suspend fun clearCharacters()

    @Query("SELECT name FROM characters WHERE id = :characterId LIMIT 1")
    suspend fun getCharacterName(characterId: Int): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacterFilmRefs(refs: List<CharacterFilmCrossRef>)

    @Query("DELETE FROM character_film_cross_ref WHERE characterId = :characterId")
    suspend fun deleteCharacterFilmRefs(characterId: Int)
}
