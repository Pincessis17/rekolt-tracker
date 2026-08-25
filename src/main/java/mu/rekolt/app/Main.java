package mu.rekolt.app;

import mu.rekolt.model.CashCropProduce;
import mu.rekolt.model.CerealProduce;
import mu.rekolt.model.Delivery;
import mu.rekolt.model.Grade;
import mu.rekolt.model.Member;
import mu.rekolt.model.PerishableProduce;
import mu.rekolt.model.Produce;
import mu.rekolt.model.SeasonStore;
import mu.rekolt.service.PaymentCalculator;
import mu.rekolt.util.ConsoleInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Objective 5: abstraction and inheritance.
 * The four static collections that used to live directly in this class
 * (Objective 3) are now owned by one SeasonStore. The produce price list
 * is a List<Produce> - four objects, two of them CerealProduce, one
 * PerishableProduce, one CashCropProduce - processed everywhere below
 * through Produce's own public methods only. Nothing here ever asks
 * "which subclass is this?" with instanceof, and nothing ever casts one
 * back down to a concrete type.
 */
public class Main {

    private static final List<Produce> PRODUCE_CATALOG = List.of(
            new CerealProduce("MZE", "Maize", 30.0),
            new CerealProduce("BNS", "Beans", 90.0),
            new PerishableProduce("POT", "Potatoes", 45.0),
            new CashCropProduce("TEA", "Tea", 25.0)
    );

    private static final SeasonStore store = new SeasonStore();
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

        Member member = null;
        while (member == null) {
            String memberId = ConsoleInput.readMemberId(sc);
            String memberName = ConsoleInput.readMemberName(sc);
            try {
                member = store.getOrCreateMember(memberId, memberName);
            } catch (IllegalArgumentException e) {
                System.out.println("  " + e.getMessage() + ". Please try again.");
            }
        }

        String produceCode = ConsoleInput.readProduceCode(sc);
        Produce produce = findProduceByCode(produceCode);
        double massKg = ConsoleInput.readMass(sc);
        int qualityScore = ConsoleInput.readIntInRange(sc, "Quality score (0-100) : ", 0, 100);
        int week = ConsoleInput.readIntInRange(sc, "Week of delivery (1-20) : ", 1, 20);

        Delivery delivery = new Delivery(nextDeliveryId(), member, produce, massKg, qualityScore, week);
        store.addDelivery(delivery);

        System.out.println();
        System.out.println("Delivery " + delivery.getDeliveryId() + " recorded. Grade " + delivery.getGrade());
        printReceipt(delivery);
    }

    private static void printReceipt(Delivery d) {
        Produce produce = d.getProduce();
        Grade grade = d.getGrade();

        double baseValue = d.getMassKg() * produce.getBasePricePerKg();
        double afterGrade = baseValue * grade.getMultiplier();
        double afterCategory = afterGrade * produce.categoryMultiplier();
        boolean rejected = grade == Grade.REJECT;
        double commission = rejected ? 0.0 : afterCategory * PaymentCalculator.COMMISSION_RATE;
        double transportLevy = rejected ? 0.0 : d.getMassKg() * PaymentCalculator.TRANSPORT_LEVY_PER_KG;

        System.out.printf("  Base value      %.1f x %.2f = %,.2f%n", d.getMassKg(), produce.getBasePricePerKg(), baseValue);
        System.out.printf("  Grade %-7s x %.2f = %,.2f%n", grade, grade.getMultiplier(), afterGrade);
        System.out.printf("  %-15s x %.2f = %,.2f%n", produce.categoryLabel(), produce.categoryMultiplier(), afterCategory);
        System.out.printf("  Commission 5%%    -        %,.2f%n", commission);
        System.out.printf("  Transport levy  %.1f x %.2f -        %,.2f%n", d.getMassKg(), 2.0, transportLevy);
        System.out.printf("  NET PAYABLE     =        %,.2f MUR%n", d.payableAmount());
    }

    // --- Option 2: season figures on screen ---------------------------

    private static void printSeasonFigures() {
        System.out.println();
        printProduceCatalog();
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

    /**
     * The produce catalog, processed polymorphically: every element is a
     * Produce reference, and every call below (getCode, categoryLabel,
     * getBasePricePerKg, categoryMultiplier) dispatches to whichever
     * subclass that element actually is. No instanceof, no downcasting.
     */
    private static void printProduceCatalog() {
        System.out.println("Produce catalog");
        for (Produce produce : PRODUCE_CATALOG) {
            System.out.printf("  %-4s %-10s %-12s %6.2f MUR/kg  x%.2f%n",
                    produce.getCode(), produce.getLabel(), produce.categoryLabel(),
                    produce.getBasePricePerKg(), produce.categoryMultiplier());
        }
    }

    /** Total payment per member, via each Member's own Payable.payableAmount(). */
    private static void printMemberTotals() {
        System.out.println("Total payment per member (MUR) - " + store.distinctMemberCount() + " member(s) this season");
        for (Member member : store.getMembers().values()) {
            System.out.printf("  %-8s %-18s %,10.2f%n", member.getMemberId(), member.getName(), member.payableAmount());
        }
    }

    /** Weekly volume grid (kg), held as a real 2D array, built with nested loops. */
    private static void printWeeklyGrid() {
        int maxWeek = 0;
        for (Delivery d : store.getDeliveries()) {
            if (d.getWeek() > maxWeek) {
                maxWeek = d.getWeek();
            }
        }

        double[][] grid = new double[maxWeek + 1][PRODUCE_CATALOG.size()];
        for (Delivery d : store.getDeliveries()) {
            int col = indexOfProduceCode(d.getProduce().getCode());
            grid[d.getWeek()][col] += d.getMassKg();
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

    /**
     * Top deliveries by value. REJECT deliveries are worth nothing, so
     * they're removed from this working copy through an Iterator before
     * sorting - they still count towards the weekly grid above, just not
     * this ranking.
     */
    private static void printTopDeliveriesByValue() {
        List<Delivery> workingCopy = new ArrayList<>(store.getDeliveries());

        Iterator<Delivery> it = workingCopy.iterator();
        while (it.hasNext()) {
            if (it.next().getGrade() == Grade.REJECT) {
                it.remove();
            }
        }

        workingCopy.sort(Comparator.comparingDouble(Delivery::payableAmount).reversed());

        System.out.println("Top deliveries by value (REJECT excluded)");
        int limit = Math.min(5, workingCopy.size());
        for (int i = 0; i < limit; i++) {
            Delivery d = workingCopy.get(i);
            System.out.printf("  %d. %-6s %-8s %-3s %6.1f kg %-7s %,10.2f%n",
                    i + 1, d.getDeliveryId(), d.getMember().getMemberId(), d.getProduce().getCode(),
                    d.getMassKg(), d.getGrade(), d.payableAmount());
        }
    }

    /** Same deliveries, in their natural order - Delivery.compareTo, by ID. */
    private static void printDeliveriesInNaturalOrder() {
        List<Delivery> byId = new ArrayList<>(store.getDeliveries());
        Collections.sort(byId);

        System.out.println("Deliveries in ID order");
        for (Delivery d : byId) {
            System.out.printf("  %-6s %-8s %-3s%n", d.getDeliveryId(), d.getMember().getMemberId(), d.getProduce().getCode());
        }
    }

    /** Demonstrates the identifier search, for both a delivery that exists and one that doesn't. */
    private static void printLookupDemo() {
        String knownId = store.getDeliveries().get(0).getDeliveryId();
        String unknownId = "D-9999";

        System.out.println("Delivery lookup demo");
        printLookupResult(knownId);
        printLookupResult(unknownId);
    }

    private static void printLookupResult(String deliveryId) {
        Optional<Delivery> found = store.findDeliveryById(deliveryId);
        if (found.isPresent()) {
            System.out.println("  " + deliveryId + " -> found, member " + found.get().getMember().getMemberId());
        } else {
            System.out.println("  " + deliveryId + " -> not found this season");
        }
    }

    // --- Helpers ------------------------------------------------

    /** Linear search of the produce catalog by code - compares codes only, never branches on subtype. */
    private static Produce findProduceByCode(String code) {
        for (Produce produce : PRODUCE_CATALOG) {
            if (produce.getCode().equals(code)) {
                return produce;
            }
        }
        throw new IllegalArgumentException("Unknown produce code: " + code);
    }

    private static int indexOfProduceCode(String code) {
        for (int i = 0; i < PRODUCE_CATALOG.size(); i++) {
            if (PRODUCE_CATALOG.get(i).getCode().equals(code)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown produce code: " + code);
    }

    private static String nextDeliveryId() {
        return "D-" + (nextDeliveryNumber++);
    }

    private static void seedDelivery(String memberId, String memberName, String produceCode,
                                      double massKg, int qualityScore, int week) {
        Member member = store.getOrCreateMember(memberId, memberName);
        Produce produce = findProduceByCode(produceCode);
        Delivery delivery = new Delivery(nextDeliveryId(), member, produce, massKg, qualityScore, week);
        store.addDelivery(delivery);
    }

    /** A dozen deliveries held in code, per the spec's Objective 2 allowance. */
    private static void seedSeason() {
        seedDelivery("M-0042", "Devi Ramjaun", "BNS", 236.0, 91, 3);
        seedDelivery("M-0117", "Jean Ah-Kine", "MZE", 412.5, 78, 1);
        seedDelivery("M-0088", "Anisha Beeharry", "POT", 150.0, 62, 2);
        seedDelivery("M-0042", "Devi Ramjaun", "TEA", 88.3, 95, 1);
        seedDelivery("M-0117", "Jean Ah-Kine", "BNS", 390.5, 70, 2);
        seedDelivery("M-0088", "Anisha Beeharry", "MZE", 180.0, 85, 2);
        seedDelivery("M-0042", "Devi Ramjaun", "POT", 95.0, 45, 2);   // REJECT
        seedDelivery("M-0117", "Jean Ah-Kine", "TEA", 60.0, 88, 3);
        seedDelivery("M-0088", "Anisha Beeharry", "BNS", 210.0, 73, 3);
        seedDelivery("M-0042", "Devi Ramjaun", "MZE", 300.0, 91, 4);
        seedDelivery("M-0117", "Jean Ah-Kine", "POT", 125.0, 55, 4);
        seedDelivery("M-0088", "Anisha Beeharry", "TEA", 72.4, 60, 4);
    }
}
