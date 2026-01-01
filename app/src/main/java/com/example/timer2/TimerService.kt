package com.example.timer2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class TimerService : Service() {
    private lateinit var prefs: AppPreferences
    private lateinit var notificationHelper: NotificationHelper
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var startTime: Long = 0

    companion object {
        const val ACTION_SCREEN_ON = "com.example.timer2.SCREEN_ON"
        const val ACTION_SCREEN_OFF = "com.example.timer2.SCREEN_OFF"
        const val ACTION_TIMER_UPDATE = "com.example.timer2.TIMER_UPDATE"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "timer2_channel"
    }

    override fun onCreate() {
        super.onCreate()
        prefs = AppPreferences(this)
        notificationHelper = NotificationHelper(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SCREEN_ON -> {
                startTimer()
            }
            ACTION_SCREEN_OFF -> {
                stopTimer()
            }
            else -> {
                // Initial start - check if screen is on
                if (prefs.isScreenOn()) {
                    startTimer()
                } else {
                    startForeground(NOTIFICATION_ID, createForegroundNotification())
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_description)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.foreground_notification_title))
            .setContentText(getString(R.string.foreground_notification_text))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun startTimer() {
        stopTimer() // Stop any existing timer
        
        startTime = System.currentTimeMillis()
        prefs.setElapsedTime(0L)
        
        startForeground(NOTIFICATION_ID, createForegroundNotification())
        
        timerRunnable = object : Runnable {
            override fun run() {
                val elapsedMillis = System.currentTimeMillis() - startTime
                val elapsedSeconds = elapsedMillis / 1000
                prefs.setElapsedTime(elapsedSeconds)
                
                // Broadcast update
                sendBroadcast(Intent(ACTION_TIMER_UPDATE))
                
                // Check if limit reached
                val limitMinutes = prefs.getTimeLimit()
                val limitSeconds = limitMinutes * 60L
                
                if (elapsedSeconds >= limitSeconds) {
                    // Send notification
                    notificationHelper.sendLimitNotification(limitMinutes)
                    
                    // Reset timer and continue
                    startTime = System.currentTimeMillis()
                    prefs.setElapsedTime(0L)
                }
                
                // Continue timer
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun stopTimer() {
        timerRunnable?.let {
            handler.removeCallbacks(it)
            timerRunnable = null
        }
        prefs.setElapsedTime(0L)
        sendBroadcast(Intent(ACTION_TIMER_UPDATE))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }
}
