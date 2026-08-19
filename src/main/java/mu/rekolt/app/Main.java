package mu.rekolt.app;

import mu.rekolt.model.Delivery;
import mu.rekolt.service.Grading;
import mu.rekolt.service.PaymentCalculator;
import mu.rekolt.util.ConsoleInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

/**
 * Objective 3: collections.
 * The season's deliveries now live in a generic ArrayList instead of the
 * fixed-capacity array from Objective 2. Three more collections track
 * derived information as deliveries are added: a HashMap of running
 * totals per member, a HashMap of each member's own delivery list, and
 * a HashSet of the distinct member identifiers seen this season - all
 * kept in step inside addDelivery so nothing downstream has to rebuild
 * them from scratch.
 */
public class Main {

    private static final String[] PRODUCE_CODES = {"MZE", "BNS", "POT", "TEA"};

    private static final List<Delivery> deliveries = new ArrayList<>();
    private static final Map<String, Double> totalPaymentByMember = new HashMap<>();
    private static final Map<String, List<Delivery>> deliveriesByMember = new HashMap<>();
    private static final Set<String> memberIds = new HashSet<>();

    private static int nextDeliveryNumber = 1000;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        seedSeason();

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("REKOLT PRODUCE TRACKER - season 2026");
            System.out.println("1. Record a delivery");
            System.out.println("2. Season figures on screen");
            System.out.println("3. Exit");
            System.out.println();

            int choice = ConsoleInput.readIntInRange(sc, "Choose an option: ", 1, 3);

            switch (choice) {
                case 1:
                    recordDelivery(sc);
                    break;
                case 2:
                    printSeasonFigures();
                    break;
                case 3:
                    running = false;
                    System.out.println("Goodbye.");
                    break;
            }
        }

        sc.close();
    }

    // --- Option 1: record a delivery ---------------------------------

    private static void recordDelivery(Scanner sc) {
        System.out.println();
        String memberId = ConsoleInput.readMemberId(sc);
        String memberName = ConsoleInput.readMemberName(sc);
        String produceCode = ConsoleInput.readProduceCode(sc);
        double massKg = ConsoleInput.readMass(sc);
        int qualityScore = ConsoleInput.readIntInRange(sc, "Quality score (0-100) : ", 0, 100);
        int week = ConsoleInput.readIntInRange(sc, "Week of delivery (1-20) : ", 1, 20);

        Delivery delivery = new Delivery(nextDeliveryId(), memberId, memberName,
                produceCode, massKg, qualityScore, week);
        addDelivery(delivery);

        System.out.println();
        System.out.println("Delivery " + delivery.deliveryId + " recorded. Grade " + delivery.grade);
        printReceipt(delivery);
    }

    private static void printReceipt(Delivery d) {
        double basePrice = PaymentCalculator.basePriceFor(d.produceCode);
        double categoryMultiplier = PaymentCalculator.categoryMultiplierFor(d.produceCode);
        String categoryLabel = PaymentCalculator.categoryLabelFor(d.produceCode);

        double baseValue = d.massKg * basePrice;
        double gradeMultiplier = Grading.gradeMultiplier(d.grade);
        double afterGrade = baseValue * gradeMultiplier;
        double afterCategory = afterGrade * categoryMultiplier;
        double commission = d.grade.equals("REJECT") ? 0.0 : afterCategory * 0.05;
        double transportLevy = d.grade.equals("REJECT") ? 0.0 : d.massKg * 2.0;

        System.out.printf("  Base value      %.1f x %.2f = %,.2f%n", d.massKg, basePrice, baseValue);
        System.out.printf("  Grade %-7s x %.2f = %,.2f%n", d.grade, gradeMultiplier, afterGrade);
        System.out.printf("  %-15s x %.2f = %,.2f%n", categoryLabel, categoryMultiplier, afterCategory);
        System.out.printf("  Commission 5%%    -        %,.2f%n", commission);
        System.out.printf("  Transport levy  %.1f x %.2f -        %,.2f%n", d.massKg, 2.0, transportLevy);
        System.out.printf("  NET PAYABLE     =        %,.2f MUR%n", d.netPayable);
    }

    // --- Option 2: season figures on screen ---------------------------

    private static void printSeasonFigures() {
        System.out.println();
        printMemberTotals();
        System.out.println();
        printWeeklyGrid();
        System.out.println();
        printTopDeliveriesByValue();
        System.out.println();
        printDeliveriesInNaturalOrder();
        System.out.println();
        printLookupDemo();
    }

    /** Total payment per member, read straight from the HashMap kept up to date in addDelivery. */
    private static void printMemberTotals() {
        System.out.println("Total payment per member (MUR) - " + memberIds.size() + " member(s) this season");
        for (String memberId : memberIds) {
            List<Delivery> theirDeliveries = deliveriesByMember.get(memberId);
            String memberName = theirDeliveries.get(0).memberName;
            double total = totalPaymentByMember.get(memberId);
            System.out.printf("  %-8s %-18s %,10.2f%n", memberId, memberName, total);
        }
    }

    /** Weekly volume grid (kg), held as a real 2D array, built with nested loops. */
    private static void printWeeklyGrid() {
        int maxWeek = 0;
        for (Delivery d : deliveries) {
            if (d.week > maxWeek) {
                maxWeek = d.week;
            }
        }

        double[][] grid = new double[maxWeek + 1][PRODUCE_CODES.length];
        for (Delivery d : deliveries) {
            int col = indexOfProduceCode(d.produceCode);
            grid[d.week][col] += d.massKg;
        }

        System.out.println("Weekly volume grid (kg)");
        System.out.printf("  %-5s %8s %8s %8s %8s %10s%n", "Week", "MZE", "BNS", "POT", "TEA", "Total");
        for (int week = 1; week <= maxWeek; week++) {
            double rowTotal = 0.0;
            for (double v : grid[week]) {
                rowTotal += v;
            }
            System.out.printf("  %-5d %8.1f %8.1f %8.1f %8.1f %10.1f%n",
                    week, grid[week][0], grid[week][1], grid[week][2], grid[week][3], rowTotal);
        }
    }

    private static int indexOfProduceCode(String produceCode) {
        for (int i = 0; i < PRODUCE_CODES.length; i++) {
            if (PRODUCE_CODES[i].equals(produceCode)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown produce code: " + produceCode);
    }

    /**
     * Top deliveries by value. REJECT deliveries are worth nothing, so
     * they're removed from this working copy through an Iterator before
     * sorting - they still count towards the weekly grid above, just not
     * this ranking.
     */
    private static void printTopDeliveriesByValue() {
        List<Delivery> workingCopy = new ArrayList<>(deliveries);

        Iterator<Delivery> it = workingCopy.iterator();
        while (it.hasNext()) {
            if (it.next().grade.equals("REJECT")) {
                it.remove();
            }
        }

        workingCopy.sort(Comparator.comparingDouble((Delivery d) -> d.netPayable).reversed());

        System.out.println("Top deliveries by value (REJECT excluded)");
        int limit = Math.min(5, workingCopy.size());
        for (int i = 0; i < limit; i++) {
            Delivery d = workingCopy.get(i);
            System.out.printf("  %d. %-6s %-8s %-3s %6.1f kg %-7s %,10.2f%n",
                    i + 1, d.deliveryId, d.memberId, d.produceCode, d.massKg, d.grade, d.netPayable);
        }
    }

    /** Same deliveries, in their natural order - Delivery.compareTo, by ID. */
    private static void printDeliveriesInNaturalOrder() {
        List<Delivery> byId = new ArrayList<>(deliveries);
        Collections.sort(byId);

        System.out.println("Deliveries in ID order");
        for (Delivery d : byId) {
            System.out.printf("  %-6s %-8s %-3s%n", d.deliveryId, d.memberId, d.produceCode);
        }
    }

    /** Demonstrates the identifier search, for both a delivery that exists and one that doesn't. */
    private static void printLookupDemo() {
        String knownId = deliveries.get(0).deliveryId;
        String unknownId = "D-9999";

        System.out.println("Delivery lookup demo");
        printLookupResult(knownId);
        printLookupResult(unknownId);
    }

    private static void printLookupResult(String deliveryId) {
        Optional<Delivery> found = findDeliveryById(deliveryId);
        if (found.isPresent()) {
            System.out.println("  " + deliveryId + " -> found, member " + found.get().memberId);
        } else {
            System.out.println("  " + deliveryId + " -> not found this season");
        }
    }

    /** Linear search by delivery ID; returns empty rather than null or throwing when absent. */
    private static Optional<Delivery> findDeliveryById(String deliveryId) {
        for (Delivery d : deliveries) {
            if (d.deliveryId.equals(deliveryId)) {
                return Optional.of(d);
            }
        }
        return Optional.empty();
    }

    // --- Storage helpers ------------------------------------------------

    private static void addDelivery(Delivery d) {
        deliveries.add(d);
        memberIds.add(d.memberId);
        deliveriesByMember.computeIfAbsent(d.memberId, k -> new ArrayList<>()).add(d);
        totalPaymentByMember.merge(d.memberId, d.netPayable, Double::sum);
    }

    private static String nextDeliveryId() {
        return "D-" + (nextDeliveryNumber++);
    }

    /** A dozen or so deliveries held in code, per the spec's Objective 2 allowance. */
    private static void seedSeason() {
        addDelivery(new Delivery(nextDeliveryId(), "M-0042", "Devi Ramjaun", "BNS", 236.0, 91, 3));
        addDelivery(new Delivery(nextDeliveryId(), "M-0117", "Jean Ah-Kine", "MZE", 412.5, 78, 1));
        addDelivery(new Delivery(nextDeliveryId(), "M-0088", "Anisha Beeharry", "POT", 150.0, 62, 2));
        addDelivery(new Delivery(nextDeliveryId(), "M-0042", "Devi Ramjaun", "TEA", 88.3, 95, 1));
        addDelivery(new Delivery(nextDeliveryId(), "M-0117", "Jean Ah-Kine", "BNS", 390.5, 70, 2));
        addDelivery(new Delivery(nextDeliveryId(), "M-0088", "Anisha Beeharry", "MZE", 180.0, 85, 2));
        addDelivery(new Delivery(nextDeliveryId(), "M-0042", "Devi Ramjaun", "POT", 95.0, 45, 2));   // REJECT
        addDelivery(new Delivery(nextDeliveryId(), "M-0117", "Jean Ah-Kine", "TEA", 60.0, 88, 3));
        addDelivery(new Delivery(nextDeliveryId(), "M-0088", "Anisha Beeharry", "BNS", 210.0, 73, 3));
        addDelivery(new Delivery(nextDeliveryId(), "M-0042", "Devi Ramjaun", "MZE", 300.0, 91, 4));
        addDelivery(new Delivery(nextDeliveryId(), "M-0117", "Jean Ah-Kine", "POT", 125.0, 55, 4));
        addDelivery(new Delivery(nextDeliveryId(), "M-0088", "Anisha Beeharry", "TEA", 72.4, 60, 4));
    }
}
