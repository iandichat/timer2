package com.example.timer2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
    private var currentElapsedSeconds: Long = 0
    private var screenStateReceiver: BroadcastReceiver? = null

    companion object {
        const val ACTION_SCREEN_ON = "com.example.timer2.SCREEN_ON"
        const val ACTION_SCREEN_OFF = "com.example.timer2.SCREEN_OFF"
        const val ACTION_START_WITHOUT_RESET = "com.example.timer2.START_WITHOUT_RESET"
        const val ACTION_TIMER_UPDATE = "com.example.timer2.TIMER_UPDATE"
        const val EXTRA_ELAPSED_SECONDS = "elapsed_seconds"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "timer2_channel"
    }

    override fun onCreate() {
        super.onCreate()
        prefs = AppPreferences(this)
        notificationHelper = NotificationHelper(this)
        createNotificationChannel()
        registerScreenStateReceiver()
    }

    private fun registerScreenStateReceiver() {
        screenStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (context == null || intent == null) return
                if (!prefs.isEnabled()) return

                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> {
                        // Screen turned on - start timer
                        prefs.setScreenOn(true)
                        startTimer()
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        // Screen turned off - stop timer
                        prefs.setScreenOn(false)
                        stopTimer()
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenStateReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenStateReceiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SCREEN_ON -> {
                startTimer()
            }
            ACTION_SCREEN_OFF -> {
                stopTimer()
            }
            ACTION_START_WITHOUT_RESET -> {
                // Start service without resetting timer if already running
                if (timerRunnable == null) {
                    startTimer()
                } else {
                    // Just ensure foreground notification is shown
                    startForeground(NOTIFICATION_ID, createForegroundNotification())
                }
            }
            else -> {
                // Initial start - start timer when service is started
                startTimer()
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
            .setSmallIcon(R.drawable.ic_paw_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun startTimer() {
        stopTimer() // Stop any existing timer
        
        startTime = System.currentTimeMillis()
        currentElapsedSeconds = 0L
        prefs.setElapsedTime(0L)
        
        startForeground(NOTIFICATION_ID, createForegroundNotification())
        
        timerRunnable = object : Runnable {
            override fun run() {
                val elapsedMillis = System.currentTimeMillis() - startTime
                val elapsedSeconds = elapsedMillis / 1000
                currentElapsedSeconds = elapsedSeconds
                
                // Write to SharedPreferences only every 10 seconds to reduce disk I/O
                if (elapsedSeconds % 10 == 0L) {
                    prefs.setElapsedTime(elapsedSeconds)
                }
                
                // Broadcast update with current elapsed time
                val updateIntent = Intent(ACTION_TIMER_UPDATE)
                updateIntent.putExtra(EXTRA_ELAPSED_SECONDS, elapsedSeconds)
                sendBroadcast(updateIntent)
                
                // Check if limit reached
                val limitMinutes = prefs.getTimeLimit()
                val limitSeconds = limitMinutes * 60L
                
                if (elapsedSeconds >= limitSeconds) {
                    // Send notification
                    notificationHelper.sendLimitNotification(limitMinutes)
                    
                    // Reset timer and continue
                    startTime = System.currentTimeMillis()
                    currentElapsedSeconds = 0L
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
        currentElapsedSeconds = 0L
        prefs.setElapsedTime(0L)
        val updateIntent = Intent(ACTION_TIMER_UPDATE)
        updateIntent.putExtra(EXTRA_ELAPSED_SECONDS, 0L)
        sendBroadcast(updateIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        unregisterScreenStateReceiver()
    }

    private fun unregisterScreenStateReceiver() {
        screenStateReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                // Receiver not registered
            }
            screenStateReceiver = null
        }
    }
}
