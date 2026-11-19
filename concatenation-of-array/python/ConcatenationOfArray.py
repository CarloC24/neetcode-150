"""Concatenation of Array (LeetCode 1929).

Given an integer array `nums` of length n, return an array `ans` of length 2n
where ans[i] == nums[i] and ans[i + n] == nums[i] for 0 <= i < n. In other
words, `nums` followed by itself.

The two approaches from https://neetcode.io/solutions/concatenation-of-array,
plus the built-in one, ordered from most to least code.
"""

from typing import Callable, List, Sequence, Tuple


def get_concatenation_append(nums: List[int]) -> List[int]:
    """Append every element, twice over. Time: O(n). Space: O(n) for the output.

    Two passes over `nums`, appending to a new list. The list grows as it
    goes, which Python makes amortized O(1) per append.
    """
    ans = []
    for _ in range(2):
        for num in nums:
            ans.append(num)
    return ans


def get_concatenation_index(nums: List[int]) -> List[int]:
    """Preallocate 2n slots and fill both halves in one pass.
    Time: O(n). Space: O(n) for the output.

    ans[i] and ans[i + n] both get nums[i]. The size is known up front, so
    there's no growing, and this is the version that translates directly to
    fixed-size arrays in Java or C.
    """
    n = len(nums)
    ans = [0] * (2 * n)
    for i, num in enumerate(nums):
        ans[i] = num
        ans[i + n] = num
    return ans


def get_concatenation_builtin(nums: List[int]) -> List[int]:
    """List concatenation with +. Time: O(n). Space: O(n) for the output.

    `nums + nums` builds a new list and leaves `nums` alone. `nums * 2` does
    the same. Both copy element references, which is fine for ints.
    """
    return nums + nums


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int]], List[int]]], ...] = (
    ("append", get_concatenation_append),
    ("index", get_concatenation_index),
    ("builtin", get_concatenation_builtin),
)

CASES: Sequence[Tuple[List[int], List[int]]] = (
    ([1, 2, 1], [1, 2, 1, 1, 2, 1]),
    ([1, 3, 2, 1], [1, 3, 2, 1, 1, 3, 2, 1]),
    ([7], [7, 7]),  # n = 1, the smallest input
    ([5, 5, 5], [5, 5, 5, 5, 5, 5]),  # all equal
    ([1, 1000], [1, 1000, 1, 1000]),  # value bounds
    ([3, 2, 1], [3, 2, 1, 3, 2, 1]),  # the copy is not sorted or reversed
    (list(range(1, 1001)), list(range(1, 1001)) * 2),  # n = 1000, the length ceiling
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(list(nums)) == expected for nums, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")

    # No approach may change its input or hand it back as the answer.
    nums = [1, 2, 3]
    ok = True
    for _, solve in SOLUTIONS:
        ans = solve(nums)
        ok = ok and nums == [1, 2, 3] and ans is not nums
    print(f"{'PASS' if ok else 'FAIL'}  input left unmodified: {nums}")


if __name__ == "__main__":
    main()
