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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) vm.startLocationUpdates()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start location if already granted, otherwise prompt immediately
        if (hasLocationPermission()) {
            vm.startLocationUpdates()
        } else {
            requestLocationPermission()
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
        if (hasLocationPermission()) {
            vm.startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        // reduce sensor and location activity when not visible
        vm.stopLocationUpdates()
        vm.stopHeading()
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CompassDial(
                modifier = Modifier.size(110.dp),
                heading = state.heading,
                qibla = state.qiblaBearing,
                aligned = state.aligned
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (state.userLat == null) {
                Button(onClick = onRequestPermission) {
                    Text("Enable Location")
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Waiting for location…", fontSize = 11.sp, color = Color(0xFF8896AB))
            } else {
                Text(
                    text = if (state.aligned) "Facing Qibla" else state.qiblaBearing?.let { "${it.toInt()}°" } ?: "--°",
                    fontSize = 22.sp,
                    color = if (state.aligned) Color(0xFF3DD68C) else Color.White
                )
                Text(
                    text = state.distanceKm?.let { "${it.toInt()} km" } ?: "-- km",
                    fontSize = 12.sp,
                    color = Color(0xFF8896AB)
                )
                if (!state.aligned) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Turn until arrow points up", fontSize = 10.sp, color = Color(0xFF8896AB))
                }
            }
            if (state.calibrationNeeded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Move device in figure-8 to calibrate", fontSize = 11.sp, color = Color(0xFFE5484D))
            }
        }
    }
}

@Composable
fun CompassDial(modifier: Modifier = Modifier, heading: Float?, qibla: Double?, aligned: Boolean) {
    val rotation = if (heading != null && qibla != null) (qibla - heading).toFloat()
        else qibla?.toFloat() ?: 0f
    val arrowColor = if (aligned) Color(0xFF3DD68C) else Color(0xFFC9A84C)
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        val c = Offset(size.width / 2f, size.height / 2f)
        // dial ring
        drawCircle(color = Color(0xFF1E2636), radius = r - 1.dp.toPx(), style = Stroke(width = 2.dp.toPx()))
        // fixed marker at top — the direction the watch is pointing
        drawCircle(color = Color(0xFF8896AB), radius = 3.dp.toPx(), center = Offset(c.x, c.y - r + 7.dp.toPx()))
        // arrow pointing toward the Qibla
        rotate(degrees = rotation, pivot = c) {
            val tip = Offset(c.x, c.y - r * 0.72f)
            val tail = Offset(c.x, c.y + r * 0.45f)
            drawLine(color = arrowColor, start = tail, end = tip, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
            val head = r * 0.26f
            val arrowHead = Path().apply {
                moveTo(tip.x, tip.y - 2.dp.toPx())
                lineTo(tip.x - head * 0.62f, tip.y + head)
                lineTo(tip.x + head * 0.62f, tip.y + head)
                close()
            }
            drawPath(arrowHead, color = arrowColor)
        }
        drawCircle(color = arrowColor, radius = 4.dp.toPx(), center = c)
    }
}
