package com.mnazar.shakescreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.app.admin.DevicePolicyManager
import android.content.ComponentName

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("shake_prefs", Context.MODE_PRIVATE)
            val shouldRestartService = prefs.getBoolean("service_running", false)

            if (shouldRestartService) {
                // Check if device admin is still active
                val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val adminComponentName = ComponentName(context, MyDeviceAdminReceiver::class.java)
                
                if (devicePolicyManager.isAdminActive(adminComponentName)) {
                    Log.d("BootReceiver", "Restarting shake service after boot...")
                    val serviceIntent = Intent(context, ShakeForegroundService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } else {
                    Log.w("BootReceiver", "Device admin permission is not active, can't start service")
                    // Reset service running state since we can't start without admin permission
                    prefs.edit().putBoolean("service_running", false).apply()
                }
            }
        }
    }
}
