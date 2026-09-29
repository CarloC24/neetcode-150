# Merge Two Sorted Lists

LeetCode 21 · [NeetCode solution page](https://neetcode.io/solutions/merge-two-sorted-lists)

Given the heads of two sorted linked lists, merge them into one sorted list by
splicing together the nodes of the first two, and return its head.

**Constraints:** each list holds `0` to `50` nodes, `-100 <= Node.val <= 100`,
and both lists are sorted in non-decreasing order.

```
merge-two-sorted-lists/
├── java/
│   └── MergeTwoSortedLists.java
├── python/
│   └── MergeTwoSortedLists.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Collect and sort | O((m+n) log(m+n)) | O(m+n) | Ignore the sortedness, sort all values, build a new list. |
| 2 | Recursion | O(m+n) | O(m+n) | Take the smaller head, merge the rest behind it. |
| 3 | **Iteration with a dummy head** | **O(m+n)** | **O(1)** | Walk both lists, always appending the smaller node. |

**Approach 3 is the one to reach for.** NeetCode publishes approaches 2 and 3;
approach 1 is the brute-force baseline added here to show what the sortedness
buys you: it takes you from a sort to a single linear pass.

## The dummy node

This is the pattern to take away from the problem, because it comes back in
almost every linked-list question that builds a new chain (Add Two Numbers,
Remove Nth Node, Partition List, Merge K Sorted Lists).

Without a dummy, the head of the result is a special case. You have to pick it
before the loop, which means handling "either list is empty" up front, and then
every append needs to check whether it is the first one. With a dummy node,
`tail` always has something to hang the next node on, and the answer is just
`dummy.next`:

```
dummy -> ?                    tail = dummy
dummy -> 1                    take 1 from list1
dummy -> 1 -> 1               take 1 from list2
dummy -> 1 -> 1 -> 2 -> ...   ...
return dummy.next
```

The dummy costs one allocation and removes every edge case at the front.

## Attaching the leftover

Both approaches stop comparing as soon as one list runs out. Whatever is left in
the other list is already sorted, and every value in it is at least as large as
everything merged so far, so it gets attached in **one assignment**
(`tail.next = list1 or list2`) rather than walked node by node. The recursion
does the same thing through its base cases: `if not list1: return list2`.

## Recursion fits this time

In Reverse Linked List the recursion went as deep as the list was long, and
Python's recursion limit of 1000 was smaller than LeetCode's 5000-node bound.
Here each call consumes one node from either list, so the depth is at most
`m + n` = 100, well within Python's limit. The approach is still O(m + n)
space, though, and it would stop fitting if the lists were allowed to be large.
That is the reason to prefer the loop in general.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 merge-two-sorted-lists/python/MergeTwoSortedLists.py
```

Expected output:

```
PASS  sort: 12/12 cases
PASS  recursion: 12/12 cases
PASS  iteration: 12/12 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java merge-two-sorted-lists/java/MergeTwoSortedLists.java
```

### Option 2 — compile, then run

```bash
cd merge-two-sorted-lists/java
javac MergeTwoSortedLists.java   # produces MergeTwoSortedLists*.class
java MergeTwoSortedLists         # note: no .class extension
```

Expected output (either option):

```
PASS  sort: 12/12 cases
PASS  recursion: 12/12 cases
PASS  iteration: 12/12 cases
```

To clean up the compiled artifacts from option 2:

```bash
rm merge-two-sorted-lists/java/MergeTwoSortedLists*.class
```

## Notes

- **Approaches 2 and 3 destroy their inputs.** They rewire the `next` pointers
  of the nodes they are given, so after a merge, `list1` and `list2` no longer
  describe the lists they used to. The harness builds fresh lists for each
  approach on each case for that reason.
- **Approach 1 passes the tests but not the spec.** The problem says to splice
  the existing nodes. The sort-based version allocates new ones, which produces
  the same values from different nodes. Tests that compare values alone cannot
  tell the difference.
- **`<=` keeps the merge stable.** On a tie, the node from `list1` goes first.
  The values come out identical either way, but it is the conventional choice.
- **Verified beyond the test set.** All three approaches were checked against
  5,000 random pairs of sorted lists (lengths 0–50 each, values −100…100,
  duplicates included) with zero failures. For approaches 2 and 3, the result
  was also confirmed to contain exactly the input nodes, checked by object
  identity, so they really do splice rather than copy.
- **Method naming:** LeetCode uses `mergeTwoLists`. These files use `merge*` /
  `merge_*` suffixed by approach so all three can coexist. Rename to
  `mergeTwoLists` when submitting, and drop the `ListNode` definition, which the
  judge supplies.
- The Java file is named `MergeTwoSortedLists.java` to match its
  `public class MergeTwoSortedLists`, as `javac` requires.
