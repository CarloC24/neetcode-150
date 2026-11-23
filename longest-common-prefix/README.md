# Longest Common Prefix

LeetCode 14 · [NeetCode solution page](https://neetcode.io/solutions/longest-common-prefix)

Given an array of strings `strs`, return the longest string that is a prefix of
every one of them. If there is none, return `""`.

`["bat", "bag", "bank", "band"]` gives `"ba"`.
`["dance", "dag", "danger", "damage"]` gives `"da"`.
`["neet", "feet"]` gives `""`.

**Constraints:** `1 <= strs.length <= 200`, `0 <= strs[i].length <= 200`,
`strs[i]` is lowercase English letters only.

```
longest-common-prefix/
├── java/
│   └── LongestCommonPrefix.java
├── python/
│   └── LongestCommonPrefix.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

`n` = number of strings, `m` = length of the shortest string, `S` = total
characters across all strings.

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Horizontal scanning | O(n · m) | O(1) | Start with `strs[0]` and trim it against each string in turn. |
| 2 | **Vertical scanning** | **O(n · m)** | **O(1)** | Check column `i` in every string, and stop at the first mismatch. |
| 3 | Sorting | O(n · m · log n) | O(n) | Sort, then the answer is the common prefix of the first and last strings. |
| 4 | Trie | O(S) | O(S) | Insert every string, then walk down while there's exactly one path. |

All four are NeetCode's. Space excludes the answer string.

**Vertical scanning is the one to use.** Horizontal and vertical have the same
worst case, but vertical stops reading the moment any string disagrees. It
reads at most `n × (answer length + 1)` characters. Horizontal can read all of
a long `strs[0]` against `strs[1]` before a later string cuts the prefix to
nothing.

## Two checks that are easy to miss

Both are in the test set, and each one breaks exactly one approach if removed.

**Vertical: check the length before indexing.** `strs[0]` isn't necessarily
the shortest string. In `["ab", "a"]`, column 1 exists in `"ab"` but not in
`"a"`. So the condition has to be `i == len(s) or s[i] != first[i]`, in that
order. Swap them and Python raises `IndexError`, Java throws
`StringIndexOutOfBoundsException`.

**Trie: stop at the end of a word.** `["a", "ab"]` builds one path, `a → b`,
with no branch anywhere. Walking "while there's one child" would return
`"ab"`. But `"a"` ends at the first node, so the prefix can't go past it. Each
node records `end`, and the walk stops on it.

## Why sorting only needs two strings

After sorting, strings that share a prefix sit next to each other. The first
and last strings are the most different pair, so any prefix they share is
shared by every string in between:

```
sorted: ["bag", "band", "bank", "bat"]
         ^^                       ^^     first and last agree on "ba"
```

That also means the sort is wasted work. `min(strs)` and `max(strs)` are the
same first and last strings, found in one O(n · m) pass. The sorting approach
copies before sorting (`sorted()` in Python, `strs.clone()` in Java) so the
caller's list keeps its order. The final test checks that.

## When the trie is worth it

For one query, the trie is the slowest and uses the most memory: it allocates
a node per character. It earns its cost when the strings stay fixed and you
query prefixes over and over. That's the setup in the Tries category later in
the roadmap (Implement Trie, Word Search II).

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 longest-common-prefix/python/LongestCommonPrefix.py
```

Or from inside the folder:

```bash
cd longest-common-prefix/python
python3 LongestCommonPrefix.py
```

Expected output:

```
PASS  horizontal: 16/16 cases
PASS  vertical: 16/16 cases
PASS  sorting: 16/16 cases
PASS  trie: 16/16 cases
PASS  input left unmodified: ['c', 'b', 'a']
```

## Java

**Requires:** JDK 11+ for the single-file launcher below (and for
`String.repeat`). Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java longest-common-prefix/java/LongestCommonPrefix.java
```

### Option 2 — compile, then run

```bash
cd longest-common-prefix/java
javac LongestCommonPrefix.java   # produces LongestCommonPrefix*.class
java LongestCommonPrefix         # note: no .class extension
```

Expected output (either option):

```
PASS  horizontal: 16/16 cases
PASS  vertical: 16/16 cases
PASS  sorting: 16/16 cases
PASS  trie: 16/16 cases
PASS  input left unmodified: [c, b, a]
```

To clean up the compiled artifacts from option 2:

```bash
rm longest-common-prefix/java/LongestCommonPrefix*.class
```

## Notes

- **Checked against a reference.** Besides the 16 cases, all four Python
  approaches were run on 20,000 random inputs (1–5 strings of `a`/`b`, length
  0–4) and compared with `os.path.commonprefix`. They all agreed.
- **Test cases** cover an empty string first and last, one string alone, one
  string that's a prefix of another (both orders), all strings equal, a sort
  that reorders the input, and the 200 × 200 ceiling.
- **The Java trie uses a 26-slot array** plus a child count, so "exactly one
  child" is an O(1) check. The Python trie uses a dict, where `len(children)`
  does the same.
- **Method naming:** LeetCode uses `longestCommonPrefix`. These files use
  `longestCommonPrefix*` / `longest_common_prefix_*` with an approach suffix, so
  all four can coexist. Rename to plain `longestCommonPrefix` when submitting.
- The Java file is named `LongestCommonPrefix.java` to match its
  `public class LongestCommonPrefix`, as `javac` requires.
