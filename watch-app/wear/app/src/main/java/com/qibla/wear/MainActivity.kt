package com.qibla.wear

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val vm: QiblaViewModel by viewModels()
    private var lastVibrateAt = 0L

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.startLocationUpdates()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If permission already granted, start location updates
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            vm.startLocationUpdates()
        }

        setContent {
            MaterialTheme {
                val state by vm.uiState.collectAsState(initial = vm.uiState.value)
                WatchCompassScreen(state = state, onRequestPermission = { requestLocationPermission() })
                // Haptics for alignment
                LaunchedEffect(state.aligned) {
                    if (state.aligned) {
                        maybeVibrateAligned()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.startHeading()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            vm.startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        // reduce sensor and location activity when not visible
        vm.stopLocationUpdates()
        vm.stopHeading()
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun maybeVibrateAligned() {
        val now = System.currentTimeMillis()
        if (now - lastVibrateAt < 3000) return // debounce 3s
        lastVibrateAt = now
        val vib = getSystemService(VIBRATOR_SERVICE) as? Vibrator
        vib?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

@Composable
fun WatchCompassScreen(state: QiblaUiState, onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CompassDial(modifier = Modifier.size(140.dp), heading = state.heading, qibla = state.qiblaBearing)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = state.qiblaBearing?.let { "${it.toInt()}°" } ?: "--°", fontSize = 28.sp, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = state.distanceKm?.let { "${it.toInt()} km" } ?: "-- km", fontSize = 12.sp, color = Color(0xFF8896AB))
            Spacer(modifier = Modifier.height(8.dp))
            if (state.userLat == null) {
                Button(onClick = onRequestPermission) {
                    Text("Enable Location")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (state.calibrationNeeded) {
                Text(text = "Move device in figure-8 to calibrate", fontSize = 11.sp, color = Color(0xFFE5484D))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = state.status, fontSize = 11.sp, color = Color(0xFF8896AB))
        }
    }
}

@Composable
fun CompassDial(modifier: Modifier = Modifier, heading: Float?, qibla: Double?) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Outer ring
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            drawCircle(color = androidx.compose.ui.graphics.Color(0xFF8B6914), radius = radius, style = androidx.compose.ui.graphics.drawscope.Fill)
            drawCircle(color = androidx.compose.ui.graphics.Color(0xFF0A0E17), radius = radius - 6.dp.toPx(), style = androidx.compose.ui.graphics.drawscope.Fill)
        }

        // Qibla arrow (rotated)
        val rotation = if (heading != null && qibla != null) {
            (qibla - heading).toFloat()
        } else if (qibla != null) {
            qibla.toFloat()
        } else 0f

        Box(modifier = Modifier
            .size(60.dp)
            .rotate(rotation)) {
            Text(text = "🕋", fontSize = 28.sp)
        }
    }
}
