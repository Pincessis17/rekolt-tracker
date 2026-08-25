package mu.rekolt.service;

import mu.rekolt.model.Grade;
import mu.rekolt.model.Produce;

/**
 * Objective 5: the price-list array and the linear search by produce code
 * are gone - Produce (and its subclasses) now know their own base price
 * and category multiplier, so this class only holds the two steps that
 * are not produce-specific: commission and the transport levy.
 */
public class PaymentCalculator {

    public static final double COMMISSION_RATE = 0.05;
    public static final double TRANSPORT_LEVY_PER_KG = 2.0;

    /**
     * The full five-step calculation. Steps 1-3 (base value, grade,
     * category) are delegated to Produce.valuation(), which dispatches
     * polymorphically to whichever subclass this produce actually is.
     * A REJECT delivery pays nothing and has nothing deducted from it.
     */
    public static double computeNetPayable(double massKg, Produce produce, Grade grade) {
        if (grade == Grade.REJECT) {
            return 0.0;
        }

        double afterCategory = produce.valuation(massKg, grade);          // Steps 1-3
        double commission = commissionFor(massKg, produce, grade);        // Step 4
        double transportLevy = transportLevyFor(massKg, grade);           // Step 5
        return afterCategory - commission - transportLevy;
    }

    /**
     * Step 4 alone - broken out so the season report (Objective 6) can show
     * commission per delivery without duplicating the REJECT rule a third
     * time next to computeNetPayable and Main's printReceipt.
     */
    public static double commissionFor(double massKg, Produce produce, Grade grade) {
        if (grade == Grade.REJECT) {
            return 0.0;
        }
        return produce.valuation(massKg, grade) * COMMISSION_RATE;
    }

    /** Step 5 alone - see commissionFor. */
    public static double transportLevyFor(double massKg, Grade grade) {
        if (grade == Grade.REJECT) {
            return 0.0;
        }
        return massKg * TRANSPORT_LEVY_PER_KG;
    }
}
