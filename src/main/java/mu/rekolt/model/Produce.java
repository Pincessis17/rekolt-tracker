package mu.rekolt.model;

import java.util.Objects;

/**
 * Objective 5: the produce price list from Objective 3 (an array of
 * PriceListEntry rows, searched by code) is replaced by this abstract
 * type. A category's multiplier and label are no longer data sitting
 * next to the price - they are behaviour, implemented differently by
 * each subclass below. Nothing outside this hierarchy branches on
 * produce code to work out a multiplier any more.
 */
public abstract class Produce {

    private final String code;
    private final String label;
    private final double basePricePerKg;

    protected Produce(String code, String label, double basePricePerKg) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Produce code cannot be blank");
        }
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Produce label cannot be blank");
        }
        if (basePricePerKg <= 0) {
            throw new IllegalArgumentException("basePricePerKg must be positive");
        }
        this.code = code;
        this.label = label;
        this.basePricePerKg = basePricePerKg;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public double getBasePricePerKg() {
        return basePricePerKg;
    }

    /** Rule 3's category multiplier - a fixed constant per subclass. */
    public abstract double categoryMultiplier();

    /** The category name shown on receipts and reports. */
    public abstract String categoryLabel();

    /**
     * Steps 1-3 of the net payment calculation (base value, grade, category),
     * the part that only needs the produce and the grade. Steps 4-5
     * (commission, transport levy) depend on nothing produce-specific, so
     * they stay in PaymentCalculator.
     */
    public double valuation(double massKg, Grade grade) {
        double baseValue = massKg * basePricePerKg;
        double afterGrade = baseValue * grade.getMultiplier();
        return afterGrade * categoryMultiplier();
    }

    @Override
    public String toString() {
        return code + " " + label + " (" + categoryLabel() + ", " + basePricePerKg + " MUR/kg)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Produce)) {
            return false;
        }
        Produce other = (Produce) o;
        return code.equals(other.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
