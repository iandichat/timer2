package com.example.timer2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class ScreenStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val prefs = AppPreferences(context)
        if (!prefs.isEnabled()) return

        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> {
                // Screen turned on - start timer
                prefs.setScreenOn(true)
                prefs.setElapsedTime(0L)
                
                val serviceIntent = Intent(context, TimerService::class.java)
                serviceIntent.action = TimerService.ACTION_SCREEN_ON
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
            Intent.ACTION_SCREEN_OFF -> {
                // Screen turned off - stop timer
                prefs.setScreenOn(false)
                prefs.setElapsedTime(0L)
                
                val serviceIntent = Intent(context, TimerService::class.java)
                serviceIntent.action = TimerService.ACTION_SCREEN_OFF
                context.startService(serviceIntent)
            }
        }
    }
}
