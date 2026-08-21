"""Reverse Linked List (LeetCode 206).

Given the head of a singly linked list, reverse it and return the new head.

The two approaches from https://neetcode.io/solutions/reverse-linked-list, plus
the explicit-stack version that shows what the recursion is doing implicitly,
ordered from brute force to optimal.

Every approach here relinks the existing nodes in place. None of them allocate a
new list, and none of them move values between nodes -- see the README on why
that distinction matters even though the two are indistinguishable from outside.
"""

from typing import Callable, List, Optional, Sequence, Tuple


class ListNode:
    """NeetCode's definition, unchanged."""

    def __init__(self, val: int = 0, next: Optional["ListNode"] = None):
        self.val = val
        self.next = next


def reverse_stack(head: Optional[ListNode]) -> Optional[ListNode]:
    """Push every node, then pop them back into order. Time: O(n). Space: O(n).

    The brute force, and the honest picture of what approach 2 does: reversal is
    a last-in-first-out problem, so the naive answer is to reach for the data
    structure that spells that out. The recursion below uses the exact same
    stack -- it just lets the interpreter own it.

    Popping yields the nodes back to front, which is the order they need to be
    linked in. Each node points at whatever is left on top of the stack, and the
    final pop -- the original head, now the tail -- gets None.

    That last assignment is the one to notice. Without it the original head
    keeps pointing forward at the second node, which now points back at it, and
    the list closes into a cycle.
    """
    stack: List[ListNode] = []
    curr = head
    while curr:
        stack.append(curr)
        curr = curr.next

    new_head = stack[-1] if stack else None
    while stack:
        node = stack.pop()
        node.next = stack[-1] if stack else None

    return new_head


def reverse_recursive(head: Optional[ListNode]) -> Optional[ListNode]:
    """Reverse the tail, then attach the head behind it. Time: O(n). Space: O(n).

    Space is O(n) for the call stack, not O(1): the recursion goes exactly as
    deep as the list is long.

    The whole trick is `head.next.next = head`. The recursive call reverses
    everything after head and hands back the new head, but it does not touch
    head's own pointer -- so head.next still refers to the node that started the
    sublist and has just become its tail. Pointing that node's next back at head
    appends head to the end, which is where it belongs.

        head -> [2 -> 3]            before
        head -> [3 -> 2]            after the recursive call; head.next is 2
        head -> [3 -> 2 -> head]    after head.next.next = head
                [3 -> 2 -> head]    after head.next = None

    Then `head.next = None`, because head is now the last node and is still
    pointing forward at the node behind it. Drop that line and every list of two
    or more nodes comes back as a cycle -- but a single-node list still passes,
    so the bug hides on the smallest test.

    NeetCode spells the base case differently: it returns early only on None,
    seeds `newHead = head`, and guards the recursion with `if head.next`. Same
    algorithm and same result; this form short-circuits on the last node instead
    of falling through with a redundant assignment.

    In Python this approach cannot run at the stated constraints. The default
    recursion limit is 1000 and the maximum input is 1000 nodes, so the largest
    legal list is exactly the one that raises RecursionError -- and the real
    ceiling is lower still, since it counts whatever frames are already on the
    stack when you call. Measured here: 997 nodes from the module level, 947
    from 50 frames deep. The long test case above is sized well under that on
    purpose. See the README.
    """
    if not head or not head.next:
        return head

    new_head = reverse_recursive(head.next)
    head.next.next = head
    head.next = None

    return new_head


def reverse_iterative(head: Optional[ListNode]) -> Optional[ListNode]:
    """Flip each pointer as you walk. Time: O(n). Space: O(1).

    The optimal solution, and the one to write. Three names -- prev, curr and
    the temp holding curr.next -- are all the state the problem needs.

    `temp` is not a convenience. Overwriting curr.next is what the loop is for,
    so the rest of the list has to be saved before that happens or it is
    unreachable. Reorder those two lines and the first iteration strands
    everything past the head.

    prev starts at None, which is what makes the original head terminate the
    reversed list, and it ends on the last node visited -- the new head. Return
    prev, not curr: curr is None by the time the loop exits, every time.
    """
    prev, curr = None, head

    while curr:
        temp = curr.next
        curr.next = prev
        prev = curr
        curr = temp

    return prev


def build(values: Sequence[int]) -> Optional[ListNode]:
    """Build a list from values, back to front so each node gets its successor."""
    head = None
    for value in reversed(values):
        head = ListNode(value, head)
    return head


def to_values(head: Optional[ListNode], limit: int) -> List[int]:
    """Read a list back into values, refusing to loop forever.

    A wrong answer to this problem is usually a cycle, not a scramble, so an
    unbounded walk here would hang the file instead of failing it. Stopping past
    `limit` yields an over-long list that no expected answer matches, which
    reports as FAIL and lets the remaining cases run.
    """
    values: List[int] = []
    while head is not None and len(values) <= limit:
        values.append(head.val)
        head = head.next
    return values


SOLUTIONS: Tuple[Tuple[str, Callable[[Optional[ListNode]], Optional[ListNode]]], ...] = (
    ("stack", reverse_stack),
    ("recursion", reverse_recursive),
    ("iteration", reverse_iterative),
)

CASES: Sequence[Sequence[int]] = (
    (1, 2, 3, 4, 5),  # the LeetCode example
    (1, 2),  # shortest case that cycles if the tail is not nulled
    (1,),  # single node, where that same bug still passes
    (),  # empty list; the constraints allow length 0
    (1, 2, 3),  # odd length
    (1, 2, 3, 4),  # even length
    (0, 0, 0),  # duplicates must survive
    (1, 2, 1),  # palindromic: reversing changes nothing, so this alone proves nothing
    (-5000, 5000),  # the value bounds
    (-1, -2, -3),  # all negative
    tuple(range(100)),  # long enough to catch an off-by-one at either end
    tuple(range(500)),  # deep, but with headroom under the recursion limit; see the README
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = 0
        for values in CASES:
            expected = list(reversed(values))
            result = to_values(solve(build(values)), len(values) + 1)
            passed += result == expected
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()
