package ru.tarandro.magnifika.monitor

import android.content.Context
import android.content.Intent

fun Context.startMonitorService() {
    val intent = Intent(this, MonitorService::class.java)
    this.startForegroundService(intent)
}
