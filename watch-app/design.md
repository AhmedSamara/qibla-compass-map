# Qibla Compass Wear OS Design

## Purpose
Port the existing web-based Qibla Compass app to Google Pixel Watch (Wear OS) as a native app optimized for watch sensors, battery, and small-screen usability.

## Goals
- Deliver accurate Qibla bearing on watch using on-device sensors.
- Keep interaction simple: glanceable, one-screen primary experience.
- Preserve existing Qibla math behavior from the web app.
- Support offline operation for core compass functionality.
- Optionally hand off detailed map view to phone.

## Non-Goals (MVP)
- Full-featured interactive map on watch.
- Large offline map tile caching on watch.
- Feature parity with all web/PWA install and service-worker behaviors.

## Why Native Wear OS Instead of PWA
- Better sensor access and reliability for heading data.
- Better lifecycle handling for wearable constraints.
- Better battery and performance control.
- Better UX consistency on circular screens and Always On Display.

## User Experience

### Primary Screen (Compass)
- Large central compass ring.
- Qibla arrow overlay.
- Bearing text (degrees).
- Distance to Makkah.
- Alignment indicator (visual + optional haptic when within threshold).

### States
- Waiting for location.
- Compass unavailable (bearing-only fallback).
- Permissions required.
- Sensor calibration hint.

### Interactions
- Tap to open quick actions (recenter, calibration tip, settings).
- Optional swipe to settings.

---

**Related (web app)**: The web version's feature list and implementation are authoritative for web-specific behaviors — see the main project `README.md` and `index.html` for the current web features and UX.

- Web README: [../README.md](../README.md)
- Web app entry: [../index.html](../index.html)

Aim: keep feature parity where reasonable. Platform-specific tradeoffs are noted throughout this document.

## System Architecture

### Modules
1. core
- Pure Kotlin utilities.
- Qibla bearing calculation.
- Distance calculation.
- Heading smoothing utilities.

2. wearApp
- UI (Compose for Wear OS).
- Sensor access.
- Location access.
- State management (ViewModel + Kotlin Flow).

3. phoneCompanion (optional, phase 2)
- Detailed map view.
- Settings sync.
- Data Layer message exchange.

### Data Flow
1. Sensor repository emits heading samples.
2. Location repository emits current coordinates.
3. ViewModel combines streams to produce UI state.
4. UI renders compass/arrow/labels and alignment status.

## Sensor and Location Strategy

### Heading
- Primary: TYPE_ROTATION_VECTOR.
- Fallback: TYPE_ACCELEROMETER + TYPE_MAGNETIC_FIELD.
- Apply smoothing window for jitter reduction.
- Detect low confidence and show calibration hint.

### Location
- Use FusedLocationProviderClient.
- Request high accuracy only when app is in active foreground.
- Increase interval when app is ambient/inactive.

### Permissions
- ACCESS_FINE_LOCATION runtime permission.
- Graceful retry flow when denied.

## UI/Rendering Details
- Use Compose for Wear OS components.
- Circular-layout-aware spacing and typography.
- Keep one dominant focal element (arrow/compass).
- Minimal text; prioritize readability at a glance.
- Optional Always On mode with reduced updates.

## Haptics
- Trigger short vibration when alignment enters threshold (for example <= 5 degrees).
- Debounce so haptics do not fire repeatedly while held in threshold.

## Battery and Performance
- Reduce sensor update rate when not visible.
- Pause non-essential work in ambient mode.
- Avoid continuous high-accuracy GPS in background.
- Use lightweight drawing and avoid heavy map rendering on watch.

## Map Strategy

### MVP
- No full map on watch.
- Show compass and bearing-only guidance.

### Phase 2
- Phone handoff button: "Open map on phone".
- Optional tiny map preview on watch if performance permits.

## Error Handling and Fallbacks
- No compass sensor: show static bearing + direction text.
- No location: show permission/help prompt.
- Sensor noise high: show calibration guidance.
- GPS timeout: show retry action and last known location fallback.

## Testing Plan

### Functional
- Bearing correctness at known coordinates.
- Alignment threshold behavior.
- Permission flows (grant/deny/revoke).

### Device
- Pixel Watch and Pixel Watch 2.
- Indoor and outdoor heading stability.
- Motion scenarios (walking, standing, wrist rotation).

### Battery
- 15-minute active usage drain sample.
- Ambient mode behavior check.

## Migration Plan
1. Initialize new Wear OS project (Kotlin + Compose for Wear OS).
2. Port Qibla math into core module with unit tests.
3. Implement heading + location repositories.
4. Build compass screen with live state.
5. Add fallback and error states.
6. Add haptic alignment feedback.
7. Optional: add phone handoff and companion map.

## Milestones

### Milestone 1 (MVP)
- Live heading and location.
- Qibla arrow + bearing + distance.
- Permission flow and core error states.

### Milestone 2
- Haptic alignment feedback.
- Calibration UX polish.
- Improved smoothing and stability.

### Milestone 3
- Phone companion handoff for map.
- Settings sync and quality-of-life improvements.

## Risks and Mitigations
- Sensor drift indoors: provide calibration hint and fallback messaging.
- GPS lock delays: use last-known location and explicit retry controls.
- Small-screen clutter: keep strict one-screen hierarchy.
- Battery drain: adapt sensor and location rates by app state.

## Success Metrics
- Time to first usable direction after launch.
- Bearing stability (low visible jitter).
- User ability to align within threshold quickly.
- Acceptable battery impact during common usage.

## Notes on Reuse from Web App
- Reuse math formulas and smoothing concepts.
- Do not reuse web-specific install, service worker, or map tile cache logic.
- Rebuild platform integrations natively for Wear OS.
