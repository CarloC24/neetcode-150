import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

/**
 * Majority Element (LeetCode 169).
 *
 * <p>Given an array nums of size n, return the element that appears more than
 * n / 2 times. The majority element is guaranteed to exist.
 *
 * <p>The six approaches from https://neetcode.io/solutions/majority-element,
 * ordered from brute force to optimal.
 */
public class MajorityElement {

    /**
     * Counts every element's occurrences. Time: O(n^2). Space: O(1).
     *
     * <p>For each element, scans the whole array to count it, and returns the
     * first one whose count is over n / 2. The guarantee means the loop always
     * returns.
     */
    static int majorityElementBruteForce(int[] nums) {
        int n = nums.length;
        for (int num : nums) {
            int count = 0;
            for (int x : nums) {
                if (x == num) {
                    count++;
                }
            }
            if (count > n / 2) {
                return num;
            }
        }
        throw new IllegalArgumentException("no majority element");
    }

    /**
     * Counts with a HashMap, tracking the most frequent so far.
     * Time: O(n). Space: O(n).
     *
     * <p>Only the majority can ever have a count over n / 2, so the element
     * with the highest count at the end is the answer. Tracking it during the
     * pass saves a second loop over the map. merge returns the new count, so
     * there's one lookup per element instead of a put and two gets.
     */
    static int majorityElementHashMap(int[] nums) {
        Map<Integer, Integer> count = new HashMap<>();
        int res = 0;
        int maxCount = 0;
        for (int num : nums) {
            int cnt = count.merge(num, 1, Integer::sum);
            if (cnt > maxCount) {
                res = num;
                maxCount = cnt;
            }
        }
        return res;
    }

    /**
     * Sorts and returns the middle element. Time: O(n log n). Space: O(log n).
     *
     * <p>The majority fills more than half the sorted array, so its run covers
     * index n / 2 wherever the run starts. Sorts the input in place. Arrays.sort
     * on an int[] is dual-pivot quicksort, which needs only its O(log n)
     * recursion stack.
     */
    static int majorityElementSorting(int[] nums) {
        Arrays.sort(nums);
        return nums[nums.length / 2];
    }

    /**
     * Builds the answer one bit at a time. Time: O(32n). Space: O(1).
     *
     * <p>For each of the 32 bit positions, the majority's bit is whichever
     * value more than half the numbers have there, because the majority alone
     * is more than half. An int is already 32-bit two's complement, so setting
     * bit 31 with 1 << 31 makes the result negative on its own: no special case
     * for the sign bit, unlike Python.
     */
    static int majorityElementBits(int[] nums) {
        int n = nums.length;
        int[] bit = new int[32];
        for (int num : nums) {
            for (int i = 0; i < 32; i++) {
                bit[i] += (num >> i) & 1;
            }
        }
        int res = 0;
        for (int i = 0; i < 32; i++) {
            if (bit[i] > n / 2) {
                res |= 1 << i;
            }
        }
        return res;
    }

    /**
     * Boyer-Moore voting. Time: O(n). Space: O(1).
     *
     * <p>The optimal solution. Keeps one candidate and a count. A matching
     * element votes for the candidate (+1), any other element votes against it
     * (-1), and at count 0 the next element becomes the candidate.
     *
     * <p>Each -1 cancels one copy of the candidate against one other element.
     * Cancelling a pair of different values never removes the majority's lead,
     * because it loses at most one copy per pair while the other side loses one
     * too. The majority has more copies than everything else combined, so it is
     * the one left standing. See the README.
     */
    static int majorityElementBoyerMoore(int[] nums) {
        int res = 0;
        int count = 0;
        for (int num : nums) {
            if (count == 0) {
                res = num;
            }
            count += num == res ? 1 : -1;
        }
        return res;
    }

    private static final Random RNG = new Random(169);

    /**
     * Picks a random element and checks it. Time: O(n) expected. Space: O(1).
     *
     * <p>More than half the elements are the majority, so each pick succeeds
     * with probability over 1/2, and the expected number of picks is under 2.
     * Each check costs O(n). The worst case is unbounded, but the chance of
     * needing more than 20 picks is under one in a million.
     *
     * <p>The seeded generator makes the test run repeatable. The answer
     * doesn't depend on the seed, only the number of picks does.
     */
    static int majorityElementRandom(int[] nums) {
        int n = nums.length;
        while (true) {
            int candidate = nums[RNG.nextInt(n)];
            int count = 0;
            for (int num : nums) {
                if (num == candidate) {
                    count++;
                }
            }
            if (count > n / 2) {
                return candidate;
            }
        }
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

    /** count copies of a, followed by count2 copies of b. */
    private static int[] runs(int a, int count, int b, int count2) {
        return IntStream.concat(
                        IntStream.generate(() -> a).limit(count), IntStream.generate(() -> b).limit(count2))
                .toArray();
    }

    public static void main(String[] args) {
        int[] alternating = IntStream.range(0, 25_001).map(i -> i % 2 == 0 ? 8 : 3).toArray();

        Case[] cases = {
            new Case(new int[] {5, 5, 1, 1, 1, 5, 5}, 5),
            new Case(new int[] {2, 2, 2}, 2),
            new Case(new int[] {3, 2, 3}, 3),
            new Case(new int[] {7}, 7), // the smallest input
            new Case(new int[] {1, 2, 1}, 1), // the candidate is lost after 2, then won back
            new Case(new int[] {1, 2, 3, 4, 4, 4, 4}, 4), // majority all at the end
            new Case(new int[] {4, 4, 4, 4, 1, 2, 3}, 4), // majority all at the start
            new Case(new int[] {2, 1, 2, 3, 2}, 2), // 3 of 5: the bare minimum over n / 2
            new Case(new int[] {-1, -1, 2}, -1), // negative: bit 31 is the sign bit
            new Case(new int[] {0, 0, -5}, 0), // zero is a valid answer, not "not found"
            new Case(new int[] {-1_000_000_000, 1_000_000_000, -1_000_000_000}, -1_000_000_000),
            new Case(new int[] {1_000_000_000, -1_000_000_000, 1_000_000_000}, 1_000_000_000),
            new Case(runs(6, 25_000, 9, 24_999), 6), // length ceiling, the narrowest win
            new Case(alternating, 8), // alternating; Boyer-Moore's count hovers at 0
            new Case(runs(3, 1_000, 5, 1_001), 5), // brute force's worst case
        };

        Map<String, ToIntFunction<int[]>> solutions = new LinkedHashMap<>();
        solutions.put("brute force", MajorityElement::majorityElementBruteForce);
        solutions.put("hash map", MajorityElement::majorityElementHashMap);
        solutions.put("sorting", MajorityElement::majorityElementSorting);
        solutions.put("bit manipulation", MajorityElement::majorityElementBits);
        solutions.put("boyer-moore", MajorityElement::majorityElementBoyerMoore);
        solutions.put("randomization", MajorityElement::majorityElementRandom);
        Set<String> mutatesInput = Set.of("sorting");

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

        int[] original = {5, 5, 1, 1, 1, 5, 5};
        for (Map.Entry<String, ToIntFunction<int[]>> solution : solutions.entrySet()) {
            if (!mutatesInput.contains(solution.getKey())) {
                solution.getValue().applyAsInt(original);
            }
        }
        System.out.printf(
                "%s  input left unmodified (all but sorting): %s%n",
                Arrays.equals(original, new int[] {5, 5, 1, 1, 1, 5, 5}) ? "PASS" : "FAIL",
                Arrays.toString(original));
    }
}
