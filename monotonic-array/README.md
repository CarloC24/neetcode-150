# Monotonic Array

LeetCode 896 · [NeetCode solution page](https://neetcode.io/solutions/monotonic-array)

An array is **monotonic** if it is either monotone increasing or monotone
decreasing:

- **Monotone increasing:** `nums[i] <= nums[j]` for all `i <= j`. It never goes
  down.
- **Monotone decreasing:** `nums[i] >= nums[j]` for all `i <= j`. It never goes
  up.

Return whether `nums` is monotonic. Equal neighbours are allowed in both
directions, so `[1, 2, 2, 3]` and `[6, 5, 4, 4]` are monotonic, and `[1, 3, 2]`
isn't.

**Constraints:** `1 <= nums.length <= 10^5`, `-10^5 <= nums[i] <= 10^5`.

```
monotonic-array/
├── java/
│   └── MonotonicArray.java
├── python/
│   └── MonotonicArray.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Sorting | O(n log n) | O(n) | Monotonic means already sorted, forwards or backwards. |
| 2 | Two pass | O(n) | O(1) | Check never-goes-down; if that fails, check never-goes-up. |
| 3 | **First vs last** | **O(n)** | **O(1)** | The two ends decide the only possible direction; check just that one. |
| 4 | Two flags | O(n) | O(1) | Rule out each direction as soon as a step contradicts it. |

Approaches 2–4 are NeetCode's. Approach 1 is a baseline that checks the
definition directly.

**Approach 3 is the one to reach for.** It's a single scan checking a single
direction, and it stops at the first step that goes the wrong way. Approach 2
may scan the array twice. Approach 4 makes two comparisons per step instead of
one.

## Adjacent pairs are enough

The definition says "for all `i <= j`", which sounds like O(n²) pairs. It
isn't. If every **adjacent** step satisfies `nums[i] <= nums[i + 1]`, the whole
chain does too, by transitivity: `a <= b` and `b <= c` give `a <= c`. So one
step that goes down is the only way to break "increasing", and a linear scan of
neighbours finds it.

The exhaustive check below uses the literal all-pairs definition as its oracle,
so it also checks this reduction.

## Why the ends decide the direction

Approach 3 looks at `nums[0]` and `nums[-1]` before scanning anything:

| Ends | Only possible direction | Why |
|---|---|---|
| `first < last` | increasing | A decreasing array would need `first >= last`. |
| `first > last` | decreasing | An increasing array would need `first <= last`. |
| `first == last` | increasing (any check works) | A monotonic array with equal ends must be constant, and a constant array counts as both. |

So only one direction needs checking. The equal-ends row is the subtle one.
`[1, 2, 1]` and `[3, 1, 2, 3]` have equal ends but aren't constant. They're in
the test set to check that approach 3 still rejects them: it checks
"increasing", and each has a step going down.

## Strict vs non-strict

The classic slip is writing `<` where the problem means `<=`. "Monotone
increasing" here **allows** equal neighbours. Check for a strictly increasing
step and `[1, 2, 2, 3]` fails, along with every constant array.

This is the opposite of
[Longest Strictly Increasing or Strictly Decreasing Subarray](../longest-strictly-increasing-or-strictly-decreasing-subarray/),
where an equal pair breaks both directions. In approach 4 here, an equal step
rules out **neither** direction. There, it resets both counters.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 monotonic-array/python/MonotonicArray.py
```

Or from inside the folder:

```bash
cd monotonic-array/python
python3 MonotonicArray.py
```

Expected output:

```
PASS  sorting: 16/16 cases
PASS  two pass: 16/16 cases
PASS  first vs last: 16/16 cases
PASS  two flags: 16/16 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java monotonic-array/java/MonotonicArray.java
```

### Option 2 — compile, then run

```bash
cd monotonic-array/java
javac MonotonicArray.java   # produces MonotonicArray*.class
java MonotonicArray         # note: no .class extension
```

Expected output (either option):

```
PASS  sorting: 16/16 cases
PASS  two pass: 16/16 cases
PASS  first vs last: 16/16 cases
PASS  two flags: 16/16 cases
```

To clean up the compiled artifacts from option 2:

```bash
rm monotonic-array/java/MonotonicArray*.class
```

## Notes

- **A single element is monotonic**, and so is any constant array. `[1]` and
  `[5, 5, 5]` are in the test set. Every approach handles length 1 with no
  special case, because each loop starts at index 1.
- **Approach 4 exits early here; NeetCode's doesn't.** NeetCode's two-flag
  version always walks to the end, even after both flags are false. This version
  returns as soon as both directions are ruled out. On `[1, 3, 2, …]` that's after
  two steps, however long the array is.
- **Java's `Arrays.sort` has no reverse order for `int[]`.** The descending
  comparator overloads only work on object arrays like `Integer[]`, which would
  mean boxing every element. Approach 1 in Java sorts once and reads the sorted
  copy from the back for the decreasing check. Python just calls
  `sorted(nums, reverse=True)`.
- **The ceiling cases end with the break.** `[0, 1, …, 99998, 0]` goes wrong
  only at the very last step, so every approach has to scan all 10⁵ elements to
  reject it.
- **Verified exhaustively, in both languages:** every array of length 1–8 with
  values 0–3 (87,380 arrays, 349,520 checks per language), against the literal
  all-pairs definition. Zero failures in either language.
- **Method naming:** LeetCode uses `isMonotonic`. These files use
  `isMonotonic*` / `is_monotonic_*` with an approach suffix, so all four can
  coexist. Rename to plain `isMonotonic` when submitting.
- The Java file is named `MonotonicArray.java` to match its
  `public class MonotonicArray`, as `javac` requires.
