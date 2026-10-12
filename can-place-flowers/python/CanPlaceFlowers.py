"""Can Place Flowers (LeetCode 605).

Given a flowerbed of 0s (empty) and 1s (planted), where no two flowers are
adjacent, return True if n new flowers can be planted without any two flowers
ending up adjacent.

The two approaches from https://neetcode.io/solutions/can-place-flowers, plus
the in-place greedy LeetCode publishes and a version of it that leaves the
input alone and stops early, ordered from the most memory to the least.
"""

from typing import Callable, List, Sequence, Tuple


def can_place_flowers_padded(flowerbed: List[int], n: int) -> bool:
    """Pad with an empty plot at each end, then plant greedily.
    Time: O(L). Space: O(L).

    L is len(flowerbed); the problem already uses n for the number of flowers.
    The padding means every real plot has a left and a right neighbour, so the
    "is it at the edge?" checks disappear. The price is a full copy of the
    input. Planting writes to the copy, which is why the next plot sees the
    flower just placed.
    """
    f = [0] + flowerbed + [0]
    for i in range(1, len(f) - 1):
        if f[i - 1] == 0 and f[i] == 0 and f[i + 1] == 0:
            f[i] = 1
            n -= 1
    return n <= 0


def can_place_flowers_in_place(flowerbed: List[int], n: int) -> bool:
    """Plant greedily into the input itself. Time: O(L). Space: O(1).

    The same greedy with the edges checked explicitly instead of padded away:
    a missing neighbour counts as empty. It is O(1) extra space only because it
    writes the new flowers into the caller's list -- the input comes back
    changed. The test harness passes each approach a copy for exactly this
    reason.
    """
    planted = 0
    for i in range(len(flowerbed)):
        left_empty = i == 0 or flowerbed[i - 1] == 0
        right_empty = i == len(flowerbed) - 1 or flowerbed[i + 1] == 0
        if flowerbed[i] == 0 and left_empty and right_empty:
            flowerbed[i] = 1
            planted += 1
    return planted >= n


def can_place_flowers_greedy(flowerbed: List[int], n: int) -> bool:
    """Plant greedily, skip past each new flower, stop once n fit.
    Time: O(L). Space: O(1).

    The optimal solution. After planting at i, plot i + 1 can never take a
    flower, so jump straight to i + 2. That jump is what stands in for writing
    the flower down: the only plot that would ever have read it is the one
    skipped. So the input is never touched, and the left neighbour never needs
    checking -- it is either the edge, an original empty plot, or a plot just
    skipped because the one before it was planted.

    It also returns as soon as n flowers fit, rather than walking the rest of
    the bed. n == 0 returns True before looking at anything.
    """
    i = 0
    length = len(flowerbed)
    while i < length and n > 0:
        if flowerbed[i] == 1:
            i += 2  # the next plot is next to a flower
        elif i + 1 < length and flowerbed[i + 1] == 1:
            i += 3  # this plot touches the flower at i + 1; so does i + 2
        else:
            n -= 1  # plant here: the left side is clear by construction
            i += 2
    return n <= 0


def can_place_flowers_count_runs(flowerbed: List[int], n: int) -> bool:
    """Count each run of empty plots and how many it can hold.
    Time: O(L). Space: O(1).

    A run of k empty plots between two flowers holds (k - 1) // 2: the plots
    touching each flower are out, and the k - 2 left alternate. A run at an edge
    has only one flower to avoid, which works out the same as pretending there
    is one extra empty plot past the edge. So the count starts at 1 if the bed
    starts empty, and the run left over at the end is charged empty // 2 rather
    than (empty - 1) // 2.

    NeetCode writes the middle case as int((empty - 1) / 2), not
    (empty - 1) // 2, and the difference matters. When the bed starts with a
    flower, empty is 0 at that first flower, and (0 - 1) // 2 is -1 in Python,
    because // rounds down, not toward zero. Subtracting -1 adds one to n, so
    every bed that starts with a flower demands one flower more than asked:
    [1] with n = 0 returns False. int(-0.5) truncates to 0. Java's / already truncates, so the bug only
    exists in the Python spelling. The max(0, ...) here makes the intent
    explicit instead of relying on either rounding rule.
    """
    empty = 0 if flowerbed[0] else 1
    for plot in flowerbed:
        if plot:
            n -= max(0, (empty - 1) // 2)
            empty = 0
        else:
            empty += 1
    n -= empty // 2
    return n <= 0


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int], int], bool]], ...] = (
    ("padded copy", can_place_flowers_padded),
    ("in place", can_place_flowers_in_place),
    ("greedy, skip ahead", can_place_flowers_greedy),
    ("count empty runs", can_place_flowers_count_runs),
)

MUTATES_INPUT = {"in place"}

CASES: Sequence[Tuple[List[int], int, bool]] = (
    ([1, 0, 0, 0, 1], 1, True),
    ([1, 0, 0, 0, 1], 2, False),
    ([0], 1, True),  # a single empty plot has no neighbours at all
    ([1], 0, True),  # n == 0 fits in any bed, even a full one
    ([1], 1, False),
    ([0, 0], 1, True),  # an edge run of 2 holds 1
    ([0, 0], 2, False),
    ([1, 0, 1], 1, False),  # a gap of one, between two flowers, holds nothing
    ([1, 0, 0, 1], 1, False),  # a gap of two holds nothing either
    ([1, 0, 0, 0, 0, 1], 2, False),  # a gap of four holds only one
    ([0, 0, 1, 0, 0], 2, True),  # one at each edge
    ([1, 0, 0, 0, 0, 0, 1], 2, True),  # a gap of five holds two
    ([0, 0, 0, 0, 0], 3, True),  # an all-empty bed of 5 holds 3
    ([0, 0, 0, 0, 0], 4, False),
    ([1, 0, 0, 0, 0], 2, True),  # starts with a flower; see the // note above
    ([1, 0, 1, 0, 1], 0, True),
    ([0] * 20000, 10000, True),  # the length ceiling, every other plot
    ([0] * 20000, 10001, False),
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(
            solve(list(flowerbed), n) == expected for flowerbed, n, expected in CASES
        )
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")

    original = [1, 0, 0, 0, 1]
    for name, solve in SOLUTIONS:
        if name not in MUTATES_INPUT:
            solve(original, 1)
    status = "PASS" if original == [1, 0, 0, 0, 1] else "FAIL"
    print(f"{status}  input left unmodified (all but in place): {original}")


if __name__ == "__main__":
    main()
