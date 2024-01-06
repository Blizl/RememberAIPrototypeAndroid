package com.reality.rememberaiprototype.home.data

import android.app.Service
import android.content.Intent
import android.os.IBinder
import timber.log.Timber

class ImageTextRecognitionService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Timber.e("We just created the image text recogintion service to parse images")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.e("We just started")
        return super.onStartCommand(intent, flags, startId)
    }
}