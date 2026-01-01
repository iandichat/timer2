package com.example.timer2

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "timer2_prefs"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_TIME_LIMIT = "time_limit"
        private const val KEY_ELAPSED_TIME = "elapsed_time"
        private const val KEY_SCREEN_ON = "screen_on"
        private const val DEFAULT_TIME_LIMIT = 30
    }

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun getTimeLimit(): Int = prefs.getInt(KEY_TIME_LIMIT, DEFAULT_TIME_LIMIT)

    fun setTimeLimit(limit: Int) {
        prefs.edit().putInt(KEY_TIME_LIMIT, limit).apply()
    }

    fun getElapsedTime(): Long = prefs.getLong(KEY_ELAPSED_TIME, 0L)

    fun setElapsedTime(time: Long) {
        prefs.edit().putLong(KEY_ELAPSED_TIME, time).apply()
    }

    fun isScreenOn(): Boolean = prefs.getBoolean(KEY_SCREEN_ON, false)

    fun setScreenOn(on: Boolean) {
        prefs.edit().putBoolean(KEY_SCREEN_ON, on).apply()
    }
}
