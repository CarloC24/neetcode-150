# Sort an Array

LeetCode 912 · [NeetCode solution page](https://neetcode.io/solutions/sort-an-array)

Given an array of integers `nums`, sort it in ascending order and return it.
You may not use built-in sort functions, and the solution must run in
O(n log n) time with the smallest space complexity possible.

`[5, 2, 3, 1]` becomes `[1, 2, 3, 5]`.
`[5, 1, 1, 2, 0, 0]` becomes `[0, 0, 1, 1, 2, 5]`.

**Constraints:** `1 <= n <= 5 * 10^4`, `-5 * 10^4 <= nums[i] <= 5 * 10^4`.

```
sort-an-array/
├── java/
│   └── SortAnArray.java
├── python/
│   └── SortAnArray.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Extra space | Stable | Idea |
|---|----------|------|-------------|--------|------|
| 0 | Built-in sort | O(n log n) | O(n) Python, O(log n) Java | Python yes, Java n/a | Not allowed; kept as a baseline. |
| 1 | **Quick sort** | **O(n log n) avg, O(n²) worst** | **O(log n)** | No | Partition around a median-of-three pivot, recurse. |
| 2 | Merge sort | O(n log n) | O(n) | Yes | Sort each half, merge them. |
| 3 | **Heap sort** | **O(n log n)** | **O(1)** | No | Build a max-heap, pop the max to the end n times. |
| 4 | Counting sort | O(n + k) | O(k) | — | Count each value in `[min, max]`, write them back. |
| 5 | Radix sort | O(d · n) | O(n) | Yes | Stable sort by each decimal digit, ones first. |
| 6 | Shell sort | O(n²) worst, fast in practice | O(1) | No | Insertion sort over shrinking gaps. |

`k = max - min + 1` (at most 100,001 here), `d` = digits in the largest
absolute value (at most 5 here). Approaches 1–6 are NeetCode's.

**Which one to reach for:**

- **Heap sort** is the only one that meets the problem's ask on every input:
  O(n log n) worst case with O(1) extra space.
- **Quick sort** is what you'd write in practice. It's usually fastest among the
  comparison sorts, but its worst case is O(n²).
- **Merge sort** is the one to know cold. It's guaranteed O(n log n), stable,
  and the merge step reappears in Merge Two Sorted Lists, Merge K Sorted Lists
  and counting inversions.
- **Counting sort** is the fastest here, because the value range is small. It
  isn't a comparison sort, so the n log n lower bound doesn't apply to it.

## Quick sort: the partition

NeetCode's partition is more careful than the textbook "last element as pivot",
which goes O(n²) on an already sorted array. For `nums[left..right]`:

1. Swap the middle element to `left + 1`.
2. Sort the three values at `left`, `left + 1` and `right`. The median is now at
   `left + 1` and is the pivot.
3. Scan `i` right from `left + 1` while `nums[i] < pivot`, and `j` left from
   `right` while `nums[j] > pivot`. If they haven't crossed, swap and keep going.
4. Swap the pivot into `j`. Everything left of `j` is `<= pivot`, everything
   right of it is `>= pivot`.

Step 2 is what makes step 3 safe without bounds checks. `nums[right]` is
`>= pivot`, so `i` stops there at the latest, and `nums[left]` is `<= pivot`,
so `j` does too. Subarrays of size 1 or 2 are handled before partitioning,
because step 2 needs three distinct positions.

**Both scans stop on equal values.** It looks wasteful (`[7, 7, 7]` swaps 7s
with each other), but it's what keeps all-equal input fast. If `i` skipped over
equal values, it would run to the end every time, and each partition would peel
off one element: O(n²).

**One change from NeetCode.** Median-of-three still has bad inputs, built so
the pivot is always near one end. Recursing into both sides would then go n
levels deep, past Python's 1,000-frame limit. These files recurse into the
smaller side and loop on the larger one. The smaller side is at most half the
range, so the stack never goes past log₂ n frames. The worst-case time is still
O(n²); random pivots or introsort are the usual fixes for that.

## Heap sort in one picture

The array *is* the heap: node `i` has children `2i + 1` and `2i + 2`.

```
build:  sift down every parent, from n/2 - 1 back to 0  -> nums[0] is the max
repeat: swap nums[0] with the last heap slot, shrink the heap, sift nums[0] down

        [ heap (max at 0) ........ | sorted, growing left ]
          0                     end
```

Building the heap is O(n), not O(n log n): most nodes are near the bottom and
sift down only a step or two. The n pops cost O(log n) each.

NeetCode writes sift-down recursively, which uses O(log n) stack. A loop does
the same job in O(1), so these files use a loop.

## Radix sort and negative numbers

Digit extraction (`num // place % 10`) assumes non-negative numbers. NeetCode
splits the input:

1. Negate the negatives and radix-sort them: `[-3, -10, -1]` → `[3, 10, 1]` →
   `[1, 3, 10]`.
2. Reverse and negate back: `[-10, -3, -1]`. The largest absolute value is the
   smallest number.
3. Append the sorted non-negatives. Zero belongs with them.

Each digit pass has to be **stable**: equal digits keep their order from the
previous pass, which is why the write-back loop runs back to front.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 sort-an-array/python/SortAnArray.py
```

Or from inside the folder:

```bash
cd sort-an-array/python
python3 SortAnArray.py
```

Expected output (takes a few seconds: five cases are 50,000 elements long):

```
PASS  built-in sort: 16/16 cases
PASS  quick sort: 16/16 cases
PASS  merge sort: 16/16 cases
PASS  heap sort: 16/16 cases
PASS  counting sort: 16/16 cases
PASS  radix sort: 16/16 cases
PASS  shell sort: 16/16 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java sort-an-array/java/SortAnArray.java
```

### Option 2 — compile, then run

```bash
cd sort-an-array/java
javac SortAnArray.java   # produces SortAnArray.class
java SortAnArray         # note: no .class extension
```

Expected output (either option):

```
PASS  built-in sort: 16/16 cases
PASS  quick sort: 16/16 cases
PASS  merge sort: 16/16 cases
PASS  heap sort: 16/16 cases
PASS  counting sort: 16/16 cases
PASS  radix sort: 16/16 cases
PASS  shell sort: 16/16 cases
```

To clean up the compiled artifacts from option 2:

```bash
rm sort-an-array/java/SortAnArray*.class
```

## Notes

- **Every approach returns the sorted array**, as LeetCode's `sortArray`
  requires. All but radix sort sort `nums` in place and return it. Radix sort
  builds and returns a new array. The harness gives each approach a fresh copy
  of each case and compares what it returns.
- **The test set covers the edges:** both examples, a single element, two and
  three elements (the sizes quick sort handles specially), all equal values,
  negatives, both value limits, and 50,000-element arrays that are sorted,
  reversed, all equal, random, and random with only seven distinct values.
- **The random cases are seeded** (`912`), so they're the same on every run.
  Python and Java use different generators, so the two languages get different
  arrays.
- **Verified exhaustively, in both languages:** every array of length 1–7 over
  `{-11, -2, 0, 1, 10}` (97,655 arrays, 683,585 checks per language, covering
  negatives, zero and two-digit values), against the library sort. Zero
  failures in either language.
- **Shell sort's complexity.** NeetCode lists O(n log n) on average. With
  Shell's original gaps (n/2, n/4, ..., 1) the worst case is O(n²). Knuth's
  gaps (1, 4, 13, 40, ...) bring it to O(n^1.5).
- **Java overflow.** Radix sort negates the negatives, which would overflow for
  `Integer.MIN_VALUE`. The constraints keep values within ±50,000, so it can't
  happen here. The digit loop keeps `place` in a `long`, so multiplying it by
  10 past the largest value can't overflow either.
- **Method naming:** LeetCode uses `sortArray`. These files use `sortArray*` /
  `sort_array_*` with an approach suffix, so all seven can coexist. Rename to
  plain `sortArray` when submitting.
- The Java file is named `SortAnArray.java` to match its `public class
  SortAnArray`, as `javac` requires.
