package ru.tarandro.magnifika.monitor

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object AppSettings {
    private const val PREFS_NAME = "monitor_prefs"
    const val DEFAULT_ZONE = "gpio_zone"
    const val CLICK_X = "click_x"
    const val CLICK_Y = "click_y"

    fun getPrefs(context: Context): SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveConfig(context: Context, zone: Int, x: Float, y: Float) {
        getPrefs(context).edit {
            putInt(DEFAULT_ZONE, zone)
            putFloat(CLICK_X, x)
            putFloat(CLICK_Y, y)
        }
    }
}
