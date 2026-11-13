import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

/**
 * Majority Element II (LeetCode 229).
 *
 * <p>Given an integer array nums of size n, return every value that appears
 * more than n / 3 times, in any order. Follow-up: O(n) time and O(1) space.
 *
 * <p>The five approaches from https://neetcode.io/solutions/majority-element-ii,
 * ordered from brute force to Boyer-Moore voting with two candidates.
 *
 * <p>At most two values can appear more than n / 3 times: three of them would
 * need more than n elements. That's why the answer has at most two values, and
 * why two candidates are enough.
 */
public class MajorityElementII {

    /**
     * Counts every element with a full scan. Time: O(n^2). Space: O(1).
     *
     * <p>The set keeps each answer once, however many times it's counted. It
     * never holds more than two values. Too slow for n = 50,000, so the harness
     * skips the long cases for this one.
     */
    static List<Integer> majorityElementBruteForce(int[] nums) {
        Set<Integer> res = new HashSet<>();
        for (int num : nums) {
            if (res.contains(num)) {
                continue;
            }
            int count = 0;
            for (int other : nums) {
                if (other == num) {
                    count++;
                }
            }
            if (count > nums.length / 3) {
                res.add(num);
            }
        }
        return new ArrayList<>(res);
    }

    /**
     * Sorts, then measures each run of equal values.
     * Time: O(n log n). Space: O(n) here, for the sorted copy.
     *
     * <p>In sorted order, equal values sit together, so each value's count is
     * the length of its run. NeetCode sorts nums in place, which saves the copy
     * but changes the caller's array. This sorts a copy instead.
     */
    static List<Integer> majorityElementSorting(int[] nums) {
        int[] sorted = nums.clone();
        Arrays.sort(sorted);
        List<Integer> res = new ArrayList<>();
        int n = sorted.length;
        int i = 0;
        while (i < n) {
            int j = i;
            while (j < n && sorted[j] == sorted[i]) {
                j++;
            }
            if (j - i > n / 3) { // the run is sorted[i .. j - 1]
                res.add(sorted[i]);
            }
            i = j;
        }
        return res;
    }

    /** Counts everything in a hash map. Time: O(n). Space: O(n). */
    static List<Integer> majorityElementCounter(int[] nums) {
        Map<Integer, Integer> count = new HashMap<>();
        for (int num : nums) {
            count.merge(num, 1, Integer::sum);
        }
        List<Integer> res = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : count.entrySet()) {
            if (entry.getValue() > nums.length / 3) {
                res.add(entry.getKey());
            }
        }
        return res;
    }

    /**
     * Boyer-Moore voting with two candidates. Time: O(n). Space: O(1).
     *
     * <p>Keep two candidates and a count for each. A value that matches a
     * candidate votes for it. A value that matches neither takes an empty slot
     * if there is one; otherwise it cancels one vote from each candidate.
     *
     * <p>Each cancellation removes three different values at once. A value that
     * appears more than n / 3 times can't be cancelled out completely, so it
     * ends up as a candidate. The reverse isn't true: a candidate may not be a
     * majority at all ([1, 2, 3] leaves 3 behind). The second pass counts both
     * candidates for real.
     *
     * <p>Order matters in the first loop: check both candidates before looking
     * for an empty slot. Otherwise a value can take slot 1 while it's already
     * the candidate in slot 2. See the README.
     *
     * <p>The -1 starting values are just placeholders. -1 is a legal input, and
     * that's fine: a slot with count 0 that matches the next value simply
     * starts counting it, which is what taking the empty slot would do.
     */
    static List<Integer> majorityElementBoyerMoore(int[] nums) {
        int n = nums.length;
        int num1 = -1;
        int num2 = -1;
        int cnt1 = 0;
        int cnt2 = 0;
        for (int num : nums) {
            if (num == num1) {
                cnt1++;
            } else if (num == num2) {
                cnt2++;
            } else if (cnt1 == 0) {
                num1 = num;
                cnt1 = 1;
            } else if (cnt2 == 0) {
                num2 = num;
                cnt2 = 1;
            } else {
                cnt1--;
                cnt2--;
            }
        }

        // The counts above are votes left over, not occurrences. Count again.
        cnt1 = 0;
        cnt2 = 0;
        for (int num : nums) {
            if (num == num1) {
                cnt1++;
            } else if (num == num2) { // else: if num1 == num2, count it once
                cnt2++;
            }
        }

        List<Integer> res = new ArrayList<>();
        if (cnt1 > n / 3) {
            res.add(num1);
        }
        if (cnt2 > n / 3) {
            res.add(num2);
        }
        return res;
    }

    /**
     * Boyer-Moore voting with the candidates in a hash map.
     * Time: O(n). Space: O(1), the map never holds more than three entries.
     *
     * <p>The same voting, written so it generalizes. Add each value's vote. If
     * that makes three candidates, take one vote from each of them, which
     * removes the newcomer (it only had one) and drops anyone else who reaches
     * 0. For "more than n / k times", allow k - 1 candidates instead of 2.
     *
     * <p>Then count each surviving candidate for real, as in approach 4.
     */
    static List<Integer> majorityElementBoyerMooreMap(int[] nums) {
        Map<Integer, Integer> count = new HashMap<>();
        for (int num : nums) {
            count.merge(num, 1, Integer::sum);
            if (count.size() > 2) {
                Map<Integer, Integer> next = new HashMap<>();
                for (Map.Entry<Integer, Integer> entry : count.entrySet()) {
                    if (entry.getValue() > 1) {
                        next.put(entry.getKey(), entry.getValue() - 1);
                    }
                }
                count = next;
            }
        }

        List<Integer> res = new ArrayList<>();
        for (int candidate : count.keySet()) {
            int frequency = 0;
            for (int num : nums) {
                if (num == candidate) {
                    frequency++;
                }
            }
            if (frequency > nums.length / 3) {
                res.add(candidate);
            }
        }
        return res;
    }

    private static List<Integer> expected(int[] nums) {
        List<Integer> res = majorityElementCounter(nums);
        Collections.sort(res);
        return res;
    }

    private static int[] shuffled(List<Integer> values, Random rng) {
        Collections.shuffle(values, rng);
        return values.stream().mapToInt(Integer::intValue).toArray();
    }

    public static void main(String[] args) {
        final int n = 50_000;
        Random rng = new Random(229);

        int[][] shortCases = {
            {3, 2, 3}, // LeetCode example 1
            {1}, // LeetCode example 2, the smallest input
            {1, 2}, // LeetCode example 3: n / 3 == 0, so both count
            {5, 2, 3, 2, 2, 2, 2, 5, 5, 5}, // NeetCode example 1: two answers
            {4, 4, 4, 4, 4}, // NeetCode example 2
            {1, 2, 3}, // NeetCode example 3: no answer, but 3 is left as a candidate
            {1, 1, 2, 2, 3, 3}, // each exactly n / 3 times: not enough
            {1, 1, 1, 2, 2, 3, 3}, // n = 7, 1 appears 3 > 2 times
            {-1, -1, -1}, // -1, the placeholder value, as the answer
            {-1, 5, 5}, // -1 present but not an answer
            {2, 1, 1, 3, 1, 4, 5, 6}, // the majority is cancelled down, then recovers
            {1_000_000_000, -1_000_000_000, 1_000_000_000}, // the value limits
            {1, 1}, // breaks checking empty slots first: 1 fills both slots
            {1, 1, 2, 2, 2}, // ...and here that loses the 1 altogether
            {1, 1, 2}, // breaks skipping the second pass, and >= instead of >
        };

        int[] allSeven = new int[n];
        Arrays.fill(allSeven, 7);
        // two values 16,667 times each (just over 50,000 / 3 == 16,666)
        List<Integer> two = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            two.add(i < 16_667 ? 1 : i < 33_334 ? 2 : i);
        }
        // one value exactly 16,666 times: not enough
        List<Integer> exact = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            exact.add(i < 16_666 ? 1 : i);
        }
        int[][] longCases = {
            allSeven,
            shuffled(two, rng),
            shuffled(exact, rng),
            rng.ints(n, -3, 4).toArray(), // seven values, roughly n / 7 each
        };

        Map<String, Function<int[], List<Integer>>> solutions = new LinkedHashMap<>();
        solutions.put("brute force", MajorityElementII::majorityElementBruteForce);
        solutions.put("sorting", MajorityElementII::majorityElementSorting);
        solutions.put("hash map", MajorityElementII::majorityElementCounter);
        solutions.put("boyer-moore", MajorityElementII::majorityElementBoyerMoore);
        solutions.put("boyer-moore, hash map", MajorityElementII::majorityElementBoyerMooreMap);

        for (Map.Entry<String, Function<int[], List<Integer>>> solution : solutions.entrySet()) {
            boolean runsLong = !solution.getKey().equals("brute force");
            List<int[]> cases = new ArrayList<>(Arrays.asList(shortCases));
            if (runsLong) {
                cases.addAll(Arrays.asList(longCases));
            }
            int passed = 0;
            for (int[] testCase : cases) {
                int[] nums = testCase.clone();
                List<Integer> result = new ArrayList<>(solution.getValue().apply(nums));
                Collections.sort(result);
                // any order is fine, but no repeats and no changes to the input
                if (result.equals(expected(testCase)) && Arrays.equals(nums, testCase)) {
                    passed++;
                }
            }
            System.out.printf(
                    "%s  %s: %d/%d cases%s%n",
                    passed == cases.size() ? "PASS" : "FAIL",
                    solution.getKey(),
                    passed,
                    cases.size(),
                    runsLong ? "" : " (skips the " + longCases.length + " long cases)");
        }
    }
}
