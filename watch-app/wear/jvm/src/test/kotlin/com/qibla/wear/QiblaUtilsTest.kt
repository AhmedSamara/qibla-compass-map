package com.qibla.wear

import kotlin.test.Test
import kotlin.test.assertEquals

class QiblaUtilsTest {
    @Test
    fun testCalculateQiblaKnownPoint() {
        // From Cairo (30.0444 N, 31.2357 E) the Qibla bearing is ~136.1°
        val bearing = QiblaUtils.calculateQibla(30.0444, 31.2357)
        assertEquals(136, Math.round(bearing))
    }

    @Test
    fun testCalculateDistanceKnownPoint() {
        val dist = QiblaUtils.calculateDistance(30.0444, 31.2357)
        // Cairo -> Makkah great-circle distance is ~1287 km
        if (dist < 1200 || dist > 1400) {
            throw AssertionError("Distance out of expected range: $dist")
        }
    }
}
