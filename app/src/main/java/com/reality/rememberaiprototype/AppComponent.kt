package com.reality.rememberaiprototype


import android.app.Application
import com.reality.rememberaiprototype.home.data.ImageTextRecognitionService
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(modules = [ApplicationModule::class])
interface AppComponent {

    // This method binds the Application instance to the AppComponent
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        // Method that links the Builder interface to the AppComponent
        fun build(): AppComponent
    }

    fun inject(application: RememberAiPrototypeApplication)

    fun inject(imageTextRecognitionService: ImageTextRecognitionService)
}
