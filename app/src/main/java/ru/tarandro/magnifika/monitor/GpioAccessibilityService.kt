package ru.tarandro.magnifika.monitor

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

class GpioAccessibilityService : AccessibilityService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == MonitorService.ACTION_CLICK_UNLOCK) {
            val prefs = AppSettings.getPrefs(this)
            val x = prefs.getFloat(AppSettings.CLICK_X, 180f)
            val y = prefs.getFloat(AppSettings.CLICK_Y, 910f)
            tapAt(x, y)
        }
        return START_STICKY
    }

    private fun tapAt(x: Float, y: Float) {
        val path = Path()
        path.moveTo(x, y)
        val stroke = GestureDescription.StrokeDescription(path, 0, 150)
        val builder = GestureDescription.Builder()
        builder.addStroke(stroke)
        dispatchGesture(builder.build(), null, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
