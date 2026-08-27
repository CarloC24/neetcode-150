# Binary Search

LeetCode 704 · [NeetCode solution page](https://neetcode.io/solutions/binary-search)

Given an array `nums` of **distinct** integers sorted in ascending order and an
integer `target`, return the index of `target` in `nums`, or `-1` if it is not
there.

**Constraints:** `1 <= nums.length <= 10^4`, `-10^4 < nums[i], target < 10^4`,
all values distinct, `nums` sorted ascending. The problem statement also
requires **O(log n) time**.

```
binary-search/
├── java/
│   └── BinarySearch.java
├── python/
│   └── BinarySearch.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Linear scan | O(n) | O(1) | Look at every element. The one the problem forbids. |
| 2 | Recursion | O(log n) | O(log n) | Halve the interval, recurse into the half that can hold it. |
| 3 | **Iteration** | **O(log n)** | **O(1)** | The same halving, with the interval in two variables. |
| 4 | Upper bound | O(log n) | O(1) | Find the first index past `target`, look one to its left. |
| 5 | Lower bound | O(log n) | O(1) | Find the first index not below `target`, check if it *is* `target`. |
| 6 | Built-in | O(log n) | O(1) | `bisect_left` / `Arrays.binarySearch`. |

NeetCode numbers these 1–5; approach 1 here is the linear scan it does not
publish, so everything after is offset by one from the page.

**Approach 3 is the one to write**, and it is the only one that is optimal on
both axes. Approach 2 is the same algorithm — the recursion is tail-recursive,
and approach 3 is what unrolling it by hand looks like — but it carries the
interval in call frames instead of two ints, which is the O(log n) space.

**Approaches 4 and 5 are worth more than approach 3 outside this problem**, and
that is the real reason to write them out. Keep reading past the table.

**Approach 1 is here to make the sortedness load-bearing.** It is correct, it is
the fastest thing to write, and it is the only approach that does not need the
array sorted — which is exactly what makes it wrong. Everything after it answers
"what does sortedness actually buy", and the answer is: each comparison discards
half of what remains instead of one element.

## The two invariants, and the six ways to mix them

This is the whole difficulty of the problem. There are two correct conventions
for the search interval, and each one fixes all three of its knobs together:

| | `r` starts at | loop while | reject via |
|---|---|---|---|
| **Closed** `[l, r]` | `n - 1` | `l <= r` | `r = m - 1` |
| **Half-open** `[l, r)` | `n` | `l < r` | `r = m` |

Approaches 2 and 3 use the closed form; approaches 4 and 5 use the half-open
one. Both are correct. **Mixing them is not, and every mismatch fails
differently.** All eight combinations, run against every sorted distinct array
of length 0–5 drawn from `0..7` — 848 arrays, 10 targets each, 2,190 checks per
combination:

| `r` init | loop | reject | ok | wrong | hang | crash |
|---|---|---|---|---|---|---|
| `n-1` | `l <= r` | `r = m - 1` | **2190** | 0 | 0 | 0 |
| `n-1` | `l <= r` | `r = m` | 1258 | 0 | **932** | 0 |
| `n-1` | `l < r` | `r = m - 1` | 1790 | **400** | 0 | 0 |
| `n-1` | `l < r` | `r = m` | 1972 | **218** | 0 | 0 |
| `n` | `l <= r` | `r = m - 1` | 1724 | 0 | 0 | **466** |
| `n` | `l <= r` | `r = m` | 792 | 0 | **932** | **466** |
| `n` | `l < r` | `r = m - 1` | 1924 | **266** | 0 | 0 |
| `n` | `l < r` | `r = m` | **2190** | 0 | 0 | 0 |

Two of eight work, and they are exactly the two rows of the convention table.
The failure modes sort cleanly, which is what makes them diagnosable:

- **`r = m` with `l <= r` hangs.** Once the interval is one element and that
  element is greater than the target, `r = m` leaves `l` and `r` where they
  were. The state is identical on the next pass, forever.
- **`r = n` with `l <= r` reads out of bounds.** `m` can reach `n`.
- **`l < r` on a closed interval loses the last candidate.** No hang, no crash,
  just a wrong answer — and the smallest input that shows it is `[0]` searching
  for `0`, which returns `-1` for an element that is right there.

That last one is the dangerous one, because it fails silently and it fails on
the *successful* searches. `[1]` searching for `1` is in the test set of both
files for this reason.

## The mid formula, and where it actually matters

`m = l + (r - l) // 2`, not `(l + r) // 2`. The reason is famous and it is real,
but it is a **Java** reason:

```
l = 1_500_000_000, r = 2_000_000_000

(l + r) / 2      ->  -397483648      ← int overflow, wraps negative
l + (r - l) / 2  ->   1750000000
(l + r) >>> 1    ->   1750000000
```

An `int` index into `nums` then throws `ArrayIndexOutOfBoundsException`. This is
the bug that sat in `Arrays.binarySearch` in the JDK itself for nine years; the
fix shipped was `(l + r) >>> 1`, which works because the unsigned shift reads
the wrapped sum's top bit as value rather than as sign. The subtraction form
avoids the wrap altogether and reads better.

It needs `l + r >= 2^31`, so with `l, r ≈ n` the array must hold about
**1,073,741,825** ints — 4 GiB. This problem caps `n` at 10,000, so it cannot
happen here, and it cannot happen in the Python file at any size: Python ints
are arbitrary precision, so the two forms are identical and always will be.

Write the safe form anyway. It costs nothing, and the habit is for the language
where the array is real.

## Approaches 4 and 5 answer a better question

The loops in approaches 4 and 5 never compare against the target for equality.
They are not looking for the target at all — they are looking for a **boundary**:

- **Upper bound** (approach 4): the first index whose value is `> target`. On
  exit, `l` is the count of elements `<= target`, so a present target is at
  `l - 1`.
- **Lower bound** (approach 5): the first index whose value is `>= target`. On
  exit, a present target is at `l` itself, with no offset.

The only difference between them is `>` versus `>=`. One character slides the
boundary by the width of the target's own run.

What `l` holds on exit is an **insertion point** — where the target belongs
whether or not it is there — and that is strictly more information than "found
or not". It is also what the neighbouring problems want: first element `>= x`,
count of elements `< x`, search-insert-position, and the boundary hunts in
rotated or implicit arrays where there is no equality test to write in the first
place. **Approach 5 is the binary search worth memorising**, even though
approach 3 is what to submit here.

Both need a final guard, and it is not decoration: the loop answered a question
about placement, so something still has to ask whether the element found is
actually the target. Approach 4 guards the low end (`l` can be 0), approach 5
the high end (`l` can be `n`).

### The same guard, structural in one language and a formality in the other

Approach 4's guard is `l > 0` in Java and `l` in Python, and they are not doing
the same amount of work.

In **Java**, dropping it indexes `nums[-1]` and throws
`ArrayIndexOutOfBoundsException: Index -1 out of bounds` on **every target below
the array** — not a rare edge, just "the target is small".

In **Python**, `nums[-1]` is legal and wraps to the last element. Dropping the
guard is then harmless on every non-empty array, and not by luck: `l == 0` means
everything is `> target`, so the last element is `> target` too, the comparison
is false, and `-1` comes back anyway. Verified across every sorted distinct
array of length 1–6 drawn from `0..9` against every target in `-2..11` — 11,858
checks, **zero differences**. The only input it catches is the empty array,
where `nums[-1]` raises `IndexError`.

Same line, same position: a correctness guard in one file and an empty-input
check in the other.

## Early exit is not free

Approach 3 can return the moment it lands on the target. Approaches 4 and 5
always run the loop to completion. That sounds like a clear win for approach 3,
and by iteration count it is — but iterations are not the unit that costs
anything. Approach 3 does up to **two** array comparisons per iteration
(`> target`, then `< target`); the bound loops do exactly **one**.

Counting comparisons against `nums`, over `n = 10,000` and all 10,000 present
targets:

| | min | max | mean |
|---|---|---|---|
| Iteration (approach 3) | 2 | 28 | **19.26** |
| Lower bound (approach 5) | 14 | 15 | **14.36** |

The early exit wins spectacularly on the handful of targets sitting near an
early midpoint and loses on average, by about five comparisons. On absent
targets the gap is wider still: 28 versus 14 for anything above the array.

None of which changes the answer to "which do I write" — both are O(log n), the
constant is irrelevant at `n = 10^4`, and approach 3 is the clearer statement of
what the problem asks. It is worth knowing that "it exits early" is not the
argument it sounds like.

## Recursion depth, unlike Reverse Linked List

Approach 2 is O(log n) on the call stack, and this is the reassuring case. The
depth is `log2(n)`, so the largest legal input recurses **14** frames deep
against Python's default limit of **1000**. Measured: 14 iterations is the
maximum any approach here takes at `n = 10,000`.

Worth stating next to [Reverse Linked List](../reverse-linked-list/), where the
recursion is O(n) deep and genuinely does not fit in Python at the stated
constraints. Same "O(n) space on the call stack" caveat, opposite verdict — the
log is what makes it a style choice rather than a hazard.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 binary-search/python/BinarySearch.py
```

Or from inside the folder:

```bash
cd binary-search/python
python3 BinarySearch.py
```

Expected output:

```
PASS  linear: 20/20 cases
PASS  recursion: 20/20 cases
PASS  iteration: 20/20 cases
PASS  upper bound: 20/20 cases
PASS  lower bound: 20/20 cases
PASS  built-in: 20/20 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java binary-search/java/BinarySearch.java
```

### Option 2 — compile, then run

```bash
cd binary-search/java
javac BinarySearch.java   # produces BinarySearch*.class
java BinarySearch         # note: no .class extension
```

Expected output (either option):

```
PASS  linear: 20/20 cases
PASS  recursion: 20/20 cases
PASS  iteration: 20/20 cases
PASS  upper bound: 20/20 cases
PASS  lower bound: 20/20 cases
PASS  built-in: 20/20 cases
```

To clean up the compiled artifacts from option 2 — note the plural, since the
nested `Search` and `Case` types compile to their own files:

```bash
rm binary-search/java/BinarySearch*.class
```

## Notes

- **The built-ins are not the same function.** `bisect_left` is specified to
  return the *first* match among duplicates; `Arrays.binarySearch` is documented
  to return an unspecified one. On `[1, 2, 2, 2, 3]` searching for `2`,
  `bisect_left` gives 1 and `Arrays.binarySearch` gives 2. Verified. Invisible
  in this problem, since the values are distinct — which is the point of the
  next note.
- **The test set cannot tell approaches 3, 4 and 5 apart, and that is a
  guarantee doing the work.** On `[1, 2, 2, 2, 3]` searching for `2`, approach 3
  returns 2, approach 4 returns 3 and approach 5 returns 1 — arbitrary, last and
  first. All three are "correct"; they answer different questions. Distinctness
  is what collapses them to one answer here, so a test set built from this
  problem's constraints proves nothing about which one to reach for elsewhere.
- **`Arrays.binarySearch` encodes the miss, and this problem throws it away.**
  It returns `-(insertion point) - 1` on failure: negative so success and
  failure are distinguishable by sign, offset by one so an insertion point of 0
  does not collide with a found index of 0. Searching `[-1, 0, 2, 4, 6, 8]` for
  `3` returns `-4`, not `-1`. The `index >= 0 ? index : -1` in approach 6
  discards the insertion point; approach 5 is that same information kept.
  Searching below everything happens to return `-1` already — `-(0) - 1` — which
  is a coincidence of the encoding, not a shortcut to lean on.
- **Termination comes from excluding the midpoint.** Every branch in every
  approach moves past `m` (`m + 1`, `m - 1`, or `r = m` on a half-open
  interval), so the interval strictly shrinks each step. That is also the whole
  proof of O(log n): the width at least halves, and it starts at `n`.
- **Nothing here mutates its input**, unlike Reverse Linked List, so the harness
  shares one array across all six approaches per case rather than rebuilding.
- **The empty array is outside the constraints** — LeetCode guarantees at least
  one element — but it is in the test set anyway. It is the only input that
  catches Python's missing `l` guard in approach 4, and it is free to support.
- **Verified beyond the test set, in both languages.** All six approaches were
  checked against a linear-scan oracle on: every sorted distinct array of length
  0–6 drawn from `0..9` against every target in `-1..10` (848 arrays, 61,056
  checks); every prefix of `range(n)` for `n` in 1–300 against every present
  target plus two misses (274,500 checks); and 4,000 random sorted distinct
  arrays of length 0–200 with values in `-10000..10000`, targets present 60% of
  the time (24,000 checks). Zero failures, Python and Java alike.
- **Method naming:** both NeetCode and LeetCode use `search`. These files use
  `search*` / `search_*` suffixed by approach so all six can coexist — rename to
  plain `search` when submitting.
- The Java file is named `BinarySearch.java` to match its
  `public class BinarySearch`, as `javac` requires.
- **NeetCode's upper-bound guard is spelled `if (l && nums[l - 1] == target)`
  in Python and `l > 0 && ...` in Java**, which these files keep verbatim rather
  than normalising — the asymmetry is the subject of the section above.
