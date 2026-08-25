package mu.rekolt.model;

import mu.rekolt.service.PaymentCalculator;

import java.util.Objects;

/**
 * Objective 5: Delivery now holds real references to a Member and a
 * Produce, instead of a memberId/memberName/produceCode carried as plain
 * strings. Grade and net payable are still derived in the constructor
 * from the fields above, so a Delivery can never disagree with its own
 * quality score or its own produce's rules.
 */
public class Delivery implements Payable, Reportable, Comparable<Delivery> {

    private final String deliveryId;
    private final Member member;
    private final Produce produce;
    private final double massKg;
    private final int qualityScore;
    private final int week;
    private final Grade grade;
    private final double netPayable;

    public Delivery(String deliveryId, Member member, Produce produce,
                     double massKg, int qualityScore, int week) {
        if (member == null) {
            throw new IllegalArgumentException("member cannot be null");
        }
        if (produce == null) {
            throw new IllegalArgumentException("produce cannot be null");
        }
        if (massKg <= 0 || massKg > 5000) {
            throw new IllegalArgumentException("massKg must be > 0 and <= 5000");
        }
        if (qualityScore < 0 || qualityScore > 100) {
            throw new IllegalArgumentException("qualityScore must be 0-100");
        }

        this.deliveryId = deliveryId;
        this.member = member;
        this.produce = produce;
        this.massKg = massKg;
        this.qualityScore = qualityScore;
        this.week = week;

        this.grade = Grade.fromScore(qualityScore);
        this.netPayable = PaymentCalculator.computeNetPayable(massKg, produce, grade);
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public Member getMember() {
        return member;
    }

    public Produce getProduce() {
        return produce;
    }

    public double getMassKg() {
        return massKg;
    }

    public int getQualityScore() {
        return qualityScore;
    }

    public int getWeek() {
        return week;
    }

    public Grade getGrade() {
        return grade;
    }

    @Override
    public double payableAmount() {
        return netPayable;
    }

    @Override
    public String reportText() {
        return String.format("%s %s %.1fkg grade %s -> %.2f MUR",
                deliveryId, produce.getCode(), massKg, grade, netPayable);
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

    @Override
    public String toString() {
        return deliveryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Delivery)) {
            return false;
        }
        return deliveryId.equals(((Delivery) o).deliveryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveryId);
    }
}
