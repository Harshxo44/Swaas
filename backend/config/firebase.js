const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

let firebaseInitError = null;

/**
 * Initialize Firebase Admin SDK.
 * Uses a service account JSON file (downloaded from Firebase console).
 */
function initFirebase() {
  if (admin.apps.length > 0) return; // Already initialized

  try {
    let credential;
    if (process.env.FIREBASE_SERVICE_ACCOUNT_JSON) {
      // Production: pass as env var JSON string
      const serviceAccount = JSON.parse(
        process.env.FIREBASE_SERVICE_ACCOUNT_JSON,
      );
      credential = admin.credential.cert(serviceAccount);
    } else {
      // Local dev: check the documented backend root location first,
      // then fall back to the config folder for older setups.
      const candidatePaths = [
        path.join(__dirname, "..", "serviceAccountKey.json"),
        path.join(__dirname, "serviceAccountKey.json"),
      ];
      const serviceAccountPath = candidatePaths.find((candidate) =>
        fs.existsSync(candidate),
      );

      if (!serviceAccountPath) {
        firebaseInitError = new Error(
          "Missing Firebase credentials. Add backend/serviceAccountKey.json or set FIREBASE_SERVICE_ACCOUNT_JSON.",
        );
        console.warn(
          "⚠️ Firebase Admin SDK not initialized:",
          firebaseInitError.message,
        );
        return;
      }

      const serviceAccount = JSON.parse(
        fs.readFileSync(serviceAccountPath, "utf8"),
      );
      credential = admin.credential.cert(serviceAccount);
    }

    admin.initializeApp({
      credential,
      storageBucket:
        process.env.FIREBASE_STORAGE_BUCKET || "your-project-id.appspot.com",
    });

    console.log("✅ Firebase Admin SDK initialized");
  } catch (error) {
    firebaseInitError = error;
    console.warn("⚠️ Firebase Admin SDK not initialized:", error.message);
  }
}

function getFirestore() {
  if (firebaseInitError) {
    throw firebaseInitError;
  }
  return admin.firestore();
}

function getAuth() {
  if (firebaseInitError) {
    throw firebaseInitError;
  }
  return admin.auth();
}

function getMessaging() {
  if (firebaseInitError) {
    throw firebaseInitError;
  }
  return admin.messaging();
}

module.exports = { initFirebase, getFirestore, getAuth, getMessaging };
