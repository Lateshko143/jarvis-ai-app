package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.core.accessibility.JarvisAccessibilityService
import com.example.ui.MainViewModel
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSurfaceBorder
import com.example.ui.theme.JarvisSurfaceElevated

@Composable
fun DiagnosticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Live Device Diagnostics
    val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus = context.registerReceiver(null, filter)
    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val batteryPct = if (level >= 0 && scale > 0) (level / scale.toFloat() * 100).toInt() else 100
    val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

    // Storage
    val statFs = StatFs(Environment.getDataDirectory().path)
    val availableBytes = statFs.availableBlocksLong * statFs.blockSizeLong
    val totalBytes = statFs.blockCountLong * statFs.blockSizeLong
    val availableGb = availableBytes / (1024 * 1024 * 1024.0)
    val totalGb = totalBytes / (1024 * 1024 * 1024.0)

    // Network
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val caps = cm?.getNetworkCapabilities(cm.activeNetwork)
    val isOnline = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

    // Permissions
    var audioPermGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        audioPermGranted = granted
    }

    var emergencyKillSwitch by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SYSTEM DIAGNOSTICS",
                    color = JarvisCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Hardware telemetry & security protocols",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(if (emergencyKillSwitch) JarvisRedAlert else JarvisEmerald, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emergency Automation Kill Switch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (emergencyKillSwitch) JarvisRedAlert.copy(alpha = 0.15f) else JarvisSurfaceElevated,
                    RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    if (emergencyKillSwitch) JarvisRedAlert else JarvisSurfaceBorder,
                    RoundedCornerShape(12.dp)
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EMERGENCY AUTOMATION PAUSE",
                        color = if (emergencyKillSwitch) JarvisRedAlert else Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Instantly aborts all background execution and phone automation.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = emergencyKillSwitch,
                    onCheckedChange = { emergencyKillSwitch = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JarvisRedAlert,
                        checkedTrackColor = JarvisRedAlert.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hardware Diagnostics Cards
        Text(
            text = "HARDWARE TELEMETRY",
            color = JarvisCyan,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Battery Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(JarvisSurfaceElevated, RoundedCornerShape(10.dp))
                    .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("BATTERY", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$batteryPct%",
                        color = if (batteryPct > 20) JarvisEmerald else JarvisRedAlert,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = if (isCharging) "Charging" else "Discharging",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            // Storage Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(JarvisSurfaceElevated, RoundedCornerShape(10.dp))
                    .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("STORAGE", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%.1f GB", availableGb),
                        color = JarvisCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = String.format("Free of %.1f GB", totalGb),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Device Info Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(JarvisSurfaceElevated, RoundedCornerShape(10.dp))
                .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Column {
                Text("PROCESSOR & PLATFORM", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) // ABI: ${Build.SUPPORTED_ABIS.firstOrNull()}",
                    color = JarvisCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
                Text(
                    text = "Uplink: ${if (isOnline) "ONLINE (High-Speed)" else "OFFLINE (Local Engine)"}",
                    color = if (isOnline) JarvisEmerald else JarvisAmber,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Permission Checklist
        Text(
            text = "AUTHORIZATION STATUS",
            color = JarvisCyan,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mic permission
        PermissionRow(
            title = "Microphone (RECORD_AUDIO)",
            subtitle = "Required for speech-to-text voice assistant interface",
            isGranted = audioPermGranted,
            onRequest = { permLauncher.launch(Manifest.permission.RECORD_AUDIO) }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Accessibility service
        val isAccessibilityActive = JarvisAccessibilityService.isRunning()
        PermissionRow(
            title = "Accessibility Automation Service",
            subtitle = "Optional for user-confirmed on-screen interaction",
            isGranted = isAccessibilityActive,
            onRequest = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Database Maintenance Controls
        Text(
            text = "VAULT MAINTENANCE",
            color = JarvisCyan,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.clearTelemetryLogs() },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Logs", tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear Logs", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = { viewModel.clearMemories() },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Vault", tint = JarvisRedAlert, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Purge Vault", color = JarvisRedAlert, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(JarvisSurfaceElevated, RoundedCornerShape(10.dp))
            .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = if (isGranted) "Granted" else "Pending",
                        tint = if (isGranted) JarvisEmerald else JarvisAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            if (!isGranted) {
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Grant",
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
