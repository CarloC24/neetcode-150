# Find All Numbers Disappeared in an Array

LeetCode 448 · [NeetCode solution page](https://neetcode.io/solutions/find-all-numbers-disappeared-in-an-array)

Given an array `nums` of `n` integers, each in the range `[1, n]`, return every
integer in `[1, n]` that does **not** appear in `nums`.

`[4, 3, 2, 7, 8, 2, 3, 1]` gives `[5, 6]`. `[1, 1]` gives `[2]`.

**Constraints:** `n == nums.length`, `1 <= n <= 10^5`, `1 <= nums[i] <= n`.

**Follow-up:** can you do it in O(n) time without extra space? The returned
list doesn't count.

```
find-all-numbers-disappeared-in-an-array/
├── java/
│   └── FindAllNumbersDisappearedInAnArray.java
├── python/
│   └── FindAllNumbersDisappearedInAnArray.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

Space doesn't count the returned list.

| # | Approach | Time | Space | Input afterwards | Idea |
|---|----------|------|-------|------------------|------|
| 1 | Hash set | O(n) | O(n) | unchanged | Start with 1..n, remove each value seen. |
| 2 | Boolean array | O(n) | O(n) | unchanged | `seen[v - 1] = True` for each value. |
| 3 | Sorting | O(n log n) | O(n) Python, O(log n) Java | sorted | Walk 1..n alongside the sorted values. |
| 4 | **Negative marking** | **O(n)** | **O(1)** | **unchanged** | Seeing `v` makes `nums[v - 1]` negative. |
| 5 | Cyclic sort | O(n) | O(1) | permuted | Swap each value into slot `v - 1`; misfits mark the gaps. |

Approaches 1–4 are NeetCode's. Approach 5 is the other standard answer to the
follow-up.

**Approach 4 is the one to reach for.** It answers the follow-up, and this
version restores the input before returning. NeetCode's version leaves the
caller's array full of negative numbers. The restore is one more O(n) pass, so
the complexity doesn't change.

## The idea: the array is its own lookup table

Approaches 1 and 2 need a separate structure to record "I've seen `v`". The
follow-up asks for no extra space, so the record has to live **inside `nums`**.
That's possible because the values and the indices cover the same range: value
`v` has a natural home at index `v - 1`. Both O(1) approaches use that, in
different ways.

**Negative marking** stores one bit per slot. Every value is positive, so the
sign of each slot is free to use as a flag:

```
nums = [4, 3, 2, 7, 8, 2, 3, 1]

see 4  -> negate slot 3   [ 4,  3,  2, -7,  8,  2,  3,  1]
see 3  -> negate slot 2   [ 4,  3, -2, -7,  8,  2,  3,  1]
see 2  -> negate slot 1   [ 4, -3, -2, -7,  8,  2,  3,  1]
see 7  -> negate slot 6   [ 4, -3, -2, -7,  8,  2, -3,  1]
see 8  -> negate slot 7   [ 4, -3, -2, -7,  8,  2, -3, -1]
see 2  -> slot 1 already negative, stays negative
see 3  -> slot 2 already negative, stays negative
see 1  -> negate slot 0   [-4, -3, -2, -7,  8,  2, -3, -1]

slots 4 and 5 are still positive  ->  5 and 6 are missing
```

Two details make it work:

- **Read through `abs()`.** By the time the loop reaches a slot, an earlier
  step may have negated it, so the value there might be `-7`, not `7`.
- **Set negative with `-abs()`, don't just negate.** A value that appears twice
  would otherwise flip its slot back to positive, and it would be reported as
  missing. `[1, 1]` catches this.

**Cyclic sort** moves each value into its home slot instead. It keeps swapping
`nums[i]` to index `nums[i] - 1` until the slot holds its own value, or the home
is already taken by a duplicate. Afterwards, every slot `i` that doesn't hold
`i + 1` is a gap. It's O(n) despite the nested loop: each swap puts one value in
its final place for good, so there are at most `n` swaps in total. It's worth
knowing because the same swap-into-place idea solves First Missing Positive
(Hard), where negative marking needs extra care. It leaves the input permuted,
though, and that can't be undone cheaply.

## The Python swap that never terminates

Cyclic sort's swap looks like it wants to be a one-line tuple assignment:

```python
nums[i], nums[nums[i] - 1] = nums[nums[i] - 1], nums[i]   # wrong
```

Python evaluates the right-hand side first, then assigns the targets **left to
right**. So `nums[i]` gets its new value first, and *then* `nums[nums[i] - 1]`
works out its index from that new value. The second write lands in the wrong
slot.

On `[4, 3, 2, 7, 8, 2, 3, 1]`, the correct swap finishes in 6 swaps. The
one-liner never finishes. Capped at 1,000 swaps, it had corrupted the array to
`[4, 3, 2, 7, 8, 2, 4, 1]`: a `3` destroyed and a `4` duplicated.

The fix is to save the index first:

```python
j = nums[i] - 1
nums[i], nums[j] = nums[j], nums[i]
```

Java can't express the one-liner, so its three-line swap with a `tmp` has no
such trap.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 find-all-numbers-disappeared-in-an-array/python/FindAllNumbersDisappearedInAnArray.py
```

Or from inside the folder:

```bash
cd find-all-numbers-disappeared-in-an-array/python
python3 FindAllNumbersDisappearedInAnArray.py
```

Expected output:

```
PASS  hash set: 13/13 cases
PASS  boolean array: 13/13 cases
PASS  sorting: 13/13 cases
PASS  negative marking: 13/13 cases
PASS  cyclic sort: 13/13 cases
PASS  input left unmodified (all but sorting, cyclic sort): [4, 3, 2, 7, 8, 2, 3, 1]
```

## Java

**Requires:** JDK 11+ for the single-file launcher below (`List.of` and `Set.of`
need 9+). Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java find-all-numbers-disappeared-in-an-array/java/FindAllNumbersDisappearedInAnArray.java
```

### Option 2 — compile, then run

```bash
cd find-all-numbers-disappeared-in-an-array/java
javac FindAllNumbersDisappearedInAnArray.java   # produces FindAllNumbersDisappearedInAnArray*.class
java FindAllNumbersDisappearedInAnArray         # note: no .class extension
```

Expected output (either option):

```
PASS  hash set: 13/13 cases
PASS  boolean array: 13/13 cases
PASS  sorting: 13/13 cases
PASS  negative marking: 13/13 cases
PASS  cyclic sort: 13/13 cases
PASS  input left unmodified (all but sorting, cyclic sort): [4, 3, 2, 7, 8, 2, 3, 1]
```

To clean up the compiled artifacts from option 2:

```bash
rm find-all-numbers-disappeared-in-an-array/java/FindAllNumbersDisappearedInAnArray*.class
```

## Notes

- **The "add `n`" marking trick overflows in Java.** Another common O(1)
  answer adds `n` to `nums[(v - 1) % n]` for each value, then reports every slot
  still `<= n`. Each occurrence of a value adds `n` to one slot. At `n = 10^5`,
  21,475 copies of the same value take that slot past `Integer.MAX_VALUE`.
  Measured: with `1` appearing 21,475 times and the other slots holding
  `2, 3, …`, slot 0 wraps to `-2,147,467,295`, and the trick reports `1` as
  missing even though it's present. Negative marking only ever flips a sign, so
  it can't overflow. Python's ints don't overflow, but they grow, and the trick
  is no better than negative marking anyway.
- **Order of the result.** LeetCode accepts any order, so the harness sorts
  each result before comparing. Approaches 2, 4 and 5 produce ascending order by
  construction. The hash set's order is an accident of how each language hashes
  integers, and the two languages differ:
  - **CPython** iterates them in ascending order here, because a small int
    hashes to itself.
  - **Java** does too, up to 65,536. Above that, `HashMap` mixes the hash's high
    bits into its low bits (`h ^ (h >>> 16)`), which swaps neighbouring values.
    Measured at the ceiling, `n = 10^5`: the result runs in order up to 65,535,
    then goes `65537, 65536, 65539, …`.

  Neither language promises an order, and a harness that compared lists without
  sorting would pass every small case and fail only the ceiling case in Java.
- **Sorting is not O(1) space.** Python's `list.sort` is Timsort, which needs up
  to `n / 2` slots of scratch space. Java's `Arrays.sort` on an `int[]` is
  dual-pivot quicksort, which needs only its O(log n) recursion stack.
- **The harness copies each input**, and a final check runs approaches 1, 2 and
  4 on one shared array to confirm they leave it unchanged.
- **Verified exhaustively, in both languages:** every array of length 1–7 with
  values in `[1, n]` (873,612 arrays, 4,368,060 checks per language), against the
  direct definition. The same run checked that approaches 1, 2 and 4 leave the
  input unchanged. Zero failures in either language.
- **Method naming:** LeetCode uses `findDisappearedNumbers`. These files use
  `disappeared*` / `disappeared_*` with an approach suffix, so all five can
  coexist. Rename to `findDisappearedNumbers` when submitting.
- The Java file is named `FindAllNumbersDisappearedInAnArray.java` to match its
  `public class FindAllNumbersDisappearedInAnArray`, as `javac` requires.
