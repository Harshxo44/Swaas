/**
 * waterAnalysisEngine.js
 * 
 * WHO Drinking Water Standards:
 * - pH: 6.5 – 8.5
 * - Turbidity: < 5 NTU
 * - TDS: < 300 mg/L (ideal), < 500 acceptable
 * - Contaminants: Presence = Unsafe
 * 
 * Note: When exact values are not fully mapped or if missing, it will use widely accepted standard ranges.
 */

const WEIGHTS = {
    ph: 0.25,
    turbidity: 0.25,
    tds: 0.25,
    contaminants: 0.25
};

const HIGH_RISK_CONTAMINANTS = ['lead', 'arsenic', 'mercury', 'cyanide', 'heavy metals', 'e.coli', 'cholera', 'bacteria'];

/**
 * Main function to evaluate water metrics and return formatted result.
 */
function evaluateWater(params) {
    let { ph, turbidity, tds, contaminants } = params;

    let scorePh = getPhScore(ph);
    let scoreTurbidity = getTurbidityScore(turbidity);
    let scoreTds = getTdsScore(tds);
    let scoreContaminants = getContaminantsScore(contaminants);

    // Compute final score
    let finalScore = (
        (scorePh * WEIGHTS.ph) +
        (scoreTurbidity * WEIGHTS.turbidity) +
        (scoreTds * WEIGHTS.tds) +
        (scoreContaminants * WEIGHTS.contaminants)
    );

    // Apply absolute red flag overriding for raw toxins
    if (scoreContaminants === 0) {
        if (finalScore > 49) {
            finalScore = 49; // Force unsafe if known deadly contaminants present
        }
    }

    // Final category constraints
    let category = "UNSAFE";
    let precautions = [];

    if (finalScore >= 80) {
        category = "SAFE";
        precautions.push("Suitable for drinking and daily use");
    } else if (finalScore >= 50) {
        category = "MODERATE";
        precautions.push("Boil before use");
        precautions.push("Use filtration");
    } else {
        category = "UNSAFE";
        precautions.push("Do NOT consume");
        precautions.push("Possible contamination risk");
    }

    return {
        safety_score: Math.round(finalScore),
        category,
        parameters: {
            ph: scoreToText(scorePh),
            turbidity: scoreToText(scoreTurbidity),
            tds: scoreToText(scoreTds),
            contaminants: scoreToText(scoreContaminants, true)
        },
        precautions,
        confidence_note: "Based on standard water quality guidelines"
    };
}

// ----------------------------------------------------
// Scoring Functions (0 to 100)
// ----------------------------------------------------
function getPhScore(ph) {
    if (ph >= 6.5 && ph <= 8.5) return 100;
    if (ph >= 5.5 && ph < 6.5) return 60; // slightly acidic
    if (ph > 8.5 && ph <= 9.5) return 60; // slightly alkaline
    return 0; // extreme
}

function getTurbidityScore(ntu) {
    if (ntu < 5) return 100;
    if (ntu >= 5 && ntu <= 10) return 60; // moderate
    return 20; // very cloudy
}

function getTdsScore(tds) {
    if (tds < 300) return 100;
    if (tds >= 300 && tds <= 500) return 60; // moderate
    return 20; // heavy solids
}

function getContaminantsScore(c) {
    if (c === false || c === null || c === undefined || c === "") return 100;

    if (Array.isArray(c)) {
        if (c.length === 0) return 100;
        let hasDanger = c.some(toxin => HIGH_RISK_CONTAMINANTS.includes(typeof toxin === 'string' ? toxin.toLowerCase() : ''));
        return hasDanger ? 0 : 50;
    }

    // if strict true or a string implying danger
    if (c === true) return 0;
    if (typeof c === 'string') {
        let lower = c.toLowerCase();
        if (lower === 'none' || lower === 'false') return 100;
        return 0; // Contains something dangerous.
    }
    return 0;
}

function scoreToText(score, isContaminant = false) {
    if (isContaminant) {
        if (score === 100) return "SAFE";
        return "UNSAFE";
    }
    if (score === 100) return "GOOD";
    if (score >= 50) return "MODERATE";
    return "POOR";
}

module.exports = { evaluateWater };
