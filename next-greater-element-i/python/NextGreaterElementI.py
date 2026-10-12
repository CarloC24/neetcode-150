"""Next Greater Element I (LeetCode 496).

nums1 is a subset of nums2, and all values are distinct. For each value in
nums1, find where it sits in nums2 and return the first value to its right
that is larger, or -1 if there is none.

The three approaches from https://neetcode.io/solutions/next-greater-element-i,
plus the classic monotonic stack over all of nums2, ordered from brute force to
optimal.
"""

from typing import Callable, Dict, List, Sequence, Tuple


def next_greater_brute_force(nums1: List[int], nums2: List[int]) -> List[int]:
    """Scan nums2 from the right for each query. Time: O(m * n). Space: O(1).

    m = len(nums1), n = len(nums2); the output list is not counted.

    Walking right to left, every value bigger than the query overwrites the
    candidate, so when the scan reaches the query itself the candidate is the
    *nearest* bigger value to its right -- not the largest, which is what it
    would be with a max(). Reaching the query ends the scan.
    """
    res = []
    for num in nums1:
        next_greater = -1
        for i in range(len(nums2) - 1, -1, -1):
            if nums2[i] > num:
                next_greater = nums2[i]
            elif nums2[i] == num:
                break
        res.append(next_greater)
    return res


def next_greater_hash_map(nums1: List[int], nums2: List[int]) -> List[int]:
    """Walk nums2 once, scanning ahead only from query values.
    Time: O(m * n). Space: O(m).

    A value -> index map of nums1 answers "is this a query, and where does the
    answer go?" in O(1), so values of nums2 that nobody asked about are skipped.
    For each query value it still scans forward to the first bigger value, so
    the worst case -- nums2 in decreasing order, every value a query -- is the
    same O(m * n) as the brute force.
    """
    nums1_index = {num: i for i, num in enumerate(nums1)}
    res = [-1] * len(nums1)
    for i, num in enumerate(nums2):
        if num not in nums1_index:
            continue
        for j in range(i + 1, len(nums2)):
            if nums2[j] > num:
                res[nums1_index[num]] = nums2[j]
                break
    return res


def next_greater_stack_all(nums1: List[int], nums2: List[int]) -> List[int]:
    """Monotonic stack over all of nums2, then look up each query.
    Time: O(m + n). Space: O(n).

    The classic answer to the follow-up. The stack holds values still waiting
    for something bigger, and it is always decreasing from bottom to top: a
    value is only pushed after everything smaller than it has been popped.
    Each new value pops -- and answers -- every waiting value it beats. Every
    value is pushed once and popped at most once, so the inner while loop is
    O(n) across the whole run, not per step.

    It answers every value in nums2, then reads off the ones nums1 asked for.
    That wastes the map entries for values nobody asked about; approach 4
    avoids them.
    """
    next_greater: Dict[int, int] = {}
    stack: List[int] = []
    for num in nums2:
        while stack and stack[-1] < num:
            next_greater[stack.pop()] = num
        stack.append(num)
    return [next_greater.get(num, -1) for num in nums1]


def next_greater_stack_queries(nums1: List[int], nums2: List[int]) -> List[int]:
    """Monotonic stack holding only the query values.
    Time: O(m + n). Space: O(m).

    The optimal solution, and NeetCode's. Only values of nums1 are pushed, so
    the stack and the map are both bounded by m instead of n, and each answer
    is written straight into its slot in res.

    Leaving the other values off the stack is safe. A value nobody asked about
    never needs an answer, and it never blocks anything either: a value that
    would have sat on the stack below it is answered by the same later values
    either way. The stack is still decreasing, for the same reason as approach
    3 -- a query value is pushed only after everything smaller has been popped.
    """
    nums1_index = {num: i for i, num in enumerate(nums1)}
    res = [-1] * len(nums1)
    stack: List[int] = []
    for num in nums2:
        while stack and stack[-1] < num:
            res[nums1_index[stack.pop()]] = num
        if num in nums1_index:
            stack.append(num)
    return res


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int], List[int]], List[int]]], ...] = (
    ("brute force", next_greater_brute_force),
    ("hash map", next_greater_hash_map),
    ("stack, all of nums2", next_greater_stack_all),
    ("stack, queries only", next_greater_stack_queries),
)

_DECREASING = list(range(1000, 0, -1))  # the ceiling: no value has an answer
_INCREASING = list(range(1, 1001))  # every answer is the very next value

CASES: Sequence[Tuple[List[int], List[int], List[int]]] = (
    ([4, 1, 2], [1, 3, 4, 2], [-1, 3, -1]),
    ([2, 4], [1, 2, 3, 4], [3, -1]),
    ([1], [1], [-1]),  # a single value has nothing to its right
    ([3, 1], [1, 3], [-1, 3]),  # nums1 order differs from nums2 order
    ([2], [2, 1, 3], [3]),  # skips a smaller value to reach the answer
    ([1, 3, 5, 2, 4], [6, 5, 4, 3, 2, 1, 7], [7, 7, 7, 7, 7]),  # one value answers all
    ([5], [5, 6, 9], [6]),  # nearest bigger, not the largest
    ([0], [0, 10000], [10000]),  # value bounds
    ([3], [1, 2, 3], [-1]),  # the query is last in nums2
    ([2], [4, 2], [-1]),  # bigger values to the left do not count
    (_DECREASING[::7], _DECREASING, [-1] * len(_DECREASING[::7])),
    (_INCREASING[::-3], _INCREASING, [v + 1 if v < 1000 else -1 for v in _INCREASING[::-3]]),
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(
            solve(list(nums1), list(nums2)) == expected for nums1, nums2, expected in CASES
        )
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()
