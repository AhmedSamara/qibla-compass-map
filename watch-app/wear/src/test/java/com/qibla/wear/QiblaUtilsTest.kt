package com.qibla.wear

import kotlin.test.Test
import kotlin.test.assertEquals

class QiblaUtilsTest {
    @Test
    fun testCalculateQiblaKnownPoint() {
        // Example: From Cairo (30.0444 N, 31.2357 E) expected bearing ~ 136.5° (approx)
        val bearing = QiblaUtils.calculateQibla(30.0444, 31.2357)
        assertEquals(137, Math.round(bearing))
    }

    @Test
    fun testCalculateDistanceKnownPoint() {
        val dist = QiblaUtils.calculateDistance(30.0444, 31.2357)
        // Cairo -> Makkah distance approx 1500 km, allow rough rounding
        val rounded = Math.round(dist / 100.0) * 100
        // just assert it's within 100 km of 1500 for a sanity check
        if (rounded < 1400 || rounded > 1600) {
            throw AssertionError("Distance out of expected range: $dist")
        }
    }
}
