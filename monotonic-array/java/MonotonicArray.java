import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Monotonic Array (LeetCode 896).
 *
 * <p>An array is monotonic if it is either monotone increasing (never goes
 * down) or monotone decreasing (never goes up). Equal neighbours are allowed in
 * both. Return whether nums is monotonic.
 *
 * <p>The three approaches from https://neetcode.io/solutions/monotonic-array,
 * plus a sorting baseline, ordered from brute force to optimal and then the
 * variant.
 */
public class MonotonicArray {

    /**
     * Compares against the sorted order, forwards and backwards.
     * Time: O(n log n). Space: O(n).
     *
     * <p>A monotone increasing array is already in sorted order, and a monotone
     * decreasing one is in reverse sorted order, so this is the definition
     * checked directly. Arrays.sort on an int[] has no reverse-order overload,
     * so the decreasing check reads the sorted copy from the back instead.
     */
    static boolean isMonotonicSorting(int[] nums) {
        int[] sorted = nums.clone();
        Arrays.sort(sorted);
        if (Arrays.equals(nums, sorted)) {
            return true;
        }
        int n = nums.length;
        for (int i = 0; i < n; i++) {
            if (nums[i] != sorted[n - 1 - i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks never-goes-down, then never-goes-up. Time: O(n). Space: O(1).
     *
     * <p>The definition compares every pair i &lt;= j, but adjacent pairs are
     * enough: if each step is &lt;=, the whole chain is, by transitivity. So
     * each direction is one linear scan that stops at the first step going the
     * wrong way. An array that fails the first check is scanned a second time.
     */
    static boolean isMonotonicTwoPass(int[] nums) {
        boolean increasing = true;
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] < nums[i - 1]) {
                increasing = false;
                break;
            }
        }
        if (increasing) {
            return true;
        }

        for (int i = 1; i < nums.length; i++) {
            if (nums[i] > nums[i - 1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Picks the only possible direction from the ends, then checks it.
     * Time: O(n). Space: O(1).
     *
     * <p>The optimal solution. The first and last values decide which direction
     * is even possible. If nums[0] &lt; nums[n - 1], the array can't be
     * decreasing. If nums[0] &gt; nums[n - 1], it can't be increasing. If they
     * are equal, a monotonic array must be constant, which also counts as
     * increasing. So one scan, checking one direction, is enough, and it stops
     * at the first bad step.
     */
    static boolean isMonotonicFirstVsLast(int[] nums) {
        int n = nums.length;
        if (nums[0] <= nums[n - 1]) {
            for (int i = 1; i < n; i++) {
                if (nums[i] < nums[i - 1]) {
                    return false;
                }
            }
        } else {
            for (int i = 1; i < n; i++) {
                if (nums[i] > nums[i - 1]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Tracks both directions in one pass. Time: O(n). Space: O(1).
     *
     * <p>Start assuming both directions hold and rule each out as soon as a step
     * contradicts it. A step going up rules out decreasing, a step going down
     * rules out increasing, and an equal step rules out neither. NeetCode's
     * version always walks to the end; this one returns as soon as both are
     * ruled out, which on [1, 3, 2, ...] is after two steps.
     */
    static boolean isMonotonicTwoFlags(int[] nums) {
        boolean increasing = true;
        boolean decreasing = true;
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] > nums[i - 1]) {
                decreasing = false;
            } else if (nums[i] < nums[i - 1]) {
                increasing = false;
            }
            if (!increasing && !decreasing) {
                return false;
            }
        }
        return true;
    }

    /** One test case: an input and the expected answer. */
    private static class Case {
        final int[] nums;
        final boolean expected;

        Case(int[] nums, boolean expected) {
            this.nums = nums;
            this.expected = expected;
        }
    }

    public static void main(String[] args) {
        int[] brokenAtEnd = IntStream.range(0, 100000).toArray();
        brokenAtEnd[99999] = 0;

        Case[] cases = {
            new Case(new int[] {1, 2, 2, 3}, true),
            new Case(new int[] {6, 5, 4, 4}, true),
            new Case(new int[] {1, 3, 2}, false),
            new Case(new int[] {1}, true), // a single element is both
            new Case(new int[] {5, 5, 5}, true), // constant: both directions hold
            new Case(new int[] {1, 1, 2}, true), // equal steps first, then a rise
            new Case(new int[] {2, 2, 1}, true),
            new Case(new int[] {1, 2, 1}, false), // first == last, but not constant
            new Case(new int[] {3, 1, 2, 3}, false), // first == last, dips in the middle
            new Case(new int[] {1, 2, 3, 0, 4}, false), // first < last, one step down
            new Case(new int[] {4, 3, 5, 2, 1}, false), // first > last, one step up
            new Case(new int[] {1, 1, 1, 2, 1}, false), // breaks only at the very end
            new Case(new int[] {-100000, 100000}, true), // value bounds
            new Case(IntStream.range(0, 100000).toArray(), true), // the ceiling, increasing
            new Case(IntStream.iterate(100000, v -> v - 1).limit(100000).toArray(), true),
            new Case(brokenAtEnd, false), // the ceiling, broken by the last step
        };

        Map<String, Predicate<int[]>> solutions = new LinkedHashMap<>();
        solutions.put("sorting", MonotonicArray::isMonotonicSorting);
        solutions.put("two pass", MonotonicArray::isMonotonicTwoPass);
        solutions.put("first vs last", MonotonicArray::isMonotonicFirstVsLast);
        solutions.put("two flags", MonotonicArray::isMonotonicTwoFlags);

        for (Map.Entry<String, Predicate<int[]>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                if (solution.getValue().test(testCase.nums.clone()) == testCase.expected) {
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
