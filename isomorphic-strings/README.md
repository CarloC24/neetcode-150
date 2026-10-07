# Isomorphic Strings

LeetCode 205 · [NeetCode solution page](https://neetcode.io/solutions/isomorphic-strings)

Given two strings `s` and `t` of the same length, return `true` if they are
**isomorphic**: if you can replace characters in `s` to get `t`.

- Every occurrence of a character must be replaced by the same character.
- No two characters may map to the same character.
- A character may map to itself.

`"egg"` and `"add"` are isomorphic (`e→a`, `g→d`). `"foo"` and `"bar"` are not,
because `o` would have to map to both `a` and `r`.

**Constraints:** `0 <= s.length == t.length <= 5 * 10^4`, and both strings
consist of any valid ASCII character.

```
isomorphic-strings/
├── java/
│   └── IsomorphicStrings.java
├── python/
│   └── IsomorphicStrings.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

`n` is the string length; `k` is the number of distinct characters (at most
128 for ASCII).

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Brute force | O(n²) | O(1) | For every pair of positions, `s` repeats there iff `t` does. |
| 2 | Hash map, two pass | O(n) | O(k) | Check `s → t` is consistent, then `t → s`. |
| 3 | Hash map, one pass | O(n) | O(k) | Both maps at once; stop at the first conflict either way. |
| 4 | **Last seen** | **O(n)** | **O(1)** | Two 128-slot arrays of where each character last appeared. |
| 5 | Set counts | O(n) | O(k) | `s`, `t` and their pairs have the same number of distinct members. |

Approaches 2 and 3 are NeetCode's. Approach 1 is the definition checked
directly, approach 4 is the standard fixed-array solution, and approach 5 is
the Python one-liner.

**Approach 4 is the one to reach for.** It's the same single pass as
approach 3, but the two hash maps become two fixed `int[128]` arrays, and the two
checks become one comparison. Because the arrays don't grow with the input, the
space is truly O(1). Approaches 2, 3 and 5 are O(k) space, which is also bounded
by 128 here, so the practical difference is constant factors: no hashing, no
boxing in Java.

## One direction is not enough

The mistake this problem is built around: checking only that `s → t` is
consistent.

```
s = "ab"
t = "aa"
```

Walking `s → t`: `a` maps to `a`, then `b` maps to `a`. Neither character is
ever asked to map to two different things, so a one-way check returns `true`.
But `a` and `b` both map to `a`, which the problem forbids. The conflict only
shows up going the other way: `t → s` asks `a` to map to both `a` and `b`.

So the mapping must be a **bijection**, which means checking both directions.
Approach 2 does this with a second pass. Approach 3 keeps a second map, and
approach 4 compares two arrays. `("ab", "aa")` and `("badc", "baba")` are in the
test set because each fails only the reverse check.

## Why "last seen" works

Approach 4 never builds a mapping at all. For each character it records the
position (plus one) where that character last appeared, in one array per
string. At position `i`, `s[i]` and `t[i]` are consistent exactly when both were
last seen at the same position:

- **Both new:** both arrays hold `0`, so they match. The pair is new on both
  sides.
- **Both seen before, together:** both hold the same position, the last time
  they appeared side by side.
- **One new, one not:** one side holds `0` and the other a position. That's a
  character trying to take a second partner, in either direction.
- **Both seen, but apart:** two different positions, so at least one of them
  was last paired with something else.

One comparison covers both directions. The `+1` matters: it keeps `0` free to
mean "never seen". Store plain `i` and a character first seen at position 0 is
indistinguishable from one never seen at all.

## The brute force is the definition

Approach 1 checks, for every pair of positions `i, j`, that `s[i] == s[j]`
exactly when `t[i] == t[j]`. In other words, both strings **repeat in the same
places**. That's the cleanest statement of what isomorphic means, and it needs
no mapping. It's O(n²): about 1.25 × 10⁹ comparisons at the 5 × 10⁴ ceiling, far
too slow to submit. That's why the test set stays at 300 characters.

The same idea gives a quick hand check. Replace each character by the index of
its first appearance: `"paper"` becomes `[0, 1, 0, 3, 4]`, and so does
`"title"`. Two strings are isomorphic exactly when those lists are equal. The
fuzz test below uses that as its independent oracle.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 isomorphic-strings/python/IsomorphicStrings.py
```

Or from inside the folder:

```bash
cd isomorphic-strings/python
python3 IsomorphicStrings.py
```

Expected output:

```
PASS  brute force: 14/14 cases
PASS  hash map (two pass): 14/14 cases
PASS  hash map (one pass): 14/14 cases
PASS  last seen: 14/14 cases
PASS  set counts: 14/14 cases
```

## Java

**Requires:** JDK 11+ (`String.repeat`, single-file launcher). Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java isomorphic-strings/java/IsomorphicStrings.java
```

### Option 2 — compile, then run

```bash
cd isomorphic-strings/java
javac IsomorphicStrings.java   # produces IsomorphicStrings*.class
java IsomorphicStrings         # note: no .class extension
```

Expected output (either option):

```
PASS  brute force: 14/14 cases
PASS  hash map (two pass): 14/14 cases
PASS  hash map (one pass): 14/14 cases
PASS  last seen: 14/14 cases
PASS  set counts: 14/14 cases
```

To clean up the compiled artifacts from option 2:

```bash
rm isomorphic-strings/java/IsomorphicStrings*.class
```

## Notes

- **In Java, the ASCII constraint hides a boxing bug.** A
  `Map<Character, Character>` stores boxed `Character` objects. Comparing a
  `Character` with a `char` (`mapped != b`) unboxes and compares values, which
  is what these files and NeetCode's do. Comparing two `Character` objects with
  `!=` compares **references**. That still happens to work here, because
  `Character.valueOf` caches every value from 0 to 127, so equal ASCII
  characters are always the same object. Outside that range it breaks. Measured
  on JDK 26: two boxed `'a'` are `==`, two boxed `(char) 200` are not. Code that
  passes every LeetCode test would fail on `"é"`. Use `.equals`, or keep one
  side a primitive `char`.
- **The set-count idiom can't stop early.** `len(set(s)) == len(set(t)) ==
  len(set(zip(s, t)))` is correct and short, but it builds three sets over the
  whole input even when the conflict is at position 1. Approaches 3 and 4 return
  at the first conflict. It's fine to mention in an interview; write approach 4.
- **In Java, approach 5 packs each pair into one int** as `a * 128 + b`.
  That's unique because both characters are below 128. It saves allocating a
  pair object for every position.
- **The test set covers non-letters.** The constraints say "any valid ASCII
  character", so `("13", "42")` and `("a b", "x!y")` check that digits, spaces
  and punctuation are treated as ordinary characters. `("", "")` covers the
  allowed length 0.
- **Verified beyond the test set, in both languages:** 20,000 random string
  pairs of length 0–10 over small alphabets that include a space and a digit.
  Every approach was checked against the first-index oracle above, with zero
  failures in either language.
- **Method naming:** LeetCode uses `isIsomorphic`. These files use
  `isIsomorphic*` / `is_isomorphic_*` with an approach suffix, so all five can
  coexist. Rename to plain `isIsomorphic` when submitting.
- The Java file is named `IsomorphicStrings.java` to match its
  `public class IsomorphicStrings`, as `javac` requires.
