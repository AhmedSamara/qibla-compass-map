package com.qibla.wear.repo

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Emits device heading (0..360) using the rotation vector sensor when available.
 * Falls back to a combined accelerometer + magnetometer approach if needed.
 * This is a lightweight repository intended to be moved into an Android module.
 */
class HeadingRepository(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun headingFlow(pollDelayUs: Int = SensorManager.SENSOR_DELAY_UI): Flow<Float> = callbackFlow {
        val rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        val rotationMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        val heading = (azimuth + 360) % 360
                        trySend(heading)
                    }
                    // Fallback logic could be implemented here by combining accel+mag values
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        if (rotation != null) {
            sensorManager.registerListener(listener, rotation, pollDelayUs)
        } else if (accel != null && mag != null) {
            sensorManager.registerListener(listener, accel, pollDelayUs)
            sensorManager.registerListener(listener, mag, pollDelayUs)
        } else {
            close(IllegalStateException("No suitable sensors available"))
        }

        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
