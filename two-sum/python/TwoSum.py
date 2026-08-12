"""Two Sum (LeetCode 1).

Given an integer array nums and an integer target, return the two indices i and
j such that nums[i] + nums[j] == target, with i != j. Exactly one such pair
exists.

The four approaches from https://neetcode.io/solutions/two-sum, ordered from
brute force to optimal.
"""

from typing import Callable, List, Sequence, Tuple


def two_sum_brute_force(nums: List[int], target: int) -> List[int]:
    """Try every pair. Time: O(n^2). Space: O(1).

    The inner loop starts at i + 1, which does double duty: it skips pairs
    already tried, and it enforces i != j so an element is never added to
    itself. On nums = [3, 2, 4], target = 6, starting at i would match 3 + 3
    and wrongly return [0, 0].
    """
    for i in range(len(nums)):
        for j in range(i + 1, len(nums)):
            if nums[i] + nums[j] == target:
                return [i, j]
    return []


def two_sum_two_pointers(nums: List[int], target: int) -> List[int]:
    """Sort, then walk inward from both ends. Time: O(n log n). Space: O(n).

    Once sorted, a sum that is too small can only grow by moving the left
    pointer right, and one that is too large can only shrink by moving the
    right pointer left -- so each step discards a value that cannot be part of
    the answer, and neither pointer ever needs to backtrack.

    Sorting destroys the original positions, so each value carries its index
    along as [value, index]. That pairing is also what makes this O(n) space,
    and it is why the answer is rebuilt with min/max: sorted order says nothing
    about which index came first.
    """
    indexed = sorted([num, i] for i, num in enumerate(nums))

    left, right = 0, len(indexed) - 1
    while left < right:
        total = indexed[left][0] + indexed[right][0]
        if total == target:
            return [
                min(indexed[left][1], indexed[right][1]),
                max(indexed[left][1], indexed[right][1]),
            ]
        if total < target:
            left += 1
        else:
            right -= 1
    return []


def two_sum_hash_map_two_pass(nums: List[int], target: int) -> List[int]:
    """Index every value, then look up each complement. Time: O(n). Space: O(n).

    The explicit indices[diff] != i check is what keeps a value from pairing
    with itself -- on nums = [3, 2, 4], target = 6, the complement of 3 is 3,
    found at its own index, and must be rejected.

    Duplicate values collapse in the map, which keeps only the last index for
    each. That is harmless here: if the answer needs a value twice, the stored
    index is the later occurrence and the scan reaches the earlier one first.
    """
    indices = {num: i for i, num in enumerate(nums)}

    for i, num in enumerate(nums):
        diff = target - num
        if diff in indices and indices[diff] != i:
            return [i, indices[diff]]
    return []


def two_sum_hash_map_one_pass(nums: List[int], target: int) -> List[int]:
    """Look back while building the map. Time: O(n). Space: O(n).

    The optimal solution. Each value checks the complement against the values
    already behind it, then records itself. Storing only after the check is
    what makes the i != j test unnecessary -- a value cannot be its own
    complement because it is not in the map yet when it looks.
    """
    seen = {}
    for i, num in enumerate(nums):
        diff = target - num
        if diff in seen:
            return [seen[diff], i]
        seen[num] = i
    return []


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int], int], List[int]]], ...] = (
    ("brute force", two_sum_brute_force),
    ("two pointers", two_sum_two_pointers),
    ("hash map (two pass)", two_sum_hash_map_two_pass),
    ("hash map (one pass)", two_sum_hash_map_one_pass),
)

# Every case has exactly one valid pair, as the constraints promise -- otherwise
# approaches returning different-but-correct answers could not be compared.
CASES: Sequence[Tuple[List[int], int, List[int]]] = (
    ([3, 4, 5, 6], 7, [0, 1]),
    ([4, 5, 6], 10, [0, 2]),
    ([5, 5], 10, [0, 1]),  # the pair is two copies of one value
    ([2, 7, 11, 15], 9, [0, 1]),
    ([3, 2, 4], 6, [1, 2]),  # target is 2x nums[0]; must not return [0, 0]
    ([1, 2, 3, 4, 5], 9, [3, 4]),  # answer at the very end, no early exit
    ([-1, -2, -3, -4, -5], -8, [2, 4]),  # all negative
    ([0, 4, 3, 0], 0, [0, 3]),  # duplicate zeros, target 0
    ([-10000000, 10000000], 0, [0, 1]),  # value bounds
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(
            solve(list(nums), target) == expected for nums, target, expected in CASES
        )
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")

    original = [3, 2, 4]
    for _, solve in SOLUTIONS:
        solve(original, 6)
    status = "PASS" if original == [3, 2, 4] else "FAIL"
    print(f"{status}  input left unmodified: {original}")


if __name__ == "__main__":
    main()
