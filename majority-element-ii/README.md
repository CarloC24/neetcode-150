# Majority Element II

LeetCode 229 · [NeetCode solution page](https://neetcode.io/solutions/majority-element-ii)

Given an integer array `nums` of size `n`, return every value that appears
**more than** `⌊n / 3⌋` times, in any order.

`[3, 2, 3]` gives `[3]`.
`[1, 2]` gives `[1, 2]` (`⌊2/3⌋ = 0`, so anything that appears counts).
`[1, 2, 3]` gives `[]`.

**Constraints:** `1 <= n <= 5 * 10^4`, `-10^9 <= nums[i] <= 10^9`.

**Follow-up:** solve it in O(n) time and O(1) space.

This is the follow-on to [Majority Element](../majority-element/), which asks
for the one value that appears more than `n / 2` times.

```
majority-element-ii/
├── java/
│   └── MajorityElementII.java
├── python/
│   └── MajorityElementII.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Brute force | O(n²) | O(1) | Count each element with a full scan. |
| 2 | Sorting | O(n log n) | O(n) | Equal values form runs; measure each run. |
| 3 | Hash map | O(n) | O(n) | Count everything, keep the counts over `n / 3`. |
| 4 | **Boyer-Moore voting** | **O(n)** | **O(1)** | Two candidates; cancel three different values at a time. |
| 5 | Boyer-Moore, hash map | O(n) | O(1) | The same voting with the candidates in a map of at most 3. |

All five are NeetCode's. Space doesn't count the output, which has at most two
values.

**Approach 4 is the one to reach for.** It's the only one that meets the
follow-up with plain variables. Approach 5 is the same algorithm, and worth
knowing because it extends to "more than `n / k` times" by allowing `k - 1`
candidates. The hash map is the simplest correct answer if O(n) space is
allowed.

## At most two answers

Three different values that each appear more than `n / 3` times would need more
than `n` elements between them. So there are at most two answers, and two
candidate slots are enough.

## Boyer-Moore with two candidates

Keep two candidates, each with a vote count. For each value:

1. **It's one of the candidates:** add a vote for it.
2. **A slot is empty** (count 0): it becomes that candidate, with 1 vote.
3. **Otherwise:** take one vote from each candidate. That cancels three
   different values at once: this one and one of each candidate.

A value that appears more than `n / 3` times can't be cancelled out: every
cancellation uses up three different values, and there aren't enough other
values to cancel all of its votes. So every answer ends up as a candidate.

The reverse isn't true. `[1, 2, 3]` ends with 3 as a candidate, and 3 is not an
answer. **A second pass counts both candidates for real.**

On NeetCode's first example:

```
num  action           num1 cnt1  num2 cnt2
5    take slot 1         5    1    -1    0
2    take slot 2         5    1     2    1
3    cancel one each     5    0     2    0
2    vote for num2       5    0     2    1
2    vote for num2       5    0     2    2
2    vote for num2       5    0     2    3
2    vote for num2       5    0     2    4
5    vote for num1       5    1     2    4
5    vote for num1       5    2     2    4
5    vote for num1       5    3     2    4
```

The second pass counts 5 four times and 2 five times. Both are more than
`⌊10/3⌋ = 3`, so the answer is `[5, 2]`.

Rows 4 onwards show why NeetCode's `-1` starting values are safe even though
`-1` is a legal input. After the cancel, slot 1 still says `5` with 0 votes.
When the next 5 arrives it matches slot 1 and gets 1 vote, which is what
taking the empty slot would have done anyway. A placeholder only matters with
0 votes, and then matching it and taking the slot are the same thing.

## Three easy ways to break it

| Bug | Smallest input that breaks it | Result |
|---|---|---|
| Checking for an empty slot before checking the candidates | `[1, 1]` | `[1, 1]`: the second 1 takes slot 2 while 1 is already in slot 1. |
| ...the same bug, deduplicated | `[1, 1, 2, 2, 2]` | `[2]`: both slots hold 1, the 2s cancel them out, and 1 is lost. |
| Skipping the second pass, or using `>=` | `[1, 1, 2]` | `[1, 2]`: 2 is left as a candidate, but appears once, not more than once. |

All three inputs were found by searching every array in length order, and all
three are in the test set. In the second pass, `elif` / `else if` matters too:
if both slots ever hold the same value, it must be counted once.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 majority-element-ii/python/MajorityElementII.py
```

Or from inside the folder:

```bash
cd majority-element-ii/python
python3 MajorityElementII.py
```

Expected output:

```
PASS  brute force: 15/15 cases (skips the 4 long cases)
PASS  sorting: 19/19 cases
PASS  hash map: 19/19 cases
PASS  boyer-moore: 19/19 cases
PASS  boyer-moore, hash map: 19/19 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java majority-element-ii/java/MajorityElementII.java
```

### Option 2 — compile, then run

```bash
cd majority-element-ii/java
javac MajorityElementII.java   # produces MajorityElementII.class
java MajorityElementII         # note: no .class extension
```

Expected output (either option):

```
PASS  brute force: 15/15 cases (skips the 4 long cases)
PASS  sorting: 19/19 cases
PASS  hash map: 19/19 cases
PASS  boyer-moore: 19/19 cases
PASS  boyer-moore, hash map: 19/19 cases
```

To clean up the compiled artifacts from option 2:

```bash
rm majority-element-ii/java/MajorityElementII*.class
```

## Notes

- **Test cases:** the LeetCode and NeetCode examples, values that appear exactly
  `n / 3` times (not enough), `-1` (the placeholder) as an answer and as a
  non-answer, the ±10^9 limits, the three bug inputs above, and four
  50,000-element arrays: all one value, two values 16,667 times each (just over
  the threshold), one value exactly 16,666 times (not enough), and random values
  from -3 to 3. The long arrays are seeded (`229`). Brute force skips them: it
  would take 2.5 billion comparisons.
- **The harness accepts any order** but no repeats: it sorts the result and
  compares it to the expected answer. It also checks that `nums` is unchanged.
- **Sorting copies the array.** NeetCode sorts `nums` in place, which saves the
  O(n) copy but changes the caller's array. Sorting in place would make the
  space O(1) in Java (O(log n) for the sort's stack).
- **Approach 5's map holds at most three entries**, and only for a moment: as
  soon as a third value enters, everything loses a vote and the newcomer drops
  out. Rebuilding a map of three entries is O(1).
- **Verified exhaustively, in both languages:** every array of length 1–8 over
  `{-1, 0, 1, 2}` (87,380 arrays, 436,900 checks per language), against counts
  from the definition. Zero failures in either language.
- **Method naming:** LeetCode uses `majorityElement`. These files use
  `majorityElement*` / `majority_element_*` with an approach suffix, so all
  five can coexist. Rename to plain `majorityElement` when submitting.
- The Java file is named `MajorityElementII.java` to match its `public class
  MajorityElementII`, as `javac` requires.
