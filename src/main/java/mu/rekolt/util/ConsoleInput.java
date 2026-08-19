package mu.rekolt.util;

import java.util.Scanner;

/**
 * Objective 2: every prompt here loops until the user enters something
 * acceptable. A bad entry prints a short reason and asks again; nothing
 * in this class ever lets bad input crash the program with a stack trace.
 */
public class ConsoleInput {

    private static final String MEMBER_ID_PATTERN = "M-\\d{4}";
    private static final String[] PRODUCE_CODES = {"MZE", "BNS", "POT", "TEA"};

    /** Generic validated whole-number prompt, reused for the menu, quality score and week. */
    public static int readIntInRange(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value < min || value > max) {
                    System.out.println("  Enter a whole number from " + min + " to " + max + ". Please try again.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("  That is not a whole number. Please try again.");
            }
        }
    }

    public static double readMass(Scanner sc) {
        while (true) {
            System.out.print("Mass in kg : ");
            String line = sc.nextLine().trim();
            try {
                double value = Double.parseDouble(line);
                if (value <= 0 || value > 5000) {
                    System.out.println("  Mass must be above 0 and not more than 5000. Please try again.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("  That is not a number. Please try again.");
            }
        }
    }

    public static String readMemberId(Scanner sc) {
        while (true) {
            System.out.print("Member identifier : ");
            String line = sc.nextLine().trim();
            if (line.matches(MEMBER_ID_PATTERN)) {
                return line;
            }
            System.out.println("  Use the format M-0042 (the letter M, a hyphen, four digits). Please try again.");
        }
    }

    public static String readMemberName(Scanner sc) {
        while (true) {
            System.out.print("Member name : ");
            String line = sc.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("  Member name cannot be empty. Please try again.");
        }
    }

    public static String readProduceCode(Scanner sc) {
        while (true) {
            System.out.print("Produce code (MZE/BNS/POT/TEA) : ");
            String line = sc.nextLine().trim().toUpperCase();
            for (String code : PRODUCE_CODES) {
                if (code.equals(line)) {
                    return line;
                }
            }
            System.out.println("  Enter MZE, BNS, POT or TEA. Please try again.");
        }
    }
}
