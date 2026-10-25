"""Majority Element (LeetCode 169).

Given an array nums of size n, return the element that appears more than
n // 2 times. The majority element is guaranteed to exist.

The six approaches from https://neetcode.io/solutions/majority-element,
ordered from brute force to optimal.
"""

import random
from typing import Callable, Dict, List, Sequence, Tuple


def majority_element_brute_force(nums: List[int]) -> int:
    """Count every element's occurrences. Time: O(n^2). Space: O(1).

    For each element, scan the whole array to count it, and return the first
    one whose count is over n // 2. The guarantee means the loop always
    returns.
    """
    n = len(nums)
    for num in nums:
        count = sum(1 for x in nums if x == num)
        if count > n // 2:
            return num
    raise ValueError("no majority element")


def majority_element_hash_map(nums: List[int]) -> int:
    """Count with a dict, tracking the most frequent so far.
    Time: O(n). Space: O(n).

    Only the majority can ever have a count over n // 2, so the element with
    the highest count at the end is the answer. Tracking it during the pass
    saves a second loop over the dict.
    """
    count: Dict[int, int] = {}
    res = max_count = 0
    for num in nums:
        count[num] = count.get(num, 0) + 1
        if count[num] > max_count:
            res, max_count = num, count[num]
    return res


def majority_element_sorting(nums: List[int]) -> int:
    """Sort and return the middle element. Time: O(n log n). Space: O(n).

    The majority fills more than half the sorted array, so its run covers
    index n // 2 wherever the run starts. Sorts the input in place; Python's
    Timsort needs up to n / 2 slots of scratch space.
    """
    nums.sort()
    return nums[len(nums) // 2]


def majority_element_bits(nums: List[int]) -> int:
    """Build the answer one bit at a time. Time: O(32n). Space: O(1).

    For each of the 32 bit positions, the majority's bit is whichever value
    more than half the numbers have there, because the majority alone is more
    than half. Values fit in a 32-bit signed int, so bit 31 is the sign bit:
    it is worth -2^31, not +2^31, in two's complement.

    Python's >> on a negative int shifts in copies of the sign bit forever, so
    (num >> i) & 1 reads the same bits as a 32-bit two's complement number for
    every i < 32.
    """
    n = len(nums)
    bit = [0] * 32
    for num in nums:
        for i in range(32):
            bit[i] += (num >> i) & 1
    res = 0
    for i in range(32):
        if bit[i] > n // 2:
            res += -(1 << i) if i == 31 else (1 << i)
    return res


def majority_element_boyer_moore(nums: List[int]) -> int:
    """Boyer-Moore voting. Time: O(n). Space: O(1).

    The optimal solution. Keep one candidate and a count. A matching element
    votes for the candidate (+1), any other element votes against it (-1),
    and at count 0 the next element becomes the candidate.

    Each -1 cancels one copy of the candidate against one other element.
    Cancelling a pair of different values never removes the majority's lead,
    because it loses at most one copy per pair while the other side loses one
    too. The majority has more copies than everything else combined, so it is
    the one left standing. See the README.
    """
    res = count = 0
    for num in nums:
        if count == 0:
            res = num
        count += 1 if num == res else -1
    return res


_RNG = random.Random(169)


def majority_element_random(nums: List[int]) -> int:
    """Pick a random element and check it. Time: O(n) expected. Space: O(1).

    More than half the elements are the majority, so each pick succeeds with
    probability over 1/2, and the expected number of picks is under 2. Each
    check costs O(n). The worst case is unbounded, but the chance of needing
    more than 20 picks is under one in a million.

    The module-level seeded generator makes the test run repeatable. The answer doesn't
    depend on the seed, only the number of picks does.
    """
    n = len(nums)
    while True:
        candidate = _RNG.choice(nums)
        if nums.count(candidate) > n // 2:
            return candidate


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int]], int]], ...] = (
    ("brute force", majority_element_brute_force),
    ("hash map", majority_element_hash_map),
    ("sorting", majority_element_sorting),
    ("bit manipulation", majority_element_bits),
    ("boyer-moore", majority_element_boyer_moore),
    ("randomization", majority_element_random),
)

MUTATES_INPUT = {"sorting"}

CASES: Sequence[Tuple[List[int], int]] = (
    ([5, 5, 1, 1, 1, 5, 5], 5),
    ([2, 2, 2], 2),
    ([3, 2, 3], 3),
    ([7], 7),  # the smallest input
    ([1, 2, 1], 1),  # the candidate is lost after 2, then won back
    ([1, 2, 3, 4, 4, 4, 4], 4),  # majority all at the end
    ([4, 4, 4, 4, 1, 2, 3], 4),  # majority all at the start
    ([2, 1, 2, 3, 2], 2),  # 3 of 5: the bare minimum over n // 2
    ([-1, -1, 2], -1),  # negative: bit 31 is set and must count as -2^31
    ([0, 0, -5], 0),  # zero is a valid answer, not "not found"
    ([-10**9, 10**9, -10**9], -10**9),  # value bounds
    ([10**9, -10**9, 10**9], 10**9),
    ([6] * 25_000 + [9] * 24_999, 6),  # length ceiling, the narrowest win
    ([8, 3] * 12_500 + [8], 8),  # ceiling, alternating; Boyer-Moore's count hovers at 0
    ([3] * 1_000 + [5] * 1_001, 5),  # brute force's worst case: 1,000 full scans first
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(list(nums)) == expected for nums, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")

    original = [5, 5, 1, 1, 1, 5, 5]
    for name, solve in SOLUTIONS:
        if name not in MUTATES_INPUT:
            solve(original)
    status = "PASS" if original == [5, 5, 1, 1, 1, 5, 5] else "FAIL"
    print(f"{status}  input left unmodified (all but sorting): {original}")


if __name__ == "__main__":
    main()
