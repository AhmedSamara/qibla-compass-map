# Notes for future agents

Context that isn't obvious from the code or the human-facing READMEs. Read [README.md](README.md) and [watch-app/README.md](watch-app/README.md) first for the project overview; this file is the agent-specific cheatsheet.

## Quick map of the repo

- **Web PWA** — root: `index.html` (single-file app), `manifest.json`, `sw.js`. Deployed on GitHub Pages.
- **Wear OS native app** — `watch-app/wear/` (Gradle: `:app` Android + `:jvm` pure-Kotlin for unit-testable math).
- **Docs** — `watch-app/README.md`, `watch-app/design.md`, `watch-app/tasks.md`, `watch-app/wear/README.md`.
- **Makefile** — `watch-app/wear/Makefile` has all the dev commands (`make deploy`, `make connect IP=…`, `make logcat`, `make screenshot`).

There is **no native Android phone app**. If "Android app" comes up for the Play Store, the path is to wrap the PWA as a Trusted Web Activity (Bubblewrap / PWABuilder), not to build native.

## Watch dev loop (this is the productive workflow)

1. Watch must be on Wi-Fi developer mode (Settings → System → Developer options → Wireless debugging).
2. The pairing auth drops when the watch sleeps or reboots, so most "won't connect" sessions need a re-pair:
   ```bash
   adb pair 192.168.1.x:<pair-port> <6-digit code>   # from "Pair new device" on the watch
   adb connect 192.168.1.x:<connect-port>            # from the main Wireless debugging screen
   ```
   `make connect IP=…` runs the connect; pair has to be a one-off command. The watch's **connect port and pair port are different and both change every session**.
3. Sometimes `adb connect` fails the first time after a fresh `adb start-server`; a second try after a brief sleep usually works.
4. Once connected: `cd watch-app/wear && make deploy` builds, installs, and you can screenshot via `make screenshot` (writes `watch.png`).
5. `make logcat` tails app logs only (filtered by pid). Useful for crash diagnosis when alignment triggers something — the VIBRATE permission saga taught us crashes during alignment are common.

## Compass / arrow conventions (the most-bit area)

This conversation went around the loop several times before settling. The current code in `MainActivity.MapCompass` is the right answer:

- **Gold arrow** uses `rotate(qibla)` — stays at a fixed angle on the screen relative to the (unrotated) map content. Think "fixed on the map."
- **Blue needle** uses `rotate(qibla - heading)` — behaves like a magnetic compass needle but world-locked on Mecca instead of north. Straight up when you face Mecca; counter-rotates as your wrist turns.

Mistakes that have happened and shouldn't be repeated:
- Using `rotate(heading)` for a "needle pointing at X" — that points the line **at** the watch's heading, not at X. Wrong.
- Adding `+180` to the raw azimuth in `HeadingRepository` — Android's `getOrientation()` already returns a standard azimuth (0=N, 90=E, CW from N). No offset needed. We added 180 once, the user confirmed it made things worse, we reverted.
- Removing the blue needle or making both arrows point at Mecca — the user wants two visually distinct lines with different roles. Don't conflate them.

The map itself is drawn unrotated. North-up vs heading-up has been re-litigated; the current "unrotated map + the two-arrow scheme above" is the answer the user landed on.

## Build matrix (don't drift)

JDK 21 · Gradle 8.4 · AGP 8.2.0 · Kotlin **1.9.10** · Compose compiler **1.5.3**.

The Kotlin↔Compose-compiler pair is the touchy bit. If you bump Kotlin, you must also bump the Compose compiler to a matching version, and vice versa. The original project shipped with Kotlin 1.8.20 + Compose compiler 1.5.3 (mismatched) and it took a build cycle to spot.

`:jvm` exists specifically so the math tests run without the Android SDK (`./gradlew :jvm:test`). Don't merge `:jvm`'s `QiblaUtils` into `:app` — the duplication is intentional.

## Permissions and platform gotchas

- **`VIBRATE`** is required. The alignment haptic fires when the user faces Mecca; without the permission, that crashes the app with a SecurityException. Easy to miss; we hit it.
- **Location on Android 12+** requires **both** `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` in the manifest *and* in the runtime request. Requesting fine-only silently fails — no dialog appears, no error.
- **Permission requests on Wear** must come from `onCreate` or a button — there's no install-time auto-grant.
- **Launcher icon** is a placeholder adaptive icon at `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` referencing two vector drawables. minSdk=26 so v26 adaptive is enough — no legacy PNGs needed. Replace before any release.

## Map tiles

- We pull Carto `light_all` tiles (`https://{a|b|c|d}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png`), same source as the web app.
- Tiles are cached on disk under the app's `cacheDir/tiles/`, keyed by `{zoom}_{x}_{y}.png`. There's an LRU eviction at ~20 MB.
- **Cannot reuse Google Maps' offline data.** Android app sandboxing makes that impossible — don't try.
- For publishing, Carto's terms require attribution ("© OpenStreetMap contributors © CARTO"). The free basemap tier may need a Carto account for high-volume production use. This is an open question for whoever publishes.

## Web PWA install panel

- The install panel is **inline on the page** — `#installPanel` in `index.html`. It always shows actionable instructions regardless of whether Chrome fires `beforeinstallprompt`.
- Detection branches: iOS Safari (renders inline Share icon SVG + steps), iOS other (open in Safari), Android (Chrome `⋮ → Install app` tip), desktop fallback.
- Manifest icons **must** include a `purpose: "any"` 192px and 512px entry. `"any maskable"` alone trips Chrome's installability check on some devices (we hit this on a Cat S22).
- Bump `CACHE_NAME` in `sw.js` whenever you change the PWA so existing users actually pick up the change (the service worker otherwise keeps serving the old version).
- Some budget Androids (e.g. Cat S22) ship without a magnetometer; the web app already handles this gracefully (`noCompassAvailable` falls back to bearing-only). Don't chase ghost compass bugs on those devices.

## Collaboration conventions

- **The user handles all git work.** Don't commit, push, branch, or amend. Edit files and stop there.
- **Verify on the device when possible.** For UI changes, screenshot via `adb exec-out screencap -p` and look at the actual output rather than reasoning about pixels. The compass arc went wrong precisely because we trusted derivations over the user's empirical reports — when the user says "it's wrong," they're right; if your math says otherwise, your model is off.
- **Auto mode is sometimes on.** Bias toward acting rather than asking, but a single AskUserQuestion is correct for genuine forks (e.g., the heading-up vs north-up map decision, or PWA-wrap vs native phone app). Don't multi-ask.
- **Watch deploys are the cycle.** Edit → `make deploy` → screenshot → check. If the watch is disconnected, compile-only with `./gradlew :app:assembleDebug` is a fine partial verification.

## Open / known items

- **`HeadingRepository` fallback** — currently only TYPE_ROTATION_VECTOR; if the sensor is absent the flow closes with an exception instead of degrading to bearing-only. `tasks.md` tracks this.
- **No accel+magnetometer fallback heading source.** Same file as above.
- **Stale `jvm/bin/`** — an Eclipse-style output directory was once tracked by mistake. It's gitignored now, but if it re-appears in `git status`, untrack with `git rm -r --cached watch-app/wear/jvm/bin`.
- **Deprecated APIs** still in the code: `LocationRequest.create()` and `VIBRATOR_SERVICE`. They produce warnings, not errors. Safe to modernize when convenient.
- **Publishing** to Play Store is unimplemented — no release signing config, no Play App Signing setup, no privacy policy URL. See the publishing discussion in `tasks.md` Phase 6.

## When in doubt

1. Read [watch-app/wear/MainActivity.kt](watch-app/wear/app/src/main/java/com/qibla/wear/MainActivity.kt) and [QiblaViewModel.kt](watch-app/wear/app/src/main/java/com/qibla/wear/QiblaViewModel.kt) — most of the recent learning is encoded as comments there.
2. Check `git log -p` on the watch-app subtree to see how the design has evolved.
3. Ask before doing anything destructive. The user's time-to-undo a wrong change is much higher than the time to ask.
