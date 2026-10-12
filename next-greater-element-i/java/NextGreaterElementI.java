import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

/**
 * Next Greater Element I (LeetCode 496).
 *
 * <p>nums1 is a subset of nums2, and all values are distinct. For each value in
 * nums1, find where it sits in nums2 and return the first value to its right
 * that is larger, or -1 if there is none.
 *
 * <p>The three approaches from
 * https://neetcode.io/solutions/next-greater-element-i, plus the classic
 * monotonic stack over all of nums2, ordered from brute force to optimal.
 */
public class NextGreaterElementI {

    /**
     * Scans nums2 from the right for each query. Time: O(m * n). Space: O(1).
     *
     * <p>m = nums1.length, n = nums2.length; the output array is not counted.
     *
     * <p>Walking right to left, every value bigger than the query overwrites the
     * candidate, so when the scan reaches the query itself the candidate is the
     * <em>nearest</em> bigger value to its right -- not the largest, which is
     * what it would be with a Math.max. Reaching the query ends the scan.
     */
    static int[] nextGreaterBruteForce(int[] nums1, int[] nums2) {
        int[] res = new int[nums1.length];
        for (int i = 0; i < nums1.length; i++) {
            int nextGreater = -1;
            for (int j = nums2.length - 1; j >= 0; j--) {
                if (nums2[j] > nums1[i]) {
                    nextGreater = nums2[j];
                } else if (nums2[j] == nums1[i]) {
                    break;
                }
            }
            res[i] = nextGreater;
        }
        return res;
    }

    /**
     * Walks nums2 once, scanning ahead only from query values.
     * Time: O(m * n). Space: O(m).
     *
     * <p>A value -&gt; index map of nums1 answers "is this a query, and where
     * does the answer go?" in O(1), so values of nums2 that nobody asked about
     * are skipped. For each query value it still scans forward to the first
     * bigger value, so the worst case -- nums2 in decreasing order, every value
     * a query -- is the same O(m * n) as the brute force.
     */
    static int[] nextGreaterHashMap(int[] nums1, int[] nums2) {
        Map<Integer, Integer> nums1Index = new HashMap<>();
        for (int i = 0; i < nums1.length; i++) {
            nums1Index.put(nums1[i], i);
        }

        int[] res = new int[nums1.length];
        Arrays.fill(res, -1);
        for (int i = 0; i < nums2.length; i++) {
            Integer index = nums1Index.get(nums2[i]);
            if (index == null) {
                continue;
            }
            for (int j = i + 1; j < nums2.length; j++) {
                if (nums2[j] > nums2[i]) {
                    res[index] = nums2[j];
                    break;
                }
            }
        }
        return res;
    }

    /**
     * Monotonic stack over all of nums2, then looks up each query.
     * Time: O(m + n). Space: O(n).
     *
     * <p>The classic answer to the follow-up. The stack holds values still
     * waiting for something bigger, and it is always decreasing from bottom to
     * top: a value is only pushed after everything smaller than it has been
     * popped. Each new value pops -- and answers -- every waiting value it
     * beats. Every value is pushed once and popped at most once, so the inner
     * while loop is O(n) across the whole run, not per step.
     *
     * <p>It answers every value in nums2, then reads off the ones nums1 asked
     * for. That wastes the map entries for values nobody asked about; approach
     * 4 avoids them.
     */
    static int[] nextGreaterStackAll(int[] nums1, int[] nums2) {
        Map<Integer, Integer> nextGreater = new HashMap<>();
        Deque<Integer> stack = new ArrayDeque<>();
        for (int num : nums2) {
            while (!stack.isEmpty() && stack.peek() < num) {
                nextGreater.put(stack.pop(), num);
            }
            stack.push(num);
        }

        int[] res = new int[nums1.length];
        for (int i = 0; i < nums1.length; i++) {
            res[i] = nextGreater.getOrDefault(nums1[i], -1);
        }
        return res;
    }

    /**
     * Monotonic stack holding only the query values.
     * Time: O(m + n). Space: O(m).
     *
     * <p>The optimal solution, and NeetCode's. Only values of nums1 are pushed,
     * so the stack and the map are both bounded by m instead of n, and each
     * answer is written straight into its slot in res.
     *
     * <p>Leaving the other values off the stack is safe. A value nobody asked
     * about never needs an answer, and it never blocks anything either: a value
     * that would have sat on the stack below it is answered by the same later
     * values either way. The stack is still decreasing, for the same reason as
     * approach 3 -- a query value is pushed only after everything smaller has
     * been popped.
     *
     * <p>ArrayDeque rather than NeetCode's java.util.Stack, which synchronizes
     * every call for no benefit. push, pop and peek all work on the head, so it
     * behaves as a stack.
     */
    static int[] nextGreaterStackQueries(int[] nums1, int[] nums2) {
        Map<Integer, Integer> nums1Index = new HashMap<>();
        for (int i = 0; i < nums1.length; i++) {
            nums1Index.put(nums1[i], i);
        }

        int[] res = new int[nums1.length];
        Arrays.fill(res, -1);
        Deque<Integer> stack = new ArrayDeque<>();
        for (int num : nums2) {
            while (!stack.isEmpty() && stack.peek() < num) {
                res[nums1Index.get(stack.pop())] = num;
            }
            if (nums1Index.containsKey(num)) {
                stack.push(num);
            }
        }
        return res;
    }

    /** One test case: two inputs and the expected answer. */
    private static class Case {
        final int[] nums1;
        final int[] nums2;
        final int[] expected;

        Case(int[] nums1, int[] nums2, int[] expected) {
            this.nums1 = nums1;
            this.nums2 = nums2;
            this.expected = expected;
        }
    }

    public static void main(String[] args) {
        // The ceiling: 1000 values. Decreasing, no value has an answer.
        int[] decreasing = IntStream.iterate(1000, v -> v - 1).limit(1000).toArray();
        int[] decreasingQueries = IntStream.iterate(1000, v -> v - 7).limit(143).toArray();
        int[] noAnswers = new int[decreasingQueries.length];
        Arrays.fill(noAnswers, -1);
        // Increasing: every answer is the very next value.
        int[] increasing = IntStream.rangeClosed(1, 1000).toArray();
        int[] increasingQueries = IntStream.iterate(1000, v -> v - 3).limit(334).toArray();
        int[] nextValues = Arrays.stream(increasingQueries).map(v -> v < 1000 ? v + 1 : -1).toArray();

        Case[] cases = {
            new Case(new int[] {4, 1, 2}, new int[] {1, 3, 4, 2}, new int[] {-1, 3, -1}),
            new Case(new int[] {2, 4}, new int[] {1, 2, 3, 4}, new int[] {3, -1}),
            new Case(new int[] {1}, new int[] {1}, new int[] {-1}), // nothing to the right
            new Case(new int[] {3, 1}, new int[] {1, 3}, new int[] {-1, 3}), // order differs
            new Case(new int[] {2}, new int[] {2, 1, 3}, new int[] {3}), // skips a smaller value
            new Case( // one value answers all
                    new int[] {1, 3, 5, 2, 4},
                    new int[] {6, 5, 4, 3, 2, 1, 7},
                    new int[] {7, 7, 7, 7, 7}),
            new Case(new int[] {5}, new int[] {5, 6, 9}, new int[] {6}), // nearest, not largest
            new Case(new int[] {0}, new int[] {0, 10000}, new int[] {10000}), // value bounds
            new Case(new int[] {3}, new int[] {1, 2, 3}, new int[] {-1}), // query is last
            new Case(new int[] {2}, new int[] {4, 2}, new int[] {-1}), // bigger on the left
            new Case(decreasingQueries, decreasing, noAnswers),
            new Case(increasingQueries, increasing, nextValues),
        };

        Map<String, BinaryOperator<int[]>> solutions = new LinkedHashMap<>();
        solutions.put("brute force", NextGreaterElementI::nextGreaterBruteForce);
        solutions.put("hash map", NextGreaterElementI::nextGreaterHashMap);
        solutions.put("stack, all of nums2", NextGreaterElementI::nextGreaterStackAll);
        solutions.put("stack, queries only", NextGreaterElementI::nextGreaterStackQueries);

        for (Map.Entry<String, BinaryOperator<int[]>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                int[] actual = solution.getValue().apply(testCase.nums1.clone(), testCase.nums2.clone());
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
    }
}
