# Is Subsequence

LeetCode 392 · [NeetCode solution page](https://neetcode.io/solutions/is-subsequence)

Given two strings `s` and `t`, return `true` if `s` is a **subsequence** of `t`:
every character of `s` appears in `t` in the same order, though not necessarily
side by side. `"node"` is a subsequence of `"neetcode"`; `"axc"` is not a
subsequence of `"ahbgdc"`.

**Constraints:** `0 <= s.length <= 100`, `0 <= t.length <= 10^4`, and both
strings consist only of lowercase English letters.

**Follow-up:** if there are many incoming `s` (say 10⁹ of them) to check
against one `t`, how would you change the approach?

```
is-subsequence/
├── java/
│   └── IsSubsequence.java
├── python/
│   └── IsSubsequence.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

`n = len(s)`, `m = len(t)`.

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Recursion | O(m) | O(m) stack | Match `s[i]` against `t[j]`; always advance `j`. |
| 2 | Recursion + memo | O(m) | O(n·m) | The same, plus a table that is never read back. |
| 3 | Bottom-up DP | O(n·m) | O(n·m) | `dp[i][j]`: is `s[i:]` a subsequence of `t[j:]`? |
| 4 | **Two pointers** | **O(n+m)** | **O(1)** | Walk `t` once, ticking off `s` as it matches. |
| 5 | Next-index table *(follow-up)* | O(26·m) build, O(n) per query | O(26·m) | For every position, where each letter next appears. |
| 6 | Binary search *(follow-up)* | O(m) build, O(n log m) per query | O(m) | Each letter's positions in `t`, binary-searched. |

Approaches 1–5 are NeetCode's. Approach 6 isn't on NeetCode's page, but it's
the standard answer to the follow-up, so it's included here.

**Approach 4 is the one to reach for** on a single query. It's one pass with
two indices and no allocation. It's also an unusual use of two pointers:
in [Valid Palindrome](../valid-palindrome/) both pointers walk *one* sequence
inward from both ends. Here each pointer walks its own sequence in the same
direction. `j` moves every step and `i` moves only on a match.

**Why the greedy match is safe.** When `s[i]` matches the first `t[j]` it
can, it never spoils a later match. Taking the earliest copy of a letter leaves
the most of `t` for the letters after it. If an answer exists that uses a later
copy of `s[i]`, you can swap that copy for the earlier one and the answer still
works. So once the scan runs out of `t`, no other choice of matches could have
succeeded.

## NeetCode's complexities for the recursions are wrong

NeetCode lists approach 1 as O(n·m) time and O(n) space, and approach 2 as
O(n·m) for both. Neither recursion ever branches. Every call either returns
or makes **exactly one** recursive call, and every call advances `j` by one. So
there are at most `m + 1` calls in a single chain:

- **Time is O(m)**, not O(n·m). `i` never moves without `j` also moving.
- **Stack depth is O(m)**, not O(n). The chain runs as long as `t`, not `s`.

The memo in approach 2 never gets a hit. A cache only pays off when the same
`(i, j)` is reached twice, which needs a call that branches into two paths that
meet again. This recursion has one path. All the memo adds is an n·m table,
up to 10⁶ cells, allocated and filled with `-1` before the first comparison.
Approach 2 is strictly worse than approach 1. It's worth writing once to see
that **memoising a recursion that doesn't branch buys nothing.**

Approach 3 really does fill every cell, so it's the only one that actually costs
O(n·m). The table makes sense for Longest Common Subsequence, where a mismatch
branches into "skip from `s`" or "skip from `t`". Is Subsequence never skips
from `s`, which is why it collapses to a scan.

## The recursions overflow at the constraint ceiling, in both languages

The recursion depth is `m + 1`, and `t` can be 10⁴ characters.

**Python** fails deterministically. The default recursion limit is 1000, so
any `t` of about 1,000 characters or more raises `RecursionError`. Called from
module level, the first failing length is 997, and it drops further when the
call starts deeper in the stack. Approaches 1
and 2 can't run at LeetCode's bounds at all. This is the same
problem as [Reverse Linked List](../reverse-linked-list/), but worse, since the
bound here is 10× the limit rather than 5×.

**Java** fails too, which was the surprise. Measured on JDK 26 with the default
stack, on a cold JVM:

| Depth | Survives |
|---|---|
| 6,000 | 5 / 5 runs |
| 7,000 | 0 / 5 runs |
| 10,000 (the ceiling) | 0 / 5 runs |

Once the JIT has compiled `match`, its frames shrink, and the ceiling rises to
roughly **12,500–13,300**. That's enough for 10⁴. So in Java, whether the
recursion survives the constraint ceiling depends on how warm the JVM is, which
is the worst kind of failure to debug. Reverse Linked List measured about
39,000 frames before overflowing. This `match` takes four arguments (two
`String` references and two `int`s) where that one took one, so each frame is
larger and fewer fit.

The test harness reflects all of this:

- The shared cases top out at `|t| = 600`, which every approach handles in both
  languages.
- A separate **ceiling check** runs every approach on `|s| = 100`,
  `|t| = 10⁴`.
  - In Python, the recursions are *expected* to raise `RecursionError`. If one
    succeeds, the check prints `FAIL`, because the README above would be out of
    date.
  - In Java, the recursions print a `NOTE` line with whatever happened, since the
    outcome isn't deterministic. The non-recursive approaches must `PASS` in
    both languages.

## The follow-up: many `s`, one `t`

Two pointers costs O(m) per query because it rescans `t` every time. With 10⁹
queries against a 10⁴-character `t`, that's 10¹³ steps. The fix is to index
`t` once, so that each query only costs work proportional to `s`.

**Approach 5, the next-index table.** `next_at[j][c]` is the first index `>= j`
where letter `c` appears. Build it right to left: row `j` is a copy of row `j+1`
with `t[j]`'s own slot set to `j`. A query then jumps straight from match to
match in O(1) per character, so it costs **O(n) no matter how long `t` is.** The
price is 26 ints per position: 2.6×10⁵ at the ceiling.

**Approach 6, binary search.** For each letter, keep the sorted list of indices
where it appears. To match a character, binary-search its list for the first
index `>= j`. Each lookup costs O(log m) instead of O(1), but the whole index is
exactly `m` integers. That trade usually wins when `t` is large or the alphabet
isn't tiny.

Neither approach helps a single query: building the index already costs
O(m), which is what two pointers costs in total. The files call each approach
once per query and rebuild the index every time, to keep one signature for every
approach. In real use you'd build the index once and reuse it.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 is-subsequence/python/IsSubsequence.py
```

Or from inside the folder:

```bash
cd is-subsequence/python
python3 IsSubsequence.py
```

Expected output:

```
PASS  recursion: 14/14 cases
PASS  recursion + memo: 14/14 cases
PASS  bottom-up dp: 14/14 cases
PASS  two pointers: 14/14 cases
PASS  next-index table: 14/14 cases
PASS  binary search: 14/14 cases
PASS  recursion at |t| = 10^4: RecursionError (expected)
PASS  recursion + memo at |t| = 10^4: RecursionError (expected)
PASS  bottom-up dp at |t| = 10^4: True
PASS  two pointers at |t| = 10^4: True
PASS  next-index table at |t| = 10^4: True
PASS  binary search at |t| = 10^4: True
```

## Java

**Requires:** JDK 11+ (`String.repeat`, single-file launcher). Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java is-subsequence/java/IsSubsequence.java
```

### Option 2 — compile, then run

```bash
cd is-subsequence/java
javac IsSubsequence.java   # produces IsSubsequence.class
java IsSubsequence         # note: no .class extension
```

Expected output (either option):

```
PASS  recursion: 14/14 cases
PASS  recursion + memo: 14/14 cases
PASS  bottom-up dp: 14/14 cases
PASS  two pointers: 14/14 cases
PASS  next-index table: 14/14 cases
PASS  binary search: 14/14 cases
NOTE  recursion at |t| = 10^4: StackOverflowError
NOTE  recursion + memo at |t| = 10^4: StackOverflowError
PASS  bottom-up dp at |t| = 10^4: true
PASS  two pointers at |t| = 10^4: true
PASS  next-index table at |t| = 10^4: true
PASS  binary search at |t| = 10^4: true
```

The two `NOTE` lines may read `true` on a different JVM or with a larger
stack (`java -Xss4m ...`). Both outcomes are fine; see above.

To clean up the compiled artifacts from option 2:

```bash
rm is-subsequence/java/*.class
```

## Notes

- **Return `i == len(s)`, not `j == len(t)`.** The loop stops when *either*
  string runs out. Only `s` running out means success. `t` running out is
  success only if `s` happened to finish on the same step.
- **The empty `s` is a subsequence of everything**, including the empty `t`.
  Every approach gets this from its base case (`i == len(s)` is checked
  before `j == len(t)`). Swap the order of those two checks in the recursion
  and `("", "")` returns `false`.
- **The next-index table uses one extra row instead of two special cases.**
  NeetCode's version uses `m + 1` as a "not found" sentinel and needs a separate
  `if m == 0` guard, because its table has no row for an empty `t`. Adding a row
  at index `m` filled with `m` covers both. A miss jumps `j` to `m + 1` and the
  next check fails. An empty `t` starts in that row.
- **Copy the row; don't alias it.** Building the table, `next_at[j] =
  next_at[j + 1][:]` (Python) or `.clone()` (Java) is essential. Without the
  copy, every row is the same list, and the final table answers "where does
  each letter *first* appear in `t`" for every `j`.
- **`Collections.binarySearch` returns `-(insertion point) - 1` on a miss.**
  Approach 6 in Java decodes that back into "first index `>= j`". Python's
  `bisect_left` returns the insertion point directly. A hit needs no adjustment,
  since each letter's index list holds distinct values.
- **Test cases that catch shortcuts:** both files include `("ba", "ab")`
  (right letters, wrong order), which a letter-counting solution gets wrong,
  and `("aaa", "aa")` (`t` runs out of repeats), which a "is every letter of `s`
  somewhere in `t`" set check gets wrong. Both shortcuts pass the two NeetCode
  examples.
- **Method naming:** LeetCode uses `isSubsequence`. These files use
  `isSubsequence*` / `is_subsequence_*` with an approach suffix, so all six can
  coexist. Rename to plain `isSubsequence` when submitting.
- The Java file is named `IsSubsequence.java` to match its
  `public class IsSubsequence`, as `javac` requires.
