package com.reality.rememberaiprototype.home.data

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters


class ScreenshotWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        // Start the foreground service to capture screenshots
        val serviceIntent = Intent(applicationContext, ScreenshotService::class.java)
        ContextCompat.startForegroundService(applicationContext, serviceIntent)
        return Result.success()
    }
}