"""Number of Students Unable to Eat Lunch (LeetCode 1700).

Students stand in a queue and sandwiches sit in a stack, each a 0 (circular)
or a 1 (square). The front student takes the top sandwich if it's the type
they want; otherwise they go to the back of the queue. This stops when no one
left in the queue wants the top sandwich. Return how many students can't eat.

The three approaches from
https://neetcode.io/solutions/number-of-students-unable-to-eat-lunch,
ordered from full simulation to counting.
"""

from collections import deque
from typing import Callable, List, Sequence, Tuple


def count_students_queue(students: List[int], sandwiches: List[int]) -> int:
    """Simulate the queue with a deque. Time: O(n^2). Space: O(n).

    For each sandwich, rotate the queue until the front student wants it. If
    a full rotation of n moves finds no one, nobody left wants it, and every
    remaining student goes hungry. Each sandwich can cost up to n rotations.

    The queue never holds more than n students, so n moves always cover
    everyone still in it.
    """
    n = len(students)
    q = deque(students)
    res = n
    for sandwich in sandwiches:
        cnt = 0
        while cnt < n and q[0] != sandwich:
            q.append(q.popleft())
            cnt += 1
        if q[0] == sandwich:
            q.popleft()
            res -= 1
        else:
            break
    return res


def count_students_circular(students: List[int], sandwiches: List[int]) -> int:
    """Walk the students array in a circle instead of rotating a queue.
    Time: O(n^2). Space: O(1). Overwrites students.

    The same simulation without the deque. A student who has eaten is marked
    -1, which matches no sandwich, so the walk steps over them. idx stays
    where the last student ate, which is where the next round starts, just
    like the front of the rotated queue.
    """
    n = len(students)
    idx = 0
    res = n
    for sandwich in sandwiches:
        cnt = 0
        while cnt < n and students[idx] != sandwich:
            idx = (idx + 1) % n
            cnt += 1
        if students[idx] == sandwich:
            students[idx] = -1
            res -= 1
        else:
            break
    return res


def count_students_counting(students: List[int], sandwiches: List[int]) -> int:
    """Count each preference; serve sandwiches until a type runs out.
    Time: O(n). Space: O(1).

    The optimal solution. The queue keeps rotating, so any student who wants
    the top sandwich reaches the front eventually. Their position doesn't
    matter, only whether one exists. Two counters replace the queue. The
    first sandwich whose type has no takers left ends it, and everyone still
    waiting is the answer. See the README.
    """
    cnt = [0, 0]
    for s in students:
        cnt[s] += 1
    res = len(students)
    for sandwich in sandwiches:
        if cnt[sandwich] == 0:
            break
        cnt[sandwich] -= 1
        res -= 1
    return res


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int], List[int]], int]], ...] = (
    ("queue", count_students_queue),
    ("circular index", count_students_circular),
    ("counting", count_students_counting),
)

MUTATES_INPUT = {"circular index"}

CASES: Sequence[Tuple[List[int], List[int], int]] = (
    ([1, 1, 0, 0], [0, 1, 0, 1], 0),
    ([1, 1, 1, 0, 0, 1], [1, 0, 0, 0, 1, 1], 3),
    ([0], [0], 0),  # the smallest input
    ([0], [1], 1),
    ([1, 1, 1], [0, 1, 1], 3),  # stuck on the first sandwich: nobody eats
    ([0, 1], [1, 0], 0),  # order never matters: everyone eats
    ([0, 0, 1], [1, 1, 0], 2),  # the only 1 eats, then the second 1 is stuck
    ([1, 0, 1], [1, 1, 0], 0),
    ([0] * 100, [0] * 99 + [1], 1),  # length ceiling: stuck on the last sandwich
    ([0, 1] * 50, [0] * 50 + [1] * 50, 0),  # ceiling: everyone eats
    ([1] * 99 + [0], [0] + [1] * 99, 0),  # ceiling: 99 rotations before the first meal
    ([1] * 100, [0] + [1] * 99, 100),  # ceiling: stuck at once, 99 sandwiches unserved
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(
            solve(list(students), list(sandwiches)) == expected
            for students, sandwiches, expected in CASES
        )
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")

    students, sandwiches = [1, 1, 0, 0], [0, 1, 0, 1]
    for name, solve in SOLUTIONS:
        if name not in MUTATES_INPUT:
            solve(students, sandwiches)
    status = "PASS" if (students, sandwiches) == ([1, 1, 0, 0], [0, 1, 0, 1]) else "FAIL"
    print(f"{status}  input left unmodified (all but circular index): {students}, {sandwiches}")


if __name__ == "__main__":
    main()
