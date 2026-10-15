"""Find All Numbers Disappeared in an Array (LeetCode 448).

Given an array nums of n integers, each in the range [1, n], return every
integer in [1, n] that does not appear in nums.

The four approaches from
https://neetcode.io/solutions/find-all-numbers-disappeared-in-an-array, plus
cyclic sort, the other O(1)-space answer to the follow-up. Ordered from the
most extra memory to the least.
"""

from typing import Callable, List, Sequence, Tuple


def disappeared_hash_set(nums: List[int]) -> List[int]:
    """Start with every number, cross off the ones seen. Time: O(n). Space: O(n).

    The most direct statement of the problem. The result comes out in
    ascending order here only because CPython stores small ints in a set by
    their own value, so iteration happens to walk them in order. Nothing
    promises that, and LeetCode accepts any order, so the harness sorts every
    result before comparing.
    """
    store = set(range(1, len(nums) + 1))
    for num in nums:
        store.discard(num)
    return list(store)


def disappeared_boolean_array(nums: List[int]) -> List[int]:
    """Mark each value's slot in a separate array. Time: O(n). Space: O(n).

    The same idea as the set, but the values are exactly 1..n, so a plain list
    indexed by value - 1 can do the set's job without any hashing. The result
    comes out in ascending order by construction.
    """
    seen = [False] * len(nums)
    for num in nums:
        seen[num - 1] = True
    return [i + 1 for i, was_seen in enumerate(seen) if not was_seen]


def disappeared_sorting(nums: List[int]) -> List[int]:
    """Sort, then walk 1..n alongside the sorted values.
    Time: O(n log n). Space: O(n) in Python.

    For each number 1..n, skip past any smaller values; if what is left is not
    that number, it is missing. Sorts the input in place. Python's sort is
    Timsort, which needs up to n / 2 slots of scratch space, so this is not the
    O(1) it looks like.
    """
    nums.sort()
    n = len(nums)
    res = []
    idx = 0
    for num in range(1, n + 1):
        while idx < n and nums[idx] < num:
            idx += 1
        if idx == n or nums[idx] > num:
            res.append(num)
    return res


def disappeared_negative_marking(nums: List[int]) -> List[int]:
    """Use the sign of nums[v - 1] as the "seen v" flag.
    Time: O(n). Space: O(1).

    The optimal solution, and NeetCode's answer to the follow-up. Every value
    is positive, so the sign bit of each slot is free to use as a flag: seeing
    v makes nums[v - 1] negative. Any slot still positive at the end belongs to
    a missing number.

    Two details. The value read must go through abs(), because an earlier step
    may already have flipped this slot. And the flip is -abs(), not negation,
    so a value that appears twice leaves its slot negative instead of flipping
    it back.

    NeetCode stops there and returns with the caller's array full of negative
    numbers. This version adds one more pass to restore every sign, so the
    input comes back exactly as it went in. Still O(n), still O(1).
    """
    for num in nums:
        i = abs(num) - 1
        nums[i] = -abs(nums[i])

    res = [i + 1 for i, num in enumerate(nums) if num > 0]

    for i in range(len(nums)):
        nums[i] = abs(nums[i])
    return res


def disappeared_cyclic_sort(nums: List[int]) -> List[int]:
    """Swap every value into its own slot, then read off the misfits.
    Time: O(n). Space: O(1).

    The other O(1)-space answer. Value v belongs at index v - 1. Keep swapping
    nums[i] into its home until the slot holds its own value or its home is
    already taken by a duplicate. Afterwards, any slot i not holding i + 1 is
    a missing number.

    O(n) despite the nested loop: every swap puts one value in its final
    place, and that value is never moved again, so there are at most n swaps in
    total.

    The swap target is saved to j first. The tempting one-liner
    `nums[i], nums[nums[i] - 1] = nums[nums[i] - 1], nums[i]` assigns nums[i]
    before it evaluates the second target index, so it writes to the wrong slot.
    On [4, 3, 2, 7, 8, 2, 3, 1] it never terminates. See the README.

    Leaves the input permuted, unlike approach 4.
    """
    for i in range(len(nums)):
        while nums[i] != nums[nums[i] - 1]:
            j = nums[i] - 1
            nums[i], nums[j] = nums[j], nums[i]
    return [i + 1 for i, num in enumerate(nums) if num != i + 1]


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int]], List[int]]], ...] = (
    ("hash set", disappeared_hash_set),
    ("boolean array", disappeared_boolean_array),
    ("sorting", disappeared_sorting),
    ("negative marking", disappeared_negative_marking),
    ("cyclic sort", disappeared_cyclic_sort),
)

MUTATES_INPUT = {"sorting", "cyclic sort"}

CASES: Sequence[Tuple[List[int], List[int]]] = (
    ([4, 3, 2, 7, 8, 2, 3, 1], [5, 6]),
    ([1, 1], [2]),
    ([1], []),  # the smallest input, nothing missing
    ([2, 1], []),  # a permutation: nothing missing
    ([2, 2], [1]),  # the missing number is the first
    ([1, 1, 1, 1], [2, 3, 4]),  # one value repeated: all others missing
    ([4, 4, 4, 4], [1, 2, 3]),  # the repeated value is n
    ([3, 3, 3], [1, 2]),
    ([5, 4, 3, 2, 1], []),  # reversed permutation
    ([2, 3, 4, 5, 5], [1]),
    ([1, 2, 3, 4, 4], [5]),  # the missing number is the last
    ([1] * 100000, list(range(2, 100001))),  # the ceiling, almost everything missing
    (list(range(100000, 0, -1)), []),  # the ceiling, nothing missing
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(sorted(solve(list(nums))) == expected for nums, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")

    original = [4, 3, 2, 7, 8, 2, 3, 1]
    for name, solve in SOLUTIONS:
        if name not in MUTATES_INPUT:
            solve(original)
    status = "PASS" if original == [4, 3, 2, 7, 8, 2, 3, 1] else "FAIL"
    print(f"{status}  input left unmodified (all but sorting, cyclic sort): {original}")


if __name__ == "__main__":
    main()
