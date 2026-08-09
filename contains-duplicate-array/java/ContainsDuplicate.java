import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Contains Duplicate (LeetCode 217).
 *
 * <p>Given an integer array, return true if any value appears more than once.
 *
 * <p>The four approaches from https://neetcode.io/solutions/contains-duplicate,
 * ordered from brute force to optimal.
 */
public class ContainsDuplicate {

    /**
     * Compares every pair of elements. Time: O(n^2). Space: O(1).
     *
     * <p>The inner loop starts at i + 1, not i, so an element is never compared
     * against itself -- that mistake would report a duplicate for every input.
     */
    static boolean hasDuplicateBruteForce(int[] nums) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] == nums[j]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sorts, then looks for equal neighbours. Time: O(n log n). Space: O(n).
     *
     * <p>Duplicates land next to each other once sorted. Sorts a copy so the
     * caller's array keeps its original order.
     */
    static boolean hasDuplicateSorting(int[] nums) {
        int[] ordered = Arrays.copyOf(nums, nums.length);
        Arrays.sort(ordered);
        for (int i = 1; i < ordered.length; i++) {
            if (ordered[i] == ordered[i - 1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remembers values already seen. Time: O(n). Space: O(n).
     *
     * <p>The optimal solution: set lookups are O(1), and it returns on the first
     * repeat instead of always scanning the whole array.
     */
    static boolean hasDuplicateHashSet(int[] nums) {
        Set<Integer> seen = new HashSet<>();
        for (int num : nums) {
            if (seen.contains(num)) {
                return true;
            }
            seen.add(num);
        }
        return false;
    }

    /**
     * Compares the distinct count to the array's length. Time: O(n). Space: O(n).
     *
     * <p>Shortest to write, but it always consumes the whole stream -- no early
     * exit on an array whose first two elements already match.
     */
    static boolean hasDuplicateDistinctCount(int[] nums) {
        return Arrays.stream(nums).distinct().count() < nums.length;
    }

    /** One test case: an expected answer and the input it applies to. */
    private static class Case {
        final boolean expected;
        final int[] nums;

        Case(boolean expected, int... nums) {
            this.expected = expected;
            this.nums = nums;
        }
    }

    public static void main(String[] args) {
        Case[] cases = {
            new Case(true, 1, 2, 3, 3),
            new Case(false, 1, 2, 3, 4),
            new Case(false),
            new Case(false, 7),
            new Case(true, 0, 0),
            new Case(true, -1, -1),
            new Case(false, -1000000000, 1000000000, 0),
        };

        Map<String, Predicate<int[]>> solutions = new LinkedHashMap<>();
        solutions.put("brute force", ContainsDuplicate::hasDuplicateBruteForce);
        solutions.put("sorting", ContainsDuplicate::hasDuplicateSorting);
        solutions.put("hash set", ContainsDuplicate::hasDuplicateHashSet);
        solutions.put("distinct count", ContainsDuplicate::hasDuplicateDistinctCount);

        for (Map.Entry<String, Predicate<int[]>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                if (solution.getValue().test(testCase.nums) == testCase.expected) {
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

        int[] original = {3, 1, 2};
        for (Predicate<int[]> solve : solutions.values()) {
            solve.test(original);
        }
        boolean unmodified = Arrays.equals(original, new int[] {3, 1, 2});
        System.out.printf(
                "%s  input left unmodified: %s%n",
                unmodified ? "PASS" : "FAIL", Arrays.toString(original));
    }
}
