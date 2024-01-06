package com.reality.rememberaiprototype.home.di

import android.app.Application
import androidx.room.Room
import com.reality.rememberaiprototype.home.data.AppDatabase
import com.reality.rememberaiprototype.home.data.DefaultHomeRepository
import com.reality.rememberaiprototype.home.data.DefaultLocalRepository
import com.reality.rememberaiprototype.home.data.MemoryDao
import com.reality.rememberaiprototype.home.data.MlKitTextRecognition
import com.reality.rememberaiprototype.home.domain.HomeRepository
import com.reality.rememberaiprototype.home.domain.LocalRepository
import com.reality.rememberaiprototype.home.domain.TextRecognitionProcessor
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
    fun providesImageRepository(
        application: Application,
        localRepository: LocalRepository,
        textRecognitionProcessor: TextRecognitionProcessor
    ): HomeRepository {
        return DefaultHomeRepository(application, localRepository, textRecognitionProcessor)
    }

    @Provides
    @Singleton
    fun providesLocalRepository(memoryDao: MemoryDao): LocalRepository {
        return DefaultLocalRepository(memoryDao)
    }

    @Provides
    @Singleton
    fun providesMemoryDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(
            application.applicationContext,
            AppDatabase::class.java, "memory-database"
        ).build()
    }

    @Provides
    @Singleton
    fun providesMemoryDao(db: AppDatabase): MemoryDao {
        return db.memoryDao()
    }

    @Provides
    @Singleton
    fun providesTextRecognitionProcessor(application: Application): TextRecognitionProcessor {
        return MlKitTextRecognition(application.contentResolver)
    }
}
