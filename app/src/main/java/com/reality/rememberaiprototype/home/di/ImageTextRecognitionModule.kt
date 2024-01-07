package com.reality.rememberaiprototype.home.di

import android.app.Application
import com.reality.rememberaiprototype.AppComponent
import com.reality.rememberaiprototype.ApplicationModule
import com.reality.rememberaiprototype.home.data.ImageTextRecognitionService
import com.reality.rememberaiprototype.home.data.MlKitTextRecognition
import com.reality.rememberaiprototype.home.domain.TextRecognitionProcessor
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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

//@InstallIn(SingletonComponent::class)
//@Module
//abstract class ImageTextRecognitionModule {
//
//    companion object {
//        @Provides
//        fun provideTextRecognition(application: Application): TextRecognitionProcessor {
//            return MlKitTextRecognition(application.contentResolver)
//        }
//    }
//}