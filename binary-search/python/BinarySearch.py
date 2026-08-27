"""Binary Search (LeetCode 704).

Given an array `nums` of distinct integers sorted in ascending order and an
integer `target`, return the index of `target`, or -1 if it is not there. The
problem requires O(log n) time.

All five approaches from https://neetcode.io/solutions/binary-search, plus the
linear scan the problem exists to rule out, ordered from brute force to optimal.

The last three do not test for equality inside the loop at all. They compute an
insertion point and check it once at the end -- see the README on why that is
the version worth internalising even though approach 3 is the one to write here.
"""

import bisect
from typing import Callable, Sequence, Tuple


def search_linear(nums: Sequence[int], target: int) -> int:
    """Walk the array. Time: O(n). Space: O(1).

    The brute force, and the reason this problem is stated the way it is. It is
    correct, it is the fastest thing to write, and it is the only approach here
    that does not need the array to be sorted -- which is exactly what makes it
    the wrong answer. LeetCode 704 names O(log n) in the problem statement
    rather than leaving it to the follow-up, so a linear scan is rejected on
    sight even though it passes every test below.

    It is here to make the sortedness load-bearing. Everything after this point
    is an answer to "what does knowing the array is sorted actually buy you",
    and the answer is: every comparison discards half of what is left instead of
    one element.
    """
    for i, value in enumerate(nums):
        if value == target:
            return i
    return -1


def _search_range(nums: Sequence[int], target: int, l: int, r: int) -> int:
    """Search the closed interval [l, r]. Helper for `search_recursive`."""
    if l > r:
        return -1

    m = l + (r - l) // 2
    if nums[m] == target:
        return m
    if nums[m] < target:
        return _search_range(nums, target, m + 1, r)
    return _search_range(nums, target, l, m - 1)


def search_recursive(nums: Sequence[int], target: int) -> int:
    """Halve the interval, then recurse into the half that can still hold it.
    Time: O(log n). Space: O(log n).

    Space is O(log n) for the call stack, not O(1). Unlike Reverse Linked List
    in this repo, that is harmless here: the depth is log2(n), so the maximum
    input of 10,000 elements recurses 14 frames deep against a default limit of
    1000. The recursion is a style choice at these sizes, not a hazard.

    `l > r` is the base case, and it is the empty interval: the two pointers
    have crossed, so nothing is left to look at. It is reachable only through
    `m + 1` or `m - 1`, which is also what guarantees termination -- every call
    excludes the midpoint it just rejected, so the interval strictly shrinks.

    The interval here is closed on both ends, [l, r]: `r` starts at the last
    index, the loop runs while `l <= r`, and the rejecting branch moves past the
    midpoint. Approaches 4 and 5 use the half-open [l, r) instead. Both are
    correct; mixing them is not, and the README has the table of what each
    mismatch does.
    """
    return _search_range(nums, target, 0, len(nums) - 1)


def search_iterative(nums: Sequence[int], target: int) -> int:
    """The same halving, as a loop. Time: O(log n). Space: O(1).

    The optimal solution and the one to write. It is approach 2 with the
    interval carried in two variables instead of in call frames -- the recursion
    there is tail-recursive, and this is what unrolling it by hand looks like.

    Two details are worth being able to defend out loud:

    `m = l + (r - l) // 2` rather than `(l + r) // 2`. In Python the two are
    identical and always will be, because ints are arbitrary precision. In Java
    the naive form overflows to a negative index on large arrays -- the bug that
    sat in the JDK's own binarySearch for nine years. Writing the safe form in
    Python is a habit for the languages where it matters, not a fix for anything
    here. See the README.

    `while l <= r`, not `l < r`. With `r` starting at the last index, `l < r`
    exits while one candidate is still unexamined, so a single-element array
    reports -1 for the element it contains. That is not an edge case bolted on
    afterwards -- it is the last step of nearly every successful search.
    """
    l, r = 0, len(nums) - 1

    while l <= r:
        m = l + (r - l) // 2
        if nums[m] > target:
            r = m - 1
        elif nums[m] < target:
            l = m + 1
        else:
            return m

    return -1


def search_upper_bound(nums: Sequence[int], target: int) -> int:
    """Find the first index past target, then look one to its left.
    Time: O(log n). Space: O(1).

    A different question with the same shape: this loop does not look for
    `target`, it looks for the boundary between the values `<= target` and the
    values `> target`. It always runs to completion -- there is no early exit,
    because there is nothing to exit early on.

    The interval is half-open, [l, r): `r` starts at `len(nums)`, one past the
    end, the loop runs while `l < r`, and the rejecting branch sets `r = m`
    rather than `m - 1`. `m` is being excluded either way; here it is excluded
    by the interval's open right end instead of by arithmetic. Note that `r` can
    legitimately equal `len(nums)`, which is why the loop must never evaluate
    `nums[r]`.

    On exit `l` is the count of elements `<= target`, so if target is present it
    is the element just before: index `l - 1`. The `nums[l - 1] == target` check
    is then what distinguishes "target is here" from "target would go here",
    which the loop never asked.

    The `l` guard is the more interesting half, because in Python it barely does
    anything. `l` is falsy exactly when nothing is `<= target`, and dropping the
    guard makes that case read `nums[-1]` -- which wraps to the last element
    instead of failing. That read is harmless: if nothing is `<= target` then
    the last element is `> target` too, so the comparison is false and -1 comes
    back anyway. Verified on every sorted distinct array of length 1-6 drawn
    from 0..9 against every target in -2..11: 11,858 checks, zero differences.
    The one input it does catch is the empty array, where `nums[-1]` raises
    IndexError.

    The Java file needs the same guard for a completely different reason: there
    `nums[-1]` throws every time `l` is 0, so the omission fails loudly on any
    target below the array rather than hiding. Same line, same position, and in
    one language it is structural while in the other it is an empty-input check.
    """
    l, r = 0, len(nums)

    while l < r:
        m = l + (r - l) // 2
        if nums[m] > target:
            r = m
        else:
            l = m + 1

    return l - 1 if (l and nums[l - 1] == target) else -1


def search_lower_bound(nums: Sequence[int], target: int) -> int:
    """Find the first index not below target, and check whether it is target.
    Time: O(log n). Space: O(1).

    Approach 4 with the comparison relaxed from `>` to `>=`, which slides the
    boundary from "first element past target" to "first element not below
    target" -- so a present target lands on `l` itself rather than one before
    it, and no offset is needed.

    That single character is the whole difference, and it is the version to
    reach for outside this problem. `l` is an insertion point: the index where
    target belongs whether or not it is there. That is strictly more than "found
    or not", and it is what the neighbouring problems actually want -- first
    element >= x, count of elements < x, search-insert-position, and the
    boundary hunts in rotated or implicit arrays where there is no equality test
    to write. This is also exactly what `bisect_left` computes in approach 6.

    Here the guard needs `l < len(nums)` rather than approach 4's `l`, because
    the failure has moved to the other end: `l` runs past the array when every
    element is below target. Reading `nums[l]` unguarded raises IndexError
    instead of quietly wrapping the way `nums[l - 1]` does.
    """
    l, r = 0, len(nums)

    while l < r:
        m = l + (r - l) // 2
        if nums[m] >= target:
            r = m
        else:
            l = m + 1

    return l if (l < len(nums) and nums[l] == target) else -1


def search_builtin(nums: Sequence[int], target: int) -> int:
    """Let the standard library do it. Time: O(log n). Space: O(1).

    `bisect_left` is approach 5, already written and already correct, and the
    same final guard is still needed: it returns an insertion point, so it
    answers "where would target go" and never "is target there". A bare
    `bisect_left` result is not an answer to this problem.

    Not what to submit -- the exercise is the loop -- but worth knowing exists,
    and worth knowing that its Java counterpart is not the same function. On
    duplicates `bisect_left` is specified to return the first match, while
    `Arrays.binarySearch` may return any of them; verified in the README. That
    difference is invisible here, since the values are distinct.
    """
    index = bisect.bisect_left(nums, target)
    return index if index < len(nums) and nums[index] == target else -1


SOLUTIONS: Tuple[Tuple[str, Callable[[Sequence[int], int], int]], ...] = (
    ("linear", search_linear),
    ("recursion", search_recursive),
    ("iteration", search_iterative),
    ("upper bound", search_upper_bound),
    ("lower bound", search_lower_bound),
    ("built-in", search_builtin),
)

_MAX: Sequence[int] = tuple(range(-9999, 10000, 2))  # all 10,000 odd values in range

CASES: Sequence[Tuple[Sequence[int], int, int]] = (
    ((-1, 0, 2, 4, 6, 8), 4, 3),  # the NeetCode example: present
    ((-1, 0, 2, 4, 6, 8), 3, -1),  # the NeetCode example: absent, between two elements
    ((1,), 1, 0),  # single element, present: lost by `while l < r` on a closed interval
    ((1,), 2, -1),  # single element, absent above
    ((1,), 0, -1),  # single element, absent below
    ((), 5, -1),  # empty: outside the constraints, but nothing here should crash
    ((1, 2), 1, 0),  # even length, first
    ((1, 2), 2, 1),  # even length, last
    ((1, 2, 3), 1, 0),  # odd length, first
    ((1, 2, 3), 3, 2),  # odd length, last: reached only after the interval collapses
    ((1, 2, 3, 4), 1, 0),  # both ends of an even length, where the halves are uneven
    ((1, 2, 3, 4), 4, 3),
    ((-9999, 0, 9999), -9999, 0),  # the value bounds, low -- they are strict, so this is the extreme
    ((-9999, 0, 9999), 9999, 2),  # the value bounds, high
    ((-9999, 0, 9999), 9998, -1),  # absent, just under the top
    ((-5, -3, -1), -4, -1),  # all negative, absent between two elements
    ((-5, -3, -1), -6, -1),  # absent below everything: the case approach 4's guard exists for
    (_MAX, -9999, 0),  # largest legal input, first element
    (_MAX, 9999, 9999),  # largest legal input, last element
    (_MAX, 0, -1),  # largest legal input, absent: it holds only odd values
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = 0
        for nums, target, expected in CASES:
            passed += solve(nums, target) == expected
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()
