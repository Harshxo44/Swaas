const { getFirestore, getMessaging } = require('../config/firebase');

// ── WHO Standard Ranges (mirrors Android WaterSafetyAnalyzer.java) ────────
const WHO = {
    ph: { min: 6.5, max: 8.5, ideal: 7.0 },
    turbidity: { ideal: 1.0, max: 5.0 },
    tds: { ideal: 300, max: 500, danger: 1000 },
};
const HIGH_RISK_CONTAMINANTS = ['arsenic', 'lead', 'mercury', 'cyanide', 'e.coli', 'cholera',
    'pesticide', 'nitrate', 'fluoride', 'bacteria'];
const MODERATE_RISK_CONTAMINANTS = ['iron', 'manganese', 'chlorine', 'sulfate', 'sediment'];

/** Deterministic safety analysis — identical logic to Android WaterSafetyAnalyzer.java */
function analyzeSafety(ph, turbidity, tds, contaminants = '') {
    // pH Score (weight 25%)
    let phScore;
    if (ph >= WHO.ph.min && ph <= WHO.ph.max) {
        const dev = Math.abs(ph - WHO.ph.ideal);
        phScore = 100 - (dev / 1.0) * 20;
    } else {
        const dist = ph < WHO.ph.min ? WHO.ph.min - ph : ph - WHO.ph.max;
        phScore = Math.max(0, 60 - dist * 20);
    }

    // Turbidity Score (weight 30%)
    let turbScore;
    if (turbidity <= WHO.turbidity.ideal) turbScore = 100;
    else if (turbidity <= WHO.turbidity.max)
        turbScore = 100 - ((turbidity - 1) / 4) * 50;
    else turbScore = Math.max(0, 50 - (turbidity - 5) * 5);

    // TDS Score (weight 25%)
    let tdsScore;
    if (tds <= WHO.tds.ideal) tdsScore = 100;
    else if (tds <= WHO.tds.max) tdsScore = 100 - ((tds - 300) / 200) * 25;
    else if (tds <= WHO.tds.danger) tdsScore = Math.max(0, 75 - ((tds - 500) / 500) * 75);
    else tdsScore = 0;

    // Contaminant Score (weight 20%)
    const lower = (contaminants || '').toLowerCase();
    const highCount = HIGH_RISK_CONTAMINANTS.filter(k => lower.includes(k)).length;
    const modCount = MODERATE_RISK_CONTAMINANTS.filter(k => lower.includes(k)).length;
    let contamScore;
    if (!lower.trim()) contamScore = 100;
    else if (highCount > 0) contamScore = Math.max(0, 30 - highCount * 10);
    else if (modCount > 0) contamScore = Math.max(0, 70 - modCount * 15);
    else contamScore = 80;

    const total = (phScore * 0.25) + (turbScore * 0.30) + (tdsScore * 0.25) + (contamScore * 0.20);
    const score = Math.round(total * 10) / 10;
    let level, precaution;
    if (score >= 80) {
        level = 'Safe';
        precaution = '✅ Suitable for drinking and daily use.';
    } else if (score >= 50) {
        level = 'Moderate';
        precaution = '⚠️ Boil before use. Filtration recommended.';
    } else {
        level = 'Unsafe';
        precaution = '🚫 Do NOT use. High risk of contamination.';
    }
    return { safetyScore: score, safetyLevel: level, precaution };
}

/** Haversine distance in km between two lat/lng points */
function haversineKm(lat1, lon1, lat2, lon2) {
    const R = 6371;
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLon = ((lon2 - lon1) * Math.PI) / 180;
    const a = Math.sin(dLat / 2) ** 2 +
        Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) * Math.sin(dLon / 2) ** 2;
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

// ── Controller Functions ───────────────────────────────────────────────────

/** GET /api/waterbodies */
const getAll = async (req, res) => {
    try {
        const snapshot = await getFirestore().collection('waterBodies').get();
        const list = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        res.json(list);
    } catch (e) { res.status(500).json({ error: e.message }); }
};

/** GET /api/waterbodies/:id */
const getById = async (req, res) => {
    try {
        const doc = await getFirestore().collection('waterBodies').doc(req.params.id).get();
        if (!doc.exists) return res.status(404).json({ error: 'Water body not found.' });
        res.json({ id: doc.id, ...doc.data() });
    } catch (e) { res.status(500).json({ error: e.message }); }
};

/** GET /api/waterbodies/nearby?lat=&lng=&radius= */
const getNearby = async (req, res) => {
    const { lat, lng, radius = 10 } = req.query;
    if (!lat || !lng) return res.status(400).json({ error: 'lat and lng are required.' });

    try {
        const snapshot = await getFirestore().collection('waterBodies').get();
        const nearby = snapshot.docs
            .map(doc => ({ id: doc.id, ...doc.data() }))
            .filter(wb => haversineKm(
                parseFloat(lat), parseFloat(lng),
                wb.latitude, wb.longitude) <= parseFloat(radius))
            .sort((a, b) =>
                haversineKm(lat, lng, a.latitude, a.longitude) -
                haversineKm(lat, lng, b.latitude, b.longitude));
        res.json(nearby);
    } catch (e) { res.status(500).json({ error: e.message }); }
};

/** GET /api/waterbodies/search?query= */
const search = async (req, res) => {
    const { query } = req.query;
    if (!query) return res.status(400).json({ error: 'query parameter is required.' });

    try {
        const snapshot = await getFirestore().collection('waterBodies')
            .orderBy('name')
            .startAt(query)
            .endAt(query + '\uf8ff')
            .get();
        res.json(snapshot.docs.map(d => ({ id: d.id, ...d.data() })));
    } catch (e) { res.status(500).json({ error: e.message }); }
};

/** POST /api/waterbodies — Contributors only */
const create = async (req, res) => {
    const { name, latitude, longitude, ph, turbidity, tds, contaminants } = req.body;
    if (!name || latitude == null || longitude == null || ph == null)
        return res.status(400).json({ error: 'name, latitude, longitude, ph are required.' });
    if (ph < 0 || ph > 14)
        return res.status(400).json({ error: 'pH must be between 0 and 14.' });

    try {
        const safety = analyzeSafety(ph, turbidity || 0, tds || 0, contaminants);
        const data = {
            name,
            latitude: parseFloat(latitude),
            longitude: parseFloat(longitude),
            ph: parseFloat(ph),
            turbidity: parseFloat(turbidity || 0),
            tds: parseFloat(tds || 0),
            contaminants: contaminants || '',
            ...safety,
            contributorId: req.user.uid,
            lastUpdated: new Date().toISOString()
        };

        const ref = await getFirestore().collection('waterBodies').add(data);

        // Send FCM alert if Unsafe or newly added
        if (safety.safetyLevel === 'Unsafe') {
            await sendWaterAlert(name, safety.safetyLevel, safety.precaution);
        }

        res.status(201).json({ id: ref.id, ...data });
    } catch (e) { res.status(500).json({ error: e.message }); }
};

/** PUT /api/waterbodies/:id — Contributors only */
const update = async (req, res) => {
    const { id } = req.params;
    const { ph, turbidity, tds, contaminants, name, latitude, longitude } = req.body;
    if (ph != null && (ph < 0 || ph > 14))
        return res.status(400).json({ error: 'pH must be between 0 and 14.' });

    try {
        const docRef = getFirestore().collection('waterBodies').doc(id);
        const existing = await docRef.get();
        if (!existing.exists) return res.status(404).json({ error: 'Water body not found.' });

        const merged = { ...existing.data() };
        if (name) merged.name = name;
        if (latitude) merged.latitude = parseFloat(latitude);
        if (longitude) merged.longitude = parseFloat(longitude);
        if (ph != null) merged.ph = parseFloat(ph);
        if (turbidity != null) merged.turbidity = parseFloat(turbidity);
        if (tds != null) merged.tds = parseFloat(tds);
        if (contaminants != null) merged.contaminants = contaminants;

        const safety = analyzeSafety(merged.ph, merged.turbidity, merged.tds, merged.contaminants);
        const updated = {
            ...merged, ...safety, contributorId: req.user.uid,
            lastUpdated: new Date().toISOString()
        };

        await docRef.set(updated);

        // Alert if became unsafe
        if (safety.safetyLevel === 'Unsafe') {
            await sendWaterAlert(merged.name, safety.safetyLevel, safety.precaution);
        }

        res.json({ id, ...updated });
    } catch (e) { res.status(500).json({ error: e.message }); }
};

/** DELETE /api/waterbodies/:id */
const remove = async (req, res) => {
    try {
        await getFirestore().collection('waterBodies').doc(req.params.id).delete();
        res.json({ message: 'Water body deleted successfully.' });
    } catch (e) { res.status(500).json({ error: e.message }); }
};

/** Send FCM push notification to the 'water_alerts' topic */
async function sendWaterAlert(waterBodyName, safetyLevel, precaution) {
    try {
        await getMessaging().sendToTopic('water_alerts', {
            notification: {
                title: `⚠️ Water Alert: ${waterBodyName}`,
                body: `Status: ${safetyLevel}. ${precaution}`
            },
            data: { waterBodyName, safetyLevel, precaution }
        });
    } catch (e) {
        console.warn('FCM notification failed:', e.message);
    }
}

module.exports = { getAll, getById, getNearby, search, create, update, remove };
