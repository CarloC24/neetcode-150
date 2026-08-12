# Two Sum

LeetCode 1 · [NeetCode solution page](https://neetcode.io/solutions/two-sum)

Given an integer array `nums` and an integer `target`, return the two indices
`i` and `j` such that `nums[i] + nums[j] == target` and `i != j`.

**Constraints:** `2 <= nums.length <= 1000`, `-10^7 <= nums[i], target <= 10^7`,
and exactly one valid pair exists.

```
two-sum/
├── java/
│   └── TwoSum.java
├── python/
│   └── TwoSum.py
└── README.md
```

Each file implements all four approaches side by side, plus a `main` that runs
them against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Brute force | O(n²) | O(1) | Try every pair. |
| 2 | Two pointers | O(n log n) | O(n) | Sort, then close in from both ends. |
| 3 | Hash map (two pass) | O(n) | O(n) | Index everything, then look up each complement. |
| 4 | **Hash map (one pass)** | **O(n)** | **O(n)** | Look the complement up among values already behind you. |

**Approach 4 is the one to reach for.** It has the same complexity as approach 3
but reads better and, more importantly, needs no guard against an element
pairing with itself — see below.

Approach 2 is worth knowing even though it is asymptotically worse here,
because the two-pointer walk is the technique the rest of this section of the
roadmap builds on. Note that it is O(n) space *because* the indices must be
carried alongside the values; a version that sorts `nums` in place would be
O(1) space but could no longer report where the answer came from.

## The `i != j` trap

Every approach has to prevent a value from pairing with itself, and each does it
differently — this is the actual substance of the problem:

| Approach | How |
|---|---|
| Brute force | Inner loop starts at `i + 1` |
| Two pointers | Loop condition is `left < right` |
| Hash map (two pass) | Explicit `indices[diff] != i` check |
| Hash map (one pass) | **Nothing** — the value is stored *after* the lookup |

Approach 4 gets it for free: when a value looks up its complement, it has not
been added to the map yet, so it cannot find itself. That is the real argument
for the one-pass version over the two-pass one — a guard you don't have to
write is a guard you can't forget.

The test case `nums = [3, 2, 4], target = 6` exists to catch exactly this. The
answer is `[1, 2]`, but `target - nums[0]` is `3`, sitting at index `0`; any
approach that forgets the rule returns `[0, 0]`.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 two-sum/python/TwoSum.py
```

Or from inside the folder:

```bash
cd two-sum/python
python3 TwoSum.py
```

Expected output:

```
PASS  brute force: 9/9 cases
PASS  two pointers: 9/9 cases
PASS  hash map (two pass): 9/9 cases
PASS  hash map (one pass): 9/9 cases
PASS  input left unmodified: [3, 2, 4]
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java two-sum/java/TwoSum.java
```

### Option 2 — compile, then run

```bash
cd two-sum/java
javac TwoSum.java   # produces TwoSum.class
java TwoSum         # note: no .class extension
```

Expected output (either option):

```
PASS  brute force: 9/9 cases
PASS  two pointers: 9/9 cases
PASS  hash map (two pass): 9/9 cases
PASS  hash map (one pass): 9/9 cases
PASS  input left unmodified: [3, 2, 4]
```

To clean up the compiled artifact from option 2:

```bash
rm two-sum/java/TwoSum.class
```

## Notes

- **Every test case has exactly one valid pair.** The constraints guarantee it,
  and the harness depends on it: if a case admitted two answers, four approaches
  could each return something different and correct, and comparing them against
  one expected value would report false failures.
- **All four happen to return the smaller index first**, so the harness compares
  results exactly rather than normalizing them. That is not something the
  problem asks for — LeetCode accepts either order — it just makes the
  comparison strict. For the two-pointer approach it takes explicit `min`/`max`,
  since sorted order says nothing about original positions.
- **The two-pass map keeps only the last index of a duplicate value**
  (`indices[num] = i` overwrites). Harmless here: when the answer needs a value
  twice, as in `[5, 5]`, the scan reaches the earlier occurrence first and the
  map supplies the later one.
- **No integer overflow, but it is closer than it looks.** These constraints cap
  values at ±10⁷, so `nums[i] + nums[j]` stays tiny. Under LeetCode's own
  stated range of ±10⁹ the brute-force sum can reach 2×10⁹ — still under
  `Integer.MAX_VALUE` (2,147,483,647), but with under 7% to spare. Widen the
  range any further and brute force needs a `long`; the hash map approaches
  compute `target - nums[i]`, which has the same headroom.
- **Method naming:** both NeetCode and LeetCode use `twoSum`. These files use
  `twoSum*` / `two_sum_*` suffixed by approach so all four can coexist — rename
  to plain `twoSum` when submitting.
- The Java file is named `TwoSum.java` to match its `public class TwoSum`, as
  `javac` requires.
