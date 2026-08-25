package mu.rekolt.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Objective 5: the season's data, previously four separate static
 * collections sitting directly in Main, is now owned by one object.
 * SeasonStore is the only place a Delivery is ever linked to a Member -
 * addDelivery keeps both the flat list and the per-member list in step,
 * the same invariant Main used to have to maintain by hand.
 */
public class SeasonStore {

    private final Map<String, Member> members = new LinkedHashMap<>();
    private final List<Delivery> deliveries = new ArrayList<>();

    /**
     * Returns the Member already recorded under this ID, creating one if
     * this is the first time it's seen. Throws if the ID is already on
     * file under a different name (design assumption E).
     */
    public Member getOrCreateMember(String memberId, String name) {
        Member existing = members.get(memberId);
        if (existing != null) {
            if (!existing.getName().equals(name)) {
                throw new IllegalArgumentException(
                        "Member " + memberId + " is already recorded as " + existing.getName());
            }
            return existing;
        }
        Member created = new Member(memberId, name);
        members.put(memberId, created);
        return created;
    }

    public void addDelivery(Delivery delivery) {
        deliveries.add(delivery);
        delivery.getMember().addDelivery(delivery);
    }

    public Optional<Delivery> findDeliveryById(String deliveryId) {
        for (Delivery delivery : deliveries) {
            if (delivery.getDeliveryId().equals(deliveryId)) {
                return Optional.of(delivery);
            }
        }
        return Optional.empty();
    }

    public int distinctMemberCount() {
        return members.size();
    }

    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    public Map<String, Member> getMembers() {
        return members;
    }
}
