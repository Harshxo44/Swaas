const express = require('express');
const router = express.Router();
const analysisController = require('../controllers/analysisController');

// POST /api/analyze-water
router.post('/', analysisController.analyzeWater);

module.exports = router;
