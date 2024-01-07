package com.reality.rememberaiprototype.imagetotext.di

import android.app.Application
import com.reality.rememberaiprototype.ApplicationModule
import com.reality.rememberaiprototype.imagetotext.ImageTextRecognitionService
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [ApplicationModule::class]
)
interface ImageTextRecognitionComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): ImageTextRecognitionComponent
    }

    fun inject(service: ImageTextRecognitionService)
}