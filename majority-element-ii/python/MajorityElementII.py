"""Majority Element II (LeetCode 229).

Given an integer array nums of size n, return every value that appears more
than n // 3 times, in any order. Follow-up: O(n) time and O(1) space.

The five approaches from https://neetcode.io/solutions/majority-element-ii,
ordered from brute force to Boyer-Moore voting with two candidates.

At most two values can appear more than n // 3 times: three of them would
need more than n elements. That's why the answer has at most two values, and
why two candidates are enough.
"""

import random
from collections import Counter, defaultdict
from typing import Callable, Dict, List, Sequence, Tuple


def majority_element_brute_force(nums: List[int]) -> List[int]:
    """Count every element with a full scan. Time: O(n^2). Space: O(1).

    The set keeps each answer once, however many times it's counted. It
    never holds more than two values. Too slow for n = 50,000, so the harness
    skips the long cases for this one.
    """
    res = set()
    for num in nums:
        if num not in res and nums.count(num) > len(nums) // 3:
            res.add(num)
    return list(res)


def majority_element_sorting(nums: List[int]) -> List[int]:
    """Sort, then measure each run of equal values.
    Time: O(n log n). Space: O(n) here, for the sorted copy.

    In sorted order, equal values sit together, so each value's count is the
    length of its run. NeetCode sorts nums in place, which saves the copy but
    changes the caller's array. This sorts a copy instead.
    """
    nums = sorted(nums)
    res, n = [], len(nums)
    i = 0
    while i < n:
        j = i
        while j < n and nums[j] == nums[i]:
            j += 1
        if j - i > n // 3:  # the run is nums[i:j]
            res.append(nums[i])
        i = j
    return res


def majority_element_counter(nums: List[int]) -> List[int]:
    """Count everything in a hash map. Time: O(n). Space: O(n)."""
    count = Counter(nums)
    return [num for num, c in count.items() if c > len(nums) // 3]


def majority_element_boyer_moore(nums: List[int]) -> List[int]:
    """Boyer-Moore voting with two candidates. Time: O(n). Space: O(1).

    Keep two candidates and a count for each. A value that matches a
    candidate votes for it. A value that matches neither takes an empty slot
    if there is one; otherwise it cancels one vote from each candidate.

    Each cancellation removes three different values at once. A value that
    appears more than n / 3 times can't be cancelled out completely, so it
    ends up as a candidate. The reverse isn't true: a candidate may not be a
    majority at all ([1, 2, 3] leaves 3 behind). The second pass counts both
    candidates for real.

    Order matters in the first loop: check both candidates before looking for
    an empty slot. Otherwise a value can take slot 1 while it's already the
    candidate in slot 2. See the README.

    The -1 starting values are just placeholders. -1 is a legal input, and
    that's fine: a slot with count 0 that matches the next value simply
    starts counting it, which is what taking the empty slot would do.
    """
    n = len(nums)
    num1 = num2 = -1
    cnt1 = cnt2 = 0
    for num in nums:
        if num == num1:
            cnt1 += 1
        elif num == num2:
            cnt2 += 1
        elif cnt1 == 0:
            num1, cnt1 = num, 1
        elif cnt2 == 0:
            num2, cnt2 = num, 1
        else:
            cnt1 -= 1
            cnt2 -= 1

    # The counts above are votes left over, not occurrences. Count again.
    cnt1 = cnt2 = 0
    for num in nums:
        if num == num1:
            cnt1 += 1
        elif num == num2:  # elif: if num1 == num2, count it once
            cnt2 += 1

    res = []
    if cnt1 > n // 3:
        res.append(num1)
    if cnt2 > n // 3:
        res.append(num2)
    return res


def majority_element_boyer_moore_map(nums: List[int]) -> List[int]:
    """Boyer-Moore voting with the candidates in a hash map.
    Time: O(n). Space: O(1), the map never holds more than three entries.

    The same voting, written so it generalizes. Add each value's vote. If
    that makes three candidates, take one vote from each of them, which
    removes the newcomer (it only had one) and drops anyone else who reaches
    0. For "more than n / k times", allow k - 1 candidates instead of 2.

    Then count each surviving candidate for real, as in approach 4.
    """
    count: Dict[int, int] = defaultdict(int)
    for num in nums:
        count[num] += 1
        if len(count) > 2:
            count = defaultdict(int, {v: c - 1 for v, c in count.items() if c > 1})

    return [v for v in count if nums.count(v) > len(nums) // 3]


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int]], List[int]], bool], ...] = (
    # (name, function, runs the 50,000-element cases)
    ("brute force", majority_element_brute_force, False),
    ("sorting", majority_element_sorting, True),
    ("hash map", majority_element_counter, True),
    ("boyer-moore", majority_element_boyer_moore, True),
    ("boyer-moore, hash map", majority_element_boyer_moore_map, True),
)

_rng = random.Random(229)
_N = 50_000


def _shuffled(values: List[int]) -> List[int]:
    _rng.shuffle(values)
    return values


SHORT: Sequence[List[int]] = (
    [3, 2, 3],  # LeetCode example 1
    [1],  # LeetCode example 2, the smallest input
    [1, 2],  # LeetCode example 3: n // 3 == 0, so both count
    [5, 2, 3, 2, 2, 2, 2, 5, 5, 5],  # NeetCode example 1: two answers
    [4, 4, 4, 4, 4],  # NeetCode example 2
    [1, 2, 3],  # NeetCode example 3: no answer, but 3 is left as a candidate
    [1, 1, 2, 2, 3, 3],  # each exactly n // 3 times: not enough
    [1, 1, 1, 2, 2, 3, 3],  # n = 7, 1 appears 3 > 2 times
    [-1, -1, -1],  # -1, the placeholder value, as the answer
    [-1, 5, 5],  # -1 present but not an answer
    [2, 1, 1, 3, 1, 4, 5, 6],  # the majority is cancelled down, then recovers
    [10**9, -(10**9), 10**9],  # the value limits
    [1, 1],  # breaks checking empty slots first: 1 fills both slots
    [1, 1, 2, 2, 2],  # ...and here that loses the 1 altogether
    [1, 1, 2],  # breaks skipping the second pass, and >= instead of >
)

LONG: Sequence[List[int]] = (
    [7] * _N,
    # two values 16,667 times each (just over 50,000 // 3 == 16,666)
    _shuffled([1] * 16_667 + [2] * 16_667 + list(range(3, 3 + _N - 33_334))),
    # one value exactly 16,666 times: not enough
    _shuffled([1] * 16_666 + list(range(2, 2 + _N - 16_666))),
    [_rng.randint(-3, 3) for _ in range(_N)],  # seven values, roughly n / 7 each
)


def _expected(nums: List[int]) -> List[int]:
    counts = Counter(nums)
    return sorted(v for v, c in counts.items() if c > len(nums) // 3)


def main() -> None:
    for name, solve, runs_long in SOLUTIONS:
        cases = list(SHORT) + (list(LONG) if runs_long else [])
        passed = 0
        for nums in cases:
            original = list(nums)
            result = solve(nums)
            # any order is fine, but no repeats and no changes to the input
            if sorted(result) == _expected(original) and nums == original:
                passed += 1
        status = "PASS" if passed == len(cases) else "FAIL"
        note = "" if runs_long else f" (skips the {len(LONG)} long cases)"
        print(f"{status}  {name}: {passed}/{len(cases)} cases{note}")


if __name__ == "__main__":
    main()
