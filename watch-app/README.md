# Qibla Compass — Watch App (Wear OS)

A native Wear OS (Pixel Watch) port of the [Qibla Compass web app](../README.md). It reuses the web app's Qibla math and tile source, rebuilt natively for the watch's sensors, battery, and small round screen.

## What it does

- **Live compass arrow** that points toward the Kaaba using the watch's rotation-vector sensor.
- **GPS Qibla bearing + distance** to Makkah, using the same great-circle math as the web app (unit-tested).
- **Alignment feedback** — the arrow turns green and the watch buzzes when you're facing within ±5° of the Qibla.
- **Heading-up map** of your current location (Carto tiles, same source as the web app) with the arrow drawn on top.
- **Crown zoom** — turn the rotating crown to zoom the map in/out.
- **Graceful fallback** to a plain dial compass when offline or before a GPS fix.

## Docs

- **Build, run & test on a watch:** [wear/README.md](wear/README.md) (or just `cd wear && make deploy`)
- **Design & architecture:** [design.md](design.md)
- **Implementation status / task list:** [tasks.md](tasks.md)

The aim is feature parity with the web PWA where practical; platform-specific tradeoffs (no offline tile caching, phone-handoff ideas, etc.) are noted in the design doc.
