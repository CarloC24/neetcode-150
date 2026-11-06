"""Sort an Array (LeetCode 912).

Given an array of integers nums, sort it in ascending order and return it,
without built-in sort functions, in O(n log n) time and the smallest space
possible.

The six approaches from https://neetcode.io/solutions/sort-an-array, plus
the library sort as a baseline. Every approach except radix sort sorts nums
in place and returns it.
"""

import random
from typing import Callable, List, Sequence, Tuple


def sort_array_builtin(nums: List[int]) -> List[int]:
    """Call the library sort. Time: O(n log n). Space: O(n) in Python.

    Not allowed by the problem. It's here as the baseline, not as an answer.
    """
    nums.sort()
    return nums


# --- 1. Quick sort -----------------------------------------------------------


def _partition(nums: List[int], left: int, right: int) -> int:
    """Median-of-three, Hoare-style partition of nums[left..right].

    Needs right - left >= 2. Moves the middle element to left + 1, then sorts
    nums[left], nums[left + 1] and nums[right]. The median ends up at
    left + 1 and becomes the pivot. The other two act as sentinels: nums[left]
    <= pivot stops j, and nums[right] >= pivot stops i, so neither scan needs
    a bounds check.

    Both scans stop on values equal to the pivot. That costs a few extra
    swaps, but it splits an array of equal values down the middle instead of
    peeling off one element at a time.

    Returns the pivot's final index j. Afterwards nums[left..j-1] <= pivot
    and nums[j+1..right] >= pivot.
    """
    mid = (left + right) // 2
    nums[mid], nums[left + 1] = nums[left + 1], nums[mid]

    if nums[left] > nums[right]:
        nums[left], nums[right] = nums[right], nums[left]
    if nums[left + 1] > nums[right]:
        nums[left + 1], nums[right] = nums[right], nums[left + 1]
    if nums[left] > nums[left + 1]:
        nums[left], nums[left + 1] = nums[left + 1], nums[left]

    pivot = nums[left + 1]
    i, j = left + 1, right
    while True:
        i += 1
        while nums[i] < pivot:
            i += 1
        j -= 1
        while nums[j] > pivot:
            j -= 1
        if i > j:
            break
        nums[i], nums[j] = nums[j], nums[i]

    nums[left + 1], nums[j] = nums[j], nums[left + 1]
    return j


def sort_array_quick(nums: List[int]) -> List[int]:
    """Quick sort with a median-of-three pivot.
    Time: O(n log n) average, O(n^2) worst. Space: O(log n).

    NeetCode's partition. One change: it recurses into the smaller side and
    loops on the larger one. That caps the stack at O(log n) even on inputs
    that make the pivot choice bad, where recursing into both sides could go
    n levels deep (Python's limit is about 1,000).
    """

    def quick_sort(left: int, right: int) -> None:
        while right - left >= 2:
            j = _partition(nums, left, right)
            if j - left < right - j:
                quick_sort(left, j - 1)
                left = j + 1
            else:
                quick_sort(j + 1, right)
                right = j - 1
        if right == left + 1 and nums[right] < nums[left]:
            nums[left], nums[right] = nums[right], nums[left]

    quick_sort(0, len(nums) - 1)
    return nums


# --- 2. Merge sort -----------------------------------------------------------


def sort_array_merge(nums: List[int]) -> List[int]:
    """Top-down merge sort. Time: O(n log n). Space: O(n).

    Split in half, sort each half, then merge the two sorted halves. The
    only approach here that is O(n log n) in the worst case and stable. The
    price is the O(n) of copies each merge makes.

    Ties take from the left half (<=), which is what keeps it stable.
    """

    def merge(lo: int, mid: int, hi: int) -> None:
        left, right = nums[lo : mid + 1], nums[mid + 1 : hi + 1]
        i, j, k = lo, 0, 0
        while j < len(left) and k < len(right):
            if left[j] <= right[k]:
                nums[i] = left[j]
                j += 1
            else:
                nums[i] = right[k]
                k += 1
            i += 1
        # At most one of these has anything left.
        while j < len(left):
            nums[i] = left[j]
            j += 1
            i += 1
        while k < len(right):
            nums[i] = right[k]
            k += 1
            i += 1

    def merge_sort(lo: int, hi: int) -> None:
        if lo >= hi:
            return
        mid = (lo + hi) // 2
        merge_sort(lo, mid)
        merge_sort(mid + 1, hi)
        merge(lo, mid, hi)

    merge_sort(0, len(nums) - 1)
    return nums


# --- 3. Heap sort ------------------------------------------------------------


def _sift_down(nums: List[int], n: int, i: int) -> None:
    """Push nums[i] down until it's no smaller than its children.

    Only looks at nums[0..n-1]. The children of i are 2i + 1 and 2i + 2.
    NeetCode writes this recursively, which costs O(log n) stack. A loop
    does the same in O(1).
    """
    while True:
        largest = i
        l, r = 2 * i + 1, 2 * i + 2
        if l < n and nums[l] > nums[largest]:
            largest = l
        if r < n and nums[r] > nums[largest]:
            largest = r
        if largest == i:
            return
        nums[i], nums[largest] = nums[largest], nums[i]
        i = largest


def sort_array_heap(nums: List[int]) -> List[int]:
    """Heap sort. Time: O(n log n). Space: O(1).

    Turn the array into a max-heap in place, from the last parent n // 2 - 1
    back to the root. Then swap the largest value (the root) to the end,
    shrink the heap by one and sift the new root down. The sorted part grows
    from the right.

    O(n log n) in the worst case and O(1) extra space: it meets the problem's
    ask best on paper. It's not stable, and it jumps around the array, so in
    practice it's slower than quick sort.
    """
    n = len(nums)
    for i in range(n // 2 - 1, -1, -1):
        _sift_down(nums, n, i)
    for end in range(n - 1, 0, -1):
        nums[0], nums[end] = nums[end], nums[0]
        _sift_down(nums, end, 0)
    return nums


# --- 4. Counting sort --------------------------------------------------------


def sort_array_counting(nums: List[int]) -> List[int]:
    """Counting sort over [min, max]. Time: O(n + k). Space: O(k).

    k = max - min + 1. Count each value, then write the values back in
    order. It never compares two elements, so the O(n log n) bound doesn't
    apply. Here values lie in [-50,000, 50,000], so k is at most 100,001 and
    this is the fastest approach.

    NeetCode counts in a hash map. An array indexed by value - min is the
    same idea without the hashing.
    """
    lo, hi = min(nums), max(nums)
    count = [0] * (hi - lo + 1)
    for num in nums:
        count[num - lo] += 1

    i = 0
    for offset, c in enumerate(count):
        for _ in range(c):
            nums[i] = offset + lo
            i += 1
    return nums


# --- 5. Radix sort -----------------------------------------------------------


def _radix_sort_non_negative(arr: List[int]) -> None:
    """LSD radix sort, base 10, of non-negative ints, in place.

    One stable counting sort per digit, ones digit first. Stability is what
    makes it work: after sorting by the tens digit, numbers with the same
    tens digit stay in ones-digit order.
    """
    if not arr:
        return
    biggest = max(arr)
    place = 1
    while biggest // place > 0:
        count = [0] * 10
        for num in arr:
            count[num // place % 10] += 1
        for d in range(1, 10):
            count[d] += count[d - 1]  # count[d] = where digit d's run ends
        out = [0] * len(arr)
        for num in reversed(arr):  # back to front keeps it stable
            d = num // place % 10
            count[d] -= 1
            out[count[d]] = num
        arr[:] = out
        place *= 10


def sort_array_radix(nums: List[int]) -> List[int]:
    """LSD radix sort, negatives handled separately.
    Time: O(d * n), d = digits in the largest |value|. Space: O(n).

    Digits only make sense for non-negative numbers, so split the array.
    Sort the absolute values of the negatives, then reverse and negate them:
    the biggest absolute value is the smallest negative. Then append the
    sorted non-negatives.

    Returns a new list; nums itself isn't sorted.
    """
    negatives = [-num for num in nums if num < 0]
    non_negatives = [num for num in nums if num >= 0]
    _radix_sort_non_negative(negatives)
    _radix_sort_non_negative(non_negatives)
    return [-num for num in reversed(negatives)] + non_negatives


# --- 6. Shell sort -----------------------------------------------------------


def sort_array_shell(nums: List[int]) -> List[int]:
    """Shell sort with gaps n/2, n/4, ..., 1.
    Time: O(n^2) worst with these gaps, much faster in practice. Space: O(1).

    Insertion sort, but comparing elements gap apart. Big gaps move values
    long distances in a few steps, so by the time gap is 1 (a plain insertion
    sort) the array is almost sorted and there's little left to do.

    NeetCode lists it as O(n log n) on average. With Shell's halving gaps
    that isn't guaranteed. Better gap sequences (Knuth's 1, 4, 13, 40, ...)
    give O(n^1.5).
    """
    n = len(nums)
    gap = n // 2
    while gap >= 1:
        for i in range(gap, n):
            tmp = nums[i]
            j = i - gap
            while j >= 0 and nums[j] > tmp:
                nums[j + gap] = nums[j]
                j -= gap
            nums[j + gap] = tmp
        gap //= 2
    return nums


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int]], List[int]]], ...] = (
    ("built-in sort", sort_array_builtin),
    ("quick sort", sort_array_quick),
    ("merge sort", sort_array_merge),
    ("heap sort", sort_array_heap),
    ("counting sort", sort_array_counting),
    ("radix sort", sort_array_radix),
    ("shell sort", sort_array_shell),
)

_rng = random.Random(912)
_N = 50_000

CASES: Sequence[List[int]] = (
    [5, 2, 3, 1],
    [5, 1, 1, 2, 0, 0],
    [10, 9, 1, 1, 1, 2, 3, 1],  # NeetCode example 1
    [5, 10, 2, 1, 3],  # NeetCode example 2
    [1],  # the smallest input
    [2, 1],
    [1, 2, 3],  # three elements: the smallest partition
    [3, 3, 3, 3],  # all equal
    [-1, -3, -2, 0],  # negatives and zero
    [-50_000, 50_000, 0, -50_000],  # both value limits
    [100, -100, 10, -10, 1, -1, 0],  # several digit counts, both signs
    list(range(_N)),  # sorted, length ceiling
    list(range(_N, 0, -1)),  # reversed, length ceiling
    [7] * _N,  # all equal, length ceiling
    [_rng.randint(-50_000, 50_000) for _ in range(_N)],  # random, seeded
    [_rng.randint(-3, 3) for _ in range(_N)],  # many duplicates, seeded
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = 0
        for case in CASES:
            if solve(list(case)) == sorted(case):
                passed += 1
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()
