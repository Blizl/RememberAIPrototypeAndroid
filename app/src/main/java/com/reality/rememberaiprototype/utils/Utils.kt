package com.reality.rememberaiprototype.utils

import android.app.ActivityManager
import android.content.Context

fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningServices = manager.getRunningServices(Integer.MAX_VALUE)

    for (serviceInfo in runningServices) {
        if (serviceClass.name == serviceInfo.service.className) {
            return true
        }
    }
    return false
}