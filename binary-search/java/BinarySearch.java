import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Binary Search (LeetCode 704).
 *
 * <p>Given an array {@code nums} of distinct integers sorted in ascending order
 * and an integer {@code target}, return the index of {@code target}, or -1 if it
 * is not there. The problem requires O(log n) time.
 *
 * <p>All five approaches from https://neetcode.io/solutions/binary-search, plus
 * the linear scan the problem exists to rule out, ordered from brute force to
 * optimal.
 *
 * <p>The last three do not test for equality inside the loop at all. They
 * compute an insertion point and check it once at the end -- see the README on
 * why that is the version worth internalising even though approach 3 is the one
 * to write here.
 */
public class BinarySearch {

    /** A solution, so the harness can run all six against the same cases. */
    @FunctionalInterface
    interface Search {
        int apply(int[] nums, int target);
    }

    /**
     * Walks the array. Time: O(n). Space: O(1).
     *
     * <p>The brute force, and the reason this problem is stated the way it is.
     * It is correct, it is the fastest thing to write, and it is the only
     * approach here that does not need the array to be sorted -- which is
     * exactly what makes it the wrong answer. LeetCode 704 names O(log n) in the
     * problem statement rather than leaving it to the follow-up, so a linear
     * scan is rejected on sight even though it passes every test below.
     *
     * <p>It is here to make the sortedness load-bearing. Everything after this
     * point is an answer to "what does knowing the array is sorted actually buy
     * you", and the answer is: every comparison discards half of what is left
     * instead of one element.
     */
    static int searchLinear(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /** Searches the closed interval [l, r]. Helper for {@link #searchRecursive}. */
    private static int searchRange(int[] nums, int target, int l, int r) {
        if (l > r) {
            return -1;
        }

        int m = l + (r - l) / 2;
        if (nums[m] == target) {
            return m;
        }
        return nums[m] < target
                ? searchRange(nums, target, m + 1, r)
                : searchRange(nums, target, l, m - 1);
    }

    /**
     * Halves the interval, then recurses into the half that can still hold the
     * target. Time: O(log n). Space: O(log n).
     *
     * <p>Space is O(log n) for the call stack, not O(1). Unlike Reverse Linked
     * List in this repo, that is harmless in either language here: the depth is
     * log2(n), so the maximum input of 10,000 elements recurses 14 frames deep.
     * The recursion is a style choice at these sizes, not a hazard.
     *
     * <p>{@code l > r} is the base case, and it is the empty interval: the two
     * pointers have crossed, so nothing is left to look at. It is reachable only
     * through {@code m + 1} or {@code m - 1}, which is also what guarantees
     * termination -- every call excludes the midpoint it just rejected, so the
     * interval strictly shrinks.
     *
     * <p>The interval here is closed on both ends, [l, r]: r starts at the last
     * index, the search runs while {@code l <= r}, and the rejecting branch
     * moves past the midpoint. Approaches 4 and 5 use the half-open [l, r)
     * instead. Both are correct; mixing them is not, and the README has the
     * table of what each mismatch does.
     */
    static int searchRecursive(int[] nums, int target) {
        return searchRange(nums, target, 0, nums.length - 1);
    }

    /**
     * The same halving, as a loop. Time: O(log n). Space: O(1).
     *
     * <p>The optimal solution and the one to write. It is approach 2 with the
     * interval carried in two variables instead of in call frames -- the
     * recursion there is tail-recursive, and this is what unrolling it by hand
     * looks like. Java does not eliminate tail calls, so here that unrolling is
     * the only way to get the O(1) space.
     *
     * <p>Two details are worth being able to defend out loud:
     *
     * <p>{@code m = l + (r - l) / 2} rather than {@code (l + r) / 2}. This is
     * the one that actually bites in Java: {@code l + r} is int arithmetic, so
     * it wraps negative once the sum passes 2^31, and {@code nums[m]} then
     * throws ArrayIndexOutOfBoundsException. It needs an array of about 1.07
     * billion ints -- 4 GiB, well past this problem's 10,000 -- but it is the
     * bug that sat in this very method in the JDK for nine years. The JDK's fix
     * was {@code (l + r) >>> 1}, which works because the unsigned shift reads
     * the wrapped sum's sign bit as value rather than as sign; the subtraction
     * form avoids the wrap altogether and reads better. Both verified in the
     * README.
     *
     * <p>{@code while (l <= r)}, not {@code l < r}. With r starting at the last
     * index, {@code l < r} exits while one candidate is still unexamined, so a
     * single-element array reports -1 for the element it contains. That is not
     * an edge case bolted on afterwards -- it is the last step of nearly every
     * successful search.
     */
    static int searchIterative(int[] nums, int target) {
        int l = 0;
        int r = nums.length - 1;

        while (l <= r) {
            int m = l + (r - l) / 2;
            if (nums[m] > target) {
                r = m - 1;
            } else if (nums[m] < target) {
                l = m + 1;
            } else {
                return m;
            }
        }

        return -1;
    }

    /**
     * Finds the first index past the target, then looks one to its left.
     * Time: O(log n). Space: O(1).
     *
     * <p>A different question with the same shape: this loop does not look for
     * the target, it looks for the boundary between the values {@code <= target}
     * and the values {@code > target}. It always runs to completion -- there is
     * no early exit, because there is nothing to exit early on.
     *
     * <p>The interval is half-open, [l, r): r starts at {@code nums.length}, one
     * past the end, the loop runs while {@code l < r}, and the rejecting branch
     * sets {@code r = m} rather than {@code m - 1}. m is being excluded either
     * way; here it is excluded by the interval's open right end instead of by
     * arithmetic. Note that r can legitimately equal {@code nums.length}, which
     * is why the loop must never evaluate {@code nums[r]}.
     *
     * <p>On exit l is the count of elements {@code <= target}, so if the target
     * is present it is the element just before: index {@code l - 1}. The
     * {@code nums[l - 1] == target} check is then what distinguishes "target is
     * here" from "target would go here", which the loop never asked.
     *
     * <p>{@code l > 0} is structural here. l is 0 exactly when nothing is
     * {@code <= target}, so dropping the guard means indexing {@code nums[-1]}
     * and throwing ArrayIndexOutOfBoundsException on every target below the
     * array -- not a rare edge, just "the target is small". The Python file
     * carries the same guard for a much weaker reason: negative indices are
     * legal there, so the same omission silently reads the last element and
     * still returns the right answer, failing only on an empty array. Same
     * line, same position, structural in one language and a formality in the
     * other. See the README.
     */
    static int searchUpperBound(int[] nums, int target) {
        int l = 0;
        int r = nums.length;

        while (l < r) {
            int m = l + (r - l) / 2;
            if (nums[m] > target) {
                r = m;
            } else {
                l = m + 1;
            }
        }

        return (l > 0 && nums[l - 1] == target) ? l - 1 : -1;
    }

    /**
     * Finds the first index not below the target, and checks whether it is the
     * target. Time: O(log n). Space: O(1).
     *
     * <p>Approach 4 with the comparison relaxed from {@code >} to {@code >=},
     * which slides the boundary from "first element past target" to "first
     * element not below target" -- so a present target lands on l itself rather
     * than one before it, and no offset is needed.
     *
     * <p>That single character is the whole difference, and it is the version to
     * reach for outside this problem. l is an insertion point: the index where
     * the target belongs whether or not it is there. That is strictly more than
     * "found or not", and it is what the neighbouring problems actually want --
     * first element >= x, count of elements < x, search-insert-position, and the
     * boundary hunts in rotated or implicit arrays where there is no equality
     * test to write.
     *
     * <p>Here the guard needs {@code l < nums.length} rather than approach 4's
     * {@code l > 0}, because the failure has moved to the other end: l runs past
     * the array when every element is below the target.
     */
    static int searchLowerBound(int[] nums, int target) {
        int l = 0;
        int r = nums.length;

        while (l < r) {
            int m = l + (r - l) / 2;
            if (nums[m] >= target) {
                r = m;
            } else {
                l = m + 1;
            }
        }

        return (l < nums.length && nums[l] == target) ? l : -1;
    }

    /**
     * Lets the standard library do it. Time: O(log n). Space: O(1).
     *
     * <p>Not what to submit -- the exercise is the loop -- but worth knowing
     * exists, and worth knowing that it is not the same function as Python's.
     * {@code Arrays.binarySearch} is documented to return an unspecified match
     * when there are duplicates, while {@code bisect_left} is specified to
     * return the first; both verified in the README. The difference is invisible
     * here, since the values are distinct.
     *
     * <p>The miss encoding is the part to actually remember. On failure this
     * returns {@code -(insertion point) - 1}, not -1 -- negative so that success
     * and failure are distinguishable by sign, offset by one so that an
     * insertion point of 0 does not collide with a found index of 0. Mapping
     * every negative to -1 is what this problem asks for and throws the
     * insertion point away; approach 5 is the same information kept.
     *
     * <p>The value returned for "smaller than everything" happens to be -1
     * already, which is a coincidence of that encoding rather than a shortcut to
     * rely on: -(0) - 1 = -1.
     */
    static int searchBuiltin(int[] nums, int target) {
        int index = Arrays.binarySearch(nums, target);
        return index >= 0 ? index : -1;
    }

    /** One test case: an array, a target, and the index expected back. */
    private static final class Case {
        final int[] nums;
        final int target;
        final int expected;

        Case(int[] nums, int target, int expected) {
            this.nums = nums;
            this.target = target;
            this.expected = expected;
        }
    }

    /** All 10,000 odd values in range: the largest input the constraints allow. */
    private static int[] max() {
        int[] values = new int[10000];
        for (int i = 0; i < values.length; i++) {
            values[i] = -9999 + i * 2;
        }
        return values;
    }

    public static void main(String[] args) {
        int[] example = {-1, 0, 2, 4, 6, 8};
        int[] bounds = {-9999, 0, 9999};
        int[] negatives = {-5, -3, -1};
        int[] largest = max();

        List<Case> cases = new ArrayList<>();
        cases.add(new Case(example, 4, 3)); // the NeetCode example: present
        cases.add(new Case(example, 3, -1)); // the NeetCode example: absent, between two elements
        cases.add(new Case(new int[] {1}, 1, 0)); // single element, present: lost by `l < r` here
        cases.add(new Case(new int[] {1}, 2, -1)); // single element, absent above
        cases.add(new Case(new int[] {1}, 0, -1)); // single element, absent below
        cases.add(new Case(new int[] {}, 5, -1)); // empty: outside the constraints, must not throw
        cases.add(new Case(new int[] {1, 2}, 1, 0)); // even length, first
        cases.add(new Case(new int[] {1, 2}, 2, 1)); // even length, last
        cases.add(new Case(new int[] {1, 2, 3}, 1, 0)); // odd length, first
        cases.add(new Case(new int[] {1, 2, 3}, 3, 2)); // odd length, last: only after l and r meet
        cases.add(new Case(new int[] {1, 2, 3, 4}, 1, 0)); // both ends of an even length,
        cases.add(new Case(new int[] {1, 2, 3, 4}, 4, 3)); // where the halves are uneven
        cases.add(new Case(bounds, -9999, 0)); // the value bounds, low -- strict, so this is the extreme
        cases.add(new Case(bounds, 9999, 2)); // the value bounds, high
        cases.add(new Case(bounds, 9998, -1)); // absent, just under the top
        cases.add(new Case(negatives, -4, -1)); // all negative, absent between two elements
        cases.add(new Case(negatives, -6, -1)); // absent below everything: approach 4's guard
        cases.add(new Case(largest, -9999, 0)); // largest legal input, first element
        cases.add(new Case(largest, 9999, 9999)); // largest legal input, last element
        cases.add(new Case(largest, 0, -1)); // largest legal input, absent: it holds only odd values

        Map<String, Search> solutions = new LinkedHashMap<>();
        solutions.put("linear", BinarySearch::searchLinear);
        solutions.put("recursion", BinarySearch::searchRecursive);
        solutions.put("iteration", BinarySearch::searchIterative);
        solutions.put("upper bound", BinarySearch::searchUpperBound);
        solutions.put("lower bound", BinarySearch::searchLowerBound);
        solutions.put("built-in", BinarySearch::searchBuiltin);

        for (Map.Entry<String, Search> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case test : cases) {
                if (solution.getValue().apply(test.nums, test.target) == test.expected) {
                    passed++;
                }
            }
            System.out.printf(
                    "%s  %s: %d/%d cases%n",
                    passed == cases.size() ? "PASS" : "FAIL",
                    solution.getKey(),
                    passed,
                    cases.size());
        }
    }
}
