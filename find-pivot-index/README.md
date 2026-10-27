# Find Pivot Index

LeetCode 724 · [NeetCode solution page](https://neetcode.io/solutions/find-pivot-index)

Given an integer array `nums`, return the **pivot index**: the index where the
sum of all numbers strictly to its left equals the sum of all numbers strictly
to its right. If there are several, return the leftmost. If there are none,
return `-1`.

At index 0 the left side is empty and sums to 0. The same goes for the right
side at the last index.

`[1, 7, 3, 6, 5, 6]` gives `3`: `1 + 7 + 3 = 11` and `5 + 6 = 11`.
`[3, 2, 1]` gives `-1`.
`[2, 1, -1]` gives `0`: the left side is empty (0), and `1 + (-1) = 0`.

**Constraints:** `1 <= nums.length <= 10^4`, `-1000 <= nums[i] <= 1000`.

This is the same problem as LeetCode 1991, Find the Middle Index in Array.

```
find-pivot-index/
├── java/
│   └── FindPivotIndex.java
├── python/
│   └── FindPivotIndex.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Brute force | O(n²) | O(1) | Sum both sides from scratch at every index. |
| 2 | Prefix sum array | O(n) | O(n) | `prefix[i]` is the sum before `i`, so either side is one subtraction. |
| 3 | **Running sum** | **O(n)** | **O(1)** | Track the left sum; the right sum is `total - left - nums[i]`. |

All three are NeetCode's.

**Approach 3 is the one to reach for.** Approach 2 shows the idea, and
approach 3 notices that the prefix array is only ever read at the current
index, so a single running variable replaces it.

## From prefix array to running sum

The prefix array has one more slot than `nums`, with `prefix[i]` holding the
sum of everything before index `i`:

```
index i:        0   1   2   3   4   5
nums:           1   7   3   6   5   6
prefix:     0   1   8  11  17  22  28
            ^ prefix[0]              ^ prefix[n] = total
```

The sums around index `i` are both one lookup:

- **left** = `prefix[i]`
- **right** = `prefix[n] - prefix[i + 1]` (the total, minus everything up to
  and including `nums[i]`)

At `i = 3`: left is `prefix[3] = 11` and right is `28 - 17 = 11`. That's the
pivot.

The leading 0 is what makes index 0 work without a special case. Its left side
is `prefix[0] = 0`, the empty sum.

Now look at what the second loop reads: `prefix[i]` and `prefix[i + 1]`, which
are the left sum before and after adding `nums[i]`. A running `left_sum` holds
exactly that, and the total is `sum(nums)`. So:

```
left + nums[i] + right = total     =>     right = total - left - nums[i]
```

No array needed.

## Order inside the loop matters

The running sum has to **compare first, then add `nums[i]`**. The left side of
`i` is strictly to its left, so `nums[i]` isn't in it yet when index `i` is
checked.

Adding first gives the wrong answer. On `[2, 1, -1]` (total 2), index 0
compares a left sum of 2 against `2 - 2 - 2 = -2`, missing the pivot at 0.
Indexes 1 and 2 compare 3 with -2 and 2 with 1, so the function returns `-1`.

## Negative numbers rule out shortcuts

With only non-negative values, the left sum could only grow and the right sum
could only shrink, so you could stop once left passes right. Here values can be
negative, so neither side is monotone. In `[-1, -1, 0, 1, 1, 0]` the left sum
goes `0, -1, -2, -2, -1, 0`, falling and then rising back. The only pivot is the
last index. Every index has to be checked, until the first match.

## Python

**Requires:** Python 3.6+ (f-strings, `1_000`-style literals). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 find-pivot-index/python/FindPivotIndex.py
```

Or from inside the folder:

```bash
cd find-pivot-index/python
python3 FindPivotIndex.py
```

Expected output:

```
PASS  brute force: 13/13 cases
PASS  prefix array: 13/13 cases
PASS  running sum: 13/13 cases
PASS  input left unmodified: [1, 7, 3, 6, 5, 6]
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java find-pivot-index/java/FindPivotIndex.java
```

### Option 2 — compile, then run

```bash
cd find-pivot-index/java
javac FindPivotIndex.java   # produces FindPivotIndex*.class
java FindPivotIndex         # note: no .class extension
```

Expected output (either option):

```
PASS  brute force: 13/13 cases
PASS  prefix array: 13/13 cases
PASS  running sum: 13/13 cases
PASS  input left unmodified: [1, 7, 3, 6, 5, 6]
```

To clean up the compiled artifacts from option 2:

```bash
rm find-pivot-index/java/FindPivotIndex*.class
```

## Notes

- **The long test cases stop at about 1,000 elements, not the 10,000
  ceiling.** Brute force is O(n²), and in Python a 10,000-element input with
  no pivot costs 10^8 additions, which took the run from 0.2 s to 11 s. The
  ceiling doesn't add anything else to check: the largest possible sum is
  `10^4 * 10^3 = 10^7`, far inside Java's `int` range.
- **A one-variable form.** `left == total - left - nums[i]` rearranges to
  `2 * left + nums[i] == total`. It's the same check written without
  `right_sum`.
- **Leftmost pivot.** `[0, 0, 0]` has a pivot at every index, and
  `[2, -3, 4, -3, 2]` has pivots at 0, 2 and 4. Scanning left to right and
  returning at the first match gives the leftmost. Both cases are in the test
  set.
- **Verified exhaustively, in both languages:** every array of length 1–8
  with values -2 to 2 (488,280 arrays, 1,464,840 checks per language), against
  the definition. The same run checked that no approach modifies its input.
  Zero failures in either language.
- **Method naming:** LeetCode uses `pivotIndex`. These files use
  `pivotIndex*` / `pivot_index_*` with an approach suffix, so all three can
  coexist. Rename to plain `pivotIndex` when submitting.
- The Java file is named `FindPivotIndex.java` to match its
  `public class FindPivotIndex`, as `javac` requires.
