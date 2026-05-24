# Qibla Compass — Wear OS module

A native Wear OS (Pixel Watch) port of the Qibla Compass web app. See [../design.md](../design.md) for the design and [../tasks.md](../tasks.md) for current status.

## Modules

- `:app` — the Wear OS app: Compose UI, sensor + location repositories, and the `ViewModel`.
- `:jvm` — a pure-Kotlin copy of the Qibla math (`QiblaUtils`) with unit tests, so the math runs without the Android SDK.

## Open in Android Studio

1. Open Android Studio.
2. Choose **Open** and select the `watch-app/wear` folder in this repo.
3. Let Android Studio sync and install any missing Gradle wrapper / Android Gradle Plugin / SDK components.

Notes:
- Android Studio may prompt to update the Gradle plugin or Kotlin version — that's fine.
- The module uses Compose for Wear OS and Play Services Location; ensure you have network access during the first sync.

## Run the unit tests (no device needed)

From this folder:

```bash
./gradlew :jvm:test
```

This validates the Qibla bearing/distance math without the Android SDK.

## Testing on a Pixel Watch

The Pixel Watch has no USB data port, so you deploy **wirelessly** over Wi-Fi (or over Bluetooth via the paired phone). The watch and your computer must be on the **same Wi-Fi network**.

### Step 1 — Enable developer mode on the watch

1. On the watch: **Settings → System → About → Versions**.
2. Tap **Build number** seven times until it says "You are now a developer."
3. Go back to **Settings → Developer options**.
4. Turn on **ADB debugging**.
5. Turn on **Wireless debugging** (older Wear OS calls this "Debug over Wi-Fi").

### Step 2 — Pair the watch with your computer

**Easiest (Android Studio):**
1. In Android Studio open **Device Manager → Pair Devices Using Wi-Fi**.
2. On the watch: **Developer options → Wireless debugging → Pair new device** — it shows a 6-digit pairing code and an `IP:port`.
3. Enter the pairing code in Android Studio when prompted.

**Or from the command line:**
```bash
# Watch: Wireless debugging → Pair new device shows <ip>:<pair-port> and a code
adb pair <ip>:<pair-port>          # enter the 6-digit code when asked
# Watch: the Wireless debugging main screen shows the connect <ip>:<port>
adb connect <ip>:<port>
adb devices                        # confirm the watch is listed
```

### Step 3 — Build and install the app

**From Android Studio:** select the watch as the deployment target in the toolbar dropdown, then click **Run ▶ (app)**. It builds, installs, and launches automatically.

**Or from the command line:**
```bash
./gradlew :app:installDebug        # builds the debug APK and installs to the connected watch
```
The APK is also written to `app/build/outputs/apk/debug/app-debug.apk` if you prefer `adb install -r <apk>`.

### Step 4 — Exercise the app on-wrist

1. On first launch, tap **Enable Location** and grant the permission (**While using the app** / **Allow**).
2. Wait for a GPS fix — the bearing (°) and distance (km) to Makkah should populate. GPS is faster outdoors or near a window.
3. Slowly rotate your wrist; the 🕋 arrow should track toward the Qibla as the heading changes.
4. If the heading is jittery or the "Move device in figure-8 to calibrate" hint appears, wave your arm in a figure-8 a few times to calibrate the magnetometer; keep away from metal/magnets.
5. When you face within ±5° of the Qibla, the watch should give a short alignment **haptic** (debounced to once every few seconds).
6. Sanity-check the bearing against the web app ([../../index.html](../../index.html)) from the same location — they use the same math and should agree.

## Alternative — Wear OS emulator

If you don't want to use the physical watch, create a **Wear OS Large Round** virtual device in Android Studio's Device Manager and Run on it. Note that compass/GPS are simulated: use the emulator's **Extended controls → Location** to set coordinates, and the rotation/sensor controls to feed heading data. The emulator is good for UI work but not for real magnetometer behavior.

## Troubleshooting

- **Watch not showing up after `adb connect`:** confirm both devices are on the same Wi-Fi, re-open Wireless debugging on the watch (the port changes each session), and re-run `adb connect`.
- **`adb` keeps disconnecting:** the watch drops the connection in deep sleep; keep the screen on (tilt-to-wake or touch) while deploying.
- **No location:** make sure location permission was granted on the watch and that the watch's own Location toggle is on (Settings → Location).
- **No compass movement:** some Wear OS emulators lack a magnetometer; test heading on the physical watch.
- **Resource/`@mipmap/ic_launcher` error when building `:app`:** the launcher icon assets are not committed yet — add a `res/mipmap-*/ic_launcher` set (Android Studio: **New → Image Asset**) before building.
