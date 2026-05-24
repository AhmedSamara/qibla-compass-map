Wear App Quick Start

This directory contains starter files to help you port the Qibla Compass to Wear OS (Pixel Watch).

What I added
- `QiblaUtils.kt` — Kotlin port of the Qibla math (bearing, distance, great circle, dest point).
- `MainActivity.kt` — a minimal Jetpack Compose screen stub showing bearing and distance.

Next steps to create a runnable Wear OS app

1. Open Android Studio and create a new Wear OS project (Empty Compose Activity) or add a new module.
2. In the new module, set package name to `com.qibla.wear` (or adjust the `package` lines in the files above).
3. Copy `QiblaUtils.kt` into `app/src/main/java/com/qibla/wear/`.
4. Copy `MainActivity.kt` into `app/src/main/java/com/qibla/wear/` and set it as the LAUNCHER activity.
5. Add Compose for Wear OS dependencies and Material3 in `build.gradle`.

Recommended dependencies (Gradle Kotlin DSL snippet):

```kotlin
dependencies {
    implementation("androidx.wear:wear-compose-material:1.2.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("com.google.android.horologist:horologist-sensors:0.6.0") // optional helpers
}
```

6. Implement sensor & location repositories using `SensorManager` and `FusedLocationProviderClient`.
7. Move logic into a `ViewModel` exposing Compose `State` and connect the UI to it.
8. Test on a Pixel Watch emulator or device.

If you want, I can scaffold the complete Gradle files and a more complete Compose UI next. Reply `scaffold gradle` and I'll add them.
