import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Find All Numbers Disappeared in an Array (LeetCode 448).
 *
 * <p>Given an array nums of n integers, each in the range [1, n], return every
 * integer in [1, n] that does not appear in nums.
 *
 * <p>The four approaches from
 * https://neetcode.io/solutions/find-all-numbers-disappeared-in-an-array, plus
 * cyclic sort, the other O(1)-space answer to the follow-up. Ordered from the
 * most extra memory to the least.
 */
public class FindAllNumbersDisappearedInAnArray {

    /**
     * Starts with every number, crosses off the ones seen.
     * Time: O(n). Space: O(n).
     *
     * <p>The most direct statement of the problem. HashSet makes no promise
     * about iteration order, and in practice the order breaks above 65,536:
     * HashMap mixes the high bits of each hash into the low bits, which swaps
     * neighbouring values, so at n = 10^5 the result goes 65537, 65536, 65539.
     * LeetCode accepts any order, so the harness sorts every result before
     * comparing.
     */
    static List<Integer> disappearedHashSet(int[] nums) {
        Set<Integer> store = new HashSet<>();
        for (int i = 1; i <= nums.length; i++) {
            store.add(i);
        }
        for (int num : nums) {
            store.remove(num);
        }
        return new ArrayList<>(store);
    }

    /**
     * Marks each value's slot in a separate array. Time: O(n). Space: O(n).
     *
     * <p>The same idea as the set, but the values are exactly 1..n, so a plain
     * boolean array indexed by value - 1 can do the set's job without any
     * hashing or boxing. The result comes out in ascending order by
     * construction.
     */
    static List<Integer> disappearedBooleanArray(int[] nums) {
        boolean[] seen = new boolean[nums.length];
        for (int num : nums) {
            seen[num - 1] = true;
        }
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < seen.length; i++) {
            if (!seen[i]) {
                res.add(i + 1);
            }
        }
        return res;
    }

    /**
     * Sorts, then walks 1..n alongside the sorted values.
     * Time: O(n log n). Space: O(log n).
     *
     * <p>For each number 1..n, skip past any smaller values; if what is left is
     * not that number, it is missing. Sorts the input in place. Arrays.sort on
     * an int[] is dual-pivot quicksort, which needs only its O(log n) recursion
     * stack; Python's Timsort needs up to n / 2 slots of scratch space.
     */
    static List<Integer> disappearedSorting(int[] nums) {
        Arrays.sort(nums);
        int n = nums.length;
        List<Integer> res = new ArrayList<>();
        int idx = 0;
        for (int num = 1; num <= n; num++) {
            while (idx < n && nums[idx] < num) {
                idx++;
            }
            if (idx == n || nums[idx] > num) {
                res.add(num);
            }
        }
        return res;
    }

    /**
     * Uses the sign of nums[v - 1] as the "seen v" flag.
     * Time: O(n). Space: O(1).
     *
     * <p>The optimal solution, and NeetCode's answer to the follow-up. Every
     * value is positive, so the sign bit of each slot is free to use as a flag:
     * seeing v makes nums[v - 1] negative. Any slot still positive at the end
     * belongs to a missing number.
     *
     * <p>Two details. The value read must go through Math.abs, because an
     * earlier step may already have flipped this slot. And the flip is
     * -Math.abs, not negation, so a value that appears twice leaves its slot
     * negative instead of flipping it back.
     *
     * <p>NeetCode stops there and returns with the caller's array full of
     * negative numbers. This version adds one more pass to restore every sign,
     * so the input comes back exactly as it went in. Still O(n), still O(1).
     */
    static List<Integer> disappearedNegativeMarking(int[] nums) {
        for (int num : nums) {
            int i = Math.abs(num) - 1;
            nums[i] = -Math.abs(nums[i]);
        }

        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] > 0) {
                res.add(i + 1);
            }
        }

        for (int i = 0; i < nums.length; i++) {
            nums[i] = Math.abs(nums[i]);
        }
        return res;
    }

    /**
     * Swaps every value into its own slot, then reads off the misfits.
     * Time: O(n). Space: O(1).
     *
     * <p>The other O(1)-space answer. Value v belongs at index v - 1. Keep
     * swapping nums[i] into its home until the slot holds its own value or its
     * home is already taken by a duplicate. Afterwards, any slot i not holding
     * i + 1 is a missing number.
     *
     * <p>O(n) despite the nested loop: every swap puts one value in its final
     * place, and that value is never moved again, so there are at most n swaps
     * in total. The target index is saved to j before the swap, so the swap
     * cannot read a slot it has just overwritten. Leaves the input permuted,
     * unlike approach 4.
     */
    static List<Integer> disappearedCyclicSort(int[] nums) {
        for (int i = 0; i < nums.length; i++) {
            while (nums[i] != nums[nums[i] - 1]) {
                int j = nums[i] - 1;
                int tmp = nums[i];
                nums[i] = nums[j];
                nums[j] = tmp;
            }
        }
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != i + 1) {
                res.add(i + 1);
            }
        }
        return res;
    }

    /** One test case: an input and the expected answer, in ascending order. */
    private static class Case {
        final int[] nums;
        final List<Integer> expected;

        Case(int[] nums, List<Integer> expected) {
            this.nums = nums;
            this.expected = expected;
        }
    }

    private static List<Integer> range(int fromInclusive, int toInclusive) {
        return IntStream.rangeClosed(fromInclusive, toInclusive).boxed().collect(Collectors.toList());
    }

    public static void main(String[] args) {
        int[] allOnes = new int[100000];
        Arrays.fill(allOnes, 1);
        int[] reversed = IntStream.iterate(100000, v -> v - 1).limit(100000).toArray();

        Case[] cases = {
            new Case(new int[] {4, 3, 2, 7, 8, 2, 3, 1}, List.of(5, 6)),
            new Case(new int[] {1, 1}, List.of(2)),
            new Case(new int[] {1}, List.of()), // the smallest input, nothing missing
            new Case(new int[] {2, 1}, List.of()), // a permutation: nothing missing
            new Case(new int[] {2, 2}, List.of(1)), // the missing number is the first
            new Case(new int[] {1, 1, 1, 1}, List.of(2, 3, 4)), // one value repeated
            new Case(new int[] {4, 4, 4, 4}, List.of(1, 2, 3)), // the repeated value is n
            new Case(new int[] {3, 3, 3}, List.of(1, 2)),
            new Case(new int[] {5, 4, 3, 2, 1}, List.of()), // reversed permutation
            new Case(new int[] {2, 3, 4, 5, 5}, List.of(1)),
            new Case(new int[] {1, 2, 3, 4, 4}, List.of(5)), // the missing number is the last
            new Case(allOnes, range(2, 100000)), // the ceiling, almost everything missing
            new Case(reversed, List.of()), // the ceiling, nothing missing
        };

        Map<String, Function<int[], List<Integer>>> solutions = new LinkedHashMap<>();
        solutions.put("hash set", FindAllNumbersDisappearedInAnArray::disappearedHashSet);
        solutions.put("boolean array", FindAllNumbersDisappearedInAnArray::disappearedBooleanArray);
        solutions.put("sorting", FindAllNumbersDisappearedInAnArray::disappearedSorting);
        solutions.put("negative marking", FindAllNumbersDisappearedInAnArray::disappearedNegativeMarking);
        solutions.put("cyclic sort", FindAllNumbersDisappearedInAnArray::disappearedCyclicSort);
        Set<String> mutatesInput = Set.of("sorting", "cyclic sort");

        for (Map.Entry<String, Function<int[], List<Integer>>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                List<Integer> actual = new ArrayList<>(solution.getValue().apply(testCase.nums.clone()));
                Collections.sort(actual);
                if (actual.equals(testCase.expected)) {
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

        int[] original = {4, 3, 2, 7, 8, 2, 3, 1};
        for (Map.Entry<String, Function<int[], List<Integer>>> solution : solutions.entrySet()) {
            if (!mutatesInput.contains(solution.getKey())) {
                solution.getValue().apply(original);
            }
        }
        System.out.printf(
                "%s  input left unmodified (all but sorting, cyclic sort): %s%n",
                Arrays.equals(original, new int[] {4, 3, 2, 7, 8, 2, 3, 1}) ? "PASS" : "FAIL",
                Arrays.toString(original));
    }
}
