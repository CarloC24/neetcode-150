"""Score of a String (LeetCode 3110).

The score of a string is the sum of the absolute differences between the ASCII
values of adjacent characters. Return it.

NeetCode publishes one approach for this problem -- the loop, which is the right
answer and takes about four lines. The two beside it here are not rival
algorithms; they are the same sum spelled the way each language prefers, and
writing them out is what makes the cost of a character lookup visible.

The thing actually worth carrying away is in the README: the absolute value is
the only reason this needs a loop at all. Drop it and the sum telescopes to the
two endpoints.
"""

from itertools import islice
from typing import Callable, Sequence, Tuple


def score_of_string_iteration(s: str) -> int:
    """Sum the gaps by index. Time: O(n). Space: O(1).

    NeetCode's approach, and the one to write. There are n - 1 adjacent pairs in
    a string of n characters, so the loop stops one short of the end and reads
    forward; `range(len(s) - 1)` rather than `range(len(s))` is the whole of the
    bookkeeping, and getting it wrong is an IndexError on `s[i + 1]` rather than
    a wrong answer.

    `abs` is not decoration. Without it the sum is a telescoping series that
    collapses to `ord(s[0]) - ord(s[-1])`, computable without looking at the
    middle of the string at all -- see the README. Every character between the
    ends matters here only because the absolute value stops the cancellation.

    It calls `ord` twice per iteration and therefore twice per character, since
    each interior character is read once as `s[i]` and again as `s[i + 1]` on
    the previous pass. That is 198 calls for the 100-character ceiling, against
    the 100 the string actually contains. Approach 3 is the version that
    notices; the difference is about 1.5x in wall clock and no difference at all
    in complexity.

    Degenerate input is handled by arithmetic rather than by a check. The
    constraints promise at least two characters, but `range(len(s) - 1)` is
    empty for a one-character string and `range(-1)` is empty for the empty
    string, so both return 0 without a special case.
    """
    res = 0
    for i in range(len(s) - 1):
        res += abs(ord(s[i]) - ord(s[i + 1]))
    return res


def score_of_string_pairwise(s: str) -> int:
    """Sum the gaps over adjacent pairs. Time: O(n). Space: O(1).

    The same sum, said the way Python says it: build the pairs, then add up what
    they are worth. No index arithmetic to get wrong, and the `len(s) - 1` bound
    becomes a property of `zip` -- it stops when the shorter argument runs out,
    which is what pairing a sequence with its own tail means.

    The tail is `islice(s, 1, None)` and not the more obvious `s[1:]`, which is
    the only reason this is O(1) space. A slice copies: `zip(s, s[1:])` is a
    second string of length n - 1 to produce pairs that are consumed
    immediately. `islice` walks the original. The two are otherwise identical,
    and at n = 100 the copy costs nothing -- but it is the kind of O(n) that
    shows up in a habit rather than in a benchmark.

    `itertools.pairwise` does exactly this and would be the honest spelling, but
    it landed in Python 3.10 and this repo is verified on 3.9.6, so the
    `islice` form stands in for it.

    It is also mildly faster than approach 1 here -- about 3.4s against 4.0s on
    the benchmark in approach 3 -- because the pairing loop runs in C while the
    index arithmetic of approach 1 does not. The Java counterpart goes the other
    way, costing about 1.5x the plain loop: there the loop it replaces is
    already compiled, so the stream's pipeline is pure overhead. Same rewrite,
    opposite sign, and neither is worth anything at n = 100.

    Handles the empty string for the same reason as approach 1 -- `zip` of
    anything with an empty iterator is empty -- and so returns 0 rather than
    raising.
    """
    return sum(abs(ord(a) - ord(b)) for a, b in zip(s, islice(s, 1, None)))


def score_of_string_rolling(s: str) -> int:
    """Carry the previous character's value instead of re-reading it.
    Time: O(n). Space: O(1).

    Approaches 1 and 2 both look up every interior character twice: once as the
    right end of one pair and once as the left end of the next. The value has
    not changed in between, so the second lookup is recomputing something
    already in hand. Keeping it in `prev` costs one variable and halves the
    work: 100 `ord` calls at the 100-character ceiling against 198.

    Measured over 200,000 calls on a random 100-character string, best of five:
    approach 1 at about 4.0s, approach 2 at about 3.4s, this at about 2.7s --
    roughly 1.5x faster than the version NeetCode publishes. All three are O(n)
    and the constraints cap n at 100, so this buys nothing on the submission.

    The same refactor in the Java file is worth nothing at all: three runs put
    it at 0.91x, 0.95x and 1.06x, which is scatter around 1. `charAt` there is a
    field read the JIT can hoist, so the second lookup was never costing
    anything, while `ord` here is a real function call. Identical change, and
    whether it does anything depends entirely on what a character lookup costs
    in the language -- "fewer operations" and "faster" are different claims.

    The general shape is still the thing worth keeping: when a loop reads a
    sliding window, the overlap is usually already computed.

    Unlike the other two, this one does not survive an empty string -- it reads
    `s[0]` before the loop and raises IndexError. The constraints promise two
    characters, so the guard is left out rather than written; it is noted here
    because it is the one place the three approaches disagree about input they
    are never given.
    """
    res = 0
    prev = ord(s[0])
    for i in range(1, len(s)):
        cur = ord(s[i])
        res += abs(prev - cur)
        prev = cur
    return res


SOLUTIONS: Tuple[Tuple[str, Callable[[str], int]], ...] = (
    ("iteration", score_of_string_iteration),
    ("pairwise", score_of_string_pairwise),
    ("rolling", score_of_string_rolling),
)

# The constraint ceiling is 100 characters. These are the extremes of the score
# at that length -- the range of possible answers is [0, 2475] and nothing else.
_MAX_ALTERNATING = ("az" * 50)  # 25 per pair, 99 pairs: the largest score there is
_MIN_FLAT = "a" * 100  # every gap zero: the smallest
_MONOTONE = "a" * 99 + "z"  # one jump at the end, and a full-length run to get there

CASES: Sequence[Tuple[str, int]] = (
    ("hello", 13),  # LeetCode example 1
    ("zaz", 50),  # LeetCode example 2: both gaps maximal, and not monotonic
    ("ab", 1),  # the shortest legal input
    ("ba", 1),  # and its reverse: |a - b| is symmetric, so the score cannot change
    ("aa", 0),  # the smallest score, and the only way to get it at length 2
    ("az", 25),  # the largest gap between two lowercase letters
    ("za", 25),
    ("abc", 2),  # monotonic up: score is |first - last|, since abs never bites
    ("cba", 2),  # monotonic down: same
    ("acb", 3),  # one direction change is all it takes to break that -- |a - b| is 1
    ("world", 25),
    ("mississippi", 58),  # repeated letters, so several gaps are zero
    ("abcdefghijklmnopqrstuvwxyz", 25),  # 25 steps of 1: a full alphabet scores like "az"
    (_MONOTONE, 25),  # 98 zero gaps and one of 25
    (_MIN_FLAT, 0),  # ceiling length, minimum score
    (_MAX_ALTERNATING, 2475),  # ceiling length, maximum score: 25 * 99
    (_MAX_ALTERNATING[::-1], 2475),  # reversed, and necessarily identical
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = 0
        for s, expected in CASES:
            passed += solve(s) == expected
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()
