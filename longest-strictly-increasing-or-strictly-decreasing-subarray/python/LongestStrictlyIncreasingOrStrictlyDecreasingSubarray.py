"""Longest Strictly Increasing or Strictly Decreasing Subarray (LeetCode 3105).

Given an array of integers, return the length of the longest subarray that is
either strictly increasing or strictly decreasing.

The four approaches from
https://neetcode.io/solutions/longest-strictly-increasing-or-strictly-decreasing-subarray,
ordered from brute force to optimal.
"""

from typing import Callable, List, Sequence, Tuple


def longest_brute_force(nums: List[int]) -> int:
    """Extend a run from every start. Time: O(n^2). Space: O(1).

    The first step after i fixes the run's direction; each later step must
    match it, and an equal pair ends the run in either direction. A run of one
    element is always valid, so the answer starts at 1 -- which is also why a
    one-element array needs no special case: the outer loop never runs.
    """
    n = len(nums)
    res = 1
    for i in range(n - 1):
        increasing = nums[i] < nums[i + 1]
        cur_len = 1
        for j in range(i + 1, n):
            if nums[j] == nums[j - 1] or (nums[j - 1] < nums[j]) != increasing:
                break
            cur_len += 1
        res = max(res, cur_len)
    return res


def longest_direction_flag(nums: List[int]) -> int:
    """One pass with a run length and a direction flag. Time: O(n). Space: O(1).

    direction is 1 while the current run is going up, -1 while going down, and
    0 right after an equal pair. When the direction flips, the new run is the
    two elements either side of the flip, so it restarts at 2, not 1: the last
    element of the old run is the first of the new one.
    """
    cur = res = 1
    direction = 0
    for i in range(1, len(nums)):
        if nums[i - 1] < nums[i]:
            cur = cur + 1 if direction > 0 else 2
            direction = 1
        elif nums[i - 1] > nums[i]:
            cur = cur + 1 if direction < 0 else 2
            direction = -1
        else:
            cur = 1
            direction = 0
        res = max(res, cur)
    return res


def longest_two_counters(nums: List[int]) -> int:
    """Track the increasing and the decreasing run at once.
    Time: O(n). Space: O(1).

    The optimal solution, and the simplest. inc is the length of the longest
    strictly increasing run ending at i, dec the same for decreasing. Each step
    extends one of them and resets the other to 1; an equal pair resets both.
    No direction flag, and no restart-at-2 rule to remember: the reset to 1
    plus the extension on the next step produce it automatically.
    """
    inc = dec = res = 1
    for i in range(1, len(nums)):
        if nums[i] > nums[i - 1]:
            inc, dec = inc + 1, 1
        elif nums[i] < nums[i - 1]:
            inc, dec = 1, dec + 1
        else:
            inc = dec = 1
        res = max(res, inc, dec)
    return res


def longest_direction_from_start(nums: List[int]) -> int:
    """One pass, reading the run's direction from where it started.
    Time: O(n). Space: O(1).

    Instead of storing a direction, look it up: the current run starts at
    i - cur_len, so its first step is nums[i - cur_len] -> nums[i - cur_len + 1].
    When cur_len is 1 that first step is the step being tested, so it always
    agrees with itself and the run extends to 2.

    On a flip or an equal pair it skips the res update. That is safe: a flip
    restarts at 2, and a flip can only happen once cur_len was already at
    least 2, so res is already at least 2. Clever, but the index arithmetic is
    the hardest of the four to check by eye.
    """
    cur_len = res = 1
    for i in range(1, len(nums)):
        start_up = nums[i - cur_len] < nums[i - cur_len + 1]
        if nums[i] == nums[i - 1] or start_up != (nums[i - 1] < nums[i]):
            cur_len = 1 if nums[i] == nums[i - 1] else 2
            continue
        cur_len += 1
        res = max(res, cur_len)
    return res


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int]], int]], ...] = (
    ("brute force", longest_brute_force),
    ("direction flag", longest_direction_flag),
    ("two counters", longest_two_counters),
    ("direction from start", longest_direction_from_start),
)

CASES: Sequence[Tuple[List[int], int]] = (
    ([1, 4, 3, 3, 2], 2),
    ([3, 3, 3, 3], 1),  # every pair equal: only single elements qualify
    ([3, 2, 1], 3),  # the whole array, decreasing
    ([1], 1),  # a single element is a run of one
    ([1, 2], 2),
    ([2, 2], 1),
    ([1, 3, 2, 4, 3], 2),  # zigzag: every run is two long
    ([1, 2, 3, 2, 1], 3),  # the peak is shared by both runs
    ([5, 4, 3, 3, 4, 5, 6], 4),  # the equal pair resets; the later run is longer
    ([1, 2, 2, 3, 4], 3),  # equal pair in the middle of a rising stretch
    ([3, 1, 2, 3, 4], 4),  # flip to increasing restarts at 2, not 1
    ([1, 2, 3, 4, 3, 2, 1, 0], 5),  # the decreasing run starts at the peak
    (list(range(1, 51)), 50),  # the ceiling: 50 values, all increasing
    (list(range(50, 0, -1)), 50),  # and all decreasing
    ([50] * 50, 1),
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(list(nums)) == expected for nums, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()
