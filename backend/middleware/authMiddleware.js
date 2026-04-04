const { getAuth } = require('../config/firebase');

/**
 * Middleware to verify Firebase ID tokens (JWT) from Android app.
 * Attaches decoded user info to req.user.
 */
async function verifyToken(req, res, next) {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ error: 'Unauthorized: No token provided' });
    }

    const idToken = authHeader.split('Bearer ')[1];
    try {
        const decoded = await getAuth().verifyIdToken(idToken);
        req.user = {
            uid: decoded.uid,
            email: decoded.email,
            role: decoded.role || 'user' // Custom claim set during registration
        };
        next();
    } catch (error) {
        console.error('Token verification failed:', error.message);
        return res.status(403).json({ error: 'Forbidden: Invalid or expired token' });
    }
}

/**
 * Middleware to check if authenticated user has contributor role.
 * Must be used AFTER verifyToken.
 */
async function requireContributor(req, res, next) {
    if (!req.user) return res.status(401).json({ error: 'Unauthorized' });

    // Check Firestore user document for role (more reliable than custom claims)
    const { getFirestore } = require('../config/firebase');
    const db = getFirestore();
    try {
        const userDoc = await db.collection('users').doc(req.user.uid).get();
        if (!userDoc.exists || userDoc.data().role !== 'contributor') {
            return res.status(403).json({ error: 'Access denied. Contributors only.' });
        }
        next();
    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
}

module.exports = { verifyToken, requireContributor };
