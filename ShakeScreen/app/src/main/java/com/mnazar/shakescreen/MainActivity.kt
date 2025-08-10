package com.mnazar.shakescreen


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import android.view.View
import java.util.Locale
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mnazar.shakescreen.ui.theme.ShakeScreenTheme
import androidx.core.content.edit
import androidx.core.text.layoutDirection

class MainActivity : ComponentActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponentName: ComponentName

    private var isAdminActive by mutableStateOf(false)
    private var serviceRunning by mutableStateOf(false)
    private var showAboutDialog by mutableStateOf(false)

    // Launcher for requesting Device Admin permission
    private val deviceAdminLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshAdminStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponentName = ComponentName(this, MyDeviceAdminReceiver::class.java)

        refreshAdminStatus()

        serviceRunning = getSharedPreferences(getString(R.string.pref_file_name), MODE_PRIVATE)
            .getBoolean(getString(R.string.pref_service_running), false)

        setContent {
            ShakeScreenTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    PermissionAndStatusScreen(
                        isAdminActive = isAdminActive,
                        serviceRunning = serviceRunning,
                        showAboutDialog = showAboutDialog,
                        onRequestDeviceAdmin = { requestDeviceAdmin() },
                        onStartService = { startShakeService() },
                        onStopService = { stopShakeService() },
                        onShowAbout = { showAboutDialog = true },
                        onDismissAbout = { showAboutDialog = false }
                    )
                }
            }
        }
    }

    private fun refreshAdminStatus() {
        isAdminActive = devicePolicyManager.isAdminActive(adminComponentName)
    }

    private fun requestDeviceAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponentName)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_explanation))
        }
        deviceAdminLauncher.launch(intent)
    }

    private fun startShakeService() {
        val intent = Intent(this, ShakeForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        serviceRunning = true
        getSharedPreferences(getString(R.string.pref_file_name), MODE_PRIVATE)
            .edit {
                putBoolean(getString(R.string.pref_service_running), true)
            }
    }

    private fun stopShakeService() {
        stopService(Intent(this, ShakeForegroundService::class.java))
        serviceRunning = false
        getSharedPreferences(getString(R.string.pref_file_name), MODE_PRIVATE)
            .edit {
                putBoolean(getString(R.string.pref_service_running), false)
            }
    }

}

@Composable
fun PermissionAndStatusScreen(
    isAdminActive: Boolean,
    serviceRunning: Boolean,
    showAboutDialog: Boolean,
    onRequestDeviceAdmin: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    onShowAbout: () -> Unit,
    onDismissAbout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isAdminActive) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.errorContainer
            ),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isAdminActive) Icons.Default.Security else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isAdminActive) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (isAdminActive) stringResource(R.string.device_admin_enabled) else stringResource(R.string.device_admin_not_granted),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // Request Permission Button
        if (!isAdminActive) {
            Button(
                onClick = onRequestDeviceAdmin,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Security, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.grant_device_admin))
            }
        }

        // Service Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (serviceRunning) Icons.Default.PlayArrow else Icons.Default.Stop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (serviceRunning) stringResource(R.string.service_running) else stringResource(R.string.service_stopped),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = if (serviceRunning) onStopService else onStartService,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (serviceRunning)
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        if (serviceRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (serviceRunning) stringResource(R.string.stop_service) else stringResource(R.string.start_service))
                }
            }
        }

        // About Button
        Button(
            onClick = onShowAbout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Icon(Icons.Default.Info, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.about))
        }

        // About Dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = onDismissAbout,
                title = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.about_title))
                    }
                },
                text = {
                    val configuration = android.content.res.Configuration()
                    configuration.setLocale(Locale.getDefault())
                    val isRtl = configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
                    Column(
                        horizontalAlignment = if (isRtl) Alignment.End else Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.developed_by),
                            textAlign = if (isRtl) TextAlign.Right else TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.contact_support),
                            textAlign = if (isRtl) TextAlign.Right else TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.support_email),
                            textAlign = if (isRtl) TextAlign.Right else TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = onDismissAbout) {
                        Text(stringResource(R.string.close))
                    }
                }
            )
        }
    }
}
