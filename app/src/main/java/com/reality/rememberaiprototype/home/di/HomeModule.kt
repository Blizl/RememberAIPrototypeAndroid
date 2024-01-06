package com.reality.rememberaiprototype.home.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import com.reality.rememberaiprototype.home.data.AppDatabase
import com.reality.rememberaiprototype.home.data.DefaultHomeRepository
import com.reality.rememberaiprototype.home.domain.HomeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object HomeModule {
    @Provides
    @Singleton
    fun providesImageRepository(application: Application, roomDatabase: RoomDatabase): HomeRepository {
        return DefaultHomeRepository(contentResolver = application.contentResolver, application, roomDatabase)
    }

    @Provides
    @Singleton
    fun providesMemoryDatabase(application: Application): RoomDatabase {
        return Room.databaseBuilder(
            application.applicationContext,
            AppDatabase::class.java, "memory-database"
        ).build()
    }
}
