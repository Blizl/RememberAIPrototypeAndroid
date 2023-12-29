package com.reality.rememberaiprototype.home.di

import com.reality.rememberaiprototype.home.data.DefaultImageRepository
import com.reality.rememberaiprototype.home.domain.ImageRepository
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
    fun providesImageRepository(): ImageRepository {
        return DefaultImageRepository()
    }
}
