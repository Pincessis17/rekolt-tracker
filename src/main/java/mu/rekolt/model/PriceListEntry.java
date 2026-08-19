package mu.rekolt.model;

/**
 * One row of the produce price list (rule 1, base price, and rule 3,
 * category multiplier). Objective 3 asks for the price list to be held
 * in an array; this class is just the row type that array holds.
 */
public class PriceListEntry {
    public final String code;
    public final String categoryLabel;
    public final double basePricePerKg;
    public final double categoryMultiplier;

    public PriceListEntry(String code, String categoryLabel, double basePricePerKg, double categoryMultiplier) {
        this.code = code;
        this.categoryLabel = categoryLabel;
        this.basePricePerKg = basePricePerKg;
        this.categoryMultiplier = categoryMultiplier;
    }
}
