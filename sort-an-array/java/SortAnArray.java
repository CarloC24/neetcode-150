import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.UnaryOperator;

/**
 * Sort an Array (LeetCode 912).
 *
 * <p>Given an array of integers nums, sort it in ascending order and return
 * it, without built-in sort functions, in O(n log n) time and the smallest
 * space possible.
 *
 * <p>The six approaches from https://neetcode.io/solutions/sort-an-array, plus
 * the library sort as a baseline. Every approach except radix sort sorts nums
 * in place and returns it.
 */
public class SortAnArray {

    /**
     * Calls the library sort. Time: O(n log n). Space: O(log n).
     *
     * <p>Not allowed by the problem. It's here as the baseline, not as an
     * answer.
     */
    static int[] sortArrayBuiltin(int[] nums) {
        Arrays.sort(nums);
        return nums;
    }

    // --- 1. Quick sort -------------------------------------------------------

    /**
     * Median-of-three, Hoare-style partition of nums[left..right].
     *
     * <p>Needs right - left >= 2. Moves the middle element to left + 1, then
     * sorts nums[left], nums[left + 1] and nums[right]. The median ends up at
     * left + 1 and becomes the pivot. The other two act as sentinels:
     * nums[left] <= pivot stops j, and nums[right] >= pivot stops i, so
     * neither scan needs a bounds check.
     *
     * <p>Both scans stop on values equal to the pivot. That costs a few extra
     * swaps, but it splits an array of equal values down the middle instead of
     * peeling off one element at a time.
     *
     * <p>Returns the pivot's final index j. Afterwards nums[left..j-1] <= pivot
     * and nums[j+1..right] >= pivot.
     */
    private static int partition(int[] nums, int left, int right) {
        int mid = (left + right) >>> 1;
        swap(nums, mid, left + 1);

        if (nums[left] > nums[right]) {
            swap(nums, left, right);
        }
        if (nums[left + 1] > nums[right]) {
            swap(nums, left + 1, right);
        }
        if (nums[left] > nums[left + 1]) {
            swap(nums, left, left + 1);
        }

        int pivot = nums[left + 1];
        int i = left + 1;
        int j = right;
        while (true) {
            while (nums[++i] < pivot) {}
            while (nums[--j] > pivot) {}
            if (i > j) {
                break;
            }
            swap(nums, i, j);
        }

        swap(nums, left + 1, j);
        return j;
    }

    private static void quickSort(int[] nums, int left, int right) {
        while (right - left >= 2) {
            int j = partition(nums, left, right);
            if (j - left < right - j) {
                quickSort(nums, left, j - 1);
                left = j + 1;
            } else {
                quickSort(nums, j + 1, right);
                right = j - 1;
            }
        }
        if (right == left + 1 && nums[right] < nums[left]) {
            swap(nums, left, right);
        }
    }

    /**
     * Quick sort with a median-of-three pivot.
     * Time: O(n log n) average, O(n^2) worst. Space: O(log n).
     *
     * <p>NeetCode's partition. One change: it recurses into the smaller side
     * and loops on the larger one. That caps the stack at O(log n) even on
     * inputs that make the pivot choice bad, where recursing into both sides
     * could go n levels deep.
     */
    static int[] sortArrayQuick(int[] nums) {
        quickSort(nums, 0, nums.length - 1);
        return nums;
    }

    private static void swap(int[] nums, int a, int b) {
        int tmp = nums[a];
        nums[a] = nums[b];
        nums[b] = tmp;
    }

    // --- 2. Merge sort -------------------------------------------------------

    private static void merge(int[] nums, int lo, int mid, int hi) {
        int[] left = Arrays.copyOfRange(nums, lo, mid + 1);
        int[] right = Arrays.copyOfRange(nums, mid + 1, hi + 1);
        int i = lo;
        int j = 0;
        int k = 0;
        while (j < left.length && k < right.length) {
            if (left[j] <= right[k]) {
                nums[i++] = left[j++];
            } else {
                nums[i++] = right[k++];
            }
        }
        // At most one of these has anything left.
        while (j < left.length) {
            nums[i++] = left[j++];
        }
        while (k < right.length) {
            nums[i++] = right[k++];
        }
    }

    private static void mergeSort(int[] nums, int lo, int hi) {
        if (lo >= hi) {
            return;
        }
        int mid = (lo + hi) >>> 1;
        mergeSort(nums, lo, mid);
        mergeSort(nums, mid + 1, hi);
        merge(nums, lo, mid, hi);
    }

    /**
     * Top-down merge sort. Time: O(n log n). Space: O(n).
     *
     * <p>Split in half, sort each half, then merge the two sorted halves. The
     * only approach here that is O(n log n) in the worst case and stable. The
     * price is the O(n) of copies each merge makes.
     *
     * <p>Ties take from the left half (<=), which is what keeps it stable.
     * NeetCode's Java merges into an ArrayList of Integer; plain int arrays
     * avoid the boxing.
     */
    static int[] sortArrayMerge(int[] nums) {
        mergeSort(nums, 0, nums.length - 1);
        return nums;
    }

    // --- 3. Heap sort --------------------------------------------------------

    /**
     * Pushes nums[i] down until it's no smaller than its children.
     *
     * <p>Only looks at nums[0..n-1]. The children of i are 2i + 1 and 2i + 2.
     * NeetCode writes this recursively, which costs O(log n) stack. A loop
     * does the same in O(1).
     */
    private static void siftDown(int[] nums, int n, int i) {
        while (true) {
            int largest = i;
            int l = 2 * i + 1;
            int r = 2 * i + 2;
            if (l < n && nums[l] > nums[largest]) {
                largest = l;
            }
            if (r < n && nums[r] > nums[largest]) {
                largest = r;
            }
            if (largest == i) {
                return;
            }
            swap(nums, i, largest);
            i = largest;
        }
    }

    /**
     * Heap sort. Time: O(n log n). Space: O(1).
     *
     * <p>Turn the array into a max-heap in place, from the last parent
     * n / 2 - 1 back to the root. Then swap the largest value (the root) to
     * the end, shrink the heap by one and sift the new root down. The sorted
     * part grows from the right.
     *
     * <p>O(n log n) in the worst case and O(1) extra space: it meets the
     * problem's ask best on paper. It's not stable, and it jumps around the
     * array, so in practice it's slower than quick sort.
     */
    static int[] sortArrayHeap(int[] nums) {
        int n = nums.length;
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(nums, n, i);
        }
        for (int end = n - 1; end > 0; end--) {
            swap(nums, 0, end);
            siftDown(nums, end, 0);
        }
        return nums;
    }

    // --- 4. Counting sort ----------------------------------------------------

    /**
     * Counting sort over [min, max]. Time: O(n + k). Space: O(k).
     *
     * <p>k = max - min + 1. Count each value, then write the values back in
     * order. It never compares two elements, so the O(n log n) bound doesn't
     * apply. Here values lie in [-50,000, 50,000], so k is at most 100,001 and
     * this is the fastest approach.
     *
     * <p>NeetCode counts in a HashMap. An array indexed by value - min is the
     * same idea without the hashing or boxing.
     */
    static int[] sortArrayCounting(int[] nums) {
        int lo = nums[0];
        int hi = nums[0];
        for (int num : nums) {
            lo = Math.min(lo, num);
            hi = Math.max(hi, num);
        }
        int[] count = new int[hi - lo + 1];
        for (int num : nums) {
            count[num - lo]++;
        }

        int i = 0;
        for (int offset = 0; offset < count.length; offset++) {
            for (int c = 0; c < count[offset]; c++) {
                nums[i++] = offset + lo;
            }
        }
        return nums;
    }

    // --- 5. Radix sort -------------------------------------------------------

    /**
     * LSD radix sort, base 10, of non-negative ints, in place.
     *
     * <p>One stable counting sort per digit, ones digit first. Stability is
     * what makes it work: after sorting by the tens digit, numbers with the
     * same tens digit stay in ones-digit order.
     */
    private static void radixSortNonNegative(int[] arr) {
        if (arr.length == 0) {
            return;
        }
        int biggest = Arrays.stream(arr).max().getAsInt();
        int[] out = new int[arr.length];
        for (long place = 1; biggest / place > 0; place *= 10) {
            int[] count = new int[10];
            for (int num : arr) {
                count[(int) (num / place % 10)]++;
            }
            for (int d = 1; d < 10; d++) {
                count[d] += count[d - 1]; // count[d] = where digit d's run ends
            }
            for (int i = arr.length - 1; i >= 0; i--) { // back to front keeps it stable
                int d = (int) (arr[i] / place % 10);
                out[--count[d]] = arr[i];
            }
            System.arraycopy(out, 0, arr, 0, arr.length);
        }
    }

    /**
     * LSD radix sort, negatives handled separately.
     * Time: O(d * n), d = digits in the largest |value|. Space: O(n).
     *
     * <p>Digits only make sense for non-negative numbers, so split the array.
     * Sort the absolute values of the negatives, then reverse and negate them:
     * the biggest absolute value is the smallest negative. Then append the
     * sorted non-negatives.
     *
     * <p>Returns a new array; nums itself isn't sorted. (Negating
     * Integer.MIN_VALUE overflows, but the constraints rule it out.)
     */
    static int[] sortArrayRadix(int[] nums) {
        int[] negatives = Arrays.stream(nums).filter(x -> x < 0).map(x -> -x).toArray();
        int[] nonNegatives = Arrays.stream(nums).filter(x -> x >= 0).toArray();
        radixSortNonNegative(negatives);
        radixSortNonNegative(nonNegatives);

        int[] res = new int[nums.length];
        int i = 0;
        for (int k = negatives.length - 1; k >= 0; k--) {
            res[i++] = -negatives[k];
        }
        for (int num : nonNegatives) {
            res[i++] = num;
        }
        return res;
    }

    // --- 6. Shell sort -------------------------------------------------------

    /**
     * Shell sort with gaps n/2, n/4, ..., 1.
     * Time: O(n^2) worst with these gaps, much faster in practice. Space: O(1).
     *
     * <p>Insertion sort, but comparing elements gap apart. Big gaps move values
     * long distances in a few steps, so by the time gap is 1 (a plain insertion
     * sort) the array is almost sorted and there's little left to do.
     *
     * <p>NeetCode lists it as O(n log n) on average. With Shell's halving gaps
     * that isn't guaranteed. Better gap sequences (Knuth's 1, 4, 13, 40, ...)
     * give O(n^1.5).
     */
    static int[] sortArrayShell(int[] nums) {
        int n = nums.length;
        for (int gap = n / 2; gap >= 1; gap /= 2) {
            for (int i = gap; i < n; i++) {
                int tmp = nums[i];
                int j = i - gap;
                while (j >= 0 && nums[j] > tmp) {
                    nums[j + gap] = nums[j];
                    j -= gap;
                }
                nums[j + gap] = tmp;
            }
        }
        return nums;
    }

    public static void main(String[] args) {
        final int n = 50_000;
        Random rng = new Random(912);
        int[] sorted = new int[n];
        int[] reversed = new int[n];
        int[] allSeven = new int[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = i;
            reversed[i] = n - i;
            allSeven[i] = 7;
        }
        int[] random = rng.ints(n, -50_000, 50_001).toArray();
        int[] duplicates = rng.ints(n, -3, 4).toArray();

        int[][] cases = {
            {5, 2, 3, 1},
            {5, 1, 1, 2, 0, 0},
            {10, 9, 1, 1, 1, 2, 3, 1}, // NeetCode example 1
            {5, 10, 2, 1, 3}, // NeetCode example 2
            {1}, // the smallest input
            {2, 1},
            {1, 2, 3}, // three elements: the smallest partition
            {3, 3, 3, 3}, // all equal
            {-1, -3, -2, 0}, // negatives and zero
            {-50_000, 50_000, 0, -50_000}, // both value limits
            {100, -100, 10, -10, 1, -1, 0}, // several digit counts, both signs
            sorted, // sorted, length ceiling
            reversed, // reversed, length ceiling
            allSeven, // all equal, length ceiling
            random, // random, seeded
            duplicates, // many duplicates, seeded
        };

        Map<String, UnaryOperator<int[]>> solutions = new LinkedHashMap<>();
        solutions.put("built-in sort", SortAnArray::sortArrayBuiltin);
        solutions.put("quick sort", SortAnArray::sortArrayQuick);
        solutions.put("merge sort", SortAnArray::sortArrayMerge);
        solutions.put("heap sort", SortAnArray::sortArrayHeap);
        solutions.put("counting sort", SortAnArray::sortArrayCounting);
        solutions.put("radix sort", SortAnArray::sortArrayRadix);
        solutions.put("shell sort", SortAnArray::sortArrayShell);

        for (Map.Entry<String, UnaryOperator<int[]>> solution : solutions.entrySet()) {
            int passed = 0;
            for (int[] testCase : cases) {
                int[] expected = testCase.clone();
                Arrays.sort(expected);
                int[] result = solution.getValue().apply(testCase.clone());
                if (Arrays.equals(result, expected)) {
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
