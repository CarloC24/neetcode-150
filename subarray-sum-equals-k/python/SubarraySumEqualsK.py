"""Subarray Sum Equals K (LeetCode 560).

Given an array of integers nums and an integer k, return how many subarrays
(contiguous, non-empty) sum to k. Values can be negative.

The two approaches from https://neetcode.io/solutions/subarray-sum-equals-k.
"""

import random
from collections import defaultdict
from typing import Callable, Dict, List, Sequence, Tuple


def subarray_sum_brute_force(nums: List[int], k: int) -> int:
    """Try every start, extend to every end. Time: O(n^2). Space: O(1).

    For each start i, keep a running total as the end j moves right, so each
    subarray's sum costs O(1) instead of a fresh loop. Don't stop when the
    total passes k: with negative values it can come back down.
    """
    res = 0
    for i in range(len(nums)):
        total = 0
        for j in range(i, len(nums)):
            total += nums[j]
            if total == k:
                res += 1
    return res


def subarray_sum_prefix_map(nums: List[int], k: int) -> int:
    """Prefix sums, with a hash map of how often each one has occurred.
    Time: O(n). Space: O(n).

    Let prefix be the sum of everything so far. A subarray that ends here
    and sums to k starts right after some earlier point where the prefix sum
    was prefix - k. So the number of such subarrays is the number of earlier
    points with that prefix sum, and a hash map counts them.

    Two details:
    - The map starts as {0: 1}: the empty prefix before index 0. Without it,
      subarrays that start at index 0 are never counted.
    - Look up prefix - k before adding the current prefix. Otherwise, with
      k = 0, the current point pairs with itself: an empty subarray.
    """
    res = prefix = 0
    seen: Dict[int, int] = defaultdict(int)
    seen[0] = 1  # the empty prefix
    for num in nums:
        prefix += num
        # .get, not seen[...]: indexing a defaultdict inserts missing keys,
        # which would add a useless 0 entry for every miss.
        res += seen.get(prefix - k, 0)  # subarrays ending here that sum to k
        seen[prefix] += 1
    return res


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int], int], int], bool], ...] = (
    # (name, function, runs the 20,000-element cases)
    ("brute force", subarray_sum_brute_force, False),
    ("prefix sums + hash map", subarray_sum_prefix_map, True),
)

_rng = random.Random(560)
_N = 20_000

# (nums, k, expected). Expected is None where it's checked against brute force.
SHORT: Sequence[Tuple[List[int], int, object]] = (
    ([1, 1, 1], 2, 2),  # LeetCode example 1
    ([1, 2, 3], 3, 2),  # LeetCode example 2
    ([2, -1, 1, 2], 2, 4),  # NeetCode example 1
    ([4, 4, 4, 4, 4, 4], 4, 6),  # NeetCode example 2
    ([1], 1, 1),  # breaks a map that doesn't start with {0: 1}
    ([1], 0, 0),  # breaks adding the prefix before looking it up
    ([0, 0, 0], 0, 6),  # every subarray sums to 0; also breaks a sliding window
    ([1, -1, 1], 1, 3),  # breaks a sliding window: negatives
    ([-1, -1], -2, 1),  # the smallest input that breaks a sliding window
    ([-1, -1, 1], 0, 1),
    ([3, 4, 7, 2, -3, 1, 4, 2], 7, 4),
    ([5], 7, 0),  # nothing sums to k
    # random arrays with small values, so many subarrays hit k (seeded)
    *(([_rng.randint(-3, 3) for _ in range(300)], _rng.randint(-5, 5), None) for _ in range(5)),
    ([_rng.randint(-1_000, 1_000) for _ in range(2_000)], 0, None),
)

LONG: Sequence[Tuple[List[int], int, int]] = (
    # every one of the 20,000 * 20,001 / 2 subarrays sums to 0
    ([0] * _N, 0, 200_010_000),
    # prefix sums alternate 1, 0, 1, 0: C(10,001, 2) + C(10,000, 2) pairs
    ([1, -1] * (_N // 2), 0, 100_000_000),
    # windows of 1,000 ones: 20,000 - 1,000 + 1 of them
    ([1] * _N, 1_000, 19_001),
    # value and k limits: only the whole array reaches 2 * 10^7
    ([1_000] * _N, 20_000_000, 1),
    # windows of 10,000 values of -1,000: 10,001 of them
    ([-1_000] * _N, -10_000_000, 10_001),
)


def main() -> None:
    short = [
        (nums, k, subarray_sum_brute_force(nums, k) if expected is None else expected)
        for nums, k, expected in SHORT
    ]
    for name, solve, runs_long in SOLUTIONS:
        cases = short + (list(LONG) if runs_long else [])
        passed = sum(solve(list(nums), k) == expected for nums, k, expected in cases)
        status = "PASS" if passed == len(cases) else "FAIL"
        note = "" if runs_long else f" (skips the {len(LONG)} long cases)"
        print(f"{status}  {name}: {passed}/{len(cases)} cases{note}")


if __name__ == "__main__":
    main()
