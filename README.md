# 🕋 Qibla Compass

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

## Browser Support

| Platform | Compass | GPS | Install |
|----------|---------|-----|---------|
| iOS Safari | ✅ | ✅ | Add to Home Screen |
| Android Chrome | ✅ | ✅ | Install prompt |
| Desktop Chrome | ❌ | ✅ | Install prompt |
| Other desktop | ❌ | ✅ | — |

### Troubleshooting

- **"Permission denied" on iOS** — Settings → Safari → Settings for Websites → Motion & Orientation Access → set to "Ask" or "Allow"
- **Compass not responding** — wave your phone in a figure-8 to calibrate the magnetometer
- **No GPS** — make sure location services are enabled for your browser

## Tech Stack

- Vanilla HTML / CSS / JS — no build step, single file
- [Leaflet.js](https://leafletjs.com/) — interactive map
- [Carto](https://carto.com/basemaps/) — map tiles
- [Google Fonts](https://fonts.google.com/) — Amiri + DM Sans
- Service Worker for offline caching

## License

MIT
