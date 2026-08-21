# Reverse Linked List

LeetCode 206 · [NeetCode solution page](https://neetcode.io/solutions/reverse-linked-list)

Given the head of a singly linked list, reverse it and return the new head.

**Constraints:** the list holds `0` to `5000` nodes, and `-5000 <= Node.val <= 5000`.

```
reverse-linked-list/
├── java/
│   └── ReverseLinkedList.java
├── python/
│   └── ReverseLinkedList.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Explicit stack | O(n) | O(n) | Push every node, pop them back into order. |
| 2 | Recursion | O(n) | O(n) | Reverse the tail, then attach the head behind it. |
| 3 | **Iteration** | **O(n)** | **O(1)** | Walk the list flipping each pointer as you pass it. |

**Approach 3 is the one to reach for**, and here the gap is real rather than
cosmetic: it is the only one that does not carry the whole list somewhere. All
three are O(n) time, but 1 and 2 both hold n nodes' worth of state — one in an
`ArrayDeque`, the other in call frames — to compute something that needs three
pointers.

**Approaches 1 and 2 are the same algorithm.** Reversal is last-in-first-out, so
the naive answer reaches for a stack; the recursion then uses the *same* stack
and lets the runtime own it. Approach 1 is worth writing once precisely because
it makes that visible — the call stack in approach 2 is not a different idea,
just an invisible one. And an invisible stack is the one that overflows.

## The trick in the recursion

The recursive call reverses everything after `head` and returns the new head,
but it does not touch `head`'s own pointer. So `head.next` still refers to the
node that used to start the tail and has just become its **end** — which is
exactly where `head` needs to go:

```
head -> [2 -> 3]            before
head -> [3 -> 2]            after the recursive call; head.next is still 2
head -> [3 -> 2 -> head]    head.next.next = head
        [3 -> 2 -> head]    head.next = None
```

That is the whole method. `head.next.next = head` reads like a typo and is the
one line doing work.

## Forget the last line and you build a cycle

`head.next = None` is not tidying up. After the line above, `head` is the last
node and is *still pointing forward* at the node behind it — the two now point
at each other:

```
"[1, 2, 3]" without head.next = None   ->  CYCLE
"[1, 2]"    without head.next = None   ->  CYCLE
"[1]"       without head.next = None   ->  [1]   ← still passes
```

A single-node list never enters the branch, so it comes back correct and the bug
survives the smallest test anyone writes. `[1, 2]` is the shortest input that
catches it, and it is in the test set of both files.

This shapes the test harness too. A wrong answer here is usually a cycle rather
than a scramble, so `to_values` / `toValues` stops after `n + 1` nodes instead of
walking until null. An unbounded read would hang the file instead of failing it;
the bounded one returns an over-long list that matches nothing, reports FAIL, and
lets the remaining cases run.

## The recursion does not fit in Python

This is the interesting part of the problem, and it splits by language.

The recursion goes exactly as deep as the list is long. Python's default
recursion limit is **1000**, and LeetCode 206 allows **up to 5000 nodes** — so
approach 2 cannot run at the stated constraints at all. Even under NeetCode's
smaller restatement of the problem the margin is nil:

| Called from | Largest list that survives |
|---|---|
| Module level | 997 |
| 3 frames deep | 994 |
| 10 frames deep | 987 |
| 50 frames deep | 947 |

The ceiling is not a fixed number — the limit counts *every* frame on the stack,
so each frame of ambient depth costs exactly one node of headroom. This bit
during development: a 999-node case passed when called directly and raised
`RecursionError` once it was called from inside `main`'s loop. That is also why
the long test case in both files is 500 rather than something closer to the line.

Java has no such problem at this size. A 1000-node list is nowhere near the
default stack; measured on this machine it takes roughly **39,000** frames to
throw `StackOverflowError`, and that number swings with JVM version, thread
stack size and whether the call gets inlined — so it is a ballpark, not a
guarantee.

The lesson is not "avoid recursion". It is that **O(n) space on the call stack
is a different resource from O(n) space in the heap**, with a limit that is
smaller than you expect, set by the runtime rather than the machine, and reached
by an exception rather than a slowdown. Approach 3 sidesteps it entirely, which
is a better reason to prefer it than the constant factor.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 reverse-linked-list/python/ReverseLinkedList.py
```

Or from inside the folder:

```bash
cd reverse-linked-list/python
python3 ReverseLinkedList.py
```

Expected output:

```
PASS  stack: 12/12 cases
PASS  recursion: 12/12 cases
PASS  iteration: 12/12 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java reverse-linked-list/java/ReverseLinkedList.java
```

### Option 2 — compile, then run

```bash
cd reverse-linked-list/java
javac ReverseLinkedList.java   # produces ReverseLinkedList*.class
java ReverseLinkedList         # note: no .class extension
```

Expected output (either option):

```
PASS  stack: 12/12 cases
PASS  recursion: 12/12 cases
PASS  iteration: 12/12 cases
```

To clean up the compiled artifacts from option 2 — note the plural, since the
nested `ListNode` class compiles to its own file:

```bash
rm reverse-linked-list/java/ReverseLinkedList*.class
```

## Notes

- **Every approach destroys its input.** This is the first problem in this repo
  where the solutions mutate what they are given, so the harness builds a fresh
  list for each approach on each case rather than sharing one. Running two of
  these back to back on the same head would hand the second a list the first
  already took apart. Worth remembering outside the harness too: a linked-list
  answer usually cannot be re-run on its own input.
- **The `temp` in approach 3 is load-bearing.** Overwriting `curr.next` is the
  entire point of the loop, so the successor has to be saved before that
  happens. Drop it and the failure is not a scramble but a truncation —
  `[1, 2, 3, 4, 5]` comes back as `[1]`, because after the first assignment
  `curr = curr.next` reads the pointer just written and walks backwards into the
  node it came from. Verified.
- **Return `prev`, not `curr`.** The loop exits precisely when `curr` is null,
  every time, so returning it returns an empty list — a test that only checks
  "did it not crash" will not catch this.
- **`ArrayDeque` over `Stack`, and it relies on an asymmetry.** `java.util.Stack`
  extends `Vector` and synchronizes every method for a lock nobody here
  contends. The replacement leans on `peek()` returning `null` on an empty
  deque, which is what gives the final popped node its terminating `next` —
  while `push(null)` on the same class throws `NullPointerException`. Both
  verified. The code only ever pushes real nodes, so the two behaviours
  cooperate, but a version that pushed a null sentinel would not survive the
  swap from `Stack`.
- **Reversing the *values* is a different problem.** Collecting the values into
  an array and writing them back in reverse is O(n) space and passes every test
  in these files, since nothing outside can tell the two apart. It is worth
  knowing it is not the same thing: it fails the moment nodes carry anything
  besides `val`, and interviewers generally mean the pointers. All three
  approaches here relink the existing nodes — verified by node identity, not
  just by value.
- **Verified beyond the test set.** All three approaches were checked on every
  list length from 0 to 300, on 4,000 random lists (lengths 0–60, values
  −5000…5000, duplicates and negatives included), and for in-place behaviour on
  lengths 0–59 — confirming the returned list holds the same node objects in
  reversed order, with Floyd's cycle detection proving no result loops. Zero
  failures.
- **The palindromic case proves nothing on its own.** `[1, 2, 1]` reverses to
  itself, so a solution that returns its input unchanged passes it. It is in the
  test set as a reminder, not as evidence.
- **Method naming:** both NeetCode and LeetCode use `reverseList`. These files
  use `reverse*` / `reverse_*` suffixed by approach so all three can coexist —
  rename to plain `reverseList` when submitting, and drop the `ListNode`
  definition, which the judge supplies.
- **NeetCode's recursion is spelled differently.** It returns early only on
  `None`, seeds `newHead = head`, and guards the recursive call with
  `if head.next`, which reaches `head.next = None` on the last node as a no-op.
  Same algorithm; these files short-circuit on `not head or not head.next`
  instead.
- The Java file is named `ReverseLinkedList.java` to match its
  `public class ReverseLinkedList`, as `javac` requires.
- **The follow-up asks for both.** LeetCode ends this problem with "A linked
  list can be reversed either iteratively or recursively. Could you implement
  both?" — so unlike most problems here, the non-optimal approach is explicitly
  part of the ask rather than a stepping stone. That makes the depth limit above
  worth being able to state out loud.
- **The constraints above are LeetCode 206's.** NeetCode restates this problem
  with a smaller bound of `0 <= length <= 1000`. The algorithms are unaffected;
  the difference only matters for approach 2 in Python, which cannot reach
  either ceiling.
