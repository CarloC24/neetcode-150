"""Time Needed to Buy Tickets (LeetCode 2073).

`n` people queue to buy tickets; person `i` wants `tickets[i]` of them. Each
second the person at the front buys exactly one ticket and then rejoins the back
of the line if they still want more. Return the second at which person `k`
finishes.

All three approaches from
https://neetcode.io/solutions/time-needed-to-buy-tickets, ordered from
simulation to arithmetic. There is no fourth worth writing: the progression
already goes brute force -> same thing without the queue -> closed form, and
that is the whole ladder.

The two simulations DESTROY the array they are given. That is not a flaw in
NeetCode's code -- LeetCode hands the solution a private copy -- but it makes
these three functions non-interchangeable in a way their signatures do not
admit, so the harness below gives every approach a fresh list. See the README.
"""

from collections import deque
from typing import Callable, List, Sequence, Tuple


def time_required_to_buy_queue(tickets: List[int], k: int) -> int:
    """Simulate the line with a real queue. Time: O(answer). Space: O(n).

    The literal reading of the problem, and the one to write first because it
    cannot be wrong: put everyone in a queue, sell one ticket per second to
    whoever is at the front, send them to the back if they still want more, and
    stop the moment person k's count hits zero.

    "O(n * m)" is how the complexity is usually quoted, with m the largest
    ticket count, and it is a correct upper bound rather than the actual cost.
    The loop runs exactly once per ticket sold and returns at `time`, so the
    iteration count *is* the return value -- verified equal on all 2,930
    exhaustive cases below n = 5. The bound that matters is therefore the bound
    on the answer itself, which the constraints cap at 100 x 100 = 10,000.

    **This mutates `tickets`.** Every decrement lands in the caller's list, and
    when this returns, everyone at or before k holds 0. Anything reading the
    array afterwards is reading wreckage -- approach 3 handed the same list
    returns 0. LeetCode never notices because it passes a throwaway copy.

    The `if tickets[cur] == 0` / `else` split is doing two jobs at once: it
    decides whether the person rejoins the line, and it is the only place the
    answer can be returned. A person who still wants tickets goes to the back; a
    person who is finished simply is not re-appended, and if they were person k
    the current second is the answer.
    """
    n = len(tickets)
    q = deque(range(n))

    time = 0
    while q:
        time += 1
        cur = q.popleft()
        tickets[cur] -= 1
        if tickets[cur] == 0:
            if cur == k:
                return time
        else:
            q.append(cur)

    return time


def time_required_to_buy_circular(tickets: List[int], k: int) -> int:
    """Walk the line in a circle, skipping anyone already finished.
    Time: O(answer). Space: O(1).

    The same simulation with the queue deleted. A queue of indices that always
    holds some subset of 0..n-1 in their original rotational order is not
    carrying any information a modular index cannot: `idx = (idx + 1) % n` is
    "go to the next person", and the inner loop skipping zeros is "and skip the
    ones who have left". That trades the O(n) deque for O(1).

    It is also slightly faster here -- 2.19ms against 2.39ms at the ceiling --
    because the deque operations it removes are real interpreter work. The Java
    file measures the opposite: there this is the *slowest* of the three, since
    the boxing it avoids was already free (indices below 128 are cached
    Integers) and it pays a `%` per simulated second in exchange. Same rewrite,
    opposite sign, as in Score of a String.

    The inner skip loop looks like it could make this quadratic and does not,
    for the usual amortised reason: a person is skipped only after reaching
    zero, and each zero is created once.

    **This also mutates `tickets`**, exactly as approach 1 does.

    Worth noticing that this function has no termination proof of its own. Its
    only exit is the `return`, and the inner `while tickets[idx] == 0` has no
    bound -- it is trusting that person k is still owed a ticket, so a
    non-finished person always exists to land on. Hand it an array that is
    already all zeros and it spins forever rather than raising; measured, it
    does not return within a second. The constraints promise `tickets[i] >= 1`,
    so the trust is well placed, but it is trust and not a guard. Approach 1 has
    a real bound -- `while q` empties -- and returns `time` on the same input.
    """
    n = len(tickets)
    idx = 0

    time = 0
    while True:
        time += 1
        tickets[idx] -= 1
        if tickets[idx] == 0:
            if idx == k:
                return time
        idx = (idx + 1) % n
        while tickets[idx] == 0:
            idx = (idx + 1) % n


def time_required_to_buy_direct(tickets: List[int], k: int) -> int:
    """Count what each person can possibly buy before k finishes.
    Time: O(n). Space: O(1).

    The one to write. Nothing needs simulating, because the clock stops at a
    known instant -- the moment person k buys their last ticket -- and every
    other person's contribution to that instant can be read off directly.

    Person k needs `tickets[k]` turns. So by the time they take their last one:

    - Someone **at or before** k in line reaches the counter on every one of
      those turns, including the last. They buy `min(tickets[i], tickets[k])` --
      whichever runs out first, their own demand or the clock.
    - Someone **behind** k gets one fewer opportunity, because k's final
      purchase ends the process before the line wraps around to them. They buy
      `min(tickets[i], tickets[k] - 1)`.

    The answer is the total. Both halves are `min` against a bound k sets, and
    the only difference is that one bound is a turn smaller.

    Two one-character mistakes live here, and they fail differently:

    Dropping the `- 1` for people behind k overcounts, and only sometimes: it is
    wrong on 1,560 of the 2,930 exhaustive cases -- just over half -- so it
    survives a careless test set. It is right exactly when nobody behind k
    wanted that extra turn anyway (`tickets[i] <= tickets[k] - 1` throughout).

    Writing `i < k` instead of `i <= k` sends person k down the wrong branch,
    where they buy `min(tickets[k], tickets[k] - 1)` -- one short, always. That
    is wrong on all 2,930 cases and always by exactly 1, which is the easier bug
    to catch and the easier one to misread as an off-by-one in the clock.

    Unlike the two simulations, this reads `tickets` and never writes to it,
    which is why it is the only one of the three that can be handed the same
    list twice.
    """
    res = 0
    for i in range(len(tickets)):
        if i <= k:
            res += min(tickets[i], tickets[k])
        else:
            res += min(tickets[i], tickets[k] - 1)
    return res


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int], int], int]], ...] = (
    ("queue", time_required_to_buy_queue),
    ("circular", time_required_to_buy_circular),
    ("direct", time_required_to_buy_direct),
)

# The constraint ceiling is n = 100 with tickets up to 100, and these are the
# corners of it. The answer can only ever land in [1, 10000].
_MAX = ((100,) * 100, 99, 10000)  # every ticket at the cap, k last: the largest answer there is
_MAX_FRONT = ((100,) * 100, 0, 9901)  # the same line with k at the front: 99 people lose a turn
_FLAT_LAST = ((1,) * 100, 99, 100)  # one ticket each: the answer is just the queue position
_FLAT_FIRST = ((1,) * 100, 0, 1)  # and at the front it is one second

# Cases are tuples, not lists, so the case data itself cannot be damaged by the
# two approaches that mutate -- the harness makes each of them a fresh list.
CASES: Sequence[Tuple[Sequence[int], int, int]] = (
    ((2, 3, 2), 2, 6),  # LeetCode example 1: k at the back, and everyone is capped by k
    ((5, 1, 1, 1), 0, 8),  # LeetCode example 2: k at the front wanting far more than anyone else
    ((1,), 0, 1),  # the smallest legal input in every dimension
    ((1, 1, 1), 0, 1),  # k first, one ticket each: nobody else ever buys
    ((1, 1, 1), 2, 3),  # k last, one ticket each: the answer is k + 1
    ((2, 3, 2), 0, 4),  # example 1's array with k moved to the front
    ((3, 3, 3), 1, 8),  # k in the middle, everyone equal: 3 + 3 + 2
    ((100, 1, 1), 0, 102),  # k wants everything, the others are done in one round
    ((1, 1, 100), 2, 102),  # the same demand at the back of the line
    ((1, 100, 1), 1, 102),  # and in the middle
    ((2, 1, 2), 1, 2),  # k finishes early while others are still owed tickets
    (_FLAT_FIRST[0], _FLAT_FIRST[1], _FLAT_FIRST[2]),  # ceiling length, minimum answer
    (_FLAT_LAST[0], _FLAT_LAST[1], _FLAT_LAST[2]),
    ((100,) * 99 + (1,), 99, 100),  # k last but wanting only one: everyone ahead is capped at 1
    (_MAX_FRONT[0], _MAX_FRONT[1], _MAX_FRONT[2]),
    (_MAX[0], _MAX[1], _MAX[2]),  # ceiling length, maximum answer: 100 * 100
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = 0
        for tickets, k, expected in CASES:
            # list(tickets) per approach, not per case: two of the three consume
            # what they are given, so sharing one list would test the second
            # approach against the first one's leftovers.
            passed += solve(list(tickets), k) == expected
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()
