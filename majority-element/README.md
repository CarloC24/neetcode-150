# Majority Element

LeetCode 169 · [NeetCode solution page](https://neetcode.io/solutions/majority-element)

Given an array `nums` of size `n`, return the **majority element**: the value
that appears more than `⌊n / 2⌋` times. The majority element always exists.

`[5, 5, 1, 1, 1, 5, 5]` gives `5` (4 of 7).
`[2, 2, 2]` gives `2`.

**Constraints:** `1 <= n <= 5 * 10^4`, `-10^9 <= nums[i] <= 10^9`.

**Follow-up:** solve it in O(n) time and O(1) space.

```
majority-element/
├── java/
│   └── MajorityElement.java
├── python/
│   └── MajorityElement.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Brute force | O(n²) | O(1) | Count each element with a full scan. |
| 2 | Hash map | O(n) | O(n) | Count everything; the highest count wins. |
| 3 | Sorting | O(n log n) | O(n) Python, O(log n) Java | The majority's run always covers the middle index. |
| 4 | Bit manipulation | O(32n) | O(1) | Each bit of the answer is the majority bit at that position. |
| 5 | **Boyer-Moore voting** | **O(n)** | **O(1)** | Cancel pairs of different values; the majority survives. |
| 6 | Randomization | O(n) expected | O(1) | Pick at random and check; each pick wins with odds over 1/2. |

All six are NeetCode's.

**Approach 5 is the one to reach for.** It's the only one that meets the
follow-up in the worst case. Bit manipulation is also O(n) and O(1), but with a
32× constant. Randomization is O(n) only on average.

## Why sorting works: the middle is always covered

The majority takes up at least `⌊n/2⌋ + 1` slots. In the sorted array those
slots are one contiguous run, and a run that long can't avoid index `⌊n/2⌋`:

```
n = 7, majority run of length 4, slid from leftmost to rightmost:

index:   0  1  2  3  4  5  6
         M  M  M  M  .  .  .     starts at 0, ends at 3
         .  .  .  M  M  M  M     starts at 3, ends at 6
                  ^ index 3 = ⌊7/2⌋, covered either way
```

The run can start at most at `n - (⌊n/2⌋ + 1)`, which is at most `⌊n/2⌋`. It
ends at least at `⌊n/2⌋`. So `nums[n // 2]` is always the answer.

## Why Boyer-Moore works: pairs cancel

Pair each element with a *different* element and throw both away. The
majority has more copies than every other value put together, so it can never
run out of copies first. Whatever is left over at the end must be the majority.

Boyer-Moore does that pairing in one pass with a candidate and a count:

- `count` is how many copies of the candidate are still waiting for a partner.
- An element equal to the candidate adds a waiting copy (`+1`).
- Any other element pairs off with one waiting copy, and both are discarded (`-1`).
- At `count == 0` nothing is waiting, so the next element becomes the candidate.

The candidate can change hands several times. Example 1 hands it to `1` and
back:

```
num:        5  5  1  1  1  5  5
candidate:  5  5  5  5  1  1  5
count:      1  2  1  0  1  0  1
```

When the count hits 0 at index 3, the prefix `[5, 5, 1, 1]` has cancelled
completely. It held two 5s and two non-5s, so the majority still holds a
majority in the rest of the array. The same argument repeats every time the
count reaches 0, and the final candidate is the majority.

**It needs the guarantee.** If no majority exists, Boyer-Moore still returns
*something*: `[1, 2, 3]` returns `3`. When the guarantee isn't given, count the
candidate in a second pass to confirm it. Majority Element II (LeetCode 229,
more than `⌊n/3⌋`) uses the same idea with two candidates and needs that check.

## Why bit manipulation works, and the sign bit

The majority makes up more than half the array, so at every bit position, more
than half the numbers have the majority's bit there. Count the 1s at each of
the 32 positions, and wherever more than `n / 2` numbers have a 1, the answer
has a 1.

Values fit in a 32-bit signed int, so bit 31 is the sign bit, worth `-2^31` in
two's complement:

- **Java** gets this for free. `res |= 1 << 31` makes `res` negative, because
  an `int` *is* 32-bit two's complement.
- **Python** ints have no fixed width. `1 << 31` is `+2147483648`, so the
  answer for `[-1, -1, 2]` would come out as `4294967295`. The fix is to
  subtract `1 << 31` for bit 31 instead of adding it. The test cases `[-1, -1, 2]`
  and `[-10^9, 10^9, -10^9]` catch that bug.

Reading the bits is fine in Python: `num >> i` on a negative int shifts in
copies of the sign bit forever, so `(num >> i) & 1` gives the same bits as the
32-bit form.

## Python

**Requires:** Python 3.6+ (f-strings, `10_000`-style literals). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 majority-element/python/MajorityElement.py
```

Or from inside the folder:

```bash
cd majority-element/python
python3 MajorityElement.py
```

Expected output:

```
PASS  brute force: 15/15 cases
PASS  hash map: 15/15 cases
PASS  sorting: 15/15 cases
PASS  bit manipulation: 15/15 cases
PASS  boyer-moore: 15/15 cases
PASS  randomization: 15/15 cases
PASS  input left unmodified (all but sorting): [5, 5, 1, 1, 1, 5, 5]
```

## Java

**Requires:** JDK 11+ for the single-file launcher below (`Set.of` needs 9+).
Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java majority-element/java/MajorityElement.java
```

### Option 2 — compile, then run

```bash
cd majority-element/java
javac MajorityElement.java   # produces MajorityElement*.class
java MajorityElement         # note: no .class extension
```

Expected output (either option):

```
PASS  brute force: 15/15 cases
PASS  hash map: 15/15 cases
PASS  sorting: 15/15 cases
PASS  bit manipulation: 15/15 cases
PASS  boyer-moore: 15/15 cases
PASS  randomization: 15/15 cases
PASS  input left unmodified (all but sorting): [5, 5, 1, 1, 1, 5, 5]
```

To clean up the compiled artifacts from option 2:

```bash
rm majority-element/java/MajorityElement*.class
```

## Notes

- **Randomization's odds.** Each pick is the majority with probability over
  1/2, so the expected number of picks is under 2, and needing more than 20
  is less likely than one in a million. Both files seed the generator (`169`)
  so test runs repeat exactly. The seed changes how many picks it takes, never
  the answer.
- **The hash map can stop early.** As soon as a count passes `n // 2`, that's
  the answer. NeetCode's version tracks the running maximum instead, which
  gives the same result after the full pass.
- **`Map.merge` returns the new value**, so Java's approach 2 does one lookup
  per element instead of `getOrDefault`, `put` and two `get`s.
- **Brute force has a good and a bad case.** It returns at the first majority
  element it reaches, so a majority at the front costs one scan. The test
  `[3] * 1000 + [5] * 1001` is its worst case: 1,000 full scans before the
  first 5.
- **The harness copies each input**, because approach 3 sorts in place. A final
  check runs the other five on one shared array to confirm they leave it
  unchanged.
- **Verified exhaustively, in both languages:** every array of length 1–8 over
  the values `{-2^31, -1, 0, 2^31 - 1}` that has a majority (12,920 arrays,
  77,520 checks per language), against the count-over-`n / 2` definition. The
  same run checked that every approach except sorting leaves the input
  unchanged. The extreme values push the sign bit through approach 4. Zero
  failures in either language.
- **Method naming:** LeetCode uses `majorityElement`. These files use
  `majorityElement*` / `majority_element_*` with an approach suffix, so all six
  can coexist. Rename to plain `majorityElement` when submitting.
- The Java file is named `MajorityElement.java` to match its
  `public class MajorityElement`, as `javac` requires.
