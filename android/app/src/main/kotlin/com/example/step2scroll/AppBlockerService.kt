package com.example.step2scroll

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView

class AppBlockerService : Service() {

    private val blockedPackages = mutableSetOf<String>()
    private var blockUntil: Long = 0L
    private var handler: Handler? = null
    private var windowManager: WindowManager? = null
    private var overlayView: FrameLayout? = null

    companion object {
        const val CHANNEL_ID = "app_blocker_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("App Blocker Active")
            .setContentText("Blocking selected apps")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_BLOCKING" -> {
                val packages = intent.getStringArrayListExtra("packages") ?: arrayListOf()
                val durationMinutes = intent.getIntExtra("duration", 60)
                blockedPackages.clear()
                blockedPackages.addAll(packages)
                blockUntil = System.currentTimeMillis() + durationMinutes * 60 * 1000L
                startPolling()
            }
            "STOP_BLOCKING" -> {
                stopPolling()
                removeOverlay()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startPolling() {
        handler = Handler(Looper.getMainLooper())
        handler?.post(object : Runnable {
            override fun run() {
                if (System.currentTimeMillis() > blockUntil) {
                    removeOverlay()
                    stopSelf()
                    return
                }
                val foreground = getForegroundPackage()
                if (foreground != null && foreground in blockedPackages) {
                    showOverlay()
                } else {
                    removeOverlay()
                }
                handler?.postDelayed(this, 1000)
            }
        })
    }

    private fun stopPolling() {
        handler?.removeCallbacksAndMessages(null)
        handler = null
    }

    private fun getForegroundPackage(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(now - 5000, now)
        val event = UsageEvents.Event()
        var lastPackage: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastPackage = event.packageName
            }
        }
        return lastPackage
    }

    private fun showOverlay() {
        if (overlayView != null) return
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Build overlay UI programmatically (no XML layout needed)
        overlayView = FrameLayout(this).apply {
            setBackgroundColor(0xFF1a1a2e.toInt())
            val text = TextView(this@AppBlockerService).apply {
                text = "This app is blocked\n\nFocus time remaining"
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 24f
                gravity = Gravity.CENTER
            }
            addView(text, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            ))
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        windowManager?.addView(overlayView, params)
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "App Blocker",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        stopPolling()
        removeOverlay()
        super.onDestroy()
    }
}