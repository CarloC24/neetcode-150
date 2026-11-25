import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntBiFunction;

/**
 * Remove Element (LeetCode 27).
 *
 * <p>Given an integer array {@code nums} and an integer {@code val}, remove
 * every occurrence of {@code val} in place and return k, the number of
 * elements left. The first k slots of {@code nums} must hold the kept
 * elements, in any order. What's past slot k doesn't matter.
 *
 * <p>The three approaches from https://neetcode.io/solutions/remove-element,
 * ordered as NeetCode presents them. All are O(n); they differ in extra space
 * and in how many writes they make.
 */
public class RemoveElement {

    /**
     * Copies the keepers into a new array, then copies them back.
     * Time: O(n). Space: O(n) for the copy.
     */
    static int removeElementBruteForce(int[] nums, int val) {
        int[] kept = new int[nums.length];
        int k = 0;
        for (int x : nums) {
            if (x != val) {
                kept[k++] = x;
            }
        }
        System.arraycopy(kept, 0, nums, 0, k);
        return k;
    }

    /**
     * Reads every element and writes each keeper to the next free slot.
     * Time: O(n). Space: O(1).
     *
     * <p>k never passes i, so a write only overwrites something already read.
     * Keepers stay in their original order.
     */
    static int removeElementTwoPointers(int[] nums, int val) {
        int k = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != val) {
                nums[k++] = nums[i];
            }
        }
        return k;
    }

    /**
     * Overwrites each {@code val} with the last element and shrinks the array.
     * Time: O(n). Space: O(1).
     *
     * <p>Don't advance i after a swap: the element moved in hasn't been
     * checked yet. One write per {@code val} instead of one per keeper, but
     * keepers get reordered.
     */
    static int removeElementTwoPointersSwap(int[] nums, int val) {
        int i = 0;
        int n = nums.length;
        while (i < n) {
            if (nums[i] == val) {
                nums[i] = nums[--n];
            } else {
                i++;
            }
        }
        return n;
    }

    /** One test case: an input, the value to remove, and the keepers. */
    private static class Case {
        final int[] nums;
        final int val;
        final int[] expected;

        Case(int[] nums, int val, int[] expected) {
            this.nums = nums;
            this.val = val;
            this.expected = expected;
        }
    }

    /** Judges the way LeetCode does: k must match, and the first k slots must
     *  hold the keepers in any order. */
    static boolean check(ToIntBiFunction<int[], Integer> solve, Case c) {
        int[] nums = c.nums.clone();
        int k = solve.applyAsInt(nums, c.val);
        if (k != c.expected.length) {
            return false;
        }
        int[] got = Arrays.copyOf(nums, k);
        int[] want = c.expected.clone();
        Arrays.sort(got);
        Arrays.sort(want);
        return Arrays.equals(got, want);
    }

    public static void main(String[] args) {
        int[] ceiling = new int[100];
        int[] zeros = new int[50];
        for (int i = 0; i < 100; i++) {
            ceiling[i] = i % 2 == 0 ? 0 : 50;
        }
        int[] sevens = new int[100];
        Arrays.fill(sevens, 7);

        Case[] cases = {
            new Case(new int[] {1, 1, 2, 3, 4}, 1, new int[] {2, 3, 4}),
            new Case(new int[] {0, 1, 2, 2, 3, 0, 4, 2}, 2, new int[] {0, 1, 3, 0, 4}),
            new Case(new int[] {3, 2, 2, 3}, 3, new int[] {2, 2}),
            new Case(new int[] {}, 0, new int[] {}), // empty input
            new Case(new int[] {1}, 1, new int[] {}), // the only element is removed
            new Case(new int[] {1}, 2, new int[] {1}), // the only element stays
            new Case(new int[] {4, 4, 4, 4}, 4, new int[] {}), // everything is removed
            new Case(new int[] {1, 2, 3, 4}, 5, new int[] {1, 2, 3, 4}), // val isn't there
            new Case(new int[] {2, 1, 1, 1}, 1, new int[] {2}), // swap moves in val again
            new Case(new int[] {1, 2, 3, 4}, 1, new int[] {2, 3, 4}), // val only at the front
            new Case(new int[] {1, 2, 3, 4}, 4, new int[] {1, 2, 3}), // val only at the back
            new Case(new int[] {5, 1, 5, 2, 5}, 5, new int[] {1, 2}), // alternating
            new Case(ceiling, 50, zeros), // n = 100, the upper bound
            new Case(sevens, 100, sevens), // val = 100, above every nums[i]
        };

        Map<String, ToIntBiFunction<int[], Integer>> solutions = new LinkedHashMap<>();
        solutions.put("brute force", RemoveElement::removeElementBruteForce);
        solutions.put("two pointers", RemoveElement::removeElementTwoPointers);
        solutions.put("two pointers (swap)", RemoveElement::removeElementTwoPointersSwap);

        for (Map.Entry<String, ToIntBiFunction<int[], Integer>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case c : cases) {
                if (check(solution.getValue(), c)) {
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

        // Only the swap approach may reorder the keepers.
        for (String name : new String[] {"brute force", "two pointers"}) {
            int[] nums = {3, 1, 2, 1, 4};
            int k = solutions.get(name).applyAsInt(nums, 1);
            int[] got = Arrays.copyOf(nums, k);
            boolean ok = Arrays.equals(got, new int[] {3, 2, 4});
            System.out.printf("%s  %s keeps order: %s%n", ok ? "PASS" : "FAIL", name, Arrays.toString(got));
        }
    }
}
