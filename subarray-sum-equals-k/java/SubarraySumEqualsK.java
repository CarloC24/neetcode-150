import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Subarray Sum Equals K (LeetCode 560).
 *
 * <p>Given an array of integers nums and an integer k, return how many
 * subarrays (contiguous, non-empty) sum to k. Values can be negative.
 *
 * <p>The two approaches from https://neetcode.io/solutions/subarray-sum-equals-k.
 *
 * <p>int is enough everywhere: prefix sums stay within 20,000 * 1,000 = 2 * 10^7,
 * and the answer is at most 20,000 * 20,001 / 2, about 2 * 10^8.
 */
public class SubarraySumEqualsK {

    interface Solution {
        int subarraySum(int[] nums, int k);
    }

    /**
     * Tries every start, extends to every end. Time: O(n^2). Space: O(1).
     *
     * <p>For each start i, keep a running total as the end j moves right, so
     * each subarray's sum costs O(1) instead of a fresh loop. Don't stop when
     * the total passes k: with negative values it can come back down.
     */
    static int subarraySumBruteForce(int[] nums, int k) {
        int res = 0;
        for (int i = 0; i < nums.length; i++) {
            int total = 0;
            for (int j = i; j < nums.length; j++) {
                total += nums[j];
                if (total == k) {
                    res++;
                }
            }
        }
        return res;
    }

    /**
     * Prefix sums, with a hash map of how often each one has occurred.
     * Time: O(n). Space: O(n).
     *
     * <p>Let prefix be the sum of everything so far. A subarray that ends here
     * and sums to k starts right after some earlier point where the prefix sum
     * was prefix - k. So the number of such subarrays is the number of earlier
     * points with that prefix sum, and a hash map counts them.
     *
     * <p>Two details. The map starts as {0: 1}: the empty prefix before index
     * 0. Without it, subarrays that start at index 0 are never counted. And
     * look up prefix - k before adding the current prefix. Otherwise, with
     * k = 0, the current point pairs with itself: an empty subarray.
     */
    static int subarraySumPrefixMap(int[] nums, int k) {
        int res = 0;
        int prefix = 0;
        Map<Integer, Integer> seen = new HashMap<>();
        seen.put(0, 1); // the empty prefix
        for (int num : nums) {
            prefix += num;
            res += seen.getOrDefault(prefix - k, 0); // subarrays ending here that sum to k
            seen.merge(prefix, 1, Integer::sum);
        }
        return res;
    }

    private static int[] repeat(int[] pattern, int times) {
        int[] out = new int[pattern.length * times];
        for (int i = 0; i < out.length; i++) {
            out[i] = pattern[i % pattern.length];
        }
        return out;
    }

    public static void main(String[] args) {
        final int n = 20_000;
        Random rng = new Random(560);

        // {nums, {k}, {expected}}. A null expected is checked against brute force.
        List<Object[]> shortCases = new ArrayList<>(List.of(
                new Object[] {new int[] {1, 1, 1}, 2, 2}, // LeetCode example 1
                new Object[] {new int[] {1, 2, 3}, 3, 2}, // LeetCode example 2
                new Object[] {new int[] {2, -1, 1, 2}, 2, 4}, // NeetCode example 1
                new Object[] {new int[] {4, 4, 4, 4, 4, 4}, 4, 6}, // NeetCode example 2
                new Object[] {new int[] {1}, 1, 1}, // breaks a map that doesn't start with {0: 1}
                new Object[] {new int[] {1}, 0, 0}, // breaks adding the prefix before looking it up
                new Object[] {new int[] {0, 0, 0}, 0, 6}, // every subarray sums to 0; also breaks a sliding window
                new Object[] {new int[] {1, -1, 1}, 1, 3}, // breaks a sliding window: negatives
                new Object[] {new int[] {-1, -1}, -2, 1}, // the smallest input that breaks a sliding window
                new Object[] {new int[] {-1, -1, 1}, 0, 1},
                new Object[] {new int[] {3, 4, 7, 2, -3, 1, 4, 2}, 7, 4},
                new Object[] {new int[] {5}, 7, 0})); // nothing sums to k
        // random arrays with small values, so many subarrays hit k (seeded)
        for (int c = 0; c < 5; c++) {
            shortCases.add(new Object[] {rng.ints(300, -3, 4).toArray(), rng.nextInt(11) - 5, null});
        }
        shortCases.add(new Object[] {rng.ints(2_000, -1_000, 1_001).toArray(), 0, null});
        for (Object[] c : shortCases) {
            if (c[2] == null) {
                c[2] = subarraySumBruteForce((int[]) c[0], (int) c[1]);
            }
        }

        int[] minusThousands = new int[n];
        Arrays.fill(minusThousands, -1_000);
        int[] thousands = new int[n];
        Arrays.fill(thousands, 1_000);
        List<Object[]> longCases = List.of(
                // every one of the 20,000 * 20,001 / 2 subarrays sums to 0
                new Object[] {new int[n], 0, 200_010_000},
                // prefix sums alternate 1, 0, 1, 0: C(10,001, 2) + C(10,000, 2) pairs
                new Object[] {repeat(new int[] {1, -1}, n / 2), 0, 100_000_000},
                // windows of 1,000 ones: 20,000 - 1,000 + 1 of them
                new Object[] {repeat(new int[] {1}, n), 1_000, 19_001},
                // value and k limits: only the whole array reaches 2 * 10^7
                new Object[] {thousands, 20_000_000, 1},
                // windows of 10,000 values of -1,000: 10,001 of them
                new Object[] {minusThousands, -10_000_000, 10_001});

        Map<String, Solution> solutions = new LinkedHashMap<>();
        solutions.put("brute force", SubarraySumEqualsK::subarraySumBruteForce);
        solutions.put("prefix sums + hash map", SubarraySumEqualsK::subarraySumPrefixMap);

        for (Map.Entry<String, Solution> solution : solutions.entrySet()) {
            boolean runsLong = !solution.getKey().equals("brute force");
            List<Object[]> cases = new ArrayList<>(shortCases);
            if (runsLong) {
                cases.addAll(longCases);
            }
            int passed = 0;
            for (Object[] c : cases) {
                int[] nums = ((int[]) c[0]).clone();
                if (solution.getValue().subarraySum(nums, (int) c[1]) == (int) c[2]) {
                    passed++;
                }
            }
            System.out.printf(
                    "%s  %s: %d/%d cases%s%n",
                    passed == cases.size() ? "PASS" : "FAIL",
                    solution.getKey(),
                    passed,
                    cases.size(),
                    runsLong ? "" : " (skips the " + longCases.size() + " long cases)");
        }
    }
}
