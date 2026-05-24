package com.qibla.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Note: This file is a minimal Compose stub to drop into a Wear OS app module.
// It assumes you have Compose for Wear OS and Material3 set up in the Gradle project.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WatchCompassScreen()
            }
        }
    }
}

@Composable
fun WatchCompassScreen() {
    // Placeholder state: in a real app this would come from a ViewModel
    var lat by remember { mutableStateOf(37.4219999) }
    var lng by remember { mutableStateOf(-122.0840575) }
    var heading by remember { mutableStateOf(0.0) }

    val bearing = remember(lat, lng) { QiblaUtils.calculateQibla(lat, lng) }
    val distance = remember(lat, lng) { QiblaUtils.calculateDistance(lat, lng) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Qibla Bearing", fontSize = 14.sp, color = Color(0xFFC9A84C))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${bearing.toInt()}°", fontSize = 28.sp, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "${distance.toInt()} km", fontSize = 12.sp, color = Color(0xFF8896AB))
        }
    }
}
