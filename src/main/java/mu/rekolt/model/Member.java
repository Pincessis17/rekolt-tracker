package mu.rekolt.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Objective 5: previously a member was just an ID and a name string
 * carried around on each Delivery. Now a Member is its own object,
 * holding its own deliveries, and implementing both capability
 * interfaces: payableAmount() sums what it is owed, reportText()
 * describes it for the season report.
 */
public class Member implements Payable, Reportable {

    private final String memberId;
    private final String name;
    private final List<Delivery> deliveries = new ArrayList<>();

    public Member(String memberId, String name) {
        if (memberId == null || !memberId.matches("M-\\d{4}")) {
            throw new IllegalArgumentException("memberId must match M-dddd");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        this.memberId = memberId;
        this.name = name;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    /**
     * Package-private: only SeasonStore (same package) may add a delivery to
     * a member, so the Delivery <-> Member link can never be set up from
     * outside without also being registered with the season as a whole.
     */
    void addDelivery(Delivery delivery) {
        deliveries.add(delivery);
    }

    @Override
    public double payableAmount() {
        double total = 0.0;
        for (Delivery delivery : deliveries) {
            total += delivery.payableAmount();
        }
        return total;
    }

    @Override
    public String reportText() {
        return String.format("%s %s: %d deliveries, %.2f MUR",
                memberId, name, deliveries.size(), payableAmount());
    }

    @Override
    public String toString() {
        return memberId + " " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Member)) {
            return false;
        }
        return memberId.equals(((Member) o).memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }
}
