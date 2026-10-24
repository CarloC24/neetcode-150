import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

/**
 * Number of Senior Citizens (LeetCode 2678).
 *
 * <p>Each passenger is a 15-character string: a 10-digit phone number, a gender
 * character, a 2-digit age and a 2-digit seat. Return how many passengers are
 * strictly older than 60.
 *
 * <p>The two approaches from
 * https://neetcode.io/solutions/number-of-senior-citizens, plus a direct
 * string comparison, ordered from most to least work per passenger.
 */
public class NumberOfSeniorCitizens {

    // Layout of one passenger string: phone [0, 10), gender 10, age [11, 13), seat [13, 15).
    private static final int AGE_START = 11;
    private static final int AGE_END = 13;

    /**
     * Cuts out the age and converts it with Integer.parseInt.
     * Time: O(n). Space: O(1).
     *
     * <p>n = details.length. Every string has a fixed length of 15, so each one
     * costs O(1). substring allocates a 2-character String per passenger, which
     * is constant extra space.
     */
    static int countSeniorsParse(String[] details) {
        int res = 0;
        for (String d : details) {
            if (Integer.parseInt(d.substring(AGE_START, AGE_END)) > 60) {
                res++;
            }
        }
        return res;
    }

    /**
     * Builds the age from its two digit characters. Time: O(n). Space: O(1).
     *
     * <p>c - '0' turns a digit char into its value, so the age is
     * 10 * tens + ones. No substring, no parsing, no allocation.
     */
    static int countSeniorsDigits(String[] details) {
        int res = 0;
        for (String d : details) {
            int tens = d.charAt(AGE_START) - '0';
            int ones = d.charAt(AGE_START + 1) - '0';
            if (10 * tens + ones > 60) {
                res++;
            }
        }
        return res;
    }

    /**
     * Compares the two age characters with "60" as text.
     * Time: O(n). Space: O(1).
     *
     * <p>The age is always exactly two digits, zero-padded ("07", not "7"). For
     * digit strings of the same length, comparing character by character gives
     * the same order as comparing the numbers, because the chars '0'..'9' are in
     * order. Comparing the tens char first and the ones char second is exactly
     * that comparison, without building a String.
     */
    static int countSeniorsCompare(String[] details) {
        int res = 0;
        for (String d : details) {
            char tens = d.charAt(AGE_START);
            char ones = d.charAt(AGE_START + 1);
            if (tens > '6' || (tens == '6' && ones > '0')) {
                res++;
            }
        }
        return res;
    }

    /** Builds one passenger string, so the test cases read as ages. */
    private static String passenger(int age, int seat, String phone, char gender) {
        return String.format("%s%c%02d%02d", phone, gender, age, seat);
    }

    private static String passenger(int age) {
        return passenger(age, 0, "1234567890", 'M');
    }

    /** One test case: an input and the expected answer. */
    private static class Case {
        final String[] details;
        final int expected;

        Case(String[] details, int expected) {
            this.details = details;
            this.expected = expected;
        }
    }

    public static void main(String[] args) {
        String[] everyAge = IntStream.range(0, 100)
                .mapToObj(NumberOfSeniorCitizens::passenger)
                .toArray(String[]::new);
        String[] allSeniors = IntStream.range(0, 100)
                .mapToObj(seat -> passenger(99, seat, "1234567890", 'M'))
                .toArray(String[]::new);

        Case[] cases = {
            new Case(new String[] {"7868190130M7522", "5303914400F9211", "9273338290F4010"}, 2),
            new Case(new String[] {"1313579440F2036", "2921522980M5644"}, 0),
            new Case(new String[] {passenger(60)}, 0), // exactly 60 is not a senior
            new Case(new String[] {passenger(61)}, 1), // the youngest senior
            new Case(new String[] {passenger(0), passenger(99)}, 1), // age bounds
            new Case(new String[] {passenger(6), passenger(16)}, 0), // a 6 in the wrong place
            new Case(new String[] {passenger(10, 99, "1234567890", 'M')}, 0), // old-looking seat
            new Case(new String[] {passenger(70, 0, "0000000000", 'O')}, 1), // gender 'O'
            new Case(everyAge, 39), // every age once: 61..99
            new Case(allSeniors, 100), // the length ceiling, all seniors
        };

        Map<String, ToIntFunction<String[]>> solutions = new LinkedHashMap<>();
        solutions.put("parse", NumberOfSeniorCitizens::countSeniorsParse);
        solutions.put("digits", NumberOfSeniorCitizens::countSeniorsDigits);
        solutions.put("compare", NumberOfSeniorCitizens::countSeniorsCompare);

        for (Map.Entry<String, ToIntFunction<String[]>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                if (solution.getValue().applyAsInt(testCase.details.clone()) == testCase.expected) {
                    passed++;
                }
            }
            System.out.printf(
                    "%s  %s: %d/%d cases%n",
                    passed == cases.length ? "PASS" : "FAIL",
                    solution.getKey(),
                    passed,
                    cases.length);
        }
    }
}
