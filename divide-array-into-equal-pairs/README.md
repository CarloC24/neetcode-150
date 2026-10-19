# Divide Array Into Equal Pairs

LeetCode 2206 · [NeetCode solution page](https://neetcode.io/solutions/divide-array-into-equal-pairs)

You're given an array `nums` of `2n` integers. Divide it into `n` pairs so that
every element is in exactly one pair and both elements of each pair are equal.
Return `true` if that's possible.

`[3, 2, 3, 2, 2, 2]` gives `true`: `(2, 2)`, `(3, 3)`, `(2, 2)`.
`[1, 2, 3, 4]` gives `false`.

**Constraints:** `nums.length == 2n`, `1 <= n <= 500`, `1 <= nums[i] <= 500`.

```
divide-array-into-equal-pairs/
├── java/
│   └── DivideArrayIntoEqualPairs.java
├── python/
│   └── DivideArrayIntoEqualPairs.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

The problem uses `n` for the number of pairs, so `N = 2n` is the array length.

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Sorting | O(N log N) | O(N) Python, O(log N) Java | Equal values end up in runs; every run must have even length. |
| 2 | Hash map | O(N) | O(N) | Count each value; every count must be even. |
| 3 | Hash set | O(N) | O(N) | Toggle each value in and out of a set; it must end empty. |
| 4 | **Parity array** | **O(N)** | **O(1)** | The same toggle in a fixed array of 501 flags. |

Approaches 1–3 are NeetCode's. Approach 4 replaces the set with a fixed-size
array, which the value range allows.

**Approach 4 is the one to reach for.** Values are between 1 and 500, so 501
flags cover every possible value. The array's size doesn't depend on the input,
so the space is O(1). There's also no hashing, and in Java no boxing.

## It's a counting problem, not an arrangement problem

"Divide into pairs" sounds like it's about *which* elements go together. It
isn't. Pairs don't need to be adjacent, and order doesn't matter, so the
question reduces to: **does every value appear an even number of times?**

- If every count is even, pair up each value's copies two at a time.
- If some count is odd, one copy of that value is always left over, and it
  can't pair with anything else.

All four approaches check that one condition. They differ only in how they
store the counts:

- **Sorting** makes each value's count visible as a run length.
- **The hash map** stores the full count.
- **The set and the parity array** store just the count's parity, one bit per
  value, which is all the condition needs.

## XOR is not enough

XOR has the right flavour: equal pairs cancel, since `x ^ x == 0`. So it's
tempting to XOR every value together and return whether the result is 0.

It's wrong. Pairs cancel, but unrelated values can cancel too:

```
1 ^ 2 ^ 4 ^ 7  =  0      and [1, 2, 4, 7] has no pairs at all
```

Searching every length-4 array with values 1–7 in lexicographic order, that's
the first counterexample. For an array of length 2 the shortcut happens to be
correct, since `a ^ b == 0` only when `a == b`. From length 4 on it breaks.

XOR works for a different problem: Single Number, where **every** value is
known to appear exactly twice except one. There, XOR cancels the pairs and
leaves the single value. Here nothing is known about the counts, so a zero XOR
proves nothing. `[1, 2, 4, 7]` is in the test set to catch the shortcut.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 divide-array-into-equal-pairs/python/DivideArrayIntoEqualPairs.py
```

Or from inside the folder:

```bash
cd divide-array-into-equal-pairs/python
python3 DivideArrayIntoEqualPairs.py
```

Expected output:

```
PASS  sorting: 13/13 cases
PASS  hash map: 13/13 cases
PASS  hash set: 13/13 cases
PASS  parity array: 13/13 cases
PASS  input left unmodified (all but sorting): [3, 2, 3, 2, 2, 2]
```

## Java

**Requires:** JDK 11+ for the single-file launcher below (`Set.of` needs 9+).
Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java divide-array-into-equal-pairs/java/DivideArrayIntoEqualPairs.java
```

### Option 2 — compile, then run

```bash
cd divide-array-into-equal-pairs/java
javac DivideArrayIntoEqualPairs.java   # produces DivideArrayIntoEqualPairs*.class
java DivideArrayIntoEqualPairs         # note: no .class extension
```

Expected output (either option):

```
PASS  sorting: 13/13 cases
PASS  hash map: 13/13 cases
PASS  hash set: 13/13 cases
PASS  parity array: 13/13 cases
PASS  input left unmodified (all but sorting): [3, 2, 3, 2, 2, 2]
```

To clean up the compiled artifacts from option 2:

```bash
rm divide-array-into-equal-pairs/java/DivideArrayIntoEqualPairs*.class
```

## Notes

- **`Set.add` already tells you whether the value was there.** It returns
  `false` if the value was already in the set. So Java's approach 3 writes
  `if (!odd.add(num)) odd.remove(num);`, one lookup where NeetCode's version
  does a `contains` and then an `add` or `remove`.
- **A simpler sorted check.** After sorting, every pair `(nums[0], nums[1])`,
  `(nums[2], nums[3])`, … must be equal. That's equivalent to the run-length
  check, because an odd-length run would leave its last element paired with the
  first element of the next run.
- **Two odd counts don't make an even one.** `[1, 1, 1, 2]` has an even
  length, but `1` appears three times and `2` once, so one copy of each is left
  over. Checking only that the length is even isn't enough. That case is in the
  test set.
- **The harness copies each input**, because approach 1 sorts in place. A final
  check runs the other three on one shared array to confirm they leave it
  unchanged.
- **Verified exhaustively, in both languages:** every array of even length 2–8
  with values 1–4 (69,904 arrays, 279,616 checks per language), against the
  every-count-is-even definition. The same run checked that approaches 2–4 leave
  the input unchanged. Zero failures in either language.
- **Method naming:** LeetCode uses `divideArray`. These files use
  `divideArray*` / `divide_array_*` with an approach suffix, so all four can
  coexist. Rename to plain `divideArray` when submitting.
- The Java file is named `DivideArrayIntoEqualPairs.java` to match its
  `public class DivideArrayIntoEqualPairs`, as `javac` requires.
