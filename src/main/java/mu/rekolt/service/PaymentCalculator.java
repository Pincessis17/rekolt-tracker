package mu.rekolt.service;

import mu.rekolt.model.Delivery;
import mu.rekolt.model.PriceListEntry;

/**
 * The five payment steps from section 2, plus the price list (rule 1)
 * and category multipliers (rule 3). Objective 3 asks for the price
 * list itself to be an array, searched by produce code, rather than
 * the switch statements this used in Objective 2.
 */
public class PaymentCalculator {

    private static final double COMMISSION_RATE = 0.05;
    private static final double TRANSPORT_LEVY_PER_KG = 2.0;

    private static final PriceListEntry[] PRICE_LIST = {
            new PriceListEntry("MZE", "Cereal", 30.0, 1.00),
            new PriceListEntry("BNS", "Cereal", 90.0, 1.00),
            new PriceListEntry("POT", "Perishable", 45.0, 0.90),
            new PriceListEntry("TEA", "Cash crop", 25.0, 1.10),
    };

    /** Linear search of the price list array by produce code. */
    private static PriceListEntry entryFor(String produceCode) {
        for (PriceListEntry entry : PRICE_LIST) {
            if (entry.code.equals(produceCode)) {
                return entry;
            }
        }
        throw new IllegalArgumentException("Unknown produce code: " + produceCode);
    }

    public static double basePriceFor(String produceCode) {
        return entryFor(produceCode).basePricePerKg;
    }

    public static double categoryMultiplierFor(String produceCode) {
        return entryFor(produceCode).categoryMultiplier;
    }

    public static String categoryLabelFor(String produceCode) {
        return entryFor(produceCode).categoryLabel;
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
