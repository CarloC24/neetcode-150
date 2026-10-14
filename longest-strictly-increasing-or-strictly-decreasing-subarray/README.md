# Longest Strictly Increasing or Strictly Decreasing Subarray

LeetCode 3105 · [NeetCode solution page](https://neetcode.io/solutions/longest-strictly-increasing-or-strictly-decreasing-subarray)

Given an array of integers, return the length of the longest **subarray** that
is either **strictly** increasing or **strictly** decreasing.

`[1, 4, 3, 3, 2]` gives `2`: the longest increasing run is `[1, 4]` and the
longest decreasing run is `[4, 3]` or `[3, 2]`. `[3, 3, 3, 3]` gives `1`,
since equal neighbours count as neither. `[3, 2, 1]` gives `3`.

**Constraints:** `1 <= nums.length <= 50`, `1 <= nums[i] <= 50`.

```
longest-strictly-increasing-or-strictly-decreasing-subarray/
├── java/
│   └── LongestStrictlyIncreasingOrStrictlyDecreasingSubarray.java
├── python/
│   └── LongestStrictlyIncreasingOrStrictlyDecreasingSubarray.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Brute force | O(n²) | O(1) | From every start, extend the run while the direction holds. |
| 2 | Direction flag | O(n) | O(1) | One run length plus a flag for up, down or neither. |
| 3 | **Two counters** | **O(n)** | **O(1)** | Track the increasing run and the decreasing run ending here, both at once. |
| 4 | Direction from start | O(n) | O(1) | One run length; read its direction from the run's first step. |

All four are NeetCode's.

**Approach 3 is the one to reach for.** Approaches 2, 3 and 4 are all one pass
with O(1) space, so the choice comes down to which is easiest to get right.
Approach 3 has no direction to track and no special restart rule. At each step,
one counter grows and the other resets to 1, or both reset on an equal pair:

```python
if nums[i] > nums[i - 1]:
    inc, dec = inc + 1, 1
elif nums[i] < nums[i - 1]:
    inc, dec = 1, dec + 1
else:
    inc = dec = 1
```

At `n <= 50`, even the brute force is about 1,225 steps, so the constraints
don't force anything faster. The point is the pattern: **track the length of
the best run ending at the current position**, and the longest run anywhere is
the largest of those.

## Subarray, not subsequence

A **subarray** is contiguous. That's what makes this an Easy problem with a
one-pass answer: a run is broken the moment one step goes the wrong way, and
nothing before that point can help afterwards.

The **subsequence** version, where you may skip elements, is Longest Increasing
Subsequence. It's in the 1-D DP section of the plan, and it needs O(n log n) or
O(n²) DP. On `[1, 3, 2, 4]` the longest increasing *subarray* is 2 (`[1, 3]` or
`[2, 4]`), but the longest increasing *subsequence* is 3 (`[1, 3, 4]` or
`[1, 2, 4]`).

## When the direction flips, the new run starts at 2

In approach 2, when the run changes direction the counter restarts at **2**,
not 1. The turning point belongs to both runs:

```
[3, 1, 2, 3, 4]
 3 → 1         down, run [3, 1]       length 2
     1 → 2     flip: new run [1, 2]   length 2, not 1
         → 3                          length 3
             → 4                      length 4   ← answer
```

Restart at 1 and this returns 3. An equal pair is different: neither element
can extend the other's run, so the counter really does go back to 1.

Approach 3 gets this right without a rule. On the flip, `inc` was reset to 1 on
the step before, so it becomes 2 when the flip step extends it. The
`[3, 1, 2, 3, 4]` case is in the test set to check this in every approach.

## How approach 4 finds the direction

Approach 4 doesn't store the direction at all. If the current run has length
`cur_len` and ends at `i - 1`, it started at `i - cur_len`, so its first step is
`nums[i - cur_len] → nums[i - cur_len + 1]`. That step's direction is the run's
direction.

Two details make it work:

- **A run of length 1 checks against itself.** When `cur_len` is 1, the "first
  step" is `nums[i - 1] → nums[i]`, the step being tested, so it always agrees
  and the run grows to 2.
- **Skipping the `res` update on a reset is safe.** A reset to 2 happens only on
  a direction flip, and a flip needs a run of at least 2 already. So `res` is
  already at least 2 whenever the update is skipped.

It's correct (the exhaustive check below confirms it), but it's the hardest of
the four to check by eye.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 longest-strictly-increasing-or-strictly-decreasing-subarray/python/LongestStrictlyIncreasingOrStrictlyDecreasingSubarray.py
```

Or from inside the folder:

```bash
cd longest-strictly-increasing-or-strictly-decreasing-subarray/python
python3 LongestStrictlyIncreasingOrStrictlyDecreasingSubarray.py
```

Expected output:

```
PASS  brute force: 15/15 cases
PASS  direction flag: 15/15 cases
PASS  two counters: 15/15 cases
PASS  direction from start: 15/15 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java longest-strictly-increasing-or-strictly-decreasing-subarray/java/LongestStrictlyIncreasingOrStrictlyDecreasingSubarray.java
```

### Option 2 — compile, then run

```bash
cd longest-strictly-increasing-or-strictly-decreasing-subarray/java
javac LongestStrictlyIncreasingOrStrictlyDecreasingSubarray.java
java LongestStrictlyIncreasingOrStrictlyDecreasingSubarray   # note: no .class extension
```

Expected output (either option):

```
PASS  brute force: 15/15 cases
PASS  direction flag: 15/15 cases
PASS  two counters: 15/15 cases
PASS  direction from start: 15/15 cases
```

To clean up the compiled artifacts from option 2:

```bash
rm longest-strictly-increasing-or-strictly-decreasing-subarray/java/*.class
```

## Notes

- **Strict means equal neighbours break both directions.** `[3, 3, 3, 3]` is 1,
  and `[1, 2, 2, 3, 4]` is 3, not 5. If you write `>=` or `<=` anywhere, the
  equal-pair cases in the test set fail.
- **The answer is never 0.** `n >= 1`, and a single element counts as a run of
  length 1, so every approach starts `res` at 1. A one-element array needs no
  special case: every loop is empty.
- **The peak is shared.** In `[1, 2, 3, 4, 3, 2, 1, 0]` the increasing run is
  `[1, 2, 3, 4]` and the decreasing run is `[4, 3, 2, 1, 0]`. The `4` belongs to
  both, which is why the answer is 5, not 4.
- **The file and class names are long** because they follow the repo convention
  of naming each one after the problem's NeetCode slug. LeetCode's method name is
  `longestMonotonicSubarray`.
- **Verified exhaustively, in both languages:** every array of length 1–8 with
  values 1–4 (87,380 arrays, 349,520 checks per language), against an oracle
  that tests every subarray directly. Zero failures in either language.
- **Method naming:** these files use `longest*` / `longest_*` with an approach
  suffix, so all four can coexist. Rename to `longestMonotonicSubarray` when
  submitting.
