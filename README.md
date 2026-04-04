<div align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Node.js-43853D?style=for-the-badge&logo=node.js&logoColor=white" />
  <img src="https://img.shields.io/badge/Firebase-FFA611?style=for-the-badge&logo=firebase&logoColor=white" />
  <img src="https://img.shields.io/badge/Room_DB-0081CB?style=for-the-badge&logo=sqlite&logoColor=white" />
  <img src="https://img.shields.io/badge/OpenStreetMap-7EBC6F?style=for-the-badge&logo=openstreetmap&logoColor=white" />
</div>

<h1 align="center">💧 SWAAS: Safe Water Assessment & Awareness System</h1>

<p align="center">
  <strong>A full-stack Android application that crowd-sources water quality data, ranks safety using an AI-assisted WHO-standard engine, and visually maps water bodies globally using OpenStreetMap.</strong>
</p>

---

## ✨ Key Features

- 🗺️ **Live Global Map** — High-performance interactive map via OSMdroid (OpenStreetMap), completely free with no API limits.
- 🧪 **AI Safety Analysis** — Built-in rules engine instantly ranks water as **Safe**, **Moderate**, or **Unsafe** based on WHO parameters (pH, Turbidity, TDS, Contaminants).
- 📴 **Offline-First Architecture** — Powered by Android **Room DB**, users can view cached maps and previously searched water bodies even with no internet.
- 🔐 **Role-Based Authentication** — Secure **Firebase JWT Auth**. Regular users can search and view water data, while authenticated *Contributors* can submit real-time water tests.
- 🎨 **Material Design 3 UI** — Fluid, animated RecyclerViews, beautiful bottom sheet dialogs, and color-coded safety badges ensure a premium app feel.
- 📡 **RESTful Backend** — A powerful, scalable **Node.js (Express)** microservice infrastructure connected to a NoSQL Firebase backend.

---

## 🏗️ Technology Stack

### Mobile Frontend (Android / Java)
- **Language**: Java 8
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit2 & OkHttp3
- **Local Database**: Room Persistence Library
- **Maps**: OSMdroid (Free OpenStreetMap Tiles)
- **UI/UX**: Material 3, ViewBinding, Glide

### Backend Service (Node.js)
- **Framework**: Express.js
- **Database**: Firebase Firestore (NoSQL)
- **Security**: Firebase Admin SDK, Helmet, custom JWT validation middleware
- **Algorithms**: Custom water-safety heuristics engine matching frontend validation.

---

## 🚀 Quick Start Guide

### 1. Firebase Setup (Required)
1. Navigate to the [Firebase Console](https://console.firebase.google.com).
2. Create a new project `SWAAS`.
3. Enable **Email/Password Authentication** and create a **Firestore Database** (Test Mode).
4. **For Android**: Generate a `google-services.json` file and place it in the `android/app/` directory.
5. **For Backend**: Generate a private Service Account Key JSON and place it in the `backend/` directory as `serviceAccountKey.json`.

### 2. Running the Android Application
1. Open **Android Studio**.
2. Click **File > Open** and select specifically the `Swaas/android` folder.
3. Allow Gradle to sync completely resulting in a `BUILD SUCCESSFUL` message.
4. Hit **Run App** (`Shift+F10`) to deploy to a physical phone or an emulator.

### 3. Running the Node.js API
```bash
# Navigate to the backend directory
cd Swaas/backend

# Install required node modules
npm install

# Start the local development server (with hot-reload)
npm run dev
```
> *The backend server will run smoothly on `http://localhost:3000`.*

---

## 📡 API Endpoints Architecture

| Method | Route | Access | Purpose |
|--------|-------|--------|---------|
| `POST` | `/api/auth/register` | Open | Create new user account |
| `POST` | `/api/auth/login` | Open | Verify credentials & issue scope |
| `GET`  | `/api/waterbodies` | Protected | Fetch comprehensive water body index |
| `GET`  | `/api/waterbodies/nearby` | Protected | Geo-spatial radius query |
| `POST` | `/api/waterbodies` | **Contributor** | Submit fresh water quality reports |
| `PUT`  | `/api/waterbodies/:id` | **Contributor** | Patch existing water quality metrics |

---

## 🧠 AI Safety Scoring Engine

The scoring engine applies strict weighted analysis based on **World Health Organization (WHO)** cleanliness standards to produce an objective `SafetyScore` out of 100.

| Scientific Parameter | Weight | Acceptable WHO Range |
|----------------------|--------|----------------------|
| **pH Level** | 25% | `6.5` – `8.5` |
| **Turbidity (NTU)** | 30% | `< 5.0 NTU` |
| **TDS (mg/L)** | 25% | `< 500 mg/L` |
| **Toxic Contaminants**| 20% | `0` detected hazards |

The final generated score triggers strict UI responses:
- 🟢 **Safe (80–100):** Suitable for general use.
- 🟡 **Moderate (50–79):** Requires active filtration or boiling before ingestion.
- 🔴 **Unsafe (0–49):** Strictly flagged. Direct contact and usage strongly discouraged.

<p align="center">
  <br>
  <i>Developed with ❤️ by Aumaswar </i>
</p>
