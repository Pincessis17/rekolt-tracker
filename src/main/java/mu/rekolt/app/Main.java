package mu.rekolt.app;

/**
 * Objective 1: Environment and arithmetic.
 * Computes one delivery's payment through the five steps in section 2
 * of the spec, using the worked example (M-0042, 236 kg beans, quality 91).
 * Values are set in code for this objective; console input arrives in Objective 2.
 */
public class Main {

    // Step 1 base prices, MUR per kg
    private static final double BEANS_PRICE_PER_KG = 90.0;

    // Step 3 category multiplier (beans = cereal)
    private static final double CEREAL_MULTIPLIER = 1.00;

    // Step 4 / 5 fixed rates
    private static final double COMMISSION_RATE = 0.05;
    private static final double TRANSPORT_LEVY_PER_KG = 2.0;

    public static void main(String[] args) {

        // --- Inputs for this one delivery ---
        String memberId = "M-0042";
        String produceCode = "BNS";
        double massKg = 236.0;     // double: mass can be fractional, validated later as 0 < mass <= 5000
        int qualityScore = 91;     // int: the spec defines this as a whole number 0-100

        // A deliberate int/double distinction with an explicit cast:
        // sacks are counted in whole units, so a fractional mass is
        // narrowed to an int only for this informational figure.
        // netPayable and every money figure stay double, uncast, all the way through.
        int fullSacksOf50kg = (int) (massKg / 50.0);

        // Grade multiplier from the quality score, boundaries exact (section 2, rule 2)
        String grade;
        double gradeMultiplier;
        if (qualityScore >= 85) {
            grade = "A";
            gradeMultiplier = 1.15;
        } else if (qualityScore >= 70) {
            grade = "B";
            gradeMultiplier = 1.00;
        } else if (qualityScore >= 50) {
            grade = "C";
            gradeMultiplier = 0.85;
        } else {
            grade = "REJECT";
            gradeMultiplier = 0.00;
        }

        // --- The five steps, kept unrounded until display ---
        double baseValue = massKg * BEANS_PRICE_PER_KG;          // Step 1
        double afterGrade = baseValue * gradeMultiplier;         // Step 2
        double afterCategory = afterGrade * CEREAL_MULTIPLIER;   // Step 3
        double commission = afterCategory * COMMISSION_RATE;     // Step 4
        double transportLevy = massKg * TRANSPORT_LEVY_PER_KG;   // Step 5

        // A REJECT delivery still counts towards volume but pays nothing
        // and has nothing deducted from it (section 2, note under the table).
        double netPayable;
        if (grade.equals("REJECT")) {
            netPayable = 0.0;
            commission = 0.0;
            transportLevy = 0.0;
        } else {
            netPayable = afterCategory - commission - transportLevy;
        }

        // --- Display only: money rounded to two decimals here, nowhere earlier ---
        System.out.println("Delivery for " + memberId + " (" + produceCode + "), "
                + fullSacksOf50kg + " full 50kg sacks");
        System.out.printf("Base value      %.1f x %.2f = %,.2f%n", massKg, BEANS_PRICE_PER_KG, baseValue);
        System.out.printf("Grade %-7s x %.2f = %,.2f%n", grade, gradeMultiplier, afterGrade);
        System.out.printf("Cereal          x %.2f = %,.2f%n", CEREAL_MULTIPLIER, afterCategory);
        System.out.printf("Commission 5%%    -        %,.2f%n", commission);
        System.out.printf("Transport levy  %.1f x %.2f -        %,.2f%n", massKg, TRANSPORT_LEVY_PER_KG, transportLevy);
        System.out.printf("NET PAYABLE     =        %,.2f MUR%n", netPayable);
    }
}
