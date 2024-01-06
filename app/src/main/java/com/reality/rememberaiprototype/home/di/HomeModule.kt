package com.reality.rememberaiprototype.home.di

import android.app.Application
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
    fun providesImageRepository(application: Application): HomeRepository {
        return DefaultHomeRepository(contentResolver = application.contentResolver, application)
    }
}
