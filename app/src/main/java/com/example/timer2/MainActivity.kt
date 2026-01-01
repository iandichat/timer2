package com.example.timer2

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var enableSwitch: SwitchMaterial
    private lateinit var timeLimitInput: EditText
    private lateinit var saveButton: Button
    private lateinit var statusText: TextView
    private lateinit var prefs: AppPreferences

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = AppPreferences(this)

        enableSwitch = findViewById(R.id.enableSwitch)
        timeLimitInput = findViewById(R.id.timeLimitInput)
        saveButton = findViewById(R.id.saveButton)
        statusText = findViewById(R.id.statusText)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        // Load saved settings
        enableSwitch.isChecked = prefs.isEnabled()
        timeLimitInput.setText(prefs.getTimeLimit().toString())

        // Set up listeners
        enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.setEnabled(isChecked)
            if (isChecked) {
                startTimerService()
            } else {
                stopTimerService()
            }
            updateStatus()
        }

        saveButton.setOnClickListener {
            val limitText = timeLimitInput.text.toString()
            if (limitText.isNotEmpty()) {
                val limit = limitText.toIntOrNull()
                if (limit != null && limit > 0) {
                    prefs.setTimeLimit(limit)
                    Toast.makeText(this, "설정이 저장되었습니다", Toast.LENGTH_SHORT).show()
                    if (prefs.isEnabled()) {
                        // Restart service to apply new limit
                        stopTimerService()
                        startTimerService()
                    }
                } else {
                    Toast.makeText(this, "올바른 시간을 입력하세요", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Start service if enabled
        if (prefs.isEnabled()) {
            startTimerService()
        }

        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(TimerService.ACTION_TIMER_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(statusReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(statusReceiver, filter)
        }
        updateStatus()
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(statusReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
    }

    private fun startTimerService() {
        val intent = Intent(this, TimerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopTimerService() {
        val intent = Intent(this, TimerService::class.java)
        stopService(intent)
    }

    private fun updateStatus() {
        if (!prefs.isEnabled()) {
            statusText.text = getString(R.string.current_status, getString(R.string.status_disabled))
        } else {
            val elapsedSeconds = prefs.getElapsedTime()
            if (elapsedSeconds == 0L) {
                statusText.text = getString(R.string.current_status, getString(R.string.status_screen_off))
            } else {
                val minutes = (elapsedSeconds / 60).toInt()
                val seconds = (elapsedSeconds % 60).toInt()
                statusText.text = getString(R.string.current_status, getString(R.string.status_running, minutes, seconds))
            }
        }
    }
}
