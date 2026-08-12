import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Two Sum (LeetCode 1).
 *
 * <p>Given an integer array nums and an integer target, return the two indices i
 * and j such that nums[i] + nums[j] == target, with i != j. Exactly one such
 * pair exists.
 *
 * <p>The four approaches from https://neetcode.io/solutions/two-sum, ordered
 * from brute force to optimal.
 */
public class TwoSum {

    /**
     * Tries every pair. Time: O(n^2). Space: O(1).
     *
     * <p>The inner loop starts at i + 1, which does double duty: it skips pairs
     * already tried, and it enforces i != j so an element is never added to
     * itself. On nums = [3, 2, 4], target = 6, starting at i would match 3 + 3
     * and wrongly return [0, 0].
     */
    static int[] twoSumBruteForce(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) {
                    return new int[] {i, j};
                }
            }
        }
        return new int[0];
    }

    /**
     * Sorts, then walks inward from both ends. Time: O(n log n). Space: O(n).
     *
     * <p>Once sorted, a sum that is too small can only grow by moving the left
     * pointer right, and one that is too large can only shrink by moving the
     * right pointer left -- so each step discards a value that cannot be part of
     * the answer, and neither pointer ever needs to backtrack.
     *
     * <p>Sorting destroys the original positions, so each value carries its index
     * along as {value, index}. That pairing is also what makes this O(n) space,
     * and it is why the answer is rebuilt with min/max: sorted order says nothing
     * about which index came first. Building that array also leaves the caller's
     * nums untouched.
     */
    static int[] twoSumTwoPointers(int[] nums, int target) {
        int[][] indexed = new int[nums.length][2];
        for (int i = 0; i < nums.length; i++) {
            indexed[i][0] = nums[i];
            indexed[i][1] = i;
        }
        Arrays.sort(indexed, Comparator.comparingInt(pair -> pair[0]));

        int left = 0;
        int right = nums.length - 1;
        while (left < right) {
            int total = indexed[left][0] + indexed[right][0];
            if (total == target) {
                return new int[] {
                    Math.min(indexed[left][1], indexed[right][1]),
                    Math.max(indexed[left][1], indexed[right][1])
                };
            }
            if (total < target) {
                left++;
            } else {
                right--;
            }
        }
        return new int[0];
    }

    /**
     * Indexes every value, then looks up each complement. Time: O(n). Space: O(n).
     *
     * <p>The explicit index != i check is what keeps a value from pairing with
     * itself -- on nums = [3, 2, 4], target = 6, the complement of 3 is 3, found
     * at its own index, and must be rejected.
     *
     * <p>Duplicate values collapse in the map, which keeps only the last index for
     * each. That is harmless here: if the answer needs a value twice, the stored
     * index is the later occurrence and the scan reaches the earlier one first.
     */
    static int[] twoSumHashMapTwoPass(int[] nums, int target) {
        Map<Integer, Integer> indices = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            indices.put(nums[i], i);
        }

        for (int i = 0; i < nums.length; i++) {
            Integer index = indices.get(target - nums[i]);
            if (index != null && index != i) {
                return new int[] {i, index};
            }
        }
        return new int[0];
    }

    /**
     * Looks back while building the map. Time: O(n). Space: O(n).
     *
     * <p>The optimal solution. Each value checks the complement against the values
     * already behind it, then records itself. Storing only after the check is what
     * makes the i != j test unnecessary -- a value cannot be its own complement
     * because it is not in the map yet when it looks.
     */
    static int[] twoSumHashMapOnePass(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            Integer index = seen.get(target - nums[i]);
            if (index != null) {
                return new int[] {index, i};
            }
            seen.put(nums[i], i);
        }
        return new int[0];
    }

    /** One test case: an input array, a target, and the expected pair of indices. */
    private static class Case {
        final int[] nums;
        final int target;
        final int[] expected;

        Case(int[] nums, int target, int[] expected) {
            this.nums = nums;
            this.target = target;
            this.expected = expected;
        }
    }

    public static void main(String[] args) {
        // Every case has exactly one valid pair, as the constraints promise --
        // otherwise approaches returning different-but-correct answers could not
        // be compared.
        Case[] cases = {
            new Case(new int[] {3, 4, 5, 6}, 7, new int[] {0, 1}),
            new Case(new int[] {4, 5, 6}, 10, new int[] {0, 2}),
            new Case(new int[] {5, 5}, 10, new int[] {0, 1}), // pair is one value twice
            new Case(new int[] {2, 7, 11, 15}, 9, new int[] {0, 1}),
            new Case(new int[] {3, 2, 4}, 6, new int[] {1, 2}), // must not return [0, 0]
            new Case(new int[] {1, 2, 3, 4, 5}, 9, new int[] {3, 4}), // answer at the end
            new Case(new int[] {-1, -2, -3, -4, -5}, -8, new int[] {2, 4}), // all negative
            new Case(new int[] {0, 4, 3, 0}, 0, new int[] {0, 3}), // duplicate zeros
            new Case(new int[] {-10000000, 10000000}, 0, new int[] {0, 1}), // value bounds
        };

        Map<String, BiFunction<int[], Integer, int[]>> solutions = new LinkedHashMap<>();
        solutions.put("brute force", TwoSum::twoSumBruteForce);
        solutions.put("two pointers", TwoSum::twoSumTwoPointers);
        solutions.put("hash map (two pass)", TwoSum::twoSumHashMapTwoPass);
        solutions.put("hash map (one pass)", TwoSum::twoSumHashMapOnePass);

        for (Map.Entry<String, BiFunction<int[], Integer, int[]>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                int[] actual = solution.getValue().apply(testCase.nums, testCase.target);
                if (Arrays.equals(actual, testCase.expected)) {
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

        int[] original = {3, 2, 4};
        for (BiFunction<int[], Integer, int[]> solve : solutions.values()) {
            solve.apply(original, 6);
        }
        boolean unmodified = Arrays.equals(original, new int[] {3, 2, 4});
        System.out.printf(
                "%s  input left unmodified: %s%n",
                unmodified ? "PASS" : "FAIL", Arrays.toString(original));
    }
}
