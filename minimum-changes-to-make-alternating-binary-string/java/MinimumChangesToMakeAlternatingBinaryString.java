import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * Minimum Changes To Make Alternating Binary String (LeetCode 1758).
 *
 * <p>Given a string s of '0's and '1's, one operation flips a single
 * character. Return the fewest operations that make s alternating, meaning no
 * two adjacent characters are equal.
 *
 * <p>The two approaches from
 * https://neetcode.io/solutions/minimum-changes-to-make-alternating-binary-string,
 * ordered from two passes to one.
 */
public class MinimumChangesToMakeAlternatingBinaryString {

    /**
     * Counts mismatches against "0101..." and "1010...".
     * Time: O(n). Space: O(1).
     *
     * <p>An alternating string is fixed by its first character, so there are
     * only two candidates. Each character that differs from a candidate costs
     * one flip. Walks the string once per candidate, toggling the expected bit
     * with cur ^= 1, and keeps the cheaper count. charAt avoids the char[] copy
     * that toCharArray would make.
     */
    static int minOperationsTwoPatterns(String s) {
        int cur = 0;
        int cnt1 = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) - '0' != cur) {
                cnt1++;
            }
            cur ^= 1;
        }

        cur = 1;
        int cnt2 = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) - '0' != cur) {
                cnt2++;
            }
            cur ^= 1;
        }

        return Math.min(cnt1, cnt2);
    }

    /**
     * Counts mismatches against one pattern; the other is n minus that.
     * Time: O(n). Space: O(1).
     *
     * <p>The optimal solution. The two candidates differ at every index, so
     * each character matches exactly one of them. A character that mismatches
     * "1010..." matches "0101...", and the other way round, so the two counts
     * always add up to n.
     *
     * <p>"1010..." expects '1' at even indices and '0' at odd ones.
     */
    static int minOperationsComplement(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (i % 2 == 0) {
                if (s.charAt(i) == '0') {
                    count++;
                }
            } else {
                if (s.charAt(i) == '1') {
                    count++;
                }
            }
        }
        return Math.min(count, s.length() - count);
    }

    /** One test case: an input and the expected answer. */
    private static class Case {
        final String s;
        final int expected;

        Case(String s, int expected) {
            this.s = s;
            this.expected = expected;
        }
    }

    public static void main(String[] args) {
        Case[] cases = {
            new Case("0100", 1),
            new Case("10", 0),
            new Case("1111", 2),
            new Case("0", 0), // the smallest input: one character is already alternating
            new Case("1", 0),
            new Case("00", 1),
            new Case("0101", 0), // already alternating, both ways round
            new Case("1010", 0),
            new Case("111", 1), // odd length: "101" needs one flip, "010" needs two
            new Case("000", 1),
            new Case("00101", 1), // flip s[0]; fixing clashes left to right takes 4
            new Case("0110", 2), // both patterns tie
            new Case("0".repeat(10_000), 5_000), // length ceiling
            new Case("01".repeat(5_000), 0),
            new Case("1".repeat(9_999), 4_999), // odd ceiling: "1010...1" keeps the extra 1
        };

        Map<String, ToIntFunction<String>> solutions = new LinkedHashMap<>();
        solutions.put("two patterns", MinimumChangesToMakeAlternatingBinaryString::minOperationsTwoPatterns);
        solutions.put("complement", MinimumChangesToMakeAlternatingBinaryString::minOperationsComplement);

        for (Map.Entry<String, ToIntFunction<String>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                if (solution.getValue().applyAsInt(testCase.s) == testCase.expected) {
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
