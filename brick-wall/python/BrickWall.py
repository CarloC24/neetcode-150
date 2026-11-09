"""Brick Wall (LeetCode 554).

A wall has n rows of bricks, each one unit tall, every row the same total
width. Draw one vertical line from top to bottom that crosses the fewest
bricks, and return that number. A line along the edge between two bricks
doesn't cross either one. The wall's own left and right edges don't count.

The two approaches from https://neetcode.io/solutions/brick-wall.
"""

import random
from collections import defaultdict
from typing import Callable, Dict, List, Sequence, Tuple


def least_bricks_brute_force(wall: List[List[int]]) -> int:
    """Try every line position 1 .. width - 1.
    Time: O(width * n). Space: O(N) for N bricks.

    First record where each row's brick edges are (its running widths,
    leaving out the wall's right edge). Then, for each position, count the
    rows that have no edge there: that's how many bricks a line there
    crosses.

    NeetCode keeps each row's edges in a list, which makes every check a
    linear scan. A set makes it O(1). Either way, the loop runs once per unit
    of width, and widths go up to about 2 * 10^9 per brick, so the harness
    only runs this on narrow walls.
    """
    width = sum(wall[0])
    edges = []
    for row in wall:
        pos, row_edges = 0, set()
        for brick in row[:-1]:
            pos += brick
            row_edges.add(pos)
        edges.append(row_edges)

    res = len(wall)  # a line can never cross more than one brick per row
    for line in range(1, width):
        crossed = sum(1 for row_edges in edges if line not in row_edges)
        res = min(res, crossed)
    return res


def least_bricks_hash_map(wall: List[List[int]]) -> int:
    """Count the rows with an edge at each position.
    Time: O(N) for N bricks. Space: O(N).

    Flip the question: a line crosses one brick in every row that has no
    edge where the line is. So the best line goes where the most rows have an
    edge, and the answer is n minus that count.

    Only positions where some row has an edge can be best, so there's no
    need to try the others: walk each row, add up the widths, and count each
    running total. Skip each row's last brick, whose right edge is the wall's
    right edge. The max starts at 0 for walls with no inner edges at all,
    like [[1], [1], [1]], where every line crosses every row.
    """
    edge_count: Dict[int, int] = defaultdict(int)
    for row in wall:
        pos = 0
        for brick in row[:-1]:  # the last brick ends at the wall's edge
            pos += brick
            edge_count[pos] += 1
    return len(wall) - max(edge_count.values(), default=0)


SOLUTIONS: Tuple[Tuple[str, Callable[[List[List[int]]], int]], ...] = (
    ("brute force", least_bricks_brute_force),
    ("hash map", least_bricks_hash_map),
)

BRUTE_FORCE_MAX_WIDTH = 10_000

_BIG = 2**31 - 1
_rng = random.Random(554)


def _random_row(width: int, max_brick: int) -> List[int]:
    row, left = [], width
    while left:
        brick = _rng.randint(1, min(max_brick, left))
        row.append(brick)
        left -= brick
    return row


# (wall, expected). Expected is None where it's checked against brute force.
CASES: Sequence[Tuple[List[List[int]], object]] = (
    ([[1, 2, 2, 1], [3, 1, 2], [1, 3, 2], [2, 4], [3, 1, 2], [1, 3, 1, 1]], 2),
    ([[1], [1], [1]], 3),  # no inner edges: every line crosses every row
    ([[1, 1]], 0),  # one row: the line goes through its edge
    ([[5]], 1),  # one row, one brick
    ([[1, 2], [2, 1]], 1),  # two edges, one row each
    ([[3], [1, 2], [2, 1]], 2),  # one full-width row
    ([[2, 2], [2, 2], [1, 3]], 1),  # edge at 2 in two rows, not the third
    # Brick widths at the limit: the running width passes 2^31 - 1, which
    # overflows a Java int. The best line is at 2^31 - 1 (rows 1 and 2).
    ([[_BIG, _BIG], [_BIG, _BIG], [_BIG - 1, 2, _BIG - 1]], 1),
    # Every edge is in a different place, so the answer is 1. In a Java int,
    # row 2's edge at 2^32 + 1 wraps around to 1, lands on row 1's edge at 1,
    # and the answer comes out 0.
    ([[1, _BIG, _BIG, _BIG - 1], [_BIG, _BIG, 3, _BIG - 3]], 1),
    ([[1, 1] for _ in range(10_000)], 0),  # 10,000 rows, the row limit
    ([[1] * 20_000], 0),  # 20,000 bricks in one row, the brick limit
    # random walls, checked against brute force (seeded)
    *(([_random_row(60, 6) for _ in range(30)], None) for _ in range(5)),
    ([_random_row(1_000, 50) for _ in range(200)], None),
)


def main() -> None:
    cases = [
        (wall, least_bricks_brute_force(wall) if expected is None else expected)
        for wall, expected in CASES
    ]
    for name, solve in SOLUTIONS:
        todo = cases
        if solve is least_bricks_brute_force:
            todo = [c for c in cases if sum(c[0][0]) <= BRUTE_FORCE_MAX_WIDTH]
        passed = sum(solve(wall) == expected for wall, expected in todo)
        status = "PASS" if passed == len(todo) else "FAIL"
        skipped = len(cases) - len(todo)
        note = f" (skips {skipped} too wide to scan)" if skipped else ""
        print(f"{status}  {name}: {passed}/{len(todo)} cases{note}")


if __name__ == "__main__":
    main()
