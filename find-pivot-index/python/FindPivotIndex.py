"""Find Pivot Index (LeetCode 724).

Return the leftmost index where the sum of the numbers strictly to its left
equals the sum of the numbers strictly to its right, or -1 if there is none.

The three approaches from https://neetcode.io/solutions/find-pivot-index,
ordered from brute force to optimal.
"""

from typing import Callable, List, Sequence, Tuple


def pivot_index_brute_force(nums: List[int]) -> int:
    """Sum both sides from scratch at every index. Time: O(n^2). Space: O(1).

    Index i costs O(n) to sum nums[:i] and nums[i + 1:], so n indexes cost
    O(n^2). Scanning left to right returns the leftmost pivot.
    """
    n = len(nums)
    for i in range(n):
        left_sum = right_sum = 0
        for l in range(i):
            left_sum += nums[l]
        for r in range(i + 1, n):
            right_sum += nums[r]
        if left_sum == right_sum:
            return i
    return -1


def pivot_index_prefix_array(nums: List[int]) -> int:
    """Precompute prefix sums, then read both sides in O(1).
    Time: O(n). Space: O(n).

    prefix[i] is the sum of nums[:i], so prefix[0] = 0 and prefix[n] is the
    total. The left side of i is prefix[i], and the right side is everything
    after nums[i]: prefix[n] - prefix[i + 1]. The extra slot at the front
    means index 0 needs no special case.
    """
    n = len(nums)
    prefix = [0] * (n + 1)
    for i in range(n):
        prefix[i + 1] = prefix[i] + nums[i]

    for i in range(n):
        left_sum = prefix[i]
        right_sum = prefix[n] - prefix[i + 1]
        if left_sum == right_sum:
            return i
    return -1


def pivot_index_running_sum(nums: List[int]) -> int:
    """Keep a running left sum; derive the right sum from the total.
    Time: O(n). Space: O(1).

    The optimal solution. The array splits into left + nums[i] + right =
    total, so right = total - left - nums[i]. Only the left sum needs
    tracking, and the prefix array from approach 2 disappears.

    Compare first, then add nums[i] to the left sum. Adding first would put
    nums[i] on its own left side.
    """
    total = sum(nums)
    left_sum = 0
    for i, num in enumerate(nums):
        right_sum = total - left_sum - num
        if left_sum == right_sum:
            return i
        left_sum += num
    return -1


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int]], int]], ...] = (
    ("brute force", pivot_index_brute_force),
    ("prefix array", pivot_index_prefix_array),
    ("running sum", pivot_index_running_sum),
)

CASES: Sequence[Tuple[List[int], int]] = (
    ([1, 7, 3, 6, 5, 6], 3),
    ([3, 2, 1], -1),
    ([2, 1, -1], 0),  # pivot at index 0: the empty left side sums to 0
    ([1, -1, 5], 2),  # pivot at the last index: the empty right side sums to 0
    ([5], 0),  # the smallest input: both sides are empty
    ([0, 0, 0], 0),  # every index is a pivot; return the leftmost
    ([2, -3, 4, -3, 2], 0),  # pivots at 0, 2 and 4
    ([1, 0, 1, 0], 1),
    ([-1, -1, 0, 1, 1, 0], 5),  # the left sum falls, then rises: sums aren't monotone
    ([-1, -1, -1, -1, -1, 0], 2),  # all non-positive
    ([1000] * 1_001, 500),  # long and odd: the middle balances
    ([1000] * 1_000, -1),  # even: no single middle, nothing balances
    ([-1000] * 1_001, 500),  # value floor
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(list(nums)) == expected for nums, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")

    original = [1, 7, 3, 6, 5, 6]
    for _, solve in SOLUTIONS:
        solve(original)
    status = "PASS" if original == [1, 7, 3, 6, 5, 6] else "FAIL"
    print(f"{status}  input left unmodified: {original}")


if __name__ == "__main__":
    main()
