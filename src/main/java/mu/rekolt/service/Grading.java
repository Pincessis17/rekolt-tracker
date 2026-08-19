package mu.rekolt.service;

/**
 * Objective 2 asks for the quality score to be graded with both an
 * if / else-if chain AND a switch. Here, the if / else-if chain finds
 * which boundary the score falls into (rule 2), and the switch turns
 * that letter into its multiplier - two different control-flow tools
 * doing two different jobs in the same grading process.
 */
public class Grading {

    public static String gradeLetter(int qualityScore) {
        if (qualityScore >= 85) {
            return "A";
        } else if (qualityScore >= 70) {
            return "B";
        } else if (qualityScore >= 50) {
            return "C";
        } else {
            return "REJECT";
        }
    }

    public static double gradeMultiplier(String grade) {
        switch (grade) {
            case "A":
                return 1.15;
            case "B":
                return 1.00;
            case "C":
                return 0.85;
            case "REJECT":
                return 0.00;
            default:
                throw new IllegalArgumentException("Unknown grade: " + grade);
        }
    }
}
