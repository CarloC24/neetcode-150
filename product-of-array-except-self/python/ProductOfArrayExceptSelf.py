"""Product of Array Except Self (LeetCode 238).

Given an integer array nums, return an array answer where answer[i] is the
product of every element of nums except nums[i]. Every prefix and suffix
product fits in a 32-bit integer. Follow-up: O(n) time without division.

The four approaches from
https://neetcode.io/solutions/product-of-array-except-self, ordered from
brute force to one output array and two passes.
"""

import math
from typing import Callable, List, Sequence, Tuple


def product_except_self_brute_force(nums: List[int]) -> List[int]:
    """Multiply everything else, for each index. Time: O(n^2). Space: O(1).

    Space here and below doesn't count the output array. Too slow for
    n = 100,000 (10^10 multiplications), so the harness skips the long cases
    for this one.
    """
    n = len(nums)
    res = [0] * n
    for i in range(n):
        prod = 1
        for j in range(n):
            if j != i:
                prod *= nums[j]
        res[i] = prod
    return res


def product_except_self_division(nums: List[int]) -> List[int]:
    """Total product divided by each element, zeros counted apart.
    Time: O(n). Space: O(1).

    The follow-up rules out division, but it's the first idea most people
    have, and the zeros are the whole difficulty:

        no zeros   answer[i] = prod / nums[i]
        one zero   0 everywhere except at the zero, which gets prod
        2+ zeros   0 everywhere: every product includes a zero

    prod is the product of the non-zero elements only, so it never becomes 0
    and loses the information. Dividing it by each element is exact, so //
    is safe, even for negatives.
    """
    prod, zero_count = 1, 0
    for num in nums:
        if num:
            prod *= num
        else:
            zero_count += 1

    if zero_count > 1:
        return [0] * len(nums)
    if zero_count == 1:
        return [0 if num else prod for num in nums]
    return [prod // num for num in nums]


def product_except_self_prefix_suffix(nums: List[int]) -> List[int]:
    """Prefix and suffix product arrays. Time: O(n). Space: O(n).

    Everything except nums[i] is everything to its left times everything to
    its right:

        pref[i] = nums[0] * ... * nums[i - 1]     (pref[0] = 1, nothing left)
        suff[i] = nums[i + 1] * ... * nums[n - 1] (suff[n - 1] = 1)
        answer[i] = pref[i] * suff[i]

    No division, so zeros need no special case. Both arrays leave nums[i]
    out: pref[i] is built from nums[i - 1], not nums[i].
    """
    n = len(nums)
    pref = [1] * n
    suff = [1] * n
    for i in range(1, n):
        pref[i] = pref[i - 1] * nums[i - 1]
    for i in range(n - 2, -1, -1):
        suff[i] = suff[i + 1] * nums[i + 1]
    return [pref[i] * suff[i] for i in range(n)]


def product_except_self_optimal(nums: List[int]) -> List[int]:
    """Prefix products in the output, suffix as a running value.
    Time: O(n). Space: O(1).

    The same idea as approach 3 without the two extra arrays. The first pass
    writes each prefix product into res. The second pass walks right to left
    with suffix, the product of everything already passed, and multiplies it
    in. In both passes, use the running value first and fold nums[i] in
    after: that's what leaves nums[i] out of its own answer.
    """
    n = len(nums)
    res = [1] * n

    prefix = 1
    for i in range(n):
        res[i] = prefix  # product of nums[0 .. i - 1]
        prefix *= nums[i]

    suffix = 1
    for i in range(n - 1, -1, -1):
        res[i] *= suffix  # times the product of nums[i + 1 .. n - 1]
        suffix *= nums[i]
    return res


Solution = Callable[[List[int]], List[int]]

# The last field says whether the approach can run the 100,000-element cases.
SOLUTIONS: Tuple[Tuple[str, Solution, bool], ...] = (
    ("brute force", product_except_self_brute_force, False),
    ("division", product_except_self_division, True),
    ("prefix & suffix", product_except_self_prefix_suffix, True),
    ("prefix & suffix, O(1)", product_except_self_optimal, True),
)


def _by_definition(nums: List[int]) -> List[int]:
    """The answer straight from the definition, for the short cases."""
    return [math.prod(nums[:i] + nums[i + 1 :]) for i in range(len(nums))]


SHORT: Sequence[List[int]] = (
    [1, 2, 3, 4],
    [-1, 1, 0, -3, 3],
    [1, 2, 4, 6],  # NeetCode example 1
    [-1, 0, 1, 2, 3],  # NeetCode example 2
    [2, 3],  # the smallest input
    [0, 5],  # two elements, one zero
    [0, 0],  # two elements, both zero
    [4, 0, 5, 0, 6],  # two zeros: every answer is 0
    [0, 2, 3],  # zero at the front
    [2, 3, 0],  # zero at the back
    [-2, -3, -4],  # all negative: signs flip per answer
    [30, -30, 30, -30, 30],  # value limits; the answers reach 30^4
)

_N = 100_000
_alternating = [-1, 1] * (_N // 2)
_with_zero = _alternating[:]
_with_zero[_N // 2] = 0

# Long cases with answers worked out by hand, since the definition is O(n^2).
LONG: Sequence[Tuple[List[int], List[int]]] = (
    # product of everything is 1, and 1 / x == x for x = +-1
    (_alternating, _alternating),
    # one zero, replacing a -1: 0 everywhere else; at the zero, the other
    # 49,999 -1s multiply to -1
    (_with_zero, [-1 if i == _N // 2 else 0 for i in range(_N)]),
    ([1] * _N, [1] * _N),
)


def main() -> None:
    cases = [(nums, _by_definition(nums)) for nums in SHORT]
    for name, solve, runs_long in SOLUTIONS:
        todo = cases + list(LONG) if runs_long else cases
        passed = 0
        for nums, expected in todo:
            original = list(nums)
            if solve(nums) == expected and nums == original:
                passed += 1
        status = "PASS" if passed == len(todo) else "FAIL"
        note = "" if runs_long else f" (skips the {len(LONG)} long cases)"
        print(f"{status}  {name}: {passed}/{len(todo)} cases{note}")


if __name__ == "__main__":
    main()
