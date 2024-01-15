package com.reality.rememberaiprototype


import android.app.Application
import com.reality.rememberaiprototype.imagetotext.ImageTextRecognitionService
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ViewModelModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(imageTextRecognitionService: ImageTextRecognitionService)
    fun inject(mainActivity: MainActivity)
//    fun inject(repository: ImageToTextRepository)
}
