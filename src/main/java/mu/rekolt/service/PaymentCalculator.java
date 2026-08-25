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

        double afterCategory = produce.valuation(massKg, grade);   // Steps 1-3
        double commission = afterCategory * COMMISSION_RATE;       // Step 4
        double transportLevy = massKg * TRANSPORT_LEVY_PER_KG;     // Step 5
        return afterCategory - commission - transportLevy;
    }
}
