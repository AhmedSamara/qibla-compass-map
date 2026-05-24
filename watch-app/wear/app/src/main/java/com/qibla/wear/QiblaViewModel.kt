package com.qibla.wear

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qibla.wear.repo.HeadingRepository
import com.qibla.wear.repo.LocationRepository
import com.qibla.wear.repo.MapData
import com.qibla.wear.repo.MapRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

data class QiblaUiState(
    val userLat: Double? = null,
    val userLng: Double? = null,
    val qiblaBearing: Double? = null,
    val distanceKm: Double? = null,
    val heading: Float? = null,
    val aligned: Boolean = false,
    val calibrationNeeded: Boolean = false,
    val status: String = "",
    val map: MapData? = null,
    val zoom: Int = DEFAULT_ZOOM
)

private const val MIN_ZOOM = 13
private const val MAX_ZOOM = 20
private const val DEFAULT_ZOOM = 17

class QiblaViewModel(application: Application) : AndroidViewModel(application) {
    private val headingRepo = HeadingRepository(application.applicationContext)
    private val locationRepo = LocationRepository(application.applicationContext)
    private val mapRepo = MapRepository()

    private val _uiState = MutableStateFlow(QiblaUiState(status = "Starting…"))
    val uiState: StateFlow<QiblaUiState> = _uiState

    private var headingJob: Job? = null
    private var locationJob: Job? = null
    private var mapJob: Job? = null
    private var currentZoom = DEFAULT_ZOOM

    private val headingBuffer = ArrayDeque<Float>()
    private val maxHeadingBuf = 5

    init {
        startHeading()
    }

    private fun smoothHeading(raw: Float): Float {
        headingBuffer.addLast(raw)
        if (headingBuffer.size > maxHeadingBuf) headingBuffer.removeFirst()
        // simple circular average (approx)
        var sx = 0.0
        var cx = 0.0
        for (h in headingBuffer) {
            val r = Math.toRadians(h.toDouble())
            sx += Math.sin(r)
            cx += Math.cos(r)
        }
        val avg = Math.toDegrees(kotlin.math.atan2(sx / headingBuffer.size, cx / headingBuffer.size))
        return (((avg % 360) + 360) % 360).toFloat()
    }

    fun startHeading() {
        if (headingJob != null) return
        headingJob = viewModelScope.launch {
            headingRepo.headingFlow().collectLatest { h ->
                val s = smoothHeading(h)
                val prev = _uiState.value
                var aligned = false
                // simple jitter check: if heading buffer range large, suggest calibration
                val calib = if (headingBuffer.size >= maxHeadingBuf) {
                    val min = headingBuffer.minOrNull() ?: s
                    val max = headingBuffer.maxOrNull() ?: s
                    val range = kotlin.math.abs(max - min)
                    range > 30f
                } else false
                if (prev.qiblaBearing != null) {
                    var diff = ((prev.qiblaBearing - s) % 360 + 360) % 360
                    if (diff > 180) diff = 360 - diff
                    aligned = diff < 5
                }
                _uiState.value = prev.copy(heading = s, aligned = aligned, calibrationNeeded = calib, status = "Compass active")
            }
        }
    }

    fun stopHeading() {
        headingJob?.cancel()
        headingJob = null
        headingBuffer.clear()
    }

    fun startLocationUpdates() {
        if (locationJob != null) return
        locationJob = viewModelScope.launch {
            locationRepo.locationFlow().collectLatest { loc ->
                val lat = loc.latitude
                val lng = loc.longitude
                val qibla = QiblaUtils.calculateQibla(lat, lng)
                val dist = QiblaUtils.calculateDistance(lat, lng)
                val prev = _uiState.value
                // recompute alignment with latest heading if present
                var aligned = prev.aligned
                if (prev.heading != null) {
                    var diff = ((qibla - prev.heading!!) % 360 + 360) % 360
                    if (diff > 180) diff = 360 - diff
                    aligned = diff < 5
                }
                _uiState.value = prev.copy(userLat = lat, userLng = lng, qiblaBearing = qibla, distanceKm = dist, aligned = aligned, status = "Location active")
                maybeFetchMap(lat, lng)
            }
        }
    }

    private fun maybeFetchMap(lat: Double, lng: Double) {
        if (mapJob?.isActive == true) return
        val cur = _uiState.value.map
        // (Re)fetch only on first fix or after moving ~50 m, to avoid spamming tile requests.
        val needs = cur == null || cur.zoom != currentZoom ||
            abs(cur.lat - lat) > 0.0005 || abs(cur.lng - lng) > 0.0005
        if (!needs) return
        fetchMap(lat, lng, force = false)
    }

    /** Crown rotation: positive delta zooms in, negative zooms out. */
    fun changeZoom(delta: Int) {
        val nz = (currentZoom + delta).coerceIn(MIN_ZOOM, MAX_ZOOM)
        if (nz == currentZoom) return
        currentZoom = nz
        _uiState.value = _uiState.value.copy(zoom = nz)
        val lat = _uiState.value.userLat ?: return
        val lng = _uiState.value.userLng ?: return
        fetchMap(lat, lng, force = true)
    }

    private fun fetchMap(lat: Double, lng: Double, force: Boolean) {
        mapJob?.cancel()
        val z = currentZoom
        mapJob = viewModelScope.launch {
            if (force) delay(150) // debounce rapid crown turns
            val data = mapRepo.fetchCenteredMap(lat, lng, z)
            if (data != null) {
                _uiState.value = _uiState.value.copy(map = data)
            }
        }
    }

    fun stopLocationUpdates() {
        locationJob?.cancel()
        locationJob = null
    }

    override fun onCleared() {
        super.onCleared()
        headingJob?.cancel()
        locationJob?.cancel()
        mapJob?.cancel()
    }
}
