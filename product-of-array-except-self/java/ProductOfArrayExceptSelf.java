import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Product of Array Except Self (LeetCode 238).
 *
 * <p>Given an integer array nums, return an array answer where answer[i] is
 * the product of every element of nums except nums[i]. Every prefix and suffix
 * product fits in a 32-bit integer. Follow-up: O(n) time without division.
 *
 * <p>The four approaches from
 * https://neetcode.io/solutions/product-of-array-except-self, ordered from
 * brute force to one output array and two passes.
 */
public class ProductOfArrayExceptSelf {

    /**
     * Multiplies everything else, for each index. Time: O(n^2). Space: O(1).
     *
     * <p>Space here and below doesn't count the output array. Too slow for
     * n = 100,000 (10^10 multiplications), so the harness skips the long cases
     * for this one.
     */
    static int[] productExceptSelfBruteForce(int[] nums) {
        int n = nums.length;
        int[] res = new int[n];
        for (int i = 0; i < n; i++) {
            int prod = 1;
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    prod *= nums[j];
                }
            }
            res[i] = prod;
        }
        return res;
    }

    /**
     * Total product divided by each element, zeros counted apart.
     * Time: O(n). Space: O(1).
     *
     * <p>The follow-up rules out division, but it's the first idea most people
     * have, and the zeros are the whole difficulty: with no zeros, answer[i] =
     * prod / nums[i]. With one zero, every answer is 0 except at the zero,
     * which gets prod. With two or more, every answer is 0.
     *
     * <p>prod is the product of the non-zero elements only, so it never becomes
     * 0 and loses the information. It fits in an int: with no zeros it's the
     * prefix product of the whole array, and with one zero it's that index's
     * answer (prefix times suffix). Dividing it by each element is exact.
     */
    static int[] productExceptSelfDivision(int[] nums) {
        int prod = 1;
        int zeroCount = 0;
        for (int num : nums) {
            if (num != 0) {
                prod *= num;
            } else {
                zeroCount++;
            }
        }

        int[] res = new int[nums.length]; // all 0s
        if (zeroCount > 1) {
            return res;
        }
        for (int i = 0; i < nums.length; i++) {
            if (zeroCount == 1) {
                res[i] = nums[i] == 0 ? prod : 0;
            } else {
                res[i] = prod / nums[i];
            }
        }
        return res;
    }

    /**
     * Prefix and suffix product arrays. Time: O(n). Space: O(n).
     *
     * <p>Everything except nums[i] is everything to its left times everything
     * to its right. pref[i] = nums[0] * ... * nums[i - 1] (pref[0] = 1),
     * suff[i] = nums[i + 1] * ... * nums[n - 1] (suff[n - 1] = 1), and
     * answer[i] = pref[i] * suff[i].
     *
     * <p>No division, so zeros need no special case. Both arrays leave nums[i]
     * out: pref[i] is built from nums[i - 1], not nums[i].
     */
    static int[] productExceptSelfPrefixSuffix(int[] nums) {
        int n = nums.length;
        int[] pref = new int[n];
        int[] suff = new int[n];
        pref[0] = 1;
        suff[n - 1] = 1;
        for (int i = 1; i < n; i++) {
            pref[i] = pref[i - 1] * nums[i - 1];
        }
        for (int i = n - 2; i >= 0; i--) {
            suff[i] = suff[i + 1] * nums[i + 1];
        }
        int[] res = new int[n];
        for (int i = 0; i < n; i++) {
            res[i] = pref[i] * suff[i];
        }
        return res;
    }

    /**
     * Prefix products in the output, suffix as a running value.
     * Time: O(n). Space: O(1).
     *
     * <p>The same idea as approach 3 without the two extra arrays. The first
     * pass writes each prefix product into res. The second pass walks right to
     * left with suffix, the product of everything already passed, and
     * multiplies it in. In both passes, use the running value first and fold
     * nums[i] in after: that's what leaves nums[i] out of its own answer.
     */
    static int[] productExceptSelfOptimal(int[] nums) {
        int n = nums.length;
        int[] res = new int[n];

        int prefix = 1;
        for (int i = 0; i < n; i++) {
            res[i] = prefix; // product of nums[0 .. i - 1]
            prefix *= nums[i];
        }

        int suffix = 1;
        for (int i = n - 1; i >= 0; i--) {
            res[i] *= suffix; // times the product of nums[i + 1 .. n - 1]
            suffix *= nums[i];
        }
        return res;
    }

    /** The answer straight from the definition, in long, for the short cases. */
    private static int[] byDefinition(int[] nums) {
        int[] res = new int[nums.length];
        for (int i = 0; i < nums.length; i++) {
            long prod = 1;
            for (int j = 0; j < nums.length; j++) {
                if (j != i) {
                    prod *= nums[j];
                }
            }
            res[i] = Math.toIntExact(prod);
        }
        return res;
    }

    public static void main(String[] args) {
        int[][] shortCases = {
            {1, 2, 3, 4},
            {-1, 1, 0, -3, 3},
            {1, 2, 4, 6}, // NeetCode example 1
            {-1, 0, 1, 2, 3}, // NeetCode example 2
            {2, 3}, // the smallest input
            {0, 5}, // two elements, one zero
            {0, 0}, // two elements, both zero
            {4, 0, 5, 0, 6}, // two zeros: every answer is 0
            {0, 2, 3}, // zero at the front
            {2, 3, 0}, // zero at the back
            {-2, -3, -4}, // all negative: signs flip per answer
            {30, -30, 30, -30, 30}, // value limits; the answers reach 30^4
        };

        // Long cases with answers worked out by hand, since the definition is O(n^2).
        final int n = 100_000;
        int[] alternating = new int[n];
        for (int i = 0; i < n; i++) {
            alternating[i] = i % 2 == 0 ? -1 : 1;
        }
        int[] withZero = alternating.clone();
        withZero[n / 2] = 0;
        int[] withZeroAnswer = new int[n];
        withZeroAnswer[n / 2] = -1;
        int[] ones = new int[n];
        Arrays.fill(ones, 1);

        int[][][] longCases = {
            // product of everything is 1, and 1 / x == x for x = +-1
            {alternating, alternating},
            // one zero, replacing a -1: 0 everywhere else; at the zero, the
            // other 49,999 -1s multiply to -1
            {withZero, withZeroAnswer},
            {ones, ones},
        };

        int[][][] cases = new int[shortCases.length + longCases.length][][];
        for (int i = 0; i < shortCases.length; i++) {
            cases[i] = new int[][] {shortCases[i], byDefinition(shortCases[i])};
        }
        System.arraycopy(longCases, 0, cases, shortCases.length, longCases.length);

        Map<String, UnaryOperator<int[]>> solutions = new LinkedHashMap<>();
        solutions.put("brute force", ProductOfArrayExceptSelf::productExceptSelfBruteForce);
        solutions.put("division", ProductOfArrayExceptSelf::productExceptSelfDivision);
        solutions.put("prefix & suffix", ProductOfArrayExceptSelf::productExceptSelfPrefixSuffix);
        solutions.put("prefix & suffix, O(1)", ProductOfArrayExceptSelf::productExceptSelfOptimal);

        for (Map.Entry<String, UnaryOperator<int[]>> solution : solutions.entrySet()) {
            boolean runsLong = !solution.getKey().equals("brute force");
            int total = runsLong ? cases.length : shortCases.length;
            int passed = 0;
            for (int c = 0; c < total; c++) {
                int[] nums = cases[c][0].clone();
                int[] result = solution.getValue().apply(nums);
                if (Arrays.equals(result, cases[c][1]) && Arrays.equals(nums, cases[c][0])) {
                    passed++;
                }
            }
            System.out.printf(
                    "%s  %s: %d/%d cases%s%n",
                    passed == total ? "PASS" : "FAIL",
                    solution.getKey(),
                    passed,
                    total,
                    runsLong ? "" : " (skips the " + longCases.length + " long cases)");
        }
    }
}
