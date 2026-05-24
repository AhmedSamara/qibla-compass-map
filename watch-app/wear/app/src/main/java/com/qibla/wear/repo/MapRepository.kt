package com.qibla.wear.repo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.math.PI
import kotlin.math.asinh
import kotlin.math.floor
import kotlin.math.tan

/** A composited map image centered on the user, with the user's pixel position inside it. */
data class MapData(
    val bitmap: Bitmap,
    val userX: Float,
    val userY: Float,
    val lat: Double,
    val lng: Double,
    val zoom: Int
)

/**
 * Fetches Carto "light_all" raster tiles (same source as the web app) for the user's
 * location, composites a 3x3 grid into one bitmap, and reports where the user sits in it.
 */
class MapRepository {
    private val tile = 256
    private val subs = listOf("a", "b", "c", "d")

    suspend fun fetchCenteredMap(lat: Double, lng: Double, zoom: Int = 20): MapData? =
        withContext(Dispatchers.IO) {
            try {
                val n = 1 shl zoom
                val latRad = Math.toRadians(lat)
                val fx = (lng + 180.0) / 360.0 * n
                val fy = (1.0 - asinh(tan(latRad)) / PI) / 2.0 * n
                val xtile = floor(fx).toInt()
                val ytile = floor(fy).toInt()
                val pxInTile = ((fx - xtile) * tile).toFloat()
                val pyInTile = ((fy - ytile) * tile).toFloat()

                val composite = Bitmap.createBitmap(tile * 3, tile * 3, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(composite)
                var any = false
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val tx = ((xtile + dx) % n + n) % n
                        val ty = ytile + dy
                        if (ty < 0 || ty >= n) continue
                        val sub = subs[((tx + ty) % subs.size + subs.size) % subs.size]
                        val url = "https://$sub.basemaps.cartocdn.com/light_all/$zoom/$tx/$ty.png"
                        val bmp = try {
                            URL(url).openStream().use { BitmapFactory.decodeStream(it) }
                        } catch (e: Exception) {
                            null
                        }
                        if (bmp != null) {
                            canvas.drawBitmap(bmp, ((dx + 1) * tile).toFloat(), ((dy + 1) * tile).toFloat(), null)
                            bmp.recycle()
                            any = true
                        }
                    }
                }
                if (!any) {
                    composite.recycle()
                    return@withContext null
                }
                MapData(
                    bitmap = composite,
                    userX = tile + pxInTile,
                    userY = tile + pyInTile,
                    lat = lat,
                    lng = lng,
                    zoom = zoom
                )
            } catch (e: Exception) {
                null
            }
        }
}
