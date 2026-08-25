package mu.rekolt.model;

/** Rule 3: cash crops (tea) earn a 10% premium. */
public class CashCropProduce extends Produce {

    public CashCropProduce(String code, String label, double basePricePerKg) {
        super(code, label, basePricePerKg);
    }

    @Override
    public double categoryMultiplier() {
        return 1.10;
    }

    @Override
    public String categoryLabel() {
        return "Cash crop";
    }
}
