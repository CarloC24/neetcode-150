import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

/**
 * Longest Strictly Increasing or Strictly Decreasing Subarray (LeetCode 3105).
 *
 * <p>Given an array of integers, return the length of the longest subarray that
 * is either strictly increasing or strictly decreasing.
 *
 * <p>The four approaches from
 * https://neetcode.io/solutions/longest-strictly-increasing-or-strictly-decreasing-subarray,
 * ordered from brute force to optimal.
 */
public class LongestStrictlyIncreasingOrStrictlyDecreasingSubarray {

    /**
     * Extends a run from every start. Time: O(n^2). Space: O(1).
     *
     * <p>The first step after i fixes the run's direction; each later step must
     * match it, and an equal pair ends the run in either direction. A run of one
     * element is always valid, so the answer starts at 1 -- which is also why a
     * one-element array needs no special case: the outer loop never runs.
     */
    static int longestBruteForce(int[] nums) {
        int n = nums.length;
        int res = 1;
        for (int i = 0; i < n - 1; i++) {
            boolean increasing = nums[i] < nums[i + 1];
            int curLen = 1;
            for (int j = i + 1; j < n; j++) {
                if (nums[j] == nums[j - 1] || (nums[j - 1] < nums[j]) != increasing) {
                    break;
                }
                curLen++;
            }
            res = Math.max(res, curLen);
        }
        return res;
    }

    /**
     * One pass with a run length and a direction flag. Time: O(n). Space: O(1).
     *
     * <p>direction is 1 while the current run is going up, -1 while going down,
     * and 0 right after an equal pair. When the direction flips, the new run is
     * the two elements either side of the flip, so it restarts at 2, not 1: the
     * last element of the old run is the first of the new one.
     */
    static int longestDirectionFlag(int[] nums) {
        int cur = 1;
        int res = 1;
        int direction = 0;
        for (int i = 1; i < nums.length; i++) {
            if (nums[i - 1] < nums[i]) {
                cur = direction > 0 ? cur + 1 : 2;
                direction = 1;
            } else if (nums[i - 1] > nums[i]) {
                cur = direction < 0 ? cur + 1 : 2;
                direction = -1;
            } else {
                cur = 1;
                direction = 0;
            }
            res = Math.max(res, cur);
        }
        return res;
    }

    /**
     * Tracks the increasing and the decreasing run at once.
     * Time: O(n). Space: O(1).
     *
     * <p>The optimal solution, and the simplest. inc is the length of the
     * longest strictly increasing run ending at i, dec the same for decreasing.
     * Each step extends one of them and resets the other to 1; an equal pair
     * resets both. No direction flag, and no restart-at-2 rule to remember: the
     * reset to 1 plus the extension on the next step produce it automatically.
     */
    static int longestTwoCounters(int[] nums) {
        int inc = 1;
        int dec = 1;
        int res = 1;
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] > nums[i - 1]) {
                inc++;
                dec = 1;
            } else if (nums[i] < nums[i - 1]) {
                inc = 1;
                dec++;
            } else {
                inc = 1;
                dec = 1;
            }
            res = Math.max(res, Math.max(inc, dec));
        }
        return res;
    }

    /**
     * One pass, reading the run's direction from where it started.
     * Time: O(n). Space: O(1).
     *
     * <p>Instead of storing a direction, look it up: the current run starts at
     * i - curLen, so its first step is nums[i - curLen] -&gt;
     * nums[i - curLen + 1]. When curLen is 1 that first step is the step being
     * tested, so it always agrees with itself and the run extends to 2.
     *
     * <p>On a flip or an equal pair it skips the res update. That is safe: a
     * flip restarts at 2, and a flip can only happen once curLen was already at
     * least 2, so res is already at least 2. Clever, but the index arithmetic is
     * the hardest of the four to check by eye.
     */
    static int longestDirectionFromStart(int[] nums) {
        int curLen = 1;
        int res = 1;
        for (int i = 1; i < nums.length; i++) {
            boolean startUp = nums[i - curLen] < nums[i - curLen + 1];
            if (nums[i] == nums[i - 1] || startUp != (nums[i - 1] < nums[i])) {
                curLen = nums[i] == nums[i - 1] ? 1 : 2;
                continue;
            }
            curLen++;
            res = Math.max(res, curLen);
        }
        return res;
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

    public static void main(String[] args) {
        int[] flat = new int[50];
        Arrays.fill(flat, 50);

        Case[] cases = {
            new Case(new int[] {1, 4, 3, 3, 2}, 2),
            new Case(new int[] {3, 3, 3, 3}, 1), // every pair equal: only single elements
            new Case(new int[] {3, 2, 1}, 3), // the whole array, decreasing
            new Case(new int[] {1}, 1), // a single element is a run of one
            new Case(new int[] {1, 2}, 2),
            new Case(new int[] {2, 2}, 1),
            new Case(new int[] {1, 3, 2, 4, 3}, 2), // zigzag: every run is two long
            new Case(new int[] {1, 2, 3, 2, 1}, 3), // the peak is shared by both runs
            new Case(new int[] {5, 4, 3, 3, 4, 5, 6}, 4), // equal pair resets; later run longer
            new Case(new int[] {1, 2, 2, 3, 4}, 3), // equal pair in a rising stretch
            new Case(new int[] {3, 1, 2, 3, 4}, 4), // flip to increasing restarts at 2, not 1
            new Case(new int[] {1, 2, 3, 4, 3, 2, 1, 0}, 5), // decreasing run starts at the peak
            new Case(IntStream.rangeClosed(1, 50).toArray(), 50), // the ceiling, increasing
            new Case(IntStream.iterate(50, v -> v - 1).limit(50).toArray(), 50), // decreasing
            new Case(flat, 1),
        };

        Map<String, ToIntFunction<int[]>> solutions = new LinkedHashMap<>();
        solutions.put("brute force", LongestStrictlyIncreasingOrStrictlyDecreasingSubarray::longestBruteForce);
        solutions.put("direction flag", LongestStrictlyIncreasingOrStrictlyDecreasingSubarray::longestDirectionFlag);
        solutions.put("two counters", LongestStrictlyIncreasingOrStrictlyDecreasingSubarray::longestTwoCounters);
        solutions.put(
                "direction from start",
                LongestStrictlyIncreasingOrStrictlyDecreasingSubarray::longestDirectionFromStart);

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
    }
}
