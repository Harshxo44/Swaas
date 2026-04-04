const admin = require('firebase-admin');
const path = require('path');

/**
 * Initialize Firebase Admin SDK.
 * Uses a service account JSON file (downloaded from Firebase console).
 */
function initFirebase() {
    if (admin.apps.length > 0) return; // Already initialized

    let credential;
    if (process.env.FIREBASE_SERVICE_ACCOUNT_JSON) {
        // Production: pass as env var JSON string
        const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_JSON);
        credential = admin.credential.cert(serviceAccount);
    } else {
        // Local dev: use serviceAccountKey.json file
        const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');
        credential = admin.credential.cert(serviceAccountPath);
    }

    admin.initializeApp({
        credential,
        storageBucket: process.env.FIREBASE_STORAGE_BUCKET || 'your-project-id.appspot.com'
    });

    console.log('✅ Firebase Admin SDK initialized');
}

function getFirestore() {
    return admin.firestore();
}

function getAuth() {
    return admin.auth();
}

function getMessaging() {
    return admin.messaging();
}

module.exports = { initFirebase, getFirestore, getAuth, getMessaging };
