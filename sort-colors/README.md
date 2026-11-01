# Sort Colors

LeetCode 75 · [NeetCode solution page](https://neetcode.io/solutions/sort-colors)

Given an array `nums` of `0`s (red), `1`s (white) and `2`s (blue), sort it
**in place** so that equal colors are next to each other, in the order 0, 1, 2.
You may not use the library's sort function.

`[2, 0, 2, 1, 1, 0]` becomes `[0, 0, 1, 1, 2, 2]`.
`[2, 1, 0]` becomes `[0, 1, 2]`.

**Constraints:** `1 <= n <= 300`, `nums[i]` is `0`, `1` or `2`.

**Follow-up:** a one-pass algorithm using only constant extra space.

```
sort-colors/
├── java/
│   └── SortColors.java
├── python/
│   └── SortColors.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Passes | Idea |
|---|----------|------|-------|--------|------|
| 1 | Built-in sort | O(n log n) | O(n) Python, O(log n) Java | — | Not allowed; kept as a baseline. |
| 2 | Counting sort | O(n) | O(1) | 2 | Count each color, then write them back in order. |
| 3 | **Dutch national flag** | **O(n)** | **O(1)** | **1** | Swap 0s to the front and 2s to the back. |
| 4 | Cascade | O(n) | O(1) | 1 | Grow three regions from the left with back-to-front writes. |
| 5 | Cascade, short | O(n) | O(1) | 1 | Approach 4 with the branches folded together. |

All five are NeetCode's.

**Approach 3 is the one to reach for.** It's the standard answer to the
follow-up, its invariant is easy to state, and the same partition is the core
of 3-way quicksort. Counting sort is the simplest correct answer if the
interviewer accepts two passes. Approaches 4 and 5 are also one pass, and worth
seeing as a different way to keep regions in place.

## The Dutch national flag invariant

Three pointers split the array into four regions:

```
 [ 0 0 0 | 1 1 1 | ? ? ? ? | 2 2 2 ]
   0       l       i     r   r+1   n
```

- `[0, l)` is all 0s.
- `[l, i)` is all 1s.
- `[i, r]` hasn't been looked at yet.
- `(r, n)` is all 2s.

At the start the unknown region is the whole array. Each step looks at
`nums[i]` and shrinks the unknown region by one:

- **1:** it's already in the 1s region's place. Move `i` on.
- **0:** swap it to `l`, then move both `l` and `i` on. What comes back from
  `l` is a 1 (it was the first slot of the 1s region), or the same 0 when the
  1s region is empty and `l == i`. Either way it's already been checked.
- **2:** swap it to `r`, then move `r` back. **Don't move `i`.** What comes
  back from `r` came from the unknown region, so it still has to be looked at.

The loop stops when the unknown region is empty, i.e. when `i > r`.

On the first example:

```
step              nums                 l  i  r
start             [2, 0, 2, 1, 1, 0]   0  0  5
2: swap i<->r     [0, 0, 2, 1, 1, 2]   0  0  4    i stays: the 0 is new
0: swap i<->l     [0, 0, 2, 1, 1, 2]   1  1  4
0: swap i<->l     [0, 0, 2, 1, 1, 2]   2  2  4
2: swap i<->r     [0, 0, 1, 1, 2, 2]   2  2  3    i stays: the 1 is new
1: skip           [0, 0, 1, 1, 2, 2]   2  3  3
1: skip           [0, 0, 1, 1, 2, 2]   2  4  3    i > r: done
```

## Two easy ways to break it

Both come from losing track of the unknown region:

| Bug | Smallest input that breaks it | Result |
|---|---|---|
| `while i < r` instead of `i <= r` | `[1, 0]` | `[1, 0]`: index `r` is still unknown when the loop stops. |
| Moving `i` on after the swap with `r` | `[1, 2, 0]` | `[1, 0, 2]`: the 0 swapped in from `r` is never looked at. |

Both inputs were found by searching every array in length order, and both are
in the test set.

NeetCode's version writes the second rule as `i -= 1` inside the `2` branch,
followed by an `i += 1` that every branch shares. These files move `i += 1`
into the `0` and `1` branches instead, so the `2` branch just doesn't touch
`i`. It's the same algorithm, and it says what it means.

## The cascade: writing instead of swapping

Approaches 4 and 5 keep all three regions at the front of the array and let
them grow left to right:

```
 [ 0 0 | 1 1 1 | 2 2 | (rest not read yet) ]
   0     zero    one   two = i
```

A new value has to go at the end of its region, which pushes every later
region one slot right. Rather than shifting, each region gives its first slot
to the region before it and takes one new slot at its end. With a 0:

1. `nums[two] = 2`: the 2s region grows by a slot at its end.
2. `nums[one] = 1`: the old first slot of the 2s region becomes a 1.
3. `nums[zero] = 0`: the old first slot of the 1s region becomes a 0.

The writes go back to front, so each one lands on a slot that the next region
has already claimed. When a region is empty, two of the writes hit the same
slot, and the last write, the correct one, wins.

Approach 5 notices that every value writes a 2, values below 2 also write a 1,
and values below 1 also write a 0. That turns the three branches into two
`if`s. It has to save `nums[two]` in `tmp` first, because the 2 it writes at
`two` overwrites the value it's about to test.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 sort-colors/python/SortColors.py
```

Or from inside the folder:

```bash
cd sort-colors/python
python3 SortColors.py
```

Expected output:

```
PASS  built-in sort: 13/13 cases
PASS  counting sort: 13/13 cases
PASS  dutch flag: 13/13 cases
PASS  cascade: 13/13 cases
PASS  cascade, short: 13/13 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java sort-colors/java/SortColors.java
```

### Option 2 — compile, then run

```bash
cd sort-colors/java
javac SortColors.java   # produces SortColors.class
java SortColors         # note: no .class extension
```

Expected output (either option):

```
PASS  built-in sort: 13/13 cases
PASS  counting sort: 13/13 cases
PASS  dutch flag: 13/13 cases
PASS  cascade: 13/13 cases
PASS  cascade, short: 13/13 cases
```

To clean up the compiled artifacts from option 2:

```bash
rm sort-colors/java/SortColors*.class
```

## Notes

- **Every approach sorts in place and returns nothing**, as LeetCode's
  `void sortColors` requires. The harness gives each approach a fresh copy of
  each case and compares the array afterwards. In Python it also checks that
  the return value is `None`.
- **The 300-element case is random but seeded** (`75`), so it's the same array
  on every run. Python and Java use different generators, so the two languages
  get different arrays.
- **Dutch flag is unstable, and here that's fine.** It moves equal values past
  each other, which would matter for records sorted by a key. For bare
  integers, two 0s are indistinguishable.
- **Swaps and writes.** Dutch flag does at most one swap per element. The
  cascade writes up to three times per element, but never needs a temporary
  for a swap. Both are O(n).
- **Verified exhaustively, in both languages:** every array of length 1–10
  over `{0, 1, 2}` (88,572 arrays, 442,860 checks per language), against an
  expected answer built from the counts rather than from a sort. Zero failures
  in either language.
- **Method naming:** LeetCode uses `sortColors`. These files use
  `sortColors*` / `sort_colors_*` with an approach suffix, so all five can
  coexist. Rename to plain `sortColors` when submitting.
- The Java file is named `SortColors.java` to match its `public class
  SortColors`, as `javac` requires.
