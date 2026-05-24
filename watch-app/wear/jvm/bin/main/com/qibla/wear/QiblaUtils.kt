package com.qibla.wear

import kotlin.math.*

object QiblaUtils {
    private const val KAABA_LAT = 21.4225
    private const val KAABA_LNG = 39.8262
    private const val R = 6371.0 // km

    private fun toRad(d: Double) = d * Math.PI / 180.0
    private fun toDeg(r: Double) = r * 180.0 / Math.PI

    fun calculateQibla(lat: Double, lng: Double): Double {
        val phi1 = toRad(lat)
        val phi2 = toRad(KAABA_LAT)
        val deltaLambda = toRad(KAABA_LNG - lng)
        val y = sin(deltaLambda)
        val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLambda)
        return (toDeg(atan2(y, x)) + 360.0) % 360.0
    }

    fun calculateDistance(lat: Double, lng: Double): Double {
        val deltaPhi = toRad(KAABA_LAT - lat)
        val deltaLambda = toRad(KAABA_LNG - lng)
        val a = sin(deltaPhi / 2).pow(2.0) + cos(toRad(lat)) * cos(toRad(KAABA_LAT)) * sin(deltaLambda / 2).pow(2.0)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
