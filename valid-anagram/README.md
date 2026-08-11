# Valid Anagram

LeetCode 242 · [NeetCode solution page](https://neetcode.io/solutions/valid-anagram)

Given two strings `s` and `t`, return `true` if `t` is an anagram of `s` — the
same characters in the same quantities, in any order — and `false` otherwise.

**Constraints:** `1 <= s.length, t.length <= 5 * 10^4`, both strings consist of
lowercase English letters.

```
valid-anagram/
├── java/
│   └── ValidAnagram.java
├── python/
│   └── ValidAnagram.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Sorting | O(n log n) | O(n) | Anagrams sort to the same sequence. |
| 2 | Hash map | O(n) | O(k) | Count both strings, compare the two maps. |
| 3 | **Fixed array** | **O(n)** | **O(1)** | One 26-slot array: `+1` for `s`, `-1` for `t`, expect all zeros. |

`k` is the number of *distinct* characters — bounded by 26 under these
constraints, so approach 2 is also O(1) space in practice. It becomes O(n) the
moment the input is arbitrary Unicode.

**Approach 3 is the one to reach for**, but only because the constraints
promise lowercase `a`–`z`. `count[c - 'a']` on anything else is either an
out-of-bounds crash or, for an uppercase letter, a negative index — Python
happily wraps that around to the end of the list and returns a wrong answer
with no error at all. Drop that guarantee and approach 2 is the correct choice.

All of them check the lengths up front. That is not just a fast path — it is
what makes the single-loop counting in approaches 2 and 3 correct. Drop it and
Python's `zip` stops at the shorter string, so `"ab"` vs `"aba"` counts only the
first two characters of each, finds them balanced, and returns `true`. Java is
less forgiving in a useful way: the loop runs to `s.length()`, so a shorter `t`
throws `StringIndexOutOfBoundsException` instead of lying.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 valid-anagram/python/ValidAnagram.py
```

Or from inside the folder:

```bash
cd valid-anagram/python
python3 ValidAnagram.py
```

Expected output:

```
PASS  sorting: 10/10 cases
PASS  hash map: 10/10 cases
PASS  counter: 10/10 cases
PASS  array: 10/10 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java valid-anagram/java/ValidAnagram.java
```

### Option 2 — compile, then run

```bash
cd valid-anagram/java
javac ValidAnagram.java   # produces ValidAnagram.class
java ValidAnagram         # note: no .class extension
```

Expected output (either option):

```
PASS  sorting: 10/10 cases
PASS  hash map: 10/10 cases
PASS  array: 10/10 cases
```

To clean up the compiled artifact from option 2:

```bash
rm valid-anagram/java/ValidAnagram.class
```

## Notes

- **The Python file has a fourth entry, `counter`.** `Counter(s) == Counter(t)`
  is the idiomatic one-liner and it is worth knowing it exists, but it is
  approach 2 with the loop hidden — write the explicit version in an interview,
  since the counting is the part being asked about. Java has no direct
  equivalent, so its file stops at three.
- **Method naming:** both NeetCode and LeetCode use `isAnagram`. These files use
  `isAnagram*` / `is_anagram_*` suffixed by approach so all of them can coexist
  — rename to plain `isAnagram` when submitting.
- **The array approach is the one that breaks quietly.** Java throws
  `ArrayIndexOutOfBoundsException` on an uppercase letter; Python's negative
  index wraps silently and decrements the wrong slot. If the constraints ever
  loosen, switch to the hash map rather than widening the array.
- The Java file is named `ValidAnagram.java` to match its
  `public class ValidAnagram`, as `javac` requires.
- Both files handle two empty strings (returns `true`), even though the
  constraints guarantee at least one character each.
