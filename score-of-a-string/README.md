# Score of a String

LeetCode 3110 · [NeetCode solution page](https://neetcode.io/solutions/score-of-a-string)

The **score** of a string is the sum of the absolute differences between the
ASCII values of adjacent characters. Return it.

**Constraints:** `2 <= s.length <= 100`, and `s` consists only of lowercase
English letters.

```
score-of-a-string/
├── java/
│   └── ScoreOfAString.java
├── python/
│   └── ScoreOfAString.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | **Iteration** | **O(n)** | **O(1)** | Sum the gaps by index. |
| 2 | Pairwise | O(n) | O(1) | The same sum over adjacent pairs — `zip`/`islice`, `IntStream`. |
| 3 | Rolling previous | O(n) | O(1) | Carry the last character's value instead of re-reading it. |

NeetCode publishes **one** approach for this problem, and approach 1 is it.
That is the correct amount of material: the problem is four lines and there is
no second algorithm to find. Approaches 2 and 3 are not rivals — they are the
same sum spelled differently, and they are here because writing them out is what
makes the cost of a character lookup visible.

**Approach 1 is the one to write.** The rest of this page is about why an
exercise this small is worth a page at all.

## The `abs` is the only reason this needs a loop

Take the absolute value away and the sum is a telescoping series:

```
(s₀ − s₁) + (s₁ − s₂) + (s₂ − s₃) + … + (sₙ₋₂ − sₙ₋₁)
```

Every interior term appears twice with opposite signs and cancels. What survives
is `s₀ − sₙ₋₁` — the two endpoints. The middle of the string does not matter,
there is nothing to loop over, and the whole problem is O(1):

```python
signed_score = ord(s[0]) - ord(s[-1])     # no loop, any length
```

Verified on all 9,837 strings over `{a, d, z}` of length 2–8: the signed sum
equals `ord(s[0]) - ord(s[-1])` every time, zero mismatches.

`abs` is what breaks the cancellation, and it breaks it **only where the string
changes direction**. Where the sequence is monotonic the differences all share a
sign, `abs` is a no-op on the total, and the telescoping still happens:

| string | score | `\|first − last\|` | |
|---|---:|---:|---|
| `"abc"` | 2 | 2 | monotonic up |
| `"cba"` | 2 | 2 | monotonic down |
| `"abcdefghijklmnopqrstuvwxyz"` | 25 | 25 | 25 steps of 1, scores exactly like `"az"` |
| `"acb"` | **3** | 1 | one direction change |
| `"aza"` | **50** | 0 | one change, and the endpoints say nothing at all |

Verified: across all 1,388 monotonic strings over `{a, b, c, d}` of length 2–9,
the score is `|first − last|` with zero exceptions.

So the loop is not summing distances. It is **counting direction changes and
charging for them** — the score is the total vertical distance the sequence
travels, and the endpoints only tell you the net displacement. `"aza"` and
`"aaa"` have the same first and last character and scores of 50 and 0.

That is the whole content of the problem, and it is why the constraint sits at
100 characters instead of 10⁵: there is nothing to optimise, only something to
notice.

## Two properties worth knowing, and one worth avoiding

`|a − b|` is symmetric and shift-invariant, which gives the score two properties
that make good self-checks:

- **Reversal cannot change it.** `score(s) == score(s[::-1])`.
- **A uniform shift cannot change it.** Adding the same constant to every
  character preserves every difference.

Both were used as metamorphic checks in the verification below — 20,000 random
strings each, zero violations in either language. Neither needs an oracle, which
is what makes them useful: they test the implementation against itself.

The property worth avoiding is in Java, and it is the one trap this problem
sets. The subtraction is *int* arithmetic — both `char` operands widen before it
happens — so the difference is a signed value in `[-25, 25]` and `Math.abs` does
the obvious thing. Cast it back to `char` first and the sign has nowhere to go:

```java
Math.abs('a' - 'z')          // 25
(char) ('a' - 'z')           // 65511   <- char is unsigned 16-bit
Math.abs((char) ('a' - 'z')) // 65511   <- the trap
```

Python has no equivalent; `ord` returns an `int` and there is nothing to cast.

## Nothing here can overflow, and it is worth knowing the number

The largest gap between two lowercase letters is `'z' - 'a'` = 25, and a
100-character string has 99 gaps, so the score is bounded by **25 × 99 = 2,475**.
Confirmed by DP over (position, last letter): the maximum at n = 100 is exactly
2,475, achieved by alternating `"az"`, and the minimum is 0 for any string of
one repeated letter.

The entire range of possible answers is `[0, 2475]` — which fits in a `short`,
never mind an `int`. Both extremes are in the test set at full length.

## Character lookups, and what "fewer operations" is worth

Approaches 1 and 2 read every interior character twice: once as the right end of
one pair, once as the left end of the next. The character has not changed in
between. Approach 3 keeps it in a variable, which halves the lookups — 100
instead of 198 at the ceiling.

Whether that is *faster* is a different question, and the two languages answer it
differently. Both benchmarks: n = 100, 200,000 calls, best of five.

**Python** (3.9.6), where `ord` is a real function call:

| approach | time | vs. approach 1 |
|---|---:|---|
| 1 — iteration | 4.0s | — |
| 2 — pairwise | 3.4s | 1.18× faster |
| 3 — rolling | **2.7s** | **1.5× faster** |

**Java** (JDK 26), after a 200,000-call warmup so the JIT has settled:

| approach | time | vs. approach 1 |
|---|---:|---|
| 1 — iteration | 10.0ms | — |
| 2 — pairwise | 15.5ms | 1.5× **slower** |
| 3 — rolling | 10.5ms | 0.91×–1.06× across three runs — noise |

Both rewrites reverse sign between the languages. The rolling variant is worth
1.5× in Python and nothing in Java, because `charAt` is a field read the JIT
hoists while `ord` is a call that actually happens. The stream is *slower* in
Java for the mirror-image reason: it replaces a loop that was already compiled,
so its pipeline is pure overhead, whereas Python's `zip` moves the pairing into
C and comes out ahead.

The useful part is not the ratios. It is that **"fewer operations" and "faster"
are different claims**, and the same source change can be worth 1.5×, nothing,
or −1.5× depending only on what a character lookup costs. None of it matters at
n = 100; all three approaches finish in microseconds.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 score-of-a-string/python/ScoreOfAString.py
```

Or from inside the folder:

```bash
cd score-of-a-string/python
python3 ScoreOfAString.py
```

Expected output:

```
PASS  iteration: 17/17 cases
PASS  pairwise: 17/17 cases
PASS  rolling: 17/17 cases
```

## Java

**Requires:** JDK 11+ — for the single-file launcher below, and for
`String.repeat` in the test cases. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java score-of-a-string/java/ScoreOfAString.java
```

### Option 2 — compile, then run

```bash
cd score-of-a-string/java
javac ScoreOfAString.java   # produces ScoreOfAString*.class
java ScoreOfAString         # note: no .class extension
```

Expected output (either option):

```
PASS  iteration: 17/17 cases
PASS  pairwise: 17/17 cases
PASS  rolling: 17/17 cases
```

To clean up the compiled artifacts from option 2 — note the plural, since the
nested `Solution` and `Case` types compile to their own files. The three method
references do not add any: lambdas are linked with `invokedynamic` rather than
compiled to classes on disk, so the count is three files, not six.

```bash
rm score-of-a-string/java/ScoreOfAString*.class
```

## Notes

- **`zip(s, s[1:])` is the O(n)-space spelling, and the trap is silent.** The
  slice copies the tail to produce pairs that are consumed immediately. The
  Python file uses `zip(s, islice(s, 1, None))` instead, which walks the
  original. `itertools.pairwise` says the same thing in one word but landed in
  **3.10**, and this repo is verified on 3.9.6. Java's `IntStream.range`
  materialises nothing, so the obvious spelling is already O(1) there — the same
  approach charges for the naive version in only one of the two languages.
- **Degenerate input is handled by arithmetic, not by a check — in two of
  three.** The constraints promise `n >= 2`. Given less: approaches 1 and 2
  return 0 for both `""` and `"a"`, because `range(len(s) - 1)` is empty and so
  is `range(-1)`, and because `IntStream.range(0, -1)` is empty rather than an
  error. Approach 3 reads `s[0]` before its loop and raises on `""` in both
  languages. That is the only disagreement between the three, and it is over
  input they are never given — so the guard is documented rather than written.
- **The test set includes both extremes at full length.** `"a" * 100` scoring 0
  and `"az" * 50` scoring 2,475 are the endpoints of the entire answer range,
  and the reverse of the second is there to assert what reversal cannot do.
- **Verified beyond the test set, in both languages.** All three approaches were
  checked against an independent oracle on every string over `{a, b, m, z}` of
  length 2–9 (349,520 strings, 1,048,560 checks) and on 20,000 random strings of
  length 2–100 over the full lowercase alphabet — 1,108,560 checks per language,
  zero failures. The random strings additionally checked the two metamorphic
  properties above: reversal and uniform shift each changed the score 0 times.
- **Method naming:** both NeetCode and LeetCode use `scoreOfString`. These files
  suffix by approach so all three can coexist — rename to plain `scoreOfString`
  / `score_of_string` when submitting.
- The Java file is named `ScoreOfAString.java` to match its
  `public class ScoreOfAString`, as `javac` requires.
