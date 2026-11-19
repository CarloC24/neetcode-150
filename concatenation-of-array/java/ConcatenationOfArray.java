import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * Concatenation of Array (LeetCode 1929).
 *
 * <p>Given an integer array {@code nums} of length n, return an array
 * {@code ans} of length 2n where ans[i] == nums[i] and ans[i + n] == nums[i]
 * for 0 <= i < n. In other words, {@code nums} followed by itself.
 *
 * <p>The two approaches from https://neetcode.io/solutions/concatenation-of-array,
 * plus the built-in one, ordered from most to least code.
 */
public class ConcatenationOfArray {

    /**
     * Appends every element, twice over, then copies into an int[].
     * Time: O(n). Space: O(n) for the output.
     *
     * <p>Java arrays can't grow, so "append" means an ArrayList, which boxes
     * every int and needs a final copy back into an int[]. That's the cost of
     * not knowing the size up front, even though here we do know it.
     */
    static int[] getConcatenationAppend(int[] nums) {
        List<Integer> ans = new ArrayList<>();
        for (int pass = 0; pass < 2; pass++) {
            for (int num : nums) {
                ans.add(num);
            }
        }
        int[] res = new int[ans.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = ans.get(i);
        }
        return res;
    }

    /**
     * Allocates 2n slots and fills both halves in one pass.
     * Time: O(n). Space: O(n) for the output.
     *
     * <p>ans[i] and ans[i + n] both get nums[i]. No boxing, no resizing.
     */
    static int[] getConcatenationIndex(int[] nums) {
        int n = nums.length;
        int[] ans = new int[2 * n];
        for (int i = 0; i < n; i++) {
            ans[i] = nums[i];
            ans[i + n] = nums[i];
        }
        return ans;
    }

    /**
     * Arrays.copyOf for the first half, System.arraycopy for the second.
     * Time: O(n). Space: O(n) for the output.
     *
     * <p>copyOf(nums, 2n) copies nums and pads the rest with zeros, then
     * arraycopy overwrites those zeros with a second copy. Both are bulk
     * memory copies, so this is the fastest version in practice.
     */
    static int[] getConcatenationBuiltin(int[] nums) {
        int n = nums.length;
        int[] ans = Arrays.copyOf(nums, 2 * n);
        System.arraycopy(nums, 0, ans, n, n);
        return ans;
    }

    /** One test case: an input and the expected answer. */
    private static class Case {
        final int[] nums;
        final int[] expected;

        Case(int[] nums, int[] expected) {
            this.nums = nums;
            this.expected = expected;
        }
    }

    public static void main(String[] args) {
        int[] ceiling = IntStream.rangeClosed(1, 1000).toArray();
        int[] ceilingTwice = IntStream.range(0, 2000).map(i -> i % 1000 + 1).toArray();

        Case[] cases = {
            new Case(new int[] {1, 2, 1}, new int[] {1, 2, 1, 1, 2, 1}),
            new Case(new int[] {1, 3, 2, 1}, new int[] {1, 3, 2, 1, 1, 3, 2, 1}),
            new Case(new int[] {7}, new int[] {7, 7}), // n = 1, the smallest input
            new Case(new int[] {5, 5, 5}, new int[] {5, 5, 5, 5, 5, 5}), // all equal
            new Case(new int[] {1, 1000}, new int[] {1, 1000, 1, 1000}), // value bounds
            new Case(new int[] {3, 2, 1}, new int[] {3, 2, 1, 3, 2, 1}), // not sorted or reversed
            new Case(ceiling, ceilingTwice), // n = 1000, the length ceiling
        };

        Map<String, UnaryOperator<int[]>> solutions = new LinkedHashMap<>();
        solutions.put("append", ConcatenationOfArray::getConcatenationAppend);
        solutions.put("index", ConcatenationOfArray::getConcatenationIndex);
        solutions.put("builtin", ConcatenationOfArray::getConcatenationBuiltin);

        for (Map.Entry<String, UnaryOperator<int[]>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                if (Arrays.equals(solution.getValue().apply(testCase.nums.clone()), testCase.expected)) {
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

        // No approach may change its input or hand it back as the answer.
        int[] nums = {1, 2, 3};
        boolean ok = true;
        for (UnaryOperator<int[]> solve : solutions.values()) {
            int[] ans = solve.apply(nums);
            ok = ok && Arrays.equals(nums, new int[] {1, 2, 3}) && ans != nums;
        }
        System.out.printf("%s  input left unmodified: %s%n", ok ? "PASS" : "FAIL", Arrays.toString(nums));
    }
}
