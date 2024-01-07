package com.reality.rememberaiprototype.home.di

import android.app.Application
import com.reality.rememberaiprototype.home.data.DefaultHomeRepository
import com.reality.rememberaiprototype.home.data.DefaultLocalRepository
import com.reality.rememberaiprototype.home.data.MemoryDao
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
}
