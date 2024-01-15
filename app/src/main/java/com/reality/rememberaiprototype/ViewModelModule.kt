package com.reality.rememberaiprototype

import android.app.Application
import com.reality.rememberaiprototype.home.data.DefaultHomeRepository
import com.reality.rememberaiprototype.home.domain.HomeRepository
import com.reality.rememberaiprototype.home.domain.LocalRepository
import com.reality.rememberaiprototype.imagetotext.domain.ImageToTextRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Scope
import javax.inject.Singleton


@InstallIn(ViewModelComponent::class)
@Module
object ViewModelModule {

    @Provides
    @ViewModelScoped
    fun providesHomeRepository(
        application: Application,
        localRepository: LocalRepository,
        imageToTextRepository: ImageToTextRepository
    ): HomeRepository {
        return DefaultHomeRepository(application, localRepository, imageToTextRepository)
    }

}