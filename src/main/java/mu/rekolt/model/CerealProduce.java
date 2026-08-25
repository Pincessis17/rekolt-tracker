package mu.rekolt.model;

/** Rule 3: cereals (maize, beans) carry no category adjustment. */
public class CerealProduce extends Produce {

    public CerealProduce(String code, String label, double basePricePerKg) {
        super(code, label, basePricePerKg);
    }

    @Override
    public double categoryMultiplier() {
        return 1.00;
    }

    @Override
    public String categoryLabel() {
        return "Cereal";
    }
}
