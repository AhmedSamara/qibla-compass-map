package com.qibla.wear.repo

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Emits location updates as a Flow of android.location.Location. This repository depends on
 * the Play Services Location APIs and should be used in the watch module with the proper
 * permissions (ACCESS_FINE_LOCATION).
 */
class LocationRepository(private val context: Context) {
    private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun locationFlow(intervalMs: Long = 5000L): Flow<android.location.Location> = callbackFlow {
        val request = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = intervalMs
            fastestInterval = intervalMs / 2
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation
                if (loc != null) trySend(loc)
            }
        }

        client.requestLocationUpdates(request, callback, context.mainLooper)

        awaitClose { client.removeLocationUpdates(callback) }
    }
}
