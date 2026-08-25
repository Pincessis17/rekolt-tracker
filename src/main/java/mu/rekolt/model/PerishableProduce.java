package mu.rekolt.model;

/** Rule 3: perishables (potatoes) are discounted 10% to reflect spoilage risk. */
public class PerishableProduce extends Produce {

    public PerishableProduce(String code, String label, double basePricePerKg) {
        super(code, label, basePricePerKg);
    }

    @Override
    public double categoryMultiplier() {
        return 0.90;
    }

    @Override
    public String categoryLabel() {
        return "Perishable";
    }
}
