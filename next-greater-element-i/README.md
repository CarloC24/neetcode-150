# Next Greater Element I

LeetCode 496 · [NeetCode solution page](https://neetcode.io/solutions/next-greater-element-i)

You're given two arrays of distinct integers, where every value in `nums1` also
appears in `nums2`. For each value in `nums1`, find where it sits in `nums2` and
return the first value **to its right** that is larger. If there isn't one,
return `-1` for that value.

`nums1 = [4, 1, 2]`, `nums2 = [1, 3, 4, 2]` gives `[-1, 3, -1]`. Nothing after
`4` is larger. `3` is the first value after `1` that is larger. Nothing comes
after `2`.

**Constraints:** `1 <= nums1.length <= nums2.length <= 1000`,
`0 <= nums1[i], nums2[i] <= 10^4`, all values are unique, and every value in
`nums1` appears in `nums2`.

**Follow-up:** can you do it in O(`nums1.length` + `nums2.length`)?

```
next-greater-element-i/
├── java/
│   └── NextGreaterElementI.java
├── python/
│   └── NextGreaterElementI.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

`m = len(nums1)`, `n = len(nums2)`. Space doesn't count the output array.

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Brute force | O(m·n) | O(1) | For each query, scan `nums2` from the right back to the query. |
| 2 | Hash map | O(m·n) | O(m) | Walk `nums2`; scan ahead only from values that are queries. |
| 3 | Monotonic stack, all of `nums2` | O(m+n) | O(n) | Answer every value in `nums2`, then look up the queries. |
| 4 | **Monotonic stack, queries only** | **O(m+n)** | **O(m)** | Only queries go on the stack; answers go straight into the result. |

Approaches 1, 2 and 4 are NeetCode's. Approach 3 is the textbook monotonic
stack, included because it's the version that carries over to other problems,
and because comparing it with approach 4 shows what the filtering buys.

**Approach 4 is the one to reach for.** It answers the follow-up in O(m+n), and
its stack and map hold at most `m` values, where approach 3's hold up to `n`.

**Approach 2 doesn't beat the brute force.** The map lets it skip values that
aren't queries, but from each query it still scans forward to the answer. When
`nums2` is decreasing and every value is a query, that's the same O(m·n). The
ceiling test cases include exactly that input.

## The monotonic stack

This is the idea the problem exists to teach, and it comes back in Daily
Temperatures, Car Fleet and Largest Rectangle in Histogram.

Walk `nums2` left to right. The stack holds values **still waiting** for
something larger. When a new value arrives, it is the answer for every waiting
value smaller than it. Those are all at the top of the stack, so pop them and
record the answer. Then push the new value, since it's now waiting too.

```
nums2 = [1, 3, 4, 2]

see 1   stack []        -> push 1            stack [1]
see 3   3 > 1, pop 1    -> next(1) = 3       stack [3]
see 4   4 > 3, pop 3    -> next(3) = 4       stack [4]
see 2   2 < 4, no pop   -> push 2            stack [4, 2]
end     4 and 2 never got an answer          -> -1
```

Two properties make it work:

- **The stack is always decreasing from bottom to top.** A value is pushed only
  after every smaller value above it has been popped. So the values a new
  arrival can answer are always together at the top, and the first one it
  can't beat ends the popping.
- **It's O(n) overall, even with a loop inside a loop.** Each value is pushed
  once and popped at most once, so the inner `while` runs at most `n` times
  across the whole walk, not `n` times per step. This is the same argument as
  the skipping loops in [Valid Palindrome](../valid-palindrome/).

## Why approach 4 can skip non-queries

NeetCode's version pushes only values that appear in `nums1`. That's safe for
two reasons:

1. **A value nobody asked about never needs an answer.** So there's no point
   storing it.
2. **It never changes anyone else's answer.** Say a non-query value `x` were
   on the stack. Everything below it is larger than `x`, because the stack is
   decreasing. When a new value arrives:
   - If it's larger than `x`, it pops `x` and carries on popping exactly as it
     would have without `x` there.
   - If it's smaller than `x`, it stops at `x`. But everything below `x` is
     larger still, so it would have stopped there anyway.

   Either way, every other value is popped by the same arrival, with or without
   `x` on the stack. So `x` can be left off.

The stack is still decreasing, for the same reason as before. The test with
`nums2 = [6, 5, 4, 3, 2, 1, 7]` shows the filtered stack working: `6` isn't a
query, and `7` still answers all five queries below it.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 next-greater-element-i/python/NextGreaterElementI.py
```

Or from inside the folder:

```bash
cd next-greater-element-i/python
python3 NextGreaterElementI.py
```

Expected output:

```
PASS  brute force: 12/12 cases
PASS  hash map: 12/12 cases
PASS  stack, all of nums2: 12/12 cases
PASS  stack, queries only: 12/12 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java next-greater-element-i/java/NextGreaterElementI.java
```

### Option 2 — compile, then run

```bash
cd next-greater-element-i/java
javac NextGreaterElementI.java   # produces NextGreaterElementI*.class
java NextGreaterElementI         # note: no .class extension
```

Expected output (either option):

```
PASS  brute force: 12/12 cases
PASS  hash map: 12/12 cases
PASS  stack, all of nums2: 12/12 cases
PASS  stack, queries only: 12/12 cases
```

To clean up the compiled artifacts from option 2:

```bash
rm next-greater-element-i/java/NextGreaterElementI*.class
```

## Notes

- **"Next greater" means nearest, not largest.** The brute force scans from the
  right and overwrites its candidate with every larger value it passes. So when
  it reaches the query, it holds the *nearest* larger value. NeetCode's page
  describes this as "tracking the largest element", but the code is right and
  the description isn't. Replace the overwrite with `max` and
  `([5], [5, 6, 9])` returns `9` instead of `6`. That case is in the test set
  for this reason.
- **Larger values to the left don't count.** `([2], [4, 2])` returns `-1`.
- **Java uses `ArrayDeque`, not `Stack`.** NeetCode's Java uses
  `java.util.Stack`, which synchronizes every call for no benefit. `ArrayDeque`'s
  `push`, `pop` and `peek` all work on the head, so it behaves as a stack.
- **Values are at most 10⁴**, so an `int[10001]` could replace either hash map.
  The files use maps, because they don't depend on the value range and they
  match the follow-up's intent.
- **Verified beyond the test set, in both languages:** 20,000 random inputs
  with `nums2` of length 1–12 drawn from 0–29 and `nums1` a random subset in
  random order. Every approach was checked against a direct oracle (find the
  query, take the first larger value after it), with zero failures in either
  language.
- **Method naming:** LeetCode uses `nextGreaterElement`. These files use
  `nextGreater*` / `next_greater_*` with an approach suffix, so all four can
  coexist. Rename to plain `nextGreaterElement` when submitting.
- The Java file is named `NextGreaterElementI.java` to match its
  `public class NextGreaterElementI`, as `javac` requires.
