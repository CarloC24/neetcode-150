# Valid Palindrome

LeetCode 125 · [NeetCode solution page](https://neetcode.io/solutions/valid-palindrome)

Given a string `s`, return `true` if it reads the same forwards and backwards
once every non-alphanumeric character is dropped and case is ignored.

**Constraints:** `1 <= s.length <= 2 * 10^5`, and `s` consists only of printable
ASCII characters.

```
valid-palindrome/
├── java/
│   └── ValidPalindrome.java
├── python/
│   └── ValidPalindrome.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Reverse string | O(n) | O(n) | Filter to alphanumeric lowercase, compare against the reverse. |
| 2 | Reverse string (built-in) | O(n) | O(n) | The same idea, handed to the standard library. |
| 3 | **Two pointers** | **O(n)** | **O(1)** | Close in from both ends, skipping anything non-alphanumeric. |

**Approach 3 is the one to reach for.** All three are linear in time; the
difference is the cleaned copy. Approaches 1 and 2 allocate a second string as
large as the input before they compare anything, which at the constraint ceiling
of 2×10⁵ characters is real memory for no benefit. The two-pointer walk never
allocates.

The skipping does not cost anything asymptotically. It looks like a loop inside
a loop, but the two pointers only ever move toward each other, so across the
whole run each index is visited at most once by one pointer or the other — the
work is O(n) total, not O(n) per step.

Both inner loops re-test `left < right`. That is not defensive padding: without
it, a string of two or more characters with no alphanumerics at all — `".,"` —
walks the left pointer clean off the end while it searches for a character to
compare. Python raises `IndexError`, Java throws
`StringIndexOutOfBoundsException`. Both files carry `".,"` as a test case.

## The `0P` trap

The classic wrong answer here is hand-rolled case folding. Two ASCII letters of
opposite case sit exactly 32 apart — `'A'` is 65, `'a'` is 97 — which invites a
comparison like this:

```python
if abs(ord(c1) - ord(c2)) != 32 and c1 != c2:   # wrong
    return False
```

`'0'` is 48 and `'P'` is 80. They are also exactly 32 apart, so `"0P"` reports
as a palindrome and the submission fails. `'0'`/`'p'`, `'1'`/`'Q'`, `'2'`/`'R'`
and the rest of the digits do the same thing.

The fix is to fold case with `lower()` / `Character.toLowerCase` and compare the
results, which is what all three approaches here do. The other common trick,
OR-ing in bit 32 (`c | 32`), happens to survive this case — `'0' | 32` is still
`'0'` — but it is only correct because digits already have that bit set, and it
silently mangles punctuation. Use the library function.

`"0P"` is in the test set of both files for exactly this reason.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 valid-palindrome/python/ValidPalindrome.py
```

Or from inside the folder:

```bash
cd valid-palindrome/python
python3 ValidPalindrome.py
```

Expected output:

```
PASS  reverse: 14/14 cases
PASS  reverse (built-in): 14/14 cases
PASS  two pointers: 14/14 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java valid-palindrome/java/ValidPalindrome.java
```

### Option 2 — compile, then run

```bash
cd valid-palindrome/java
javac ValidPalindrome.java   # produces ValidPalindrome.class
java ValidPalindrome         # note: no .class extension
```

Expected output (either option):

```
PASS  reverse: 14/14 cases
PASS  reverse (built-in): 14/14 cases
PASS  two pointers: 14/14 cases
```

To clean up the compiled artifact from option 2:

```bash
rm valid-palindrome/java/ValidPalindrome.class
```

## Notes

- **The alphanumeric test is three ranges, not one.** `'A' <= c <= 'z'` looks
  like a shortcut, but the gap between `'Z'` (90) and `'a'` (97) holds
  ``[ \ ] ^ _ ` `` — six punctuation marks that would be treated as letters.
  Both files spell out `A-Z`, `a-z` and `0-9` separately.
- **`isalnum` / `isLetterOrDigit` answer a wider question.** Both are
  Unicode-aware: `'é'.isalnum()` and `'²'.isalnum()` are `True`. Under these
  constraints — printable ASCII only — that is identical to the manual range
  check, which is why approach 2 can use them. Approach 2 in Java uses the
  regex `[^A-Za-z0-9]` instead, keeping it explicitly ASCII-scoped to match.
- **`StringBuilder.reverse()` mutates in place.** NeetCode's Java approach 1
  writes `newStr.toString().equals(newStr.reverse().toString())`, which works
  only because Java evaluates the receiver before the argument — the forward
  snapshot is taken before `reverse()` runs. Swap the two sides of that `equals`
  and it compares the reversed builder against itself, returning `true` for
  every input. This file assigns the forward string to a local first, so the
  ordering is not load-bearing.
- **`String.toLowerCase()` is locale-sensitive; `Character.toLowerCase(char)`
  is not.** Under a Turkish locale the string version lowercases `'I'` to
  dotless `'ı'` instead of `'i'`, so any input pairing the two cases of that
  letter — `"Ill i"` is in the test set — returns `false` on a tr-TR machine and
  nowhere else. Approach 2 in Java passes `Locale.ROOT` for this reason; the
  character version used elsewhere needs no such guard. Note that a mangled
  character sitting on the exact midpoint still matches itself, so the bug hides
  on inputs like `"Madam, I'm Adam"` and only surfaces when the `I` has a
  partner.
- **Approach 1 in Python builds a list, not a string.** NeetCode's version does
  `newStr += c` in a loop, which is O(n²) in the general case since strings are
  immutable and each `+=` copies what came before. CPython has an in-place
  resize that usually hides it, but it only fires when the string has exactly
  one reference and is an implementation detail rather than a promise.
  `list.append` plus a slice comparison is O(n) by construction.
- **Method naming:** both NeetCode and LeetCode use `isPalindrome`. These files
  use `isPalindrome*` / `is_palindrome_*` suffixed by approach so all of them
  can coexist — rename to plain `isPalindrome` when submitting.
- The Java file is named `ValidPalindrome.java` to match its
  `public class ValidPalindrome`, as `javac` requires.
- Both files handle the empty string (returns `true`), even though the
  constraints guarantee at least one character.
