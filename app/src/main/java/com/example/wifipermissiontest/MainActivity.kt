@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.wifipermissiontest

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

// Define the permissions needed based on Android version
private val PERMISSIONS_REQUIRED = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.NEARBY_WIFI_DEVICES
} else {
    Manifest.permission.ACCESS_FINE_LOCATION
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WifiScannerScreen()
        }
    }
}

@Composable
fun WifiScannerScreen() {
    val context = LocalContext.current
    val wifiManager = remember {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    // State to hold the list of Wi-Fi networks
    var wifiList by remember { mutableStateOf<List<String>>(emptyList()) }
    // State to track if we have the necessary permission
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, PERMISSIONS_REQUIRED) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                hasPermission = true
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                hasPermission = false
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                wifiList = listOf("Permission denied. Cannot scan for Wi-Fi.")
            }
        }
    )

    // Effect to launch the permission request if not already granted
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(PERMISSIONS_REQUIRED)
        }
    }

    // This effect will register a broadcast receiver and start scans
    // when the component is active and permission is granted.
    DisposableEffect(hasPermission) {
        if (!hasPermission) {
            // Do nothing if we don't have permission
            return@DisposableEffect onDispose {}
        }

        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    val results = wifiManager.scanResults
                    wifiList = results.map { result ->
                        "SSID: ${result.SSID}, RSSI: ${result.level} dBm"
                    }.ifEmpty { listOf("No Wi-Fi networks found.") }
                } else {
                    wifiList = listOf("Scan failed. Ensure Location is enabled.")
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)

        // Initial scan
        wifiManager.startScan()

        // Cleanup: unregister the receiver when the composable is disposed
        onDispose {
            context.unregisterReceiver(wifiScanReceiver)
        }
    }


    Scaffold(
        topBar = { TopAppBar(title = { Text("Wi-Fi Scanner") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (wifiList.isEmpty() && hasPermission) {
                item {
                    Text(
                        text = "Scanning for networks...",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            items(wifiList) { item ->
                Text(
                    text = item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}