"""Divide Array Into Equal Pairs (LeetCode 2206).

Given an array of 2n integers, return True if it can be divided into n pairs
where both elements of every pair are equal.

The three approaches from https://neetcode.io/solutions/divide-array-into-equal-pairs,
plus a fixed-size parity array, ordered from brute force to optimal.
"""

from typing import Callable, Dict, List, Sequence, Set, Tuple


def divide_array_sorting(nums: List[int]) -> bool:
    """Sort, then check every run of equal values has even length.
    Time: O(N log N). Space: O(N) in Python.

    N = len(nums) = 2n. Sorting puts equal values next to each other, so each
    value becomes one run, and a run can be cut into pairs exactly when its
    length is even. Sorts the input in place; Python's Timsort needs up to N / 2
    slots of scratch space.

    An equivalent check: after sorting, every pair (nums[0], nums[1]),
    (nums[2], nums[3]), ... must be equal. That works because an odd run would
    leave its last element paired with the next run's first.
    """
    nums.sort()
    i = 0
    while i < len(nums):
        j = i
        while j < len(nums) and nums[j] == nums[i]:
            j += 1
        if (j - i) % 2 != 0:
            return False
        i = j
    return True


def divide_array_hash_map(nums: List[int]) -> bool:
    """Count each value, then check every count is even.
    Time: O(N). Space: O(N).

    The order of the elements doesn't matter at all: values can be paired up
    exactly when each one appears an even number of times. So this is a
    counting problem, not an arrangement problem.
    """
    count: Dict[int, int] = {}
    for num in nums:
        count[num] = count.get(num, 0) + 1
    return all(cnt % 2 == 0 for cnt in count.values())


def divide_array_hash_set(nums: List[int]) -> bool:
    """Keep the set of values seen an odd number of times so far.
    Time: O(N). Space: O(N).

    Each occurrence toggles a value in or out of the set: the first copy adds
    it, the second removes it, the third adds it back. Only the parity of each
    count matters, so the set stores exactly that. Every value has an even
    count exactly when the set ends up empty.
    """
    odd: Set[int] = set()
    for num in nums:
        if num in odd:
            odd.remove(num)
        else:
            odd.add(num)
    return not odd


def divide_array_parity_array(nums: List[int]) -> bool:
    """Toggle one flag per possible value. Time: O(N). Space: O(1).

    The optimal solution. Values are between 1 and 500, so a fixed list of 501
    flags can stand in for the set in approach 3: flip the flag on each
    occurrence, and every flag must end up False. The list's size doesn't
    depend on the input, so the space is O(1), and there's no hashing.

    The tempting shortcut, XOR-ing every value and checking for 0, is wrong.
    Equal pairs do cancel to 0, but so do unrelated values: 1 ^ 2 ^ 4 ^ 7 == 0
    and [1, 2, 4, 7] has no pairs at all. See the README.
    """
    odd = [False] * 501
    for num in nums:
        odd[num] = not odd[num]
    return not any(odd)


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int]], bool]], ...] = (
    ("sorting", divide_array_sorting),
    ("hash map", divide_array_hash_map),
    ("hash set", divide_array_hash_set),
    ("parity array", divide_array_parity_array),
)

MUTATES_INPUT = {"sorting"}

CASES: Sequence[Tuple[List[int], bool]] = (
    ([3, 2, 3, 2, 2, 2], True),
    ([1, 2, 3, 4], False),
    ([1, 1], True),  # the smallest input
    ([1, 2], False),
    ([5, 5, 5, 5], True),  # one value, used for two pairs
    ([1, 1, 1, 2], False),  # an odd count can't be fixed by another odd count
    ([2, 1, 2, 1], True),  # pairs don't need to be adjacent
    ([1, 2, 4, 7], False),  # XORs to 0, but nothing pairs; see the README
    ([1, 1, 2, 2, 3, 3, 4, 5], False),  # only the last two fail
    ([500, 1, 500, 1], True),  # value bounds
    ([7] * 1000, True),  # the length ceiling: n = 500, a single value
    (list(range(1, 501)) * 2, True),  # the ceiling, every value twice
    (list(range(1, 501)) + list(range(2, 501)) + [3], False),  # 1 once, 3 three times
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(list(nums)) == expected for nums, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")

    original = [3, 2, 3, 2, 2, 2]
    for name, solve in SOLUTIONS:
        if name not in MUTATES_INPUT:
            solve(original)
    status = "PASS" if original == [3, 2, 3, 2, 2, 2] else "FAIL"
    print(f"{status}  input left unmodified (all but sorting): {original}")


if __name__ == "__main__":
    main()
