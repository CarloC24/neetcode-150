# Length of Last Word

LeetCode 58 · [NeetCode solution page](https://neetcode.io/solutions/length-of-last-word)

Given a string `s` of words and spaces, return the length of the **last** word,
where a word is a maximal run of non-space characters.

**Constraints:** `1 <= s.length <= 10^4`, `s` consists only of English letters
and spaces, and there is **at least one word** in `s`.

```
length-of-last-word/
├── java/
│   └── LengthOfLastWord.java
├── python/
│   └── LengthOfLastWord.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Forward scan | O(n) | O(1) | Count each word, reset after every run of spaces. |
| 2 | **Backward scan** | **O(k)** on the tail | **O(1)** | Skip the trailing spaces, count back to the previous one. |
| 3 | Split | O(n) | O(n) | Cut into words, measure the last. |
| 4 | Trim + last index | O(n) | O(n) | Drop trailing spaces, measure from the final space to the end. |

NeetCode publishes three approaches, and its third is **a different algorithm
in each language** — Python splits into a word list, Java trims and searches for
the last space. Both are written here, in both languages, as approaches 3 and 4.

**Approach 2 is the one to write.** Every approach here is O(n) on the
worst-case input and the difference does not show up in that notation at all —
which is the whole point of the problem.

## The answer is in the suffix, so start there

The last word is determined entirely by the tail of the string. Approach 2 is
the only one that acts like it: it reads the trailing spaces and the last word,
and stops. Everything before that is untouched, however much of it there is.

Character reads, measured by instrumenting the index operator:

| input | length | forward | backward | answer |
|---|---:|---:|---:|---:|
| `"hello world"` | 11 | 13 | **7** | 5 |
| `"   fly me   to   the moon  "` | 27 | 38 | **8** | 4 |
| 3,333 two-letter words | 9,998 | 16,662 | **4** | 2 |
| `"a"` + 9,997 spaces + `"bc"` | 10,000 | 10,002 | **4** | 2 |
| 9,999 spaces + `"a"` | 10,000 | 10,002 | **3** | 1 |
| one 10,000-character word | 10,000 | **10,000** | 10,001 | 10,000 |

Approach 2's cost is O(k) in the length of the tail — trailing spaces plus the
last word — and k is independent of n. The last row is its worst case and the
only one it loses: a string with no space in it is all tail, and the extra read
is the one that runs off the front and stops the loop.

The forward scan's number is worse than n, and reliably so. Every space is read
twice, once by the outer `if` and again by the inner `while` that skips the run;
so is the first letter of each word after the first, once to end the inner loop
and once by the outer one that follows. On alternating two-letter words that is
1.67 reads per character.

None of this changes the complexity class, and on a 10,000-character cap none of
it is slow. It is worth seeing anyway, because "both are O(n)" is exactly the
statement that hides it — the two ends of this string are not symmetric, and one
of the two scans is reading an entire input to answer a question about its last
few characters.

## The line that makes the forward scan work

```python
if s[i] == ' ':
    while i < len(s) and s[i] == ' ':
        i += 1
    if i == len(s):     # <- this one
        return length
    length = 0
```

The counter resets after a run of spaces, so trailing spaces would wipe out the
answer that was just computed. The check catches the run that reaches the end of
the string and returns before the reset can happen.

Remove it and the function returns **0 for every input ending in a space** —
29,514 of the 88,562 constraint-satisfying strings over `{a, b, space}` up to
length 10, every one of them returning 0 and no other kind of failure. Which is
to say it fails on `"hello world  "`, the case the problem is built around.

Note the position: after the inner loop, not inside it. The question is not "is
this character a space" but "did the spaces run out the string".

## The `i >= 0` guard, and what Python does without it

```python
while i >= 0 and s[i] != ' ':
```

The guard fires exactly when the last word reaches the front of the string with
no space before it — `"hello"`, or any single-word input. That is 2,046 of the
same 88,562 strings.

Dropping it fails loudly in both languages, and takes a longer route in Python:

- **Java** throws `StringIndexOutOfBoundsException: Index -1 out of bounds` on
  the first read past the front. Nothing subtle.
- **Python** has legal negative indices, so `s[-1]` wraps to the *end* of the
  string and the loop keeps counting. It survives only because it eventually
  walks off the front too — `s[-len(s) - 1]` raises `IndexError`.

Verified: of the 2,046 space-free strings, the unguarded Python version raises
on all 2,046 and returns a wrong answer on none. The wrap makes the failure
later, not quieter.

The interesting inputs are the ones where the wrap lands on a space. A single
word with trailing spaces — `"ab "` — wraps onto its own trailing space, the
loop exits, and the answer is right. The guard is doing nothing there, and it is
doing everything one character earlier. Both `"a"` and `"a "` are in the test
set for this reason.

**The first loop has no guard, in either language, and NeetCode's does not
either.** It is unguarded because the constraints promise a word exists, so the
skip is guaranteed to hit a non-space before it runs out of string. On an
all-spaces input it walks off the front and fails — see the last section.

## NeetCode's approach 3 is two different algorithms

The page gives one "built-in" approach and writes it twice, and the two halves
are not translations of each other:

```python
return len(s.split().pop())               # Python: build every word, take the last
```
```java
s = s.trim();
return s.length() - s.lastIndexOf(" ") - 1;   // Java: find the last space, measure past it
```

Both are here as approaches 3 and 4, in both languages, because the pair is more
informative than either alone. Approach 3 materialises every word to read one of
them. Approach 4 never looks at a word at all — after the trailing spaces are
gone, the last word is whatever follows the final space, and `lastIndexOf`
returning `-1` on a string with no space makes the arithmetic land on the full
length without a branch.

### `split()` is not `split(" ")`, and the difference flips between languages

| expression | result | last element |
|---|---|---|
| Python `"hello world  ".split()` | `['hello', 'world']` | `'world'` ✓ |
| Python `"hello world  ".split(' ')` | `['hello', 'world', '', '']` | `''` ✗ |
| Java `"hello world  ".split(" ")` | `[hello, world]` | `world` ✓ |
| Java `"  hello".split(" ")` | `[, , hello]` | `hello` ✓ |

Python's bare `split()` splits on *runs* of whitespace and drops the empties at
both ends; `split(' ')` does neither, and its trailing empty string reports 0 on
exactly the inputs this problem cares about. Java's `split` drops trailing empty
strings by default, so the naive spelling is safe there — leading empties
survive, but the answer is read off the last element, so they do no harm.
Verified against an oracle on all 88,562 strings, zero mismatches.

Same-looking expression, opposite verdict. Approach 3 is written as
`s.trim().split("\\s+")` in Java because that is what Python's `split()`
actually means, not because `split(" ")` fails.

### Half of `trim()` is dead work

`trim()` strips both ends and only the trailing end matters. The last space is
the last space regardless of what precedes it, so stripping the front changes
nothing: both-end strip against trailing-only strip on all 88,562 strings,
**zero differences**. Drop the *trailing* strip instead and 29,514 break — the
same count as the forward scan's missing check, and for the same reason, since
both are exactly the strings that end in a space.

The Python file uses `rstrip()`, which is the honest spelling. The Java file
keeps `trim()`, because Java has no `rstrip` and the wasted work is bounded by
the leading spaces.

Two footnotes on those calls:

- **The O(n) space is conditional.** `rstrip()` returns the original object when
  there is nothing to strip, and `trim()` returns `this`; both verified by
  identity comparison. Only inputs with trailing spaces pay for a copy.
- **`trim()` and `rstrip()` do not agree on what a space is.** `trim()` cuts
  every character `<= U+0020`, so tabs and newlines go and a non-breaking space
  stays. Python's `rstrip()` takes Unicode whitespace and removes the
  non-breaking space too. Invisible here, where the alphabet is letters and
  spaces.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 length-of-last-word/python/LengthOfLastWord.py
```

Or from inside the folder:

```bash
cd length-of-last-word/python
python3 LengthOfLastWord.py
```

Expected output:

```
PASS  forward: 18/18 cases
PASS  backward: 18/18 cases
PASS  split: 18/18 cases
PASS  trim+index: 18/18 cases
```

## Java

**Requires:** JDK 11+ — for the single-file launcher below, and for
`String.repeat` in the test cases. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java length-of-last-word/java/LengthOfLastWord.java
```

### Option 2 — compile, then run

```bash
cd length-of-last-word/java
javac LengthOfLastWord.java   # produces LengthOfLastWord*.class
java LengthOfLastWord         # note: no .class extension
```

Expected output (either option):

```
PASS  forward: 18/18 cases
PASS  backward: 18/18 cases
PASS  split: 18/18 cases
PASS  trim+index: 18/18 cases
```

To clean up the compiled artifacts from option 2 — note the plural, since the
nested `Solution` and `Case` types compile to their own files:

```bash
rm length-of-last-word/java/LengthOfLastWord*.class
```

## Notes

- **The all-spaces input is outside the constraints, and three of the four
  approaches do not survive it.** LeetCode guarantees at least one word. Given
  `"   "` anyway: approach 1 returns 0, approach 2 walks off the front
  (`IndexError` / `StringIndexOutOfBoundsException`), approach 3 raises in
  Python (`pop from empty list`) but returns 0 in Java (`"".split("\\s+")` is a
  one-element array holding the empty string, not an empty array), and approach
  4 returns 0 in both. It is **not** in the test set — unlike the empty array in
  [Binary Search](../binary-search/), which every approach there survived. The
  approaches that fail here are NeetCode's own, and hardening them would mean
  publishing something other than what the page publishes.
- **`.pop()` versus `[-1]`.** `len(s.split().pop())` mutates a list that no one
  else can see, since the same expression created it. It reads as a side effect
  and is not one; `[-1]` says the same thing without the mutation.
- **Nothing here mutates its input.** Strings are immutable in both languages,
  so the harness shares one string across all four approaches per case.
- **Verified beyond the test set, in both languages.** All four approaches were
  checked against an independent oracle on every string over `{a, b, space}` of
  length 1–10 that satisfies the constraints (88,562 strings), and on 20,000
  random strings of length 1–200 at five space densities from 5% to 95%
  (78,004 Python checks, 77,948 Java checks — the counts differ because the two
  generators reject a different number of all-space strings). Zero failures.
- **Method naming:** both NeetCode and LeetCode use `lengthOfLastWord`. These
  files suffix by approach so all four can coexist — rename to plain
  `lengthOfLastWord` / `length_of_last_word` when submitting.
- The Java file is named `LengthOfLastWord.java` to match its
  `public class LengthOfLastWord`, as `javac` requires.
