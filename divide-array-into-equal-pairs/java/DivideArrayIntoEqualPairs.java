import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Divide Array Into Equal Pairs (LeetCode 2206).
 *
 * <p>Given an array of 2n integers, return true if it can be divided into n
 * pairs where both elements of every pair are equal.
 *
 * <p>The three approaches from
 * https://neetcode.io/solutions/divide-array-into-equal-pairs, plus a
 * fixed-size parity array, ordered from brute force to optimal.
 */
public class DivideArrayIntoEqualPairs {

    /**
     * Sorts, then checks every run of equal values has even length.
     * Time: O(N log N). Space: O(log N).
     *
     * <p>N = nums.length = 2n. Sorting puts equal values next to each other, so
     * each value becomes one run, and a run can be cut into pairs exactly when
     * its length is even. Sorts the input in place. Arrays.sort on an int[] is
     * dual-pivot quicksort, which needs only its O(log N) recursion stack.
     */
    static boolean divideArraySorting(int[] nums) {
        Arrays.sort(nums);
        int i = 0;
        while (i < nums.length) {
            int j = i;
            while (j < nums.length && nums[j] == nums[i]) {
                j++;
            }
            if ((j - i) % 2 != 0) {
                return false;
            }
            i = j;
        }
        return true;
    }

    /**
     * Counts each value, then checks every count is even.
     * Time: O(N). Space: O(N).
     *
     * <p>The order of the elements doesn't matter at all: values can be paired
     * up exactly when each one appears an even number of times. So this is a
     * counting problem, not an arrangement problem.
     */
    static boolean divideArrayHashMap(int[] nums) {
        Map<Integer, Integer> count = new HashMap<>();
        for (int num : nums) {
            count.merge(num, 1, Integer::sum);
        }
        for (int cnt : count.values()) {
            if (cnt % 2 != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Keeps the set of values seen an odd number of times so far.
     * Time: O(N). Space: O(N).
     *
     * <p>Each occurrence toggles a value in or out of the set: the first copy
     * adds it, the second removes it, the third adds it back. Only the parity of
     * each count matters, so the set stores exactly that. Every value has an
     * even count exactly when the set ends up empty.
     *
     * <p>Set.add returns false when the value is already there, so one call
     * both tests and inserts, where NeetCode's version calls contains first.
     */
    static boolean divideArrayHashSet(int[] nums) {
        Set<Integer> odd = new HashSet<>();
        for (int num : nums) {
            if (!odd.add(num)) {
                odd.remove(num);
            }
        }
        return odd.isEmpty();
    }

    /**
     * Toggles one flag per possible value. Time: O(N). Space: O(1).
     *
     * <p>The optimal solution. Values are between 1 and 500, so a fixed array of
     * 501 flags can stand in for the set in approach 3: flip the flag on each
     * occurrence, and every flag must end up false. The array's size doesn't
     * depend on the input, so the space is O(1), and there's no hashing or
     * boxing.
     *
     * <p>The tempting shortcut, XOR-ing every value and checking for 0, is
     * wrong. Equal pairs do cancel to 0, but so do unrelated values:
     * 1 ^ 2 ^ 4 ^ 7 == 0 and [1, 2, 4, 7] has no pairs at all. See the README.
     */
    static boolean divideArrayParityArray(int[] nums) {
        boolean[] odd = new boolean[501];
        for (int num : nums) {
            odd[num] = !odd[num];
        }
        for (boolean flag : odd) {
            if (flag) {
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
        int[] sevens = new int[1000];
        Arrays.fill(sevens, 7);
        int[] everyValueTwice =
                IntStream.concat(IntStream.rangeClosed(1, 500), IntStream.rangeClosed(1, 500)).toArray();
        int[] oneOnceThreeThrice = IntStream.concat(
                        IntStream.concat(IntStream.rangeClosed(1, 500), IntStream.rangeClosed(2, 500)),
                        IntStream.of(3))
                .toArray();

        Case[] cases = {
            new Case(new int[] {3, 2, 3, 2, 2, 2}, true),
            new Case(new int[] {1, 2, 3, 4}, false),
            new Case(new int[] {1, 1}, true), // the smallest input
            new Case(new int[] {1, 2}, false),
            new Case(new int[] {5, 5, 5, 5}, true), // one value, used for two pairs
            new Case(new int[] {1, 1, 1, 2}, false), // odd counts can't fix each other
            new Case(new int[] {2, 1, 2, 1}, true), // pairs don't need to be adjacent
            new Case(new int[] {1, 2, 4, 7}, false), // XORs to 0, but nothing pairs
            new Case(new int[] {1, 1, 2, 2, 3, 3, 4, 5}, false), // only the last two fail
            new Case(new int[] {500, 1, 500, 1}, true), // value bounds
            new Case(sevens, true), // the length ceiling: n = 500, a single value
            new Case(everyValueTwice, true), // the ceiling, every value twice
            new Case(oneOnceThreeThrice, false), // 1 once, 3 three times
        };

        Map<String, Predicate<int[]>> solutions = new LinkedHashMap<>();
        solutions.put("sorting", DivideArrayIntoEqualPairs::divideArraySorting);
        solutions.put("hash map", DivideArrayIntoEqualPairs::divideArrayHashMap);
        solutions.put("hash set", DivideArrayIntoEqualPairs::divideArrayHashSet);
        solutions.put("parity array", DivideArrayIntoEqualPairs::divideArrayParityArray);
        Set<String> mutatesInput = Set.of("sorting");

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

        int[] original = {3, 2, 3, 2, 2, 2};
        for (Map.Entry<String, Predicate<int[]>> solution : solutions.entrySet()) {
            if (!mutatesInput.contains(solution.getKey())) {
                solution.getValue().test(original);
            }
        }
        System.out.printf(
                "%s  input left unmodified (all but sorting): %s%n",
                Arrays.equals(original, new int[] {3, 2, 3, 2, 2, 2}) ? "PASS" : "FAIL",
                Arrays.toString(original));
    }
}
