# Valid Parentheses

LeetCode 20 · [NeetCode solution page](https://neetcode.io/solutions/valid-parentheses)

Given a string `s` made only of the characters `(`, `)`, `[`, `]`, `{` and `}`,
return `true` if every closing bracket matches the most recent unclosed opening
bracket of the same type, and nothing is left open at the end.

**Constraints:** `1 <= s.length <= 10^4`, and `s` consists only of those six
characters.

```
valid-parentheses/
├── java/
│   └── ValidParentheses.java
├── python/
│   └── ValidParentheses.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Brute force | O(n²) | O(n) | Delete adjacent matching pairs until none are left; valid if nothing remains. |
| 2 | Stack | O(n) | O(n) | Push openers, pop when a closer matches the top. |
| 3 | **Stack (expected closer)** | **O(n)** | **O(n)** | Push the bracket you expect *next*, then compare closers directly. |

**Approach 3 is the one to reach for**, though the honest version of that
sentence is that approaches 2 and 3 are the same algorithm and the choice
between them is taste. Both push one entry per unclosed opener and both are
O(n) either way; 3 just moves the translation to the push side, so a closing
bracket compares against the stack top directly instead of mapping itself back
to an opener first. That is one map lookup per character rather than one per
character plus one per closer, and the comparison reads as the question being
asked — *is this the bracket I am waiting for?*

Approach 1 is the outlier, and the interesting thing about it here is that it
**passes**. Its worst case is `"("×n/2 + ")"×n/2`, which deletes exactly one
pair per pass: at NeetCode's ceiling of 10³ that is about 2 ms, and at
LeetCode's 10⁴ about 164 ms (Python 3.9, this machine). Slow, but inside the
time limit — the O(n²) is running as a C-level `memmove` per pass rather than a
Python loop. That makes this a rare problem where the brute force is a real
answer and not just a strawman; compare Best Time to Buy and Sell Stock next
door, where the quadratic solution is a hard timeout.

It is still the wrong one to write. The same worst case takes 1.3 ms through
the stack — 130× faster — and the gap widens with n, because one is linear and
the other is not.

## Why a counter is not enough

The instinct is to count: `(` adds one, `)` subtracts one, reject if the count
ever goes negative or ends non-zero. For a single bracket type that is exactly
right. Extend it to three counters, one per type, and it breaks:

```
s = "([)]"

open ( = 1, closed ( = 1    ✓ balanced
open [ = 1, closed [ = 1    ✓ balanced
count never goes negative   ✓
                            → reports valid; the answer is invalid
```

Every bracket has a partner of the right type and the counts are perfect. What
is wrong is the *order*: the `)` arrives while `[` is still open, so it closes
across another pair rather than inside it. Counters cannot see this because
they throw away the one thing that matters — which opener is the most recent
one. That is what the stack stores, and it is the entire problem. `"([)]"` is
in the test set of both files.

`"))(("` is the same lesson from the other direction: three counters say
balanced, and it is invalid because every closer arrives before its opener.
Both files carry it too.

## The three ways to be invalid

The stack approaches have three exit points, and each one is a different way
for a string to fail. Worth being able to name them, because dropping any one
of them is a wrong answer that passes most tests:

| Failure | Example | Where it is caught |
|---|---|---|
| Wrong type | `"(]"` | The comparison against the stack top |
| Closer with nothing open | `")"` | The empty-stack check on a closing bracket |
| Opener never closed | `"("` | The final `stack.isEmpty()` / `not stack` |

The third is the one people forget. Return `true` at the end of the loop
instead of returning whether the stack is empty and every prefix of a valid
string starts passing — `"((("` comes back valid.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 valid-parentheses/python/ValidParentheses.py
```

Or from inside the folder:

```bash
cd valid-parentheses/python
python3 ValidParentheses.py
```

Expected output:

```
PASS  brute force: 14/14 cases
PASS  stack: 14/14 cases
PASS  stack (expected closer): 14/14 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below, and for `String.repeat`
in the test set. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java valid-parentheses/java/ValidParentheses.java
```

### Option 2 — compile, then run

```bash
cd valid-parentheses/java
javac ValidParentheses.java   # produces ValidParentheses.class
java ValidParentheses         # note: no .class extension
```

Expected output (either option):

```
PASS  brute force: 14/14 cases
PASS  stack: 14/14 cases
PASS  stack (expected closer): 14/14 cases
```

To clean up the compiled artifact from option 2:

```bash
rm valid-parentheses/java/ValidParentheses.class
```

## Notes

- **`replaceAll` would hang the brute force.** Java's
  `String.replace(CharSequence, CharSequence)` is a literal replacement, which
  is what approach 1 uses. `replaceAll` takes a *regex*, and all six of these
  characters are regex metacharacters. `s.replaceAll("[]", "")` throws
  `PatternSyntaxException` for an unclosed character class, and
  `s.replaceAll("()", "")` is worse: an empty capturing group matches the empty
  string at every position, so it deletes nothing, while the literal
  `contains("()")` in the loop condition keeps reporting `true` — an infinite
  loop rather than a wrong answer. Python has no equivalent trap, since
  `str.replace` is always literal and the regex version is a different function
  (`re.sub`).
- **NeetCode's Java compares boxed `Character`s with `==`.** Both
  `stack.peek()` and `closeToOpen.get(c)` return `Character`, so
  `stack.peek() == closeToOpen.get(c)` is a reference comparison. It works, but
  only because `Character.valueOf` caches every value up to 127 and all six
  brackets are well below that — so both sides are the same interned object.
  The identical line over characters above the cache returns `false` for equal
  characters (verified: two boxed `'Ā'` are `==`-unequal and `equals`-equal).
  These files call `charValue()` to unbox before comparing.
- **`ArrayDeque` over `Stack`.** `java.util.Stack` extends `Vector`, so every
  method is synchronized for a lock nobody here contends, and it iterates
  bottom-to-top — the opposite of the order it pops in, which makes printing one
  during debugging quietly misleading (`[a, b, c]` for a stack that pops `c`).
  The `Deque` javadoc recommends `ArrayDeque` over it outright. Approach 2 uses
  `Stack` to match NeetCode; approach 3 uses `ArrayDeque`.
- **`return True if not stack else False`** is NeetCode's Python ending.
  `not stack` is already the boolean being asked for.
- **A Python list is the right stack.** `append` and `pop` from the end are
  amortized O(1). `collections.deque` buys nothing here — it only wins when you
  need the *front*, and `pop(0)` on a list is the O(n) mistake it prevents.
- **Odd-length strings are invalid before you read them.** `if len(s) % 2:
  return False` is a legitimate early exit — every valid string pairs off — and
  it is a nice thing to say out loud in an interview. It is left out of these
  files because it does not change the asymptotics and the loop already handles
  those strings correctly; `"{[}"` is in the test set as one.
- **Space is O(n), not O(1).** Worth stating plainly since two of the three
  neighbouring problems in this repo optimize down to constant space:
  `"(((((((((("` stacks the entire input, and there is no way around it — you
  genuinely have to remember every unclosed opener.
- **The empty-stack check must come before the pop.** Both files rely on
  short-circuit evaluation (`not stack or stack.pop() != char`) so `pop` only
  runs when there is something to pop. Reverse the operands and `")"` raises
  `IndexError` in Python and `NoSuchElementException` in Java.
- **Verified against a grammar.** All three approaches were checked against an
  oracle built by enumerating the language `S → ε | (S) | [S] | {S} | SS`
  directly: every one of the 2,015,539 strings of length ≤ 8 over the six
  characters, plus 20,000 random strings up to length 60 cross-checked between
  the three. Zero mismatches.
- **Method naming:** both NeetCode and LeetCode use `isValid`. These files use
  `isValid*` / `is_valid_*` suffixed by approach so all three can coexist —
  rename to plain `isValid` when submitting.
- The Java file is named `ValidParentheses.java` to match its
  `public class ValidParentheses`, as `javac` requires.
- Both files handle the empty string (returns `true`), even though the
  constraints guarantee at least one character.
- **The constraints above are LeetCode 20's.** NeetCode restates this problem
  with a smaller bound of `1 <= s.length <= 1000`. The algorithms are unaffected
  either way; the difference only shows up in how slow approach 1 is allowed to
  be, which is why both numbers are quoted above.
