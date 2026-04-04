require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const rateLimit = require('express-rate-limit');

const authRoutes = require('./routes/auth');
const waterBodyRoutes = require('./routes/waterbodies');
const { initFirebase } = require('./config/firebase');

// ── Initialize Firebase Admin SDK ──────────────────────────────────────────
initFirebase();

// ── App Setup ──────────────────────────────────────────────────────────────
const app = express();
const PORT = process.env.PORT || 3000;

// ── Security Middleware ────────────────────────────────────────────────────
app.use(helmet());
app.use(cors({ origin: '*' })); // Restrict in production

// ── Rate Limiting ──────────────────────────────────────────────────────────
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100,
  message: { error: 'Too many requests, please try again later.' }
});
app.use('/api/', limiter);

// ── Logging & Parsing ──────────────────────────────────────────────────────
app.use(morgan('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// ── Routes ─────────────────────────────────────────────────────────────────
app.use('/api/auth', authRoutes);
app.use('/api/waterbodies', waterBodyRoutes);

// ── Health Check ───────────────────────────────────────────────────────────
app.get('/health', (req, res) => {
  res.json({ status: 'ok', service: 'SWAAS API', version: '1.0.0', timestamp: new Date() });
});

// ── 404 Handler ────────────────────────────────────────────────────────────
app.use((req, res) => {
  res.status(404).json({ error: 'Route not found' });
});

// ── Global Error Handler ───────────────────────────────────────────────────
app.use((err, req, res, next) => {
  console.error('❌ Error:', err.stack);
  res.status(err.status || 500).json({
    error: err.message || 'Internal Server Error'
  });
});

// ── Start ──────────────────────────────────────────────────────────────────
app.listen(PORT, () => {
  console.log(`🚀 SWAAS API running on http://localhost:${PORT}`);
  console.log(`📡 Endpoints available at http://localhost:${PORT}/api`);
});

module.exports = app;
