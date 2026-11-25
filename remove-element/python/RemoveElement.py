"""Remove Element (LeetCode 27).

Given an integer array `nums` and an integer `val`, remove every occurrence of
`val` in place and return k, the number of elements left. The first k slots of
`nums` must hold the kept elements, in any order. What's past slot k doesn't
matter.

The three approaches from https://neetcode.io/solutions/remove-element,
ordered as NeetCode presents them. All are O(n); they differ in extra space
and in how many writes they make.
"""

from typing import Callable, List, Sequence, Tuple


def remove_element_brute_force(nums: List[int], val: int) -> int:
    """Copy the keepers into a new list, then copy them back.
    Time: O(n). Space: O(n) for the copy.

    Correct, but it misses the point of the problem, which is to do it in
    place with O(1) extra space.
    """
    kept = [x for x in nums if x != val]
    for i, x in enumerate(kept):
        nums[i] = x
    return len(kept)


def remove_element_two_pointers(nums: List[int], val: int) -> int:
    """Read every element and write each keeper to the next free slot.
    Time: O(n). Space: O(1).

    k is the next slot to write. It never passes i, the slot being read, so a
    write can only overwrite something already read. Keepers stay in their
    original order.
    """
    k = 0
    for i in range(len(nums)):
        if nums[i] != val:
            nums[k] = nums[i]
            k += 1
    return k


def remove_element_two_pointers_swap(nums: List[int], val: int) -> int:
    """Overwrite each `val` with the last element and shrink the array.
    Time: O(n). Space: O(1).

    n is the length of the live part, nums[:n]. When nums[i] == val, move
    nums[n - 1] into slot i and drop the last slot. Don't advance i: the
    element just moved in hasn't been checked yet and might be `val` too.

    Writes one element per `val` found instead of one per keeper, so it's
    faster when `val` is rare. The price is that keepers get reordered.
    """
    i, n = 0, len(nums)
    while i < n:
        if nums[i] == val:
            n -= 1
            nums[i] = nums[n]
        else:
            i += 1
    return n


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int], int], int]], ...] = (
    ("brute force", remove_element_brute_force),
    ("two pointers", remove_element_two_pointers),
    ("two pointers (swap)", remove_element_two_pointers_swap),
)

CASES: Sequence[Tuple[List[int], int, List[int]]] = (
    ([1, 1, 2, 3, 4], 1, [2, 3, 4]),
    ([0, 1, 2, 2, 3, 0, 4, 2], 2, [0, 1, 3, 0, 4]),
    ([3, 2, 2, 3], 3, [2, 2]),
    ([], 0, []),  # empty input
    ([1], 1, []),  # the only element is removed
    ([1], 2, [1]),  # the only element stays
    ([4, 4, 4, 4], 4, []),  # everything is removed
    ([1, 2, 3, 4], 5, [1, 2, 3, 4]),  # val isn't there
    ([2, 1, 1, 1], 1, [2]),  # swap: the element moved in is val again
    ([1, 2, 3, 4], 1, [2, 3, 4]),  # val only at the front
    ([1, 2, 3, 4], 4, [1, 2, 3]),  # val only at the back
    ([5, 1, 5, 2, 5], 5, [1, 2]),  # val alternates with keepers
    ([0, 50] * 50, 50, [0] * 50),  # n = 100, the upper bound
    ([7] * 100, 100, [7] * 100),  # val = 100, above every nums[i]
)


def check(solve: Callable[[List[int], int], int], nums: List[int], val: int, expected: List[int]) -> bool:
    """Judge the way LeetCode does: k must match, and nums[:k] must hold the
    kept elements in any order."""
    k = solve(nums, val)
    return k == len(expected) and sorted(nums[:k]) == sorted(expected)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(check(solve, list(nums), val, expected) for nums, val, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")

    # Only the swap approach may reorder the keepers.
    for name, solve in SOLUTIONS[:2]:
        nums = [3, 1, 2, 1, 4]
        k = solve(nums, 1)
        status = "PASS" if nums[:k] == [3, 2, 4] else "FAIL"
        print(f"{status}  {name} keeps order: {nums[:k]}")


if __name__ == "__main__":
    main()
