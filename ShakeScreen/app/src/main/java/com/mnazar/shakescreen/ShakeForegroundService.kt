package com.mnazar.shakescreen

import android.annotation.SuppressLint
import android.app.*
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class ShakeForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "shake_service_channel" // This is an internal constant, no need to extract
        const val NOTIFICATION_ID = 1
        const val ACTION_LOCK_SCREEN = "com.mnazar.shakescreen.LOCK_SCREEN"
    }

    private lateinit var shakeDetector: ShakeDetector
    private lateinit var wakeLock: PowerManager.WakeLock
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializeShakeDetection()
    }

    private fun initializeShakeDetection() {
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager

        // Restored legacy behavior: SCREEN_BRIGHT_WAKE_LOCK + ACQUIRE_CAUSES_WAKEUP
        @Suppress("DEPRECATION")
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            getString(R.string.wakelock_tag)
        )

        shakeDetector = ShakeDetector(sensorManager) {
            handleShakeDetected()
        }

        shakeDetector.start()
    }

    private fun handleShakeDetected() {
        try {
            if (!wakeLock.isHeld) {
                wakeLock.acquire(5000) // Keep screen on + CPU for 5 seconds
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_LOCK_SCREEN) {
            lockScreen()
        }

        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun createNotification(): Notification {
        // Create intent for clicking the notification itself
        val clickIntent = Intent(this, ShakeForegroundService::class.java).apply {
            action = ACTION_LOCK_SCREEN
        }
        val clickPendingIntent = PendingIntent.getService(
            this,
            0,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(clickPendingIntent) // Make the entire notification clickable
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setAutoCancel(false) // Don't dismiss when clicked
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(getString(R.string.notification_big_text)))
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun lockScreen() {
        try {
            val devicePolicyManager =
                getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val compName = ComponentName(this, MyDeviceAdminReceiver::class.java)

            if (devicePolicyManager.isAdminActive(compName)) {
                devicePolicyManager.lockNow()
                android.util.Log.d(getString(R.string.tag_shake_service), getString(R.string.log_screen_locked))
            } else {
                android.util.Log.w(getString(R.string.tag_shake_service), getString(R.string.log_admin_not_active))

                // Update notification to show error state
                updateNotificationWithError()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e(getString(R.string.tag_shake_service), getString(R.string.log_lock_screen_failed), e)
        }
    }

    private fun updateNotificationWithError() {
        val errorNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.error_notification_title))
            .setContentText(getString(R.string.error_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setAutoCancel(false)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, errorNotification)

        // Restore normal notification after 3 seconds
        handler.postDelayed({
            val normalNotification = createNotification()
            notificationManager.notify(NOTIFICATION_ID, normalNotification)
        }, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::shakeDetector.isInitialized) {
            shakeDetector.stop()
        }

        if (::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }

        handler.removeCallbacksAndMessages(null)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
