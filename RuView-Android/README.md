# RuView Android

Native Android app for [RuView](https://github.com/ruvnet/RuView) — WiFi DensePose human perception system.

## Features

- **Live Screen** — Real-time 17-keypoint COCO skeleton visualization rendered on Canvas, with FPS/RSSI/sensor HUD overlay
- **Vitals Screen** — Animated gauge arcs for breathing rate (6–30 BPM) and heart rate (40–120 BPM) with 60-sample sparkline history
- **Zones Screen** — SVG-style floor plan with color-coded zone occupancy and person count
- **MAT Screen** — Mass Casualty Triage dashboard with START protocol classification (Immediate/Delayed/Minimal/Expectant)
- **Settings Screen** — Server URL configuration, WebSocket reconnection, display toggles

## Architecture

```
app/
├── data/
│   ├── api/          # Retrofit REST endpoints (/health, /vital-signs, /zones, /mat)
│   ├── models/       # Kotlin data classes (SensingFrame, Person, Keypoint, VitalSigns, Zone, MatResponse)
│   ├── repository/   # Single source of truth combining REST + WebSocket
│   └── websocket/    # OkHttp WebSocket client with exponential backoff + simulation fallback
├── di/               # Hilt dependency injection modules
├── ui/
│   ├── components/   # Reusable: ConnectionBanner, MetricCard, GaugeArc, SkeletonCanvas, SparklineChart
│   ├── navigation/   # Bottom nav with 5 screens
│   ├── screens/      # Live, Vitals, Zones, MAT, Settings
│   └── theme/        # Dark cyberpunk theme (cyan/green/red palette, monospace font)
└── viewmodel/        # Single shared ViewModel with StateFlow for all screens
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Networking | OkHttp 4 + Retrofit 2 |
| Serialization | kotlinx.serialization |
| Async | Coroutines + StateFlow |
| Architecture | MVVM |

## Backend Connection

The app connects to a running RuView server:

| Endpoint | Purpose |
|----------|---------|
| `GET /health` | Server status check |
| `GET /api/v1/vital-signs` | Breathing + heart rate |
| `GET /api/v1/zones` | Zone occupancy |
| `GET /api/v1/mat` | Triage data |
| `WS /ws/sensing` | Real-time 20 Hz pose frames |

**Fallback**: If the WebSocket fails after 10 retries (exponential backoff), the app automatically switches to built-in simulation mode generating realistic animated skeleton data.

## Build

```bash
# Open in Android Studio, or:
cd RuView-Android
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

**Requirements**: Android API 26+ (Android 8.0 Oreo)

## Configuration

In the **Settings** tab, enter your RuView server URL:
```
http://<server-ip>:3000
```

The default is `http://192.168.1.100:3000`.
