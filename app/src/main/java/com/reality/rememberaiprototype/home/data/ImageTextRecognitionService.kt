package com.reality.rememberaiprototype.home.data

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ImageTextRecognitionService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}