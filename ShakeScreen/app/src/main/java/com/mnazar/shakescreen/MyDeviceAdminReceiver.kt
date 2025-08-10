package com.mnazar.shakescreen

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class MyDeviceAdminReceiver : DeviceAdminReceiver() {

    companion object {
        private const val TAG = "DeviceAdminReceiver"
    }

    override fun onEnabled(context: Context, intent: Intent) {
        Log.d(TAG, "Device Admin enabled")
        Toast.makeText(context, "Device Admin enabled - Screen lock available", Toast.LENGTH_LONG)
            .show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Log.d(TAG, "Device Admin disabled")
        Toast.makeText(
            context,
            "Device Admin disabled - Screen lock unavailable",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        Log.d(TAG, "Device Admin disable requested")
        return "Disabling device admin will prevent the shake-to-wake app from locking the screen"
    }
}
