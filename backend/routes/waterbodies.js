const express = require('express');
const router = express.Router();
const { verifyToken, requireContributor } = require('../middleware/authMiddleware');
const waterBodyController = require('../controllers/waterBodyController');

/**
 * GET /api/waterbodies
 * Returns all water bodies. Accessible to all authenticated users.
 */
router.get('/', verifyToken, waterBodyController.getAll);

/**
 * GET /api/waterbodies/nearby?lat=&lng=&radius=
 * Returns water bodies within a given radius (km) of a coordinate.
 */
router.get('/nearby', verifyToken, waterBodyController.getNearby);

/**
 * GET /api/waterbodies/search?query=
 * Searches water bodies by name.
 */
router.get('/search', verifyToken, waterBodyController.search);

/**
 * GET /api/waterbodies/:id
 * Returns a single water body by ID.
 */
router.get('/:id', verifyToken, waterBodyController.getById);

/**
 * POST /api/waterbodies
 * Add a new water body. Contributors only.
 * Body: { name, latitude, longitude, ph, turbidity, tds, contaminants }
 */
router.post('/', verifyToken, requireContributor, waterBodyController.create);

/**
 * PUT /api/waterbodies/:id
 * Update existing water body. Contributors only.
 */
router.put('/:id', verifyToken, requireContributor, waterBodyController.update);

/**
 * DELETE /api/waterbodies/:id
 * Delete a water body. Contributors only (admin use).
 */
router.delete('/:id', verifyToken, requireContributor, waterBodyController.remove);

module.exports = router;
