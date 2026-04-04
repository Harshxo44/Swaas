# 💧 SWAAS — Safe Water Assessment and Awareness System

A full-stack Android application that helps users identify the safety of nearby water bodies using real-time data collected by authorized field testers, powered by an AI-based safety scoring engine.

---

## 🏗️ Project Structure

```
Swaas/
├── android/                  ← Android Studio Project (Java, MVVM)
│   └── app/src/main/
│       ├── java/com/swaas/app/
│       │   ├── model/        ← User, WaterBody, SafetyResult
│       │   ├── db/           ← Room DB (offline cache)
│       │   ├── network/      ← Retrofit API service
│       │   ├── repository/   ← Firestore + Room + Retrofit data layer
│       │   ├── viewmodel/    ← AuthViewModel, MapViewModel, ContributorViewModel
│       │   ├── ui/
│       │   │   ├── activities/  ← Splash, Login, Register, Main
│       │   │   ├── fragments/   ← Map, Search, AddWaterBody, BottomSheet
│       │   │   └── adapters/    ← WaterBodyAdapter (animated cards)
│       │   └── utils/
│       │       ├── WaterSafetyAnalyzer.java   ← AI scoring engine (WHO standards)
│       │       ├── PrecautionHelper.java
│       │       ├── MarkerColorHelper.java
│       │       └── SwaasFirebaseMessagingService.java
│       └── res/              ← Layouts, drawables, colors, themes
└── backend/                  ← Node.js Express API
    ├── server.js
    ├── config/firebase.js
    ├── middleware/authMiddleware.js
    ├── routes/auth.js
    ├── routes/waterbodies.js
    └── controllers/waterBodyController.js
```

---

## ⚡ Quick Start

### Step 1 — Firebase Setup (Required)

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project named **SWAAS**
3. Enable: **Authentication** (Email/Password), **Firestore**, **Storage**, **Cloud Messaging**
4. **Android app**: Download `google-services.json` → replace `android/app/google-services.json`
5. **Backend**: Download **Service Account Key** → save as `backend/serviceAccountKey.json`

### Step 2 — Map Provider (OpenStreetMap — FREE ✅)

This project uses **OSMdroid** backed by OpenStreetMap tiles. **No API key needed.**
Tiles are loaded automatically over the internet (or cached locally for offline use).

---

### 🤖 Android App

1. Open **Android Studio** → `File > Open` → select `Swaas/android/`
2. **Sync Gradle** (Android Studio will prompt automatically)
3. Replace `app/google-services.json` with your real Firebase config
4. Run on emulator or device (`Shift+F10`)

> **Prerequisites**: JDK 17+, Android Studio Hedgehog or later, Android SDK 34

---

### 🖥️ Node.js Backend

```bash
cd Swaas/backend

# Install dependencies
npm install

# Configure environment
cp .env.example .env
# Edit .env with your Firebase Storage bucket

# Place your Firebase service account key
# Download from: Firebase Console → Project Settings → Service Accounts
# Save as: backend/serviceAccountKey.json

# Start development server
npm run dev

# Start production server
npm start
```

Backend runs on: `http://localhost:3000`
Health check: `http://localhost:3000/health`

---

## 📡 API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/auth/register` | ❌ | Register new user |
| `POST` | `/api/auth/login` | ❌ | Verify token & get profile |
| `GET` | `/api/auth/profile` | ✅ | Get current user profile |
| `GET` | `/api/waterbodies` | ✅ | Get all water bodies |
| `GET` | `/api/waterbodies/nearby?lat=&lng=&radius=` | ✅ | Nearby water bodies |
| `GET` | `/api/waterbodies/search?query=` | ✅ | Search by name |
| `GET` | `/api/waterbodies/:id` | ✅ | Get single water body |
| `POST` | `/api/waterbodies` | 🔒 Contributor | Add new water body |
| `PUT` | `/api/waterbodies/:id` | 🔒 Contributor | Update water body |
| `DELETE` | `/api/waterbodies/:id` | 🔒 Contributor | Delete water body |

✅ = Authenticated user | 🔒 = Contributors only

---

## 🧠 AI Safety Scoring (WHO Standards)

| Parameter | Weight | WHO Standard |
|-----------|--------|-------------|
| pH | 25% | 6.5 – 8.5 |
| Turbidity | 30% | < 1 NTU ideal, max 5 NTU |
| TDS | 25% | < 500 mg/L (max 1000 mg/L) |
| Contaminants | 20% | Keyword-based detection |

| Score | Level | Color |
|-------|-------|-------|
| 80–100 | ✅ Safe | 🟢 Green |
| 50–79 | ⚠️ Moderate | 🟡 Yellow |
| 0–49 | 🚫 Unsafe | 🔴 Red |

> Identical scoring logic is implemented in both **Java** (`WaterSafetyAnalyzer.java`) and **Node.js** (`waterBodyController.js`) ensuring consistency.

---

## 🗃️ Firestore Schema

**Collection: `users/{userId}`**
```json
{ "userId": "...", "name": "...", "email": "...", "role": "user|contributor" }
```

**Collection: `waterBodies/{id}`**
```json
{
  "name": "River Ganga Point A",
  "latitude": 25.3176, "longitude": 82.9739,
  "ph": 7.2, "turbidity": 2.1, "tds": 320,
  "contaminants": "",
  "safetyScore": 87.5, "safetyLevel": "Safe",
  "lastUpdated": "2026-04-04T10:30:00Z",
  "contributorId": "uid_of_tester"
}
```

---

## ✨ Features

- 🗺️ **Live Map** — Google Maps with color-coded markers (Green/Yellow/Red)
- 🧪 **AI Safety Analysis** — WHO-standard weighted scoring engine
- 🔐 **Role-based Auth** — Firebase Authentication (User / Contributor)
- 📲 **Push Notifications** — FCM alerts when water becomes unsafe
- 📴 **Offline Mode** — Room DB caches last fetched data
- 🔍 **Search & Filter** — By name, safety level, distance
- ➕ **Contributor Module** — Add/update water bodies with GPS auto-fill
- 🎨 **Smooth UI** — Animated card entrance, progress bars, safety color theming
