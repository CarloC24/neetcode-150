"""Sort Colors (LeetCode 75).

Given an array nums of 0s (red), 1s (white) and 2s (blue), sort it in place
so equal colors are adjacent, in the order 0, 1, 2. The library sort is not
allowed.

The five approaches from https://neetcode.io/solutions/sort-colors,
ordered from the library sort to one-pass three-pointer partitions.
"""

import random
from typing import Callable, List, Sequence, Tuple


def sort_colors_builtin(nums: List[int]) -> None:
    """Call the library sort. Time: O(n log n). Space: O(n) in Python.

    The baseline the problem rules out. It's here as the reference the other
    four are checked against, not as an answer. Python's Timsort needs up to
    n / 2 slots of scratch space.
    """
    nums.sort()


def sort_colors_counting(nums: List[int]) -> None:
    """Count each color, then rewrite the array. Time: O(n). Space: O(1).

    Counting sort with three buckets. It takes two passes: one to count and
    one to write. The follow-up asks for a single pass, which the next three
    approaches manage.
    """
    count = [0] * 3
    for num in nums:
        count[num] += 1

    index = 0
    for color in range(3):
        for _ in range(count[color]):
            nums[index] = color
            index += 1


def sort_colors_dutch_flag(nums: List[int]) -> None:
    """Dutch national flag partition, swapping from both ends.
    Time: O(n). Space: O(1).

    The classic one-pass answer. Three pointers split the array into four
    regions:

        [0, l)      all 0s
        [l, i)      all 1s
        [i, r]      not looked at yet
        (r, n)      all 2s

    A 0 at i swaps to l. What comes back from l is a 1 (or the same 0, when
    l == i), which was already checked, so i moves on. A 2 at i swaps to r,
    but what comes back from r hasn't been looked at, so i must stay put.
    The loop runs while i <= r: index r is still unknown. See the README.
    """
    l, i, r = 0, 0, len(nums) - 1
    while i <= r:
        if nums[i] == 0:
            nums[l], nums[i] = nums[i], nums[l]
            l += 1
            i += 1
        elif nums[i] == 2:
            nums[i], nums[r] = nums[r], nums[i]
            r -= 1  # i stays: the value swapped in from r is unchecked
        else:
            i += 1


def sort_colors_cascade(nums: List[int]) -> None:
    """Grow three regions from the left, writing instead of swapping.
    Time: O(n). Space: O(1).

    zero, one and two are where the 0s, 1s and 2s regions end, so the array
    so far is [0, zero) 0s, [zero, one) 1s, [one, two) 2s. Each new value
    pushes the regions at and after its color one slot right: write a 2 at
    the end of the 2s region, a 1 where the 1s end, a 0 where the 0s end, in
    that order. Writing back to front means each write lands on a slot the
    next region has just claimed, so nothing that's needed is lost.

    nums[i] is read before any of the writes. The 2 written at two (== i)
    overwrites it, which is fine because the branch has been chosen already.
    """
    zero = one = two = 0
    for i in range(len(nums)):
        if nums[i] == 0:
            nums[two] = 2
            nums[one] = 1
            nums[zero] = 0
            two += 1
            one += 1
            zero += 1
        elif nums[i] == 1:
            nums[two] = 2
            nums[one] = 1
            two += 1
            one += 1
        else:
            nums[two] = 2
            two += 1


def sort_colors_cascade_short(nums: List[int]) -> None:
    """The cascade with the branches folded together.
    Time: O(n). Space: O(1).

    The 2s region always ends at the loop index, so two is the loop variable.
    Every value writes a 2. Anything below 2 also writes a 1, and anything
    below 1 also writes a 0. Saving the value in tmp first is required: the
    2 written at index two would otherwise erase it.
    """
    zero = one = 0
    for two in range(len(nums)):
        tmp = nums[two]
        nums[two] = 2
        if tmp < 2:
            nums[one] = 1
            one += 1
        if tmp < 1:
            nums[zero] = 0
            zero += 1


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int]], None]], ...] = (
    ("built-in sort", sort_colors_builtin),
    ("counting sort", sort_colors_counting),
    ("dutch flag", sort_colors_dutch_flag),
    ("cascade", sort_colors_cascade),
    ("cascade, short", sort_colors_cascade_short),
)

_rng = random.Random(75)

CASES: Sequence[List[int]] = (
    [2, 0, 2, 1, 1, 0],
    [1, 0, 1, 2],
    [2, 1, 0],
    [0],  # the smallest input
    [2],
    [1, 0],  # breaks "while i < r": index r is never checked
    [1, 2, 0],  # breaks advancing i after the swap with r: the 0 is skipped
    [0, 0, 1, 1, 2, 2],  # already sorted
    [2, 2, 1, 1, 0, 0],  # reversed
    [1, 1, 1],  # one color only
    [2, 0, 2, 0],  # no 1s
    [2, 2, 2, 0],  # every swap with r brings back another 2
    [_rng.choice([0, 1, 2]) for _ in range(300)],  # length ceiling, seeded
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = 0
        for case in CASES:
            nums = list(case)
            result = solve(nums)
            if result is None and nums == sorted(case):
                passed += 1
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()
