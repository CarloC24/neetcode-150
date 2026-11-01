import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Sort Colors (LeetCode 75).
 *
 * <p>Given an array nums of 0s (red), 1s (white) and 2s (blue), sort it in
 * place so equal colors are adjacent, in the order 0, 1, 2. The library sort
 * is not allowed.
 *
 * <p>The five approaches from https://neetcode.io/solutions/sort-colors,
 * ordered from the library sort to one-pass three-pointer partitions.
 */
public class SortColors {

    /**
     * Calls the library sort. Time: O(n log n). Space: O(log n).
     *
     * <p>The baseline the problem rules out. It's here as the reference the
     * other four are checked against, not as an answer. Arrays.sort on an int[]
     * is dual-pivot quicksort, which needs only its O(log n) recursion stack.
     */
    static void sortColorsBuiltin(int[] nums) {
        Arrays.sort(nums);
    }

    /**
     * Counts each color, then rewrites the array. Time: O(n). Space: O(1).
     *
     * <p>Counting sort with three buckets. It takes two passes: one to count
     * and one to write. The follow-up asks for a single pass, which the next
     * three approaches manage.
     */
    static void sortColorsCounting(int[] nums) {
        int[] count = new int[3];
        for (int num : nums) {
            count[num]++;
        }

        int index = 0;
        for (int color = 0; color < 3; color++) {
            for (int k = 0; k < count[color]; k++) {
                nums[index++] = color;
            }
        }
    }

    /**
     * Dutch national flag partition, swapping from both ends.
     * Time: O(n). Space: O(1).
     *
     * <p>The classic one-pass answer. Three pointers split the array into four
     * regions: [0, l) all 0s, [l, i) all 1s, [i, r] not looked at yet, and
     * (r, n) all 2s.
     *
     * <p>A 0 at i swaps to l. What comes back from l is a 1 (or the same 0,
     * when l == i), which was already checked, so i moves on. A 2 at i swaps to
     * r, but what comes back from r hasn't been looked at, so i must stay put.
     * The loop runs while i <= r: index r is still unknown. See the README.
     */
    static void sortColorsDutchFlag(int[] nums) {
        int l = 0;
        int i = 0;
        int r = nums.length - 1;
        while (i <= r) {
            if (nums[i] == 0) {
                swap(nums, l, i);
                l++;
                i++;
            } else if (nums[i] == 2) {
                swap(nums, i, r);
                r--; // i stays: the value swapped in from r is unchecked
            } else {
                i++;
            }
        }
    }

    private static void swap(int[] nums, int a, int b) {
        int tmp = nums[a];
        nums[a] = nums[b];
        nums[b] = tmp;
    }

    /**
     * Grows three regions from the left, writing instead of swapping.
     * Time: O(n). Space: O(1).
     *
     * <p>zero, one and two are where the 0s, 1s and 2s regions end, so the
     * array so far is [0, zero) 0s, [zero, one) 1s, [one, two) 2s. Each new
     * value pushes the regions at and after its color one slot right: write a 2
     * at the end of the 2s region, a 1 where the 1s end, a 0 where the 0s end,
     * in that order. Writing back to front means each write lands on a slot the
     * next region has just claimed, so nothing that's needed is lost.
     *
     * <p>nums[i] is read before any of the writes. The 2 written at two (== i)
     * overwrites it, which is fine because the branch has been chosen already.
     */
    static void sortColorsCascade(int[] nums) {
        int zero = 0;
        int one = 0;
        int two = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == 0) {
                nums[two++] = 2;
                nums[one++] = 1;
                nums[zero++] = 0;
            } else if (nums[i] == 1) {
                nums[two++] = 2;
                nums[one++] = 1;
            } else {
                nums[two++] = 2;
            }
        }
    }

    /**
     * The cascade with the branches folded together.
     * Time: O(n). Space: O(1).
     *
     * <p>The 2s region always ends at the loop index, so two is the loop
     * variable. Every value writes a 2. Anything below 2 also writes a 1, and
     * anything below 1 also writes a 0. Saving the value in tmp first is
     * required: the 2 written at index two would otherwise erase it.
     */
    static void sortColorsCascadeShort(int[] nums) {
        int zero = 0;
        int one = 0;
        for (int two = 0; two < nums.length; two++) {
            int tmp = nums[two];
            nums[two] = 2;
            if (tmp < 2) {
                nums[one++] = 1;
            }
            if (tmp < 1) {
                nums[zero++] = 0;
            }
        }
    }

    public static void main(String[] args) {
        Random rng = new Random(75);
        int[] ceiling = rng.ints(300, 0, 3).toArray();

        int[][] cases = {
            {2, 0, 2, 1, 1, 0},
            {1, 0, 1, 2},
            {2, 1, 0},
            {0}, // the smallest input
            {2},
            {1, 0}, // breaks "while i < r": index r is never checked
            {1, 2, 0}, // breaks advancing i after the swap with r: the 0 is skipped
            {0, 0, 1, 1, 2, 2}, // already sorted
            {2, 2, 1, 1, 0, 0}, // reversed
            {1, 1, 1}, // one color only
            {2, 0, 2, 0}, // no 1s
            {2, 2, 2, 0}, // every swap with r brings back another 2
            ceiling, // length ceiling, seeded
        };

        Map<String, Consumer<int[]>> solutions = new LinkedHashMap<>();
        solutions.put("built-in sort", SortColors::sortColorsBuiltin);
        solutions.put("counting sort", SortColors::sortColorsCounting);
        solutions.put("dutch flag", SortColors::sortColorsDutchFlag);
        solutions.put("cascade", SortColors::sortColorsCascade);
        solutions.put("cascade, short", SortColors::sortColorsCascadeShort);

        for (Map.Entry<String, Consumer<int[]>> solution : solutions.entrySet()) {
            int passed = 0;
            for (int[] testCase : cases) {
                int[] nums = testCase.clone();
                int[] expected = testCase.clone();
                Arrays.sort(expected);
                solution.getValue().accept(nums);
                if (Arrays.equals(nums, expected)) {
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
