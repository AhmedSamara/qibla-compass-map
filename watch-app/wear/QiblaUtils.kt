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

    // Destination point given start lat/lon, bearing (deg) and distance in km
    fun destPoint(lat: Double, lng: Double, bearingDeg: Double, km: Double): Pair<Double, Double> {
        val phi1 = toRad(lat)
        val lambda1 = toRad(lng)
        val brng = toRad(bearingDeg)
        val d = km / R
        val phi2 = asin(sin(phi1) * cos(d) + cos(phi1) * sin(d) * cos(brng))
        val lambda2 = lambda1 + atan2(sin(brng) * sin(d) * cos(phi1), cos(d) - sin(phi1) * sin(phi2))
        return Pair(toDeg(phi2), toDeg(lambda2))
    }

    // Great circle path: returns list of lat,lng points (coarse resolution)
    fun greatCirclePath(lat1: Double, lng1: Double, lat2: Double, lng2: Double, steps: Int = 50): List<Pair<Double, Double>> {
        val φ1 = toRad(lat1)
        val λ1 = toRad(lng1)
        val φ2 = toRad(lat2)
        val λ2 = toRad(lng2)
        val d = 2 * asin(sqrt(sin((φ2 - φ1) / 2).pow(2.0) + cos(φ1) * cos(φ2) * sin((λ2 - λ1) / 2).pow(2.0)))
        if (d < 1e-12) return listOf(Pair(lat1, lng1), Pair(lat2, lng2))
        val pts = mutableListOf<Pair<Double, Double>>()
        for (i in 0..steps) {
            val t = i.toDouble() / steps.toDouble()
            val A = sin((1 - t) * d) / sin(d)
            val B = sin(t * d) / sin(d)
            val x = A * cos(φ1) * cos(λ1) + B * cos(φ2) * cos(λ2)
            val y = A * cos(φ1) * sin(λ1) + B * cos(φ2) * sin(λ2)
            val z = A * sin(φ1) + B * sin(φ2)
            val lat = toDeg(atan2(z, sqrt(x * x + y * y)))
            val lng = toDeg(atan2(y, x))
            pts.add(Pair(lat, lng))
        }
        return pts
    }
}
