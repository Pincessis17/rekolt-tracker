package mu.rekolt.service;

import mu.rekolt.model.Delivery;

/**
 * The five payment steps from section 2 plus the price list
 *  and category multipliers looked up by produce code.
 */
public class PaymentCalculator {

    private static final double COMMISSION_RATE = 0.05;
    private static final double TRANSPORT_LEVY_PER_KG = 2.0;

    public static double basePriceFor(String produceCode) {
        switch (produceCode) {
            case "MZE": return 30.0;
            case "BNS": return 90.0;
            case "POT": return 45.0;
            case "TEA": return 25.0;
            default: throw new IllegalArgumentException("Unknown produce code: " + produceCode);
        }
    }

    public static double categoryMultiplierFor(String produceCode) {
        switch (produceCode) {
            case "MZE":
            case "BNS":
                return 1.00; // cereal
            case "POT":
                return 0.90; // perishable
            case "TEA":
                return 1.10; // cash crop
            default: throw new IllegalArgumentException("Unknown produce code: " + produceCode);
        }
    }

    public static String categoryLabelFor(String produceCode) {
        switch (produceCode) {
            case "MZE":
            case "BNS":
                return "Cereal";
            case "POT":
                return "Perishable";
            case "TEA":
                return "Cash crop";
            default: throw new IllegalArgumentException("Unknown produce code: " + produceCode);
        }
    }

    /**
     * The full five-step calculation, given every ingredient explicitly.
     * A REJECT delivery pays nothing and has nothing deducted from it.
     */
    public static double computeNetPayable(double massKg, double basePricePerKg,
                                            double categoryMultiplier, String grade) {
        double baseValue = massKg * basePricePerKg;                       // Step 1
        double afterGrade = baseValue * Grading.gradeMultiplier(grade);   // Step 2
        double afterCategory = afterGrade * categoryMultiplier;           // Step 3

        if (grade.equals("REJECT")) {
            return 0.0;
        }

        double commission = afterCategory * COMMISSION_RATE;      // Step 4
        double transportLevy = massKg * TRANSPORT_LEVY_PER_KG;    // Step 5
        return afterCategory - commission - transportLevy;
    }

    /**
     * Overloaded version of the method above: instead of the caller supplying
     * mass, price, category multiplier and grade separately, this reads them
     * straight off a Delivery. Same calculation, a more convenient call site.
     */
    public static double computeNetPayable(Delivery delivery) {
        return computeNetPayable(
                delivery.massKg,
                basePriceFor(delivery.produceCode),
                categoryMultiplierFor(delivery.produceCode),
                delivery.grade
        );
    }
}
