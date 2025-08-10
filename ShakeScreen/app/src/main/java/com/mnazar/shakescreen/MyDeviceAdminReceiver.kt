package com.mnazar.shakescreen

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class MyDeviceAdminReceiver : DeviceAdminReceiver() {

    companion object {
        private const val TAG = "tag_device_admin"
    }

    override fun onEnabled(context: Context, intent: Intent) {
        Log.d(TAG, "Device Admin enabled")
        Toast.makeText(context, context.getString(R.string.admin_enabled_toast), Toast.LENGTH_LONG)
            .show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Log.d(TAG, "Device Admin disabled")
        Toast.makeText(
            context,
            context.getString(R.string.admin_disabled_toast),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        Log.d(TAG, "Device Admin disable requested")
        return context.getString(R.string.admin_disable_warning)
    }
}
