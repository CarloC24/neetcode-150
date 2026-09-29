"""Merge Two Sorted Lists (LeetCode 21).

Given the heads of two sorted linked lists, merge them into one sorted list by
splicing together the nodes of the first two, and return its head.

The two approaches from https://neetcode.io/solutions/merge-two-sorted-lists,
plus a brute force that ignores the fact that the inputs are already sorted,
ordered from brute force to optimal.

Approaches 2 and 3 splice the existing nodes together; approach 1 builds new
ones. The problem statement asks for splicing, so approach 1 is here only as
the baseline -- see the README.
"""

from typing import Callable, List, Optional, Sequence, Tuple


class ListNode:
    """NeetCode's definition, unchanged."""

    def __init__(self, val: int = 0, next: Optional["ListNode"] = None):
        self.val = val
        self.next = next


def merge_sort(list1: Optional[ListNode], list2: Optional[ListNode]) -> Optional[ListNode]:
    """Collect every value, sort, rebuild. Time: O((m + n) log(m + n)). Space: O(m + n).

    The brute force, and the answer you would give if nobody told you the inputs
    were sorted. It throws that fact away and pays a log factor to recover it.

    It also allocates a fresh list instead of splicing the given nodes, which the
    problem statement explicitly asks for. The values come out right, so every
    test here passes -- but the nodes are not the ones the caller handed in.
    """
    values: List[int] = []
    for head in (list1, list2):
        while head:
            values.append(head.val)
            head = head.next

    dummy = ListNode()
    tail = dummy
    for value in sorted(values):
        tail.next = ListNode(value)
        tail = tail.next

    return dummy.next


def merge_recursive(list1: Optional[ListNode], list2: Optional[ListNode]) -> Optional[ListNode]:
    """Take the smaller head, merge the rest behind it. Time: O(m + n). Space: O(m + n).

    Space is O(m + n) for the call stack: each call consumes one node, so the
    recursion goes as deep as the two lists combined. LeetCode caps each list at
    50 nodes, so that is at most 100 frames -- comfortably under Python's limit
    of 1000, unlike Reverse Linked List, where the same pattern did not fit.

    The base cases do more than stop the recursion. When one list runs out, the
    other is returned whole: whatever is left of it is already sorted, so it can
    be attached as-is without walking it.

    `<=` rather than `<` keeps the merge stable -- on a tie, the node from list1
    goes first. Values alone cannot tell the difference, but it is the
    conventional choice and costs nothing.
    """
    if not list1:
        return list2
    if not list2:
        return list1

    if list1.val <= list2.val:
        list1.next = merge_recursive(list1.next, list2)
        return list1
    list2.next = merge_recursive(list1, list2.next)
    return list2


def merge_iterative(list1: Optional[ListNode], list2: Optional[ListNode]) -> Optional[ListNode]:
    """Walk both lists with a dummy head. Time: O(m + n). Space: O(1).

    The optimal solution, and the one to write.

    The dummy node is the trick worth keeping. Without it the first node of the
    result is a special case -- you have to decide which list it comes from
    before the loop, and handle either list being empty -- and every append
    inside the loop needs an "is this the first one?" check. With it, `tail`
    always has somewhere to hang the next node, and the real head is simply
    `dummy.next` at the end.

    The loop stops as soon as either list runs out, and the leftover is attached
    in one assignment instead of being copied node by node. At most one of the
    two is non-empty by then, so `list1 or list2` picks it.
    """
    dummy = ListNode()
    tail = dummy

    while list1 and list2:
        if list1.val <= list2.val:
            tail.next = list1
            list1 = list1.next
        else:
            tail.next = list2
            list2 = list2.next
        tail = tail.next

    tail.next = list1 or list2

    return dummy.next


def build(values: Sequence[int]) -> Optional[ListNode]:
    """Build a list from values, back to front so each node gets its successor."""
    head = None
    for value in reversed(values):
        head = ListNode(value, head)
    return head


def to_values(head: Optional[ListNode], limit: int) -> List[int]:
    """Read a list back into values, refusing to loop forever.

    A splicing bug can close the result into a cycle, so an unbounded walk here
    would hang the file instead of failing it. Stopping past `limit` yields an
    over-long list that no expected answer matches, which reports as FAIL and
    lets the remaining cases run.
    """
    values: List[int] = []
    while head is not None and len(values) <= limit:
        values.append(head.val)
        head = head.next
    return values


Merge = Callable[[Optional[ListNode], Optional[ListNode]], Optional[ListNode]]

SOLUTIONS: Tuple[Tuple[str, Merge], ...] = (
    ("sort", merge_sort),
    ("recursion", merge_recursive),
    ("iteration", merge_iterative),
)

CASES: Sequence[Tuple[Sequence[int], Sequence[int]]] = (
    ((1, 2, 4), (1, 3, 4)),  # the LeetCode example
    ((), ()),  # both empty
    ((), (0,)),  # one empty: the other comes back whole
    ((0,), ()),  # the same, mirrored
    ((1, 2, 3), (4, 5, 6)),  # list1 runs out first; list2 is attached in one step
    ((4, 5, 6), (1, 2, 3)),  # the same, mirrored
    ((1, 3, 5, 7), (2, 4, 6, 8)),  # perfect interleave
    ((1, 1, 1), (1, 1)),  # all ties
    ((5,), (1, 2, 3, 4, 6)),  # a single node lands in the middle
    ((-100, 0, 100), (-100, 100)),  # the value bounds, duplicated across lists
    ((-3, -2, -1), (-5, -4)),  # all negative
    (tuple(range(0, 100, 2)), tuple(range(1, 100, 2))),  # 50 + 50: the largest input allowed
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = 0
        for values1, values2 in CASES:
            expected = sorted(list(values1) + list(values2))
            result = to_values(solve(build(values1), build(values2)), len(expected) + 1)
            passed += result == expected
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()
