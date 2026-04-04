const express = require('express');
const router = express.Router();
const { getAuth, getFirestore } = require('../config/firebase');

/**
 * POST /api/auth/register
 * Body: { email, password, name, role }
 * Creates user in Firebase Auth and saves profile to Firestore.
 */
router.post('/register', async (req, res) => {
    const { email, password, name, role } = req.body;

    if (!email || !password || !name) {
        return res.status(400).json({ error: 'email, password, and name are required.' });
    }

    const validRoles = ['user', 'contributor'];
    const userRole = validRoles.includes(role) ? role : 'user';

    try {
        const auth = getAuth();
        const db = getFirestore();

        const userRecord = await auth.createUser({ email, password, displayName: name });

        // Save profile to Firestore
        await db.collection('users').doc(userRecord.uid).set({
            userId: userRecord.uid,
            name,
            email,
            role: userRole,
            createdAt: new Date().toISOString()
        });

        // Set custom claim for role (used in token)
        await auth.setCustomUserClaims(userRecord.uid, { role: userRole });

        res.status(201).json({
            message: 'User registered successfully.',
            userId: userRecord.uid,
            role: userRole
        });
    } catch (error) {
        console.error('Register error:', error.message);
        res.status(400).json({ error: error.message });
    }
});

/**
 * POST /api/auth/login
 * Note: Actual login should happen on the client (Firebase SDK).
 * This endpoint verifies the token and returns user profile.
 * Body: { idToken }
 */
router.post('/login', async (req, res) => {
    const { idToken } = req.body;
    if (!idToken) return res.status(400).json({ error: 'idToken is required.' });

    try {
        const auth = getAuth();
        const db = getFirestore();

        const decoded = await auth.verifyIdToken(idToken);
        const userDoc = await db.collection('users').doc(decoded.uid).get();

        if (!userDoc.exists) {
            return res.status(404).json({ error: 'User profile not found.' });
        }

        res.json({ user: userDoc.data() });
    } catch (error) {
        res.status(401).json({ error: 'Invalid or expired token: ' + error.message });
    }
});

/**
 * GET /api/auth/profile
 * Returns the profile of the currently authenticated user.
 */
router.get('/profile', async (req, res) => {
    const authHeader = req.headers.authorization;
    if (!authHeader) return res.status(401).json({ error: 'Unauthorized' });
    const idToken = authHeader.split('Bearer ')[1];

    try {
        const decoded = await getAuth().verifyIdToken(idToken);
        const userDoc = await getFirestore().collection('users').doc(decoded.uid).get();
        if (!userDoc.exists) return res.status(404).json({ error: 'User not found' });
        res.json(userDoc.data());
    } catch (error) {
        res.status(401).json({ error: error.message });
    }
});

module.exports = router;
