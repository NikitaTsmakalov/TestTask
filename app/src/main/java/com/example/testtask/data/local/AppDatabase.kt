package com.example.testtask.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.testtask.data.local.dao.CharacterDao
import com.example.testtask.data.local.dao.FilmDao
import com.example.testtask.data.local.dao.PagingStateDao
import com.example.testtask.data.local.dao.PlanetDao
import com.example.testtask.data.local.dao.StarshipDao
import com.example.testtask.data.local.dao.SyncMetadataDao
import com.example.testtask.data.local.dao.RecentViewDao
import com.example.testtask.data.local.entity.CharacterEntity
import com.example.testtask.data.local.entity.CharacterFilmCrossRef
import com.example.testtask.data.local.entity.FilmEntity
import com.example.testtask.data.local.entity.PagingStateEntity
import com.example.testtask.data.local.entity.PlanetEntity
import com.example.testtask.data.local.entity.StarshipEntity
import com.example.testtask.data.local.entity.SyncMetadataEntity
import com.example.testtask.data.local.entity.RecentViewEntity

@Database(
    entities = [
        CharacterEntity::class,
        FilmEntity::class,
        CharacterFilmCrossRef::class,
        PagingStateEntity::class,
        PlanetEntity::class,
        StarshipEntity::class,
        SyncMetadataEntity::class,
        RecentViewEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun filmDao(): FilmDao
    abstract fun pagingStateDao(): PagingStateDao
    abstract fun planetDao(): PlanetDao
    abstract fun starshipDao(): StarshipDao
    abstract fun syncMetadataDao(): SyncMetadataDao
    abstract fun recentViewDao(): RecentViewDao
}
