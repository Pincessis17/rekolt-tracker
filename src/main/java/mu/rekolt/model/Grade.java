package mu.rekolt.model;

/**
 * Objective 5: a Grade is no longer just a letter (a String label handed
 * around and switched on in a service class) - it is a fixed set of named
 * values, and each one carries its own multiplier as real behaviour. Ask
 * any Grade for getMultiplier() and it answers for itself; nothing outside
 * this enum needs to know what A, B, C or REJECT are worth.
 */
public enum Grade {
    A(1.15),
    B(1.00),
    C(0.85),
    REJECT(0.00);

    private final double multiplier;

    Grade(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }

    /** Rule 2's boundaries, as a static factory rather than a separate service method. */
    public static Grade fromScore(int qualityScore) {
        if (qualityScore >= 85) {
            return A;
        } else if (qualityScore >= 70) {
            return B;
        } else if (qualityScore >= 50) {
            return C;
        } else {
            return REJECT;
        }
    }
}
