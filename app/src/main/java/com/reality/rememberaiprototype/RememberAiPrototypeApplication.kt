package com.reality.rememberaiprototype

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class RememberAiPrototypeApplication: Application() {
    lateinit var component: AppComponent
    override fun onCreate() {
        super.onCreate()
        initializeDagger()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initializeDagger() {
        component = DaggerAppComponent.builder()
            .application(this)
            .build()
    }

    fun component(): AppComponent {
        return component
    }
}