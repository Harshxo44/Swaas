const { evaluateWater } = require('../utils/waterAnalysisEngine');

/**
 * Controller to handle POST /api/analyze-water
 */
exports.analyzeWater = (req, res) => {
    try {
        const { ph, turbidity, tds, contaminants } = req.body;

        // Edge Case: Missing values return error
        if (ph === undefined || turbidity === undefined || tds === undefined) {
            return res.status(400).json({ error: "Missing required values: ph, turbidity, and tds are required." });
        }

        // Edge Case: Invalid ranges reject input
        if (ph < 0 || ph > 14 || turbidity < 0 || tds < 0) {
            return res.status(400).json({ error: "Invalid physiological ranges provided." });
        }

        // Process logic
        const resultJson = evaluateWater({ ph, turbidity, tds, contaminants });

        return res.status(200).json(resultJson);
    } catch (error) {
        console.error("Analysis Error:", error);
        res.status(500).json({ error: "Internal server error analyzing water metrics." });
    }
};
