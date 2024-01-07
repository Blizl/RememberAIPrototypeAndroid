package com.reality.rememberaiprototype


import android.app.Application
import com.reality.rememberaiprototype.home.data.ImageTextRecognitionService
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(application: RememberAiPrototypeApplication)

    fun inject(imageTextRecognitionService: ImageTextRecognitionService)
}
