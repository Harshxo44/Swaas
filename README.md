# SWAAS: Safe Water Assessment & Awareness System

SWAAS is a full-stack Android application designed to map and monitor local water safety. It collects water quality data through authorized contributors, runs the data through a rule-based safety engine utilizing WHO standards, and maps the results globally using OpenStreetMap.

## Overview

The goal of this project is to provide a reliable, offline-capable tool for tracking water safety in both urban and rural environments.

- **Offline Support**: Uses Android Room Database to cache map tiles and water body data for offline use.
- **Water Safety Engine**: Analyzes water based on pH, Turbidity, TDS, and Contaminants, scoring it as Safe, Moderate, or Unsafe.
- **Free Map Infrastructure**: Built on top of OSMdroid, requiring no paid Google Maps API keys.
- **Authentication**: Firebase Authentication with role-based access control (General Users vs. Contributors).

## Tech Stack

### Android Client
- **Language**: Java 8
- **UI Architecture**: MVVM, Material Design 3, ViewBinding
- **Networking/Data**: Retrofit2, OkHttp3, Room Persistence Library
- **Maps**: OSMdroid

### Backend Service
- **Environment**: Node.js / Express.js
- **Database**: Firebase Firestore
- **Security**: Firebase Admin SDK, JWT validation

## Getting Started

### 1. Set up Firebase
1. Create a new project in the [Firebase Console](https://console.firebase.google.com).
2. Enable **Email/Password Authentication**.
3. Create a **Firestore Database** and start it in Test Mode.
4. Download your `google-services.json` and place it in the `android/app/` folder.
5. Generate a private Service Account Key and save it as `serviceAccountKey.json` inside the `backend/` folder.

### 2. Run the Node.js Backend
From the root of the project:
```bash
cd backend
npm install
npm run dev
```

### 3. Run the Android App
1. Open up **Android Studio**.
2. Select **File > Open** and choose the `Swaas/android` directory.
3. Wait for the Gradle project sync to finish.
4. Run the app on an Android Emulator or your physical device.

## API Documentation

| Method | Endpoint | Authorization | Description |
|--------|----------|---------------|-------------|
| POST   | `/api/auth/register` | Open | Create a customized user account |
| POST   | `/api/auth/login` | Open | Issue authentication token |
| GET    | `/api/waterbodies` | User | Get a full list of logged water bodies |
| GET    | `/api/waterbodies/nearby` | User | Fetch water bodies within a given radius |
| POST   | `/api/waterbodies` | Contributor | Upload new water tests |
| PUT    | `/api/waterbodies/:id` | Contributor | Update existing water tests |

## Safety Engine Logic

The backend and frontend share a mirrored logic engine that determines safety. It relies on the following checks based loosely on standard WHO metrics:

- **pH Level** (Ideal: 6.5 - 8.5)
- **Turbidity** (Ideal: < 5.0 NTU)
- **TDS** (Ideal: < 500 mg/L)
- **Contaminants** (Must not contain toxins like Lead, Arsenic)

A score above 80 is marked as safe, 50-79 is moderate, and below 50 is strictly unsafe.
