import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * Find Pivot Index (LeetCode 724).
 *
 * <p>Return the leftmost index where the sum of the numbers strictly to its
 * left equals the sum of the numbers strictly to its right, or -1 if there is
 * none.
 *
 * <p>The three approaches from https://neetcode.io/solutions/find-pivot-index,
 * ordered from brute force to optimal.
 */
public class FindPivotIndex {

    /**
     * Sums both sides from scratch at every index. Time: O(n^2). Space: O(1).
     *
     * <p>Index i costs O(n) to sum nums[0..i) and nums(i..n), so n indexes cost
     * O(n^2). Scanning left to right returns the leftmost pivot.
     */
    static int pivotIndexBruteForce(int[] nums) {
        int n = nums.length;
        for (int i = 0; i < n; i++) {
            int leftSum = 0;
            int rightSum = 0;
            for (int l = 0; l < i; l++) {
                leftSum += nums[l];
            }
            for (int r = i + 1; r < n; r++) {
                rightSum += nums[r];
            }
            if (leftSum == rightSum) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Precomputes prefix sums, then reads both sides in O(1).
     * Time: O(n). Space: O(n).
     *
     * <p>prefix[i] is the sum of nums[0..i), so prefix[0] = 0 and prefix[n] is
     * the total. The left side of i is prefix[i], and the right side is
     * everything after nums[i]: prefix[n] - prefix[i + 1]. The extra slot at the
     * front means index 0 needs no special case.
     */
    static int pivotIndexPrefixArray(int[] nums) {
        int n = nums.length;
        int[] prefix = new int[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + nums[i];
        }

        for (int i = 0; i < n; i++) {
            int leftSum = prefix[i];
            int rightSum = prefix[n] - prefix[i + 1];
            if (leftSum == rightSum) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Keeps a running left sum and derives the right sum from the total.
     * Time: O(n). Space: O(1).
     *
     * <p>The optimal solution. The array splits into left + nums[i] + right =
     * total, so right = total - left - nums[i]. Only the left sum needs
     * tracking, and the prefix array from approach 2 disappears.
     *
     * <p>Compares first, then adds nums[i] to the left sum. Adding first would
     * put nums[i] on its own left side. The sums stay within 10^4 * 10^3 =
     * 10^7 in absolute value, far inside int range.
     */
    static int pivotIndexRunningSum(int[] nums) {
        int total = 0;
        for (int num : nums) {
            total += num;
        }

        int leftSum = 0;
        for (int i = 0; i < nums.length; i++) {
            int rightSum = total - leftSum - nums[i];
            if (leftSum == rightSum) {
                return i;
            }
            leftSum += nums[i];
        }
        return -1;
    }

    /** One test case: an input and the expected answer. */
    private static class Case {
        final int[] nums;
        final int expected;

        Case(int[] nums, int expected) {
            this.nums = nums;
            this.expected = expected;
        }
    }

    private static int[] filled(int value, int length) {
        int[] nums = new int[length];
        Arrays.fill(nums, value);
        return nums;
    }

    public static void main(String[] args) {
        Case[] cases = {
            new Case(new int[] {1, 7, 3, 6, 5, 6}, 3),
            new Case(new int[] {3, 2, 1}, -1),
            new Case(new int[] {2, 1, -1}, 0), // pivot at 0: the empty left side sums to 0
            new Case(new int[] {1, -1, 5}, 2), // pivot at the last index
            new Case(new int[] {5}, 0), // the smallest input: both sides are empty
            new Case(new int[] {0, 0, 0}, 0), // every index is a pivot; return the leftmost
            new Case(new int[] {2, -3, 4, -3, 2}, 0), // pivots at 0, 2 and 4
            new Case(new int[] {1, 0, 1, 0}, 1),
            new Case(new int[] {-1, -1, 0, 1, 1, 0}, 5), // sums aren't monotone
            new Case(new int[] {-1, -1, -1, -1, -1, 0}, 2), // all non-positive
            new Case(filled(1000, 1_001), 500), // long and odd: the middle balances
            new Case(filled(1000, 1_000), -1), // even: nothing balances
            new Case(filled(-1000, 1_001), 500), // value floor
        };

        Map<String, ToIntFunction<int[]>> solutions = new LinkedHashMap<>();
        solutions.put("brute force", FindPivotIndex::pivotIndexBruteForce);
        solutions.put("prefix array", FindPivotIndex::pivotIndexPrefixArray);
        solutions.put("running sum", FindPivotIndex::pivotIndexRunningSum);

        for (Map.Entry<String, ToIntFunction<int[]>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                if (solution.getValue().applyAsInt(testCase.nums.clone()) == testCase.expected) {
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

        int[] original = {1, 7, 3, 6, 5, 6};
        for (ToIntFunction<int[]> solve : solutions.values()) {
            solve.applyAsInt(original);
        }
        System.out.printf(
                "%s  input left unmodified: %s%n",
                Arrays.equals(original, new int[] {1, 7, 3, 6, 5, 6}) ? "PASS" : "FAIL",
                Arrays.toString(original));
    }
}
