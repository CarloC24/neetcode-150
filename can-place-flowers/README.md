# Can Place Flowers

LeetCode 605 · [NeetCode solution page](https://neetcode.io/solutions/can-place-flowers)

A flowerbed is a list of plots: `0` is empty, `1` already has a flower. No two
flowers may be in adjacent plots. Given the flowerbed and a number `n`, return
`true` if `n` new flowers can be planted without breaking that rule.

**Constraints:** `1 <= flowerbed.length <= 2 * 10^4`, each plot is `0` or `1`,
there are no two adjacent flowers to start with, and `0 <= n <= flowerbed.length`.

```
can-place-flowers/
├── java/
│   └── CanPlaceFlowers.java
├── python/
│   └── CanPlaceFlowers.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

The problem already uses `n` for the number of flowers, so `L` is the length of
the flowerbed.

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Padded copy | O(L) | O(L) | Add an empty plot at each end; plant wherever three in a row are empty. |
| 2 | In place | O(L) | O(1)* | The same greedy, with the edges checked explicitly. Writes into the input. |
| 3 | **Greedy, skip ahead** | **O(L)** | **O(1)** | Jump past each new flower instead of recording it; stop once `n` fit. |
| 4 | Count empty runs | O(L) | O(1) | Each run of `k` empty plots holds a fixed number of flowers. |

\* O(1) extra space only because it modifies the caller's array.

Approaches 1 and 4 are NeetCode's. Approach 2 is LeetCode's official solution.
Approach 3 is approach 2 reworked so it never needs to write anything down.

**Approach 3 is the one to reach for.** Approaches 2, 3 and 4 are all O(1)
space, but approach 2 gets there by modifying the input, which callers rarely
expect. Approach 4 is correct but leans on an off-by-one formula that's easy to
get wrong (see below). Approach 3 does neither, and it's the only one that
**stops early**: once `n` flowers fit, it returns without reading the rest of
the bed. When `n == 0` it returns `true` without reading anything.

## Why greedy is safe

All four approaches plant at the **leftmost** plot that can take a flower.
That never costs a flower later.

Suppose some best arrangement leaves the leftmost available plot `p` empty and
puts its first new flower further right, at `q`. Move that flower from `q` back
to `p`. Plot `p` was available, so nothing on its left conflicts. And the only
plot to its right that could conflict is `p + 1`, which can't hold a flower,
because `q` was the first new one and `p + 1` was empty to begin with. The
arrangement is still valid, with the same number of flowers, and now it agrees
with the greedy choice. Repeat for the next flower, and the greedy's count is
always as large as the best.

The exhaustive check below confirms it: every greedy approach agrees with a
brute force that tries every subset of plots.

## Skipping ahead instead of planting

Approaches 1 and 2 write each new flower into the bed, so the next plot sees it
as a neighbour. Approach 3 notices that the **only** plot that ever reads that
write is the one immediately after it. So instead of writing the flower, it
skips that plot:

```python
if flowerbed[i] == 1:
    i += 2          # the next plot is next to a flower
elif i + 1 < length and flowerbed[i + 1] == 1:
    i += 3          # this plot touches the flower at i + 1; so does i + 2
else:
    n -= 1          # plant here
    i += 2
```

Every jump lands on a plot whose left neighbour is known to be empty: the edge,
or a plot skipped *because* the one before it had a flower. So the loop never
checks the left side at all. It also never modifies the input.

## The off-by-one in counting runs

Approach 4 counts each run of consecutive empty plots and works out how many
flowers it can hold. That count depends on what's at each end:

| Run of `k` empty plots | Holds | Why |
|---|---|---|
| Between two flowers | `(k − 1) / 2` | The plots touching each flower are out; the other `k − 2` alternate. |
| At one edge | `k / 2` | Only one flower to stay clear of. |
| The whole bed | `(k + 1) / 2` | Nothing to stay clear of. |

All three come from one formula, `(k − 1) / 2`, if each edge counts as one extra
empty plot. That's why NeetCode starts the counter at `1` when the bed starts
empty, and charges the final run `empty / 2` instead of `(empty − 1) / 2`.

**The trap is in Python's `//`.** When the bed *starts* with a flower, the
counter is `0` when it reaches that flower, so the formula computes
`(0 − 1) / 2`:

| Expression | Result | Why |
|---|---|---|
| Java `(0 - 1) / 2` | `0` | Integer division truncates toward zero. |
| Python `int((0 - 1) / 2)` | `0` | `int(-0.5)` truncates too. This is NeetCode's spelling. |
| Python `(0 - 1) // 2` | **`-1`** | `//` rounds **down**. |

Subtracting `-1` from `n` *adds* a flower to the requirement. Every bed that
starts with a flower then asks for one more flower than it should:
`[1]` with `n = 0` returns `false`, and `[1, 0, 0, 0, 0]` with `n = 2` returns
`false`. NeetCode's `int((empty - 1) / 2)` looks like a clumsy way to write
`//`, but it's the version that works. Both files here write
`max(0, (empty - 1) // 2)` instead, which states the intent without depending
on either rounding rule. `([1], 0)` and `([1, 0, 0, 0, 0], 2)` are in the test
set because each catches the `//` version.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 can-place-flowers/python/CanPlaceFlowers.py
```

Or from inside the folder:

```bash
cd can-place-flowers/python
python3 CanPlaceFlowers.py
```

Expected output:

```
PASS  padded copy: 18/18 cases
PASS  in place: 18/18 cases
PASS  greedy, skip ahead: 18/18 cases
PASS  count empty runs: 18/18 cases
PASS  input left unmodified (all but in place): [1, 0, 0, 0, 1]
```

## Java

**Requires:** JDK 11+ for the single-file launcher below (`Set.of` needs 9+).
Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java can-place-flowers/java/CanPlaceFlowers.java
```

### Option 2 — compile, then run

```bash
cd can-place-flowers/java
javac CanPlaceFlowers.java   # produces CanPlaceFlowers*.class
java CanPlaceFlowers         # note: no .class extension
```

Expected output (either option):

```
PASS  padded copy: 18/18 cases
PASS  in place: 18/18 cases
PASS  greedy, skip ahead: 18/18 cases
PASS  count empty runs: 18/18 cases
PASS  input left unmodified (all but in place): [1, 0, 0, 0, 1]
```

To clean up the compiled artifacts from option 2:

```bash
rm can-place-flowers/java/CanPlaceFlowers*.class
```

## Notes

- **`n == 0` is always `true`**, even for a bed with no room at all. `([1], 0)`
  is in the test set. Approach 3 checks `n > 0` in its loop condition, so it
  returns without reading the bed.
- **`n <= 0`, not `n == 0`.** Approaches 1, 3 and 4 count `n` down. Approaches
  1 and 4 keep going after reaching zero, so `n` can end up negative. Returning
  `n == 0` would reject a bed with room to spare.
- **The harness copies each input.** Approach 2 writes flowers into the array
  it's given, so without a copy every approach after it would see a bed that's
  already been planted. Every approach gets a fresh copy of each test case. A final check runs the
  other three on one shared array and confirms it comes back unchanged.
- **Approach 1 in Java uses `System.arraycopy`** where NeetCode uses a loop.
  It's the same O(L) copy, written as one call.
- **Verified beyond the test set, in both languages:** every valid flowerbed
  of length 1–10 (no two adjacent flowers to start with), for every `n` from 0
  to the length, against a brute force that tries every subset of plots. That's
  14,156 checks per language, with zero failures.
- **Method naming:** LeetCode uses `canPlaceFlowers`. These files use
  `canPlaceFlowers*` / `can_place_flowers_*` with an approach suffix, so all four
  can coexist. Rename to plain `canPlaceFlowers` when submitting.
- The Java file is named `CanPlaceFlowers.java` to match its
  `public class CanPlaceFlowers`, as `javac` requires.
