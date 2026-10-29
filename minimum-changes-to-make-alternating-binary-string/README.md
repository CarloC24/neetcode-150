# Minimum Changes To Make Alternating Binary String

LeetCode 1758 · [NeetCode solution page](https://neetcode.io/solutions/minimum-changes-to-make-alternating-binary-string)

You're given a string `s` of `'0'`s and `'1'`s. One operation flips a single
character. Return the fewest operations that make `s` **alternating**: no two
adjacent characters are equal.

`"0100"` gives `1`: flip the last character to get `"0101"`.
`"10"` gives `0`: it's already alternating.
`"1111"` gives `2`: `"0101"` or `"1010"`.

**Constraints:** `1 <= s.length <= 10^4`, `s[i]` is `'0'` or `'1'`.

```
minimum-changes-to-make-alternating-binary-string/
├── java/
│   └── MinimumChangesToMakeAlternatingBinaryString.java
├── python/
│   └── MinimumChangesToMakeAlternatingBinaryString.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Two patterns | O(n) | O(1) | Count mismatches against `"0101..."` and `"1010..."`, take the smaller. |
| 2 | **Complement** | **O(n)** | **O(1)** | Count mismatches against one pattern; the other count is `n` minus it. |

Both are NeetCode's.

**Approach 2 is the one to reach for.** It's one pass instead of two, and it
rests on a fact worth knowing: the two counts always add up to `n`.

## There are only two possible answers

An alternating string is completely fixed by its first character. Once `s[0]`
is chosen, every later character has to be the opposite of the one before. So
for any length there are exactly two alternating strings:

```
n = 5:   0 1 0 1 0
         1 0 1 0 1
```

That turns "find the cheapest way to make it alternating" into "which of these
two targets is closer?" Each position where `s` differs from a target costs one
flip, and positions are independent, so the cost of a target is just its number
of mismatches.

## Why the counts add up to n

The two targets differ at **every** index. So each character of `s` matches
exactly one of them and mismatches the other:

```
s:            0 0 1 0 1
"10101":      x . . . .     1 mismatch
"01010":      . x x x x     4 mismatches
                            1 + 4 = 5 = n
```

Counting one target gives the other for free: `min(count, n - count)`.

## Why fixing clashes as you go fails

The natural greedy approach is: scan left to right, and whenever `s[i]` equals
the character before it, flip `s[i]`. That always produces an alternating
string, but not always the cheapest one:

```
s:        0 0 1 0 1
greedy:   0 1 0 1 0     4 flips (indexes 1-4)
best:     1 0 1 0 1     1 flip  (index 0)
```

Greedy never touches `s[0]`, so it always ends up at the target that starts
with `s[0]`. Here the right move is to flip the first character. `"00101"` is
in the test set to catch it.

## Python

**Requires:** Python 3.6+ (f-strings, `10_000`-style literals). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 minimum-changes-to-make-alternating-binary-string/python/MinimumChangesToMakeAlternatingBinaryString.py
```

Or from inside the folder:

```bash
cd minimum-changes-to-make-alternating-binary-string/python
python3 MinimumChangesToMakeAlternatingBinaryString.py
```

Expected output:

```
PASS  two patterns: 15/15 cases
PASS  complement: 15/15 cases
```

## Java

**Requires:** JDK 11+ (the single-file launcher, and `String.repeat` in the
tests). Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java minimum-changes-to-make-alternating-binary-string/java/MinimumChangesToMakeAlternatingBinaryString.java
```

### Option 2 — compile, then run

```bash
cd minimum-changes-to-make-alternating-binary-string/java
javac MinimumChangesToMakeAlternatingBinaryString.java   # produces MinimumChangesToMakeAlternatingBinaryString*.class
java MinimumChangesToMakeAlternatingBinaryString         # note: no .class extension
```

Expected output (either option):

```
PASS  two patterns: 15/15 cases
PASS  complement: 15/15 cases
```

To clean up the compiled artifacts from option 2:

```bash
rm minimum-changes-to-make-alternating-binary-string/java/MinimumChangesToMakeAlternatingBinaryString*.class
```

## Notes

- **A shorter mismatch test.** `"0101..."` has `'0'` at even indices and `'1'`
  at odd ones, so position `i` mismatches it exactly when `int(s[i]) != i % 2`.
  That's approach 2's if/else in one comparison, counted against the other
  pattern. The answer is the same, because the `min` covers both.
- **Odd lengths aren't symmetric.** For `"111"`, `"101"` needs one flip and
  `"010"` needs two. The pattern that starts with the majority character can
  hold one extra of it. `"1" * 9999` gives 4,999, not 5,000.
- **Java uses `charAt` instead of `toCharArray`.** NeetCode's approach 1 calls
  `toCharArray()` in both loops, which copies the string twice. `charAt` reads
  it in place, so the space really is O(1).
- **No input-unchanged check.** Strings are immutable in both languages, so
  none of the approaches can modify `s`.
- **Verified exhaustively, in both languages:** every binary string of length
  1–16 (131,070 strings, 262,140 checks per language). The reference doesn't
  assume there are two targets: it lists every alternating string of each
  length by brute force and takes the closest one. Zero failures in either
  language.
- **Method naming:** LeetCode uses `minOperations`. These files use
  `minOperations*` / `min_operations_*` with an approach suffix, so both can
  coexist. Rename to plain `minOperations` when submitting.
- The Java file is named `MinimumChangesToMakeAlternatingBinaryString.java` to
  match its `public class MinimumChangesToMakeAlternatingBinaryString`, as
  `javac` requires.
