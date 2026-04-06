# SWAAS

SWAAS is a full-stack Android app for tracking and visualizing water safety data. Contributors can submit field measurements, the app scores each source with a safety engine, and the results are shown on an OpenStreetMap-based map.

## What It Does

- Maps water bodies and their safety status.
- Scores samples using pH, turbidity, TDS, and contaminants.
- Supports role-based access for users and contributors.
- Works with cached data for offline-friendly browsing.
- Uses OSMdroid, so there is no paid map API dependency.

## Project Structure

- `android/` - Android client written in Java.
- `backend/` - Node.js/Express API with Firebase Admin integration.

## Tech Stack

- Android: Java, MVVM, ViewBinding, Room, Retrofit, OkHttp, OSMdroid
- Backend: Node.js, Express, Firebase Firestore, Firebase Admin SDK

## Prerequisites

- Android Studio with Android SDK installed
- Node.js 18 or newer
- A Firebase project with Authentication and Firestore enabled

## Firebase Setup

1. Create a Firebase project in the [Firebase Console](https://console.firebase.google.com).
2. Enable Email/Password Authentication.
3. Create a Firestore database.
4. Download `google-services.json` into `android/app/`.
5. Provide backend credentials as either:
   - `backend/serviceAccountKey.json`, or
   - `FIREBASE_SERVICE_ACCOUNT_JSON` in the environment.

## Run The Backend

From the repository root:

```bash
cd backend
npm install
npm run dev
```

The API runs on `http://localhost:3000` by default.

## Run The Android App

1. Open the `android/` folder in Android Studio.
2. Let Gradle sync finish.
3. Run on an emulator or a connected Android phone.

To build a debug APK from the command line:

```bash
cd android
gradlew.bat assembleDebug
```

The APK is generated at `android/app/build/outputs/apk/debug/app-debug.apk`.

## API Endpoints

| Method | Endpoint                  | Access        | Purpose                     |
| ------ | ------------------------- | ------------- | --------------------------- |
| POST   | `/api/auth/register`      | Open          | Create a user account       |
| POST   | `/api/auth/login`         | Open          | Verify a Firebase ID token  |
| GET    | `/api/auth/profile`       | Authenticated | Fetch the current profile   |
| GET    | `/api/waterbodies`        | Authenticated | List all water bodies       |
| GET    | `/api/waterbodies/nearby` | Authenticated | Find nearby water bodies    |
| GET    | `/api/waterbodies/search` | Authenticated | Search water bodies by name |
| POST   | `/api/waterbodies`        | Contributor   | Add a new water body        |
| PUT    | `/api/waterbodies/:id`    | Contributor   | Update a water body         |
| DELETE | `/api/waterbodies/:id`    | Contributor   | Remove a water body         |

## Safety Scoring

The app uses a deterministic scoring model based on these checks:

- pH: ideal range 6.5 to 8.5
- Turbidity: lower values are safer, with 5 NTU as the upper target
- TDS: lower values are safer, with 500 mg/L as the main threshold
- Contaminants: flags harmful substances such as lead, arsenic, mercury, and bacteria

Scores are classified as:

- 80 and above: Safe
- 50 to 79: Moderate
- Below 50: Unsafe

## Notes

- `android/local.properties` is machine-specific and should not be committed.
- If Firebase credentials are missing, the backend still starts, but Firestore/Auth operations will fail until credentials are provided.
