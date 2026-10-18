"""Monotonic Array (LeetCode 896).

An array is monotonic if it is either monotone increasing (never goes down) or
monotone decreasing (never goes up). Equal neighbours are allowed in both.
Return whether nums is monotonic.

The three approaches from https://neetcode.io/solutions/monotonic-array, plus a
sorting baseline, ordered from brute force to optimal and then the variant.
"""

from typing import Callable, List, Sequence, Tuple


def is_monotonic_sorting(nums: List[int]) -> bool:
    """Compare against both sorted orders. Time: O(n log n). Space: O(n).

    A monotone increasing array is already in sorted order, and a monotone
    decreasing one is in reverse sorted order, so this is the definition
    written in one line. It sorts a copy, twice, to answer a question that
    only needs one look at each adjacent pair.
    """
    return nums == sorted(nums) or nums == sorted(nums, reverse=True)


def is_monotonic_two_pass(nums: List[int]) -> bool:
    """Check never-goes-down, then never-goes-up. Time: O(n). Space: O(1).

    The definition compares every pair i <= j, but adjacent pairs are enough:
    if each step is <=, the whole chain is, by transitivity. So each direction
    is one linear scan that stops at the first step going the wrong way. An
    array that fails the first check is scanned a second time.
    """
    increasing = True
    for i in range(1, len(nums)):
        if nums[i] < nums[i - 1]:
            increasing = False
            break
    if increasing:
        return True

    for i in range(1, len(nums)):
        if nums[i] > nums[i - 1]:
            return False
    return True


def is_monotonic_first_vs_last(nums: List[int]) -> bool:
    """Pick the only possible direction from the ends, then check it.
    Time: O(n). Space: O(1).

    The optimal solution. The first and last values decide which direction is
    even possible. If nums[0] < nums[-1], the array can't be decreasing. If
    nums[0] > nums[-1], it can't be increasing. If they are equal, a monotonic
    array must be constant, which also counts as increasing. So one scan,
    checking one direction, is enough, and it stops at the first bad step.
    """
    if nums[0] <= nums[-1]:
        return all(nums[i - 1] <= nums[i] for i in range(1, len(nums)))
    return all(nums[i - 1] >= nums[i] for i in range(1, len(nums)))


def is_monotonic_two_flags(nums: List[int]) -> bool:
    """Track both directions in one pass. Time: O(n). Space: O(1).

    Start assuming both directions hold and rule each out as soon as a step
    contradicts it. A step going up rules out decreasing, a step going down
    rules out increasing, and an equal step rules out neither. NeetCode's
    version always walks to the end; this one returns as soon as both are
    ruled out, which on [1, 3, 2, ...] is after two steps.
    """
    increasing = decreasing = True
    for i in range(1, len(nums)):
        if nums[i] > nums[i - 1]:
            decreasing = False
        elif nums[i] < nums[i - 1]:
            increasing = False
        if not increasing and not decreasing:
            return False
    return True


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int]], bool]], ...] = (
    ("sorting", is_monotonic_sorting),
    ("two pass", is_monotonic_two_pass),
    ("first vs last", is_monotonic_first_vs_last),
    ("two flags", is_monotonic_two_flags),
)

CASES: Sequence[Tuple[List[int], bool]] = (
    ([1, 2, 2, 3], True),
    ([6, 5, 4, 4], True),
    ([1, 3, 2], False),
    ([1], True),  # a single element is both
    ([5, 5, 5], True),  # constant: both directions hold
    ([1, 1, 2], True),  # equal steps first, then a rise
    ([2, 2, 1], True),
    ([1, 2, 1], False),  # first == last, but not constant
    ([3, 1, 2, 3], False),  # first == last, dips in the middle
    ([1, 2, 3, 0, 4], False),  # first < last, one step down in the middle
    ([4, 3, 5, 2, 1], False),  # first > last, one step up in the middle
    ([1, 1, 1, 2, 1], False),  # breaks only at the very end
    ([-100000, 100000], True),  # value bounds
    (list(range(100000)), True),  # the length ceiling, increasing
    (list(range(100000, 0, -1)), True),  # and decreasing
    (list(range(99999)) + [0], False),  # the ceiling, broken by the last step
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(list(nums)) == expected for nums, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()
