package mu.rekolt.model;

import mu.rekolt.service.Grading;
import mu.rekolt.service.PaymentCalculator;

public class Delivery implements Comparable<Delivery> {

    public final String deliveryId;
    public final String memberId;
    public final String memberName;
    public final String produceCode;
    public final double massKg;
    public final int qualityScore;
    public final int week;
    public final String grade;
    public final double netPayable;

    public Delivery(String deliveryId, String memberId, String memberName,
                     String produceCode, double massKg, int qualityScore, int week) {
        this.deliveryId = deliveryId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.produceCode = produceCode;
        this.massKg = massKg;
        this.qualityScore = qualityScore;
        this.week = week;

        // Grade and payment are derived from the fields above, not passed in,
        // so a Delivery can never be constructed with a grade that disagrees
        // with its own quality score.
        this.grade = Grading.gradeLetter(qualityScore);
        this.netPayable = PaymentCalculator.computeNetPayable(this);
    }

    /**
     * Natural ordering: by delivery ID, i.e. the order deliveries were
     * recorded in. Objective 3 asks for both Comparable (this, the type's
     * one default order) and a separate Comparator (in Main, an unrelated
     * "by value" order) so two different sorting mechanisms are shown.
     */
    @Override
    public int compareTo(Delivery other) {
        return this.deliveryId.compareTo(other.deliveryId);
    }
}
