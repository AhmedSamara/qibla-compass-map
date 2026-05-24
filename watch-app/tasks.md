# Wear OS Implementation Tasks

Execution plan for the Pixel Watch port, ordered for a low-risk, testable workflow: core math and tests first, then UI, then sensors/location, then integration and device testing. See [design.md](design.md) for rationale and [wear/README.md](wear/README.md) for build/run/test steps.

Status key: `[x]` done · `[ ]` not started · `[ ]` + _(partial)_ note for in-progress.

## Phase 1 — Core and Scaffold
- [x] Create the Wear OS Gradle project (`:app` Android module + `:jvm` pure-Kotlin module).
- [x] Port Qibla bearing/distance math to a core Kotlin package (`QiblaUtils`).
- [x] Add unit tests for known coordinates (Cairo bearing/distance) — `./gradlew :jvm:test` passes.
- [x] Make the module importable in Android Studio (settings/Gradle wrapper, repositories).

## Phase 2 — UI and ViewModel
- [x] Compose for Wear OS screen: compass dial, Qibla arrow, bearing, distance, status.
- [x] `ViewModel` combining heading + location flows into UI state.
- [x] Heading smoothing (circular average) + low-confidence calibration hint. _(partial — uses a simple range-based heuristic; could be refined with sensor accuracy events)_

## Phase 3 — Sensors and Location
- [x] Heading repository using `TYPE_ROTATION_VECTOR`.
- [ ] Fallback heading source (accelerometer + magnetometer) when rotation vector is absent.
- [x] Location repository using `FusedLocationProviderClient` (high accuracy, foreground interval).
- [ ] Reduced update interval for ambient/inactive state (interval is parameterized but not wired to lifecycle).
- [ ] GPS timeout handling + last-known-location fallback.

## Phase 4 — Integration
- [x] Wire repositories to the `ViewModel` and render live data.
- [x] Runtime fine-location permission request flow.
- [ ] Permission denied/blocked retry UX. _(partial — an "Enable Location" button re-requests, but there is no clear messaging for permanent denial)_
- [x] Lifecycle-aware start/stop of sensors and location on resume/pause.
- [x] Alignment haptic with debounce (±5°, throttled).

## Phase 5 — Testing and Tuning
- [ ] On-device testing on Pixel Watch (deploy steps in [wear/README.md](wear/README.md)).
- [ ] Accuracy validation indoors/outdoors and across motion scenarios.
- [ ] Battery tuning and ambient-mode behavior.

## Phase 6 — Release Prep
- [x] Add launcher icon — placeholder adaptive icon (`res/mipmap-anydpi-v26/ic_launcher.xml` + vector foreground/background). Replace with final artwork before release.
- [ ] No-compass fallback UI (rotation-vector absence currently surfaces as an error rather than a bearing-only view).
- [ ] Signing, versioning, internal testing release.
- [ ] Optional: phone companion, settings sync, map handoff.

## MVP Definition of Done
- [ ] App launches and runs on a Pixel Watch (debug APK builds; needs on-device run to confirm).
- [ ] Location permission flow is complete and user-friendly. _(partial — basic flow works; denied UX is minimal)_
- [x] Qibla arrow updates from live heading.
- [x] Bearing and distance are displayed (math unit-tested).
- [ ] No-location and no-compass fallback states are usable. _(partial — no-location prompt exists; no-compass fallback not yet implemented)_
- [ ] Battery impact is acceptable for normal usage sessions (needs on-device measurement).

## Current State

- **Build & tests:** `./gradlew :jvm:test` passes and `./gradlew :app:assembleDebug` produces an installable debug APK (`app/build/outputs/apk/debug/app-debug.apk`), under JDK 21 / Gradle 8.4 / Kotlin 1.9.10 / AGP 8.2.0 / Compose compiler 1.5.3. The Qibla math is validated against Cairo (bearing ≈ 136°, distance ≈ 1287 km).
- **Implemented:** Gradle scaffold (`:app` + `:jvm`), Compose compass UI, placeholder launcher icon, `ViewModel`, heading repository (rotation vector), location repository (fused, high accuracy), runtime permission flow, lifecycle start/stop, alignment haptics, and heading smoothing with a calibration heuristic.
- **Not yet implemented:** accelerometer+magnetometer heading fallback, no-compass fallback UI, ambient/reduced update rates, GPS timeout + last-known fallback, on-device testing/battery tuning, and release signing. Several deprecated `LocationRequest`/`VIBRATOR_SERVICE` APIs produce build warnings.
- **On-device check still needed:** the APK builds, but live sensor/GPS/haptic/UI behavior is unverified until it runs on a real Pixel Watch. See [wear/README.md](wear/README.md) for deploy and test steps.
