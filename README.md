# 🕋 Qibla Compass

A beautiful, mobile-first web app that calculates and displays the direction of the Kaaba in Makkah from your current location using your device's GPS and compass sensors.

![HTML](https://img.shields.io/badge/HTML-CSS--JS-orange) ![License](https://img.shields.io/badge/license-MIT-blue)

## Features

- **Live compass** — reads your device's magnetometer and rotates in real time, with the Kaaba icon always pointing toward Qibla
- **GPS-based Qibla calculation** — uses the great circle (spherical trigonometry) formula for accurate bearing from any location on Earth
- **Interactive map** — shows your position with a great circle path drawn to Makkah so you can verify the Qibla direction against real-world landmarks
- **Heading line** — a live green line on the map shows where your phone is currently pointing, making calibration intuitive
- **Alignment detection** — visual and text feedback when you're facing within ±5° of the Qibla
- **Distance readout** — Haversine distance to the Kaaba in kilometers
- **Dark, gold-accented UI** — designed to feel reverent and easy to read outdoors

## How It Works

The app uses two browser APIs:

- **Geolocation API** — gets your latitude/longitude via GPS
- **DeviceOrientation API** — reads your phone's compass heading

The Qibla bearing is calculated using the standard great circle initial bearing formula:

```
θ = atan2(sin(Δλ), cos(φ₁)·tan(φ₂) − sin(φ₁)·cos(Δλ))
```

where φ₁ is your latitude, φ₂ is the Kaaba's latitude (21.4225°N), and Δλ is the difference in longitude from the Kaaba (39.8262°E).

## Getting Started

### Option 1: GitHub Pages (recommended)

1. Fork or clone this repo
2. Go to **Settings → Pages → Deploy from branch → main**
3. Visit `https://your-username.github.io/qibla-compass/`

### Option 2: Any static host

Upload `index.html` to any HTTPS-enabled static host (Netlify, Vercel, Cloudflare Pages, etc.)

### Option 3: Local development

```bash
# Any local server works — HTTPS is needed for sensors on mobile
npx serve .
```

> **Important:** Compass and geolocation APIs require HTTPS. They will not work over plain HTTP (except `localhost`).

## Browser Support

| Platform | Compass | GPS | Notes |
|----------|---------|-----|-------|
| iOS Safari | ✅ | ✅ | Prompts for Motion & Orientation permission |
| Android Chrome | ✅ | ✅ | Sensors work automatically over HTTPS |
| Desktop browsers | ❌ | ✅ | No compass hardware — shows map and bearing only |

### Troubleshooting

- **"Permission denied" on iOS** — Go to Settings → Safari → Settings for Websites → Motion & Orientation Access → set to "Ask" or "Allow," then reload
- **Compass not responding** — Wave your phone in a figure-8 pattern to calibrate the magnetometer
- **No GPS fix** — Make sure location services are enabled for your browser in system settings

## Tech Stack

- Vanilla HTML / CSS / JS — no build step, single file
- [Leaflet.js](https://leafletjs.com/) — interactive map
- [Carto](https://carto.com/basemaps/) — dark-styled map tiles
- [Google Fonts](https://fonts.google.com/) — Amiri (Arabic serif) + DM Sans

## License

MIT
