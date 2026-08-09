"""Contains Duplicate (LeetCode 217).

Given an integer array, return True if any value appears more than once.

The four approaches from https://neetcode.io/solutions/contains-duplicate,
ordered from brute force to optimal.
"""

from typing import Callable, List, Sequence, Tuple


def has_duplicate_brute_force(nums: List[int]) -> bool:
    """Compare every pair of elements. Time: O(n^2). Space: O(1).

    The inner loop starts at i + 1, not i, so an element is never compared
    against itself -- that mistake would report a duplicate for every input.
    """
    for i in range(len(nums)):
        for j in range(i + 1, len(nums)):
            if nums[i] == nums[j]:
                return True
    return False


def has_duplicate_sorting(nums: List[int]) -> bool:
    """Sort, then look for equal neighbors. Time: O(n log n). Space: O(n).

    Duplicates land next to each other once sorted. Uses sorted() rather than
    nums.sort() so the caller's list keeps its original order.
    """
    ordered = sorted(nums)
    for i in range(1, len(ordered)):
        if ordered[i] == ordered[i - 1]:
            return True
    return False


def has_duplicate_hash_set(nums: List[int]) -> bool:
    """Remember values already seen. Time: O(n). Space: O(n).

    The optimal solution: set lookups are O(1), and it returns on the first
    repeat instead of always scanning the whole array.
    """
    seen = set()
    for num in nums:
        if num in seen:
            return True
        seen.add(num)
    return False


def has_duplicate_set_length(nums: List[int]) -> bool:
    """Compare the set's size to the list's. Time: O(n). Space: O(n).

    A set holds only unique values, so a smaller set means something was
    dropped. Shortest to write, but it always builds the full set -- no early
    exit on an array whose first two elements already match.
    """
    return len(set(nums)) < len(nums)


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int]], bool]], ...] = (
    ("brute force", has_duplicate_brute_force),
    ("sorting", has_duplicate_sorting),
    ("hash set", has_duplicate_hash_set),
    ("set length", has_duplicate_set_length),
)

CASES: Sequence[Tuple[List[int], bool]] = (
    ([1, 2, 3, 3], True),
    ([1, 2, 3, 4], False),
    ([], False),
    ([7], False),
    ([0, 0], True),
    ([-1, -1], True),
    ([-(10**9), 10**9, 0], False),
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(list(nums)) == expected for nums, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")

    original = [3, 1, 2]
    for _, solve in SOLUTIONS:
        solve(original)
    status = "PASS" if original == [3, 1, 2] else "FAIL"
    print(f"{status}  input left unmodified: {original}")


if __name__ == "__main__":
    main()
