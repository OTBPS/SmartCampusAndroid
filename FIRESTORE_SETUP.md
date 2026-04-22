# Firestore Setup (Round 4)

This project now supports Firestore-backed data for:
- `pois`
- `favorites`

If Firebase is not configured, the app automatically falls back to local fake data.

## 1) Add Firebase config file
1. Open your Firebase project and register Android app package: `com.cnpen.smartcampus`.
2. Download `google-services.json`.
3. Place it at: `app/google-services.json`.
4. Sync Gradle and rebuild.

## 2) Enable Firestore
1. In Firebase Console, enable Cloud Firestore.
2. Start in test mode for development, then tighten security rules later.

## 3) Firestore schema used by this app

### Collection: `pois`
Document fields:
- `id` (string)
- `name` (string)
- `category` (string, example: `ACADEMIC`, `FOOD`, `DORM`, `SERVICE`, `SPORTS`)
- `description` (string)
- `latitude` (number)
- `longitude` (number)
- `imageUrl` (string, optional)
- `building` (string)
- `keywords` (array of strings)
- `popularity` (number)
- `updatedAt` (timestamp or epoch milliseconds)

### Collection: `favorites`
Document id: `poiId` (recommended)
Document fields:
- `poiId` (string)
- `updatedAt` (timestamp)

## 4) Fallback behavior
- If `pois` is empty, the app keeps running with local fallback POI data.
- Favorites are still read/written through the repository layer.

## 5) Debug seed behavior
- In debug builds, the app now performs a one-time seed check for `pois`.
- If `pois` is empty, it seeds initial Smart Campus POIs automatically.
- If `pois` already has any document, seeding is skipped and no data is overwritten.
