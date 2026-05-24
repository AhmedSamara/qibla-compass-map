# 🕋 Qibla Compass

### **[▶ Open Qibla Compass](https://ahmedsamara.github.io/qibla-compass-map/)**

A beautiful, installable Progressive Web App that finds the direction of the Kaaba in Makkah from anywhere using your phone's compass and GPS.

![HTML](https://img.shields.io/badge/vanilla-HTML%2FCSS%2FJS-orange) ![PWA](https://img.shields.io/badge/PWA-installable-blue) ![License](https://img.shields.io/badge/license-MIT-blue)

## Features

- **Live compass** — reads your device's magnetometer and rotates in real time, with the Kaaba icon always pointing toward Qibla
- **GPS-based Qibla calculation** — uses the great circle formula for accurate bearing from any location on Earth
- **Interactive map** — shows your position with a great circle path drawn to Makkah so you can verify the direction against real-world landmarks
- **Heading line** — a live green line on the map shows where your phone is currently pointing for easy calibration
- **Alignment detection** — visual feedback when you're facing within ±5° of the Qibla
- **Installable PWA** — add to your home screen on iOS or Android for a native app experience
- **Works offline** — cached via service worker after first load
- **No tracking, no ads** — just a compass

## Install on Your Phone

### Android

1. Open the app URL in Chrome
2. Tap the **"Install"** banner that appears, or tap ⋮ → **"Install app"**
3. The app appears on your home screen

### iPhone / iPad

1. Open the app URL in **Safari** (must be Safari, not Chrome)
2. Tap the **Share** button (⎋)
3. Scroll down and tap **"Add to Home Screen"**
4. Tap **"Add"**

Once installed it launches fullscreen without the browser bar and works offline.

## How It Works

The Qibla bearing is calculated using the spherical trigonometry initial bearing formula:

```
θ = atan2(sin(Δλ), cos(φ₁)·tan(φ₂) − sin(φ₁)·cos(Δλ))
```

where φ₁ is your latitude, φ₂ is the Kaaba's latitude (21.4225°N), and Δλ is the difference in longitude from the Kaaba (39.8262°E). The map line follows the true great circle path, not a straight line on the Mercator projection.

## Deploy Your Own

### GitHub Pages (free)

1. Fork this repo
2. Go to **Settings → Pages → Deploy from branch → main**
3. Your app will be live at `https://your-username.github.io/qibla-compass/`

### Any Static Host

Upload the entire folder to Netlify, Vercel, Cloudflare Pages, or any HTTPS-enabled host. No build step required.

> **Important:** HTTPS is required. Compass and geolocation APIs are blocked on plain HTTP.

## Project Structure

```
qibla-compass/
├── index.html          # The entire app (single file)
├── manifest.json       # PWA manifest
├── sw.js               # Service worker for offline support
├── icons/
│   ├── icon-72x72.png
│   ├── icon-96x96.png
│   ├── icon-128x128.png
│   ├── icon-144x144.png
│   ├── icon-152x152.png
│   ├── icon-192x192.png
│   ├── icon-384x384.png
│   └── icon-512x512.png
└── README.md
```

## Watch App (Wear OS)

This repository also contains a plan and task list for a Pixel Watch (Wear OS) native port of this web app. The watch app docs live in the `watch-app/` subdirectory:

- Design & plan: [watch-app/design.md](watch-app/design.md)
- Implementation tasks: [watch-app/tasks.md](watch-app/tasks.md)

We aim to keep feature parity between the web PWA and the watch app where practical — see the watch design doc for details on which features are expected to match and which are intentionally different due to platform constraints.

## Browser Support

| Platform | Compass | GPS | Install |
|----------|---------|-----|---------|
| iOS Safari | ✅ | ✅ | Add to Home Screen |
| Android Chrome | ✅ | ✅ | Install prompt |
| Desktop Chrome | ❌ | ✅ | Install prompt |
| Other desktop | ❌ | ✅ | — |

## Troubleshooting

### GPS / Location

**"Location permission denied"**
- **iOS:** Settings → Privacy & Security → Location Services → make sure it's **On**. Then scroll to **Safari Websites** and set to **"While Using"** or **"Ask."**
- **Android:** Settings → Location → make sure it's on. Then Settings → Apps → Chrome → Permissions → Location → Allow.
- If you previously denied permission for this site, your browser won't ask again. Reset it:
  - **iOS Safari:** Settings → Safari → Settings for Websites → Location → find the site → set to "Ask"
  - **Chrome:** Tap the lock/tune icon in the address bar → Permissions → Location → Allow

**"GPS not responding" / stuck on "Waiting for GPS"**
- Make sure you're on **HTTPS** (GitHub Pages provides this automatically)
- Try stepping outside or near a window — GPS struggles indoors
- On iOS, try closing and reopening Safari
- Some VPNs or content blockers can interfere with geolocation — try disabling them

**"GPS timed out"**
- Your device couldn't get a fix in 15 seconds. This is common indoors. Move to a location with better sky visibility and reload.

### Compass

**"Compass permission denied" (iOS)**
- Settings → Safari → **Settings for Websites → Motion & Orientation Access** → set to **"Ask"** or **"Allow"**
- Then reload the page — you should get the permission popup again

**Compass not responding / inaccurate**
- Wave your phone in a **figure-8 pattern** several times to calibrate the magnetometer
- Move away from metal objects, magnets, or electronics that cause interference
- Remove your phone from its case if it has a magnetic mount or clasp
- The green line on the map can help you verify — point your phone toward a known landmark and see if the line matches

**"No compass — showing bearing only"**
- Your device doesn't have a magnetometer, or the browser can't access it. The Qibla bearing and map still work — use the gold dashed line on the map to identify the direction visually.

### Home Screen / PWA

**"Page not found" when opening from home screen**
- Delete the shortcut and re-add it from the browser
- Make sure the site loads correctly in the browser first before adding to home screen

**App doesn't update after a new version**
- The service worker caches the app for offline use. Force a refresh: close and reopen the app, or in Safari go to the site and pull down to refresh.

### Map

**Map tiles are blank**
- You're offline and haven't cached tiles yet. Use the **"Save Map for Offline"** button while connected to download tiles around your area.
- The compass and bearing work fine without the map.

## Tech Stack

- Vanilla HTML / CSS / JS — no build step, single file
- [Leaflet.js](https://leafletjs.com/) — interactive map
- [Carto](https://carto.com/basemaps/) — map tiles
- [Google Fonts](https://fonts.google.com/) — Amiri + DM Sans
- Service Worker for offline caching

## License

MIT
