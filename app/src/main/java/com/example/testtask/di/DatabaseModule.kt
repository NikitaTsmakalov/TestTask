package com.example.testtask.di

import android.content.Context
import androidx.room.Room
import com.example.testtask.data.local.AppDatabase
import com.example.testtask.data.local.dao.CharacterDao
import com.example.testtask.data.local.dao.FilmDao
import com.example.testtask.data.local.dao.PagingStateDao
import com.example.testtask.data.local.dao.PlanetDao
import com.example.testtask.data.local.dao.StarshipDao
import com.example.testtask.data.local.dao.SyncMetadataDao
import com.example.testtask.data.local.dao.RecentViewDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "swapi.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCharacterDao(db: AppDatabase): CharacterDao = db.characterDao()

    @Provides
    fun provideFilmDao(db: AppDatabase): FilmDao = db.filmDao()

    @Provides
    fun providePagingStateDao(db: AppDatabase): PagingStateDao = db.pagingStateDao()

    @Provides
    fun providePlanetDao(db: AppDatabase): PlanetDao = db.planetDao()

    @Provides
    fun provideStarshipDao(db: AppDatabase): StarshipDao = db.starshipDao()

    @Provides
    fun provideSyncMetadataDao(db: AppDatabase): SyncMetadataDao = db.syncMetadataDao()

    @Provides
    fun provideRecentViewDao(db: AppDatabase): RecentViewDao = db.recentViewDao()
}
