package mu.rekolt.app;

import mu.rekolt.model.Delivery;
import mu.rekolt.service.Grading;
import mu.rekolt.service.PaymentCalculator;
import mu.rekolt.util.ConsoleInput;

import java.util.Scanner;

/**
 * Objective 2: control flow and version control.
 */
public class Main {

    private static final String[] PRODUCE_CODES = {"MZE", "BNS", "POT", "TEA"};
    private static final int MAX_DELIVERIES = 200;

    private static final Delivery[] deliveries = new Delivery[MAX_DELIVERIES];
    private static int deliveryCount = 0;
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
    }

    /**
     * Total payment per member.This loops over every delivery
     * for every distinct member seen - a plain nested-loop aggregation.
     */
    private static void printMemberTotals() {
        System.out.println("Total payment per member (MUR)");

        for (int i = 0; i < deliveryCount; i++) {
            String memberId = deliveries[i].memberId;

            // Skip this member if an earlier delivery already printed their total.
            boolean alreadyPrinted = false;
            for (int j = 0; j < i; j++) {
                if (deliveries[j].memberId.equals(memberId)) {
                    alreadyPrinted = true;
                    break;
                }
            }
            if (alreadyPrinted) {
                continue;
            }

            double total = 0.0;
            String memberName = deliveries[i].memberName;
            for (int j = 0; j < deliveryCount; j++) {
                if (deliveries[j].memberId.equals(memberId)) {
                    total += deliveries[j].netPayable;
                }
            }
            System.out.printf("  %-8s %-18s %,10.2f%n", memberId, memberName, total);
        }
    }

    /** Weekly volume grid (kg): rows are weeks, columns are produce codes. */
    private static void printWeeklyGrid() {
        int maxWeek = 0;
        for (int i = 0; i < deliveryCount; i++) {
            if (deliveries[i].week > maxWeek) {
                maxWeek = deliveries[i].week;
            }
        }

        System.out.println("Weekly volume grid (kg)");
        System.out.printf("  %-5s %8s %8s %8s %8s %10s%n", "Week", "MZE", "BNS", "POT", "TEA", "Total");

        for (int week = 1; week <= maxWeek; week++) {
            double[] columnTotals = new double[PRODUCE_CODES.length];

            for (int col = 0; col < PRODUCE_CODES.length; col++) {
                double sum = 0.0;
                for (int i = 0; i < deliveryCount; i++) {
                    if (deliveries[i].week == week && deliveries[i].produceCode.equals(PRODUCE_CODES[col])) {
                        sum += deliveries[i].massKg;
                    }
                }
                columnTotals[col] = sum;
            }

            double rowTotal = 0.0;
            for (double v : columnTotals) {
                rowTotal += v;
            }

            System.out.printf("  %-5d %8.1f %8.1f %8.1f %8.1f %10.1f%n",
                    week, columnTotals[0], columnTotals[1], columnTotals[2], columnTotals[3], rowTotal);
        }
    }

    // --- Storage helpers ------------------------------------------------

    private static void addDelivery(Delivery d) {
        if (deliveryCount >= deliveries.length) {
            throw new IllegalStateException("Season storage is full.");
        }
        deliveries[deliveryCount++] = d;
    }

    private static String nextDeliveryId() {
        return "D-" + (nextDeliveryNumber++);
    }

    /**deliveries */
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
