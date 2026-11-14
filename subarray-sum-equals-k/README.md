# Subarray Sum Equals K

LeetCode 560 · [NeetCode solution page](https://neetcode.io/solutions/subarray-sum-equals-k)

Given an array of integers `nums` and an integer `k`, return the number of
subarrays whose sum equals `k`. A subarray is a contiguous, non-empty part of
the array.

`nums = [1, 1, 1], k = 2` gives `2`: `[1, 1]` twice.
`nums = [2, -1, 1, 2], k = 2` gives `4`: `[2]`, `[2, -1, 1]`, `[-1, 1, 2]`
and the last `[2]`.

**Constraints:** `1 <= n <= 2 * 10^4`, `-1000 <= nums[i] <= 1000`,
`-10^7 <= k <= 10^7`.

```
subarray-sum-equals-k/
├── java/
│   └── SubarraySumEqualsK.java
├── python/
│   └── SubarraySumEqualsK.py
└── README.md
```

Each file implements both approaches side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Brute force | O(n²) | O(1) | Every start, a running total over every end. |
| 2 | **Prefix sums + hash map** | **O(n)** | **O(n)** | Count earlier prefix sums equal to `prefix - k`. |

Both are NeetCode's.

**Approach 2 is the one to reach for.** At `n = 20,000`, brute force checks
about 200 million subarrays.

## The idea: a subarray is a difference of two prefix sums

Let `prefix[j]` be the sum of the first `j` elements, with `prefix[0] = 0`. The
subarray from index `i` to `j - 1` sums to `prefix[j] - prefix[i]`. So:

```
sum(nums[i:j]) == k   <=>   prefix[i] == prefix[j] - k
```

Walking left to right, the number of subarrays that **end here** and sum to
`k` is the number of **earlier** prefix sums equal to `prefix - k`. A hash map
from prefix sum to how many times it has occurred answers that in O(1).

```python
res = prefix = 0
seen = {0: 1}                         # the empty prefix, before index 0
for num in nums:
    prefix += num
    res += seen.get(prefix - k, 0)    # look up first...
    seen[prefix] = seen.get(prefix, 0) + 1   # ...then record this prefix
return res
```

On NeetCode's first example, `[2, -1, 1, 2]` with `k = 2`:

```
num  prefix  prefix-k  times seen  res  map after
  2       2         0           1    1  {0: 1, 2: 1}
 -1       1        -1           0    1  {0: 1, 1: 1, 2: 1}
  1       2         0           1    2  {0: 1, 1: 1, 2: 2}
  2       4         2           2    4  {0: 1, 1: 1, 2: 2, 4: 1}
```

The last row finds prefix 2 twice: after `[2]` and after `[2, -1, 1]`. Those are
the two subarrays that end at the last element, `[-1, 1, 2]` and `[2]`.

## Three easy ways to get it wrong

| Bug | Smallest input that breaks it | Result |
|---|---|---|
| Starting with an empty map instead of `{0: 1}` | `[-1]`, `k = -1` | `0`: a subarray starting at index 0 has no earlier prefix to match. The answer is `1`. |
| Recording `prefix` before looking up `prefix - k` | `[-1]`, `k = 0` | `1`: with `k = 0` the current prefix matches itself, an empty subarray. The answer is `0`. |
| A sliding window | `[-1, -1]`, `k = -2` | `0` instead of `1`. |

All three inputs were found by searching every array in length order. The test
set has a case for each bug.

**Why a sliding window can't work here.** Shrinking the window when the sum is
too big assumes that adding elements only makes the sum bigger. Negative
numbers break that. Zeros break it too, even with no negatives: `[0, 0]` with
`k = 0` has three subarrays, but a window counts only one per right end and
gets `2`. A window is right only when every value is positive.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 subarray-sum-equals-k/python/SubarraySumEqualsK.py
```

Or from inside the folder:

```bash
cd subarray-sum-equals-k/python
python3 SubarraySumEqualsK.py
```

Expected output:

```
PASS  brute force: 18/18 cases (skips the 5 long cases)
PASS  prefix sums + hash map: 23/23 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java subarray-sum-equals-k/java/SubarraySumEqualsK.java
```

### Option 2 — compile, then run

```bash
cd subarray-sum-equals-k/java
javac SubarraySumEqualsK.java   # produces SubarraySumEqualsK*.class
java SubarraySumEqualsK         # note: no .class extension
```

Expected output (either option):

```
PASS  brute force: 18/18 cases (skips the 5 long cases)
PASS  prefix sums + hash map: 23/23 cases
```

To clean up the compiled artifacts from option 2:

```bash
rm subarray-sum-equals-k/java/SubarraySumEqualsK*.class
```

## Notes

- **Test cases:** the LeetCode and NeetCode examples, the bug inputs above,
  all zeros, nothing summing to `k`, six random arrays (seeded with `560`)
  checked against brute force, and five 20,000-element arrays with answers
  worked out by hand: all zeros (200,010,000 subarrays), alternating 1 and -1
  (100,000,000), all ones with `k = 1000`, and the value and `k` limits. Brute
  force skips the long arrays.
- **`int` is enough in Java.** Prefix sums stay within 20,000 × 1,000 = 2 × 10^7,
  and the answer is at most 20,000 × 20,001 / 2 = 200,010,000. The all-zeros
  case hits that maximum.
- **Python: look up with `.get`.** The Python file stores counts in a
  `defaultdict`, but indexing one inserts missing keys. `seen[prefix - k]`
  would add a 0 entry for every miss and up to double the map's size. `.get`
  reads without inserting.
- **Verified exhaustively, in both languages:** every array of length 1–7 over
  `{-2, -1, 0, 1, 2}`, each with every `k` from -3 to 3 (683,585 pairs,
  1,367,170 checks per language), against a count of every subarray's sum.
  Zero failures in either language.
- **Method naming:** LeetCode uses `subarraySum`. These files use
  `subarraySum*` / `subarray_sum_*` with an approach suffix, so both can
  coexist. Rename to plain `subarraySum` when submitting.
- The Java file is named `SubarraySumEqualsK.java` to match its `public class
  SubarraySumEqualsK`, as `javac` requires.
