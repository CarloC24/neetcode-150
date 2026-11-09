# Brick Wall

LeetCode 554 · [NeetCode solution page](https://neetcode.io/solutions/brick-wall)

A wall has `n` rows of bricks. Every brick is one unit tall, bricks in a row
can have different widths, and every row has the same total width. Draw one
vertical line from top to bottom that crosses as few bricks as possible, and
return how many it crosses.

A line along the edge between two bricks doesn't cross either of them. A line
along the wall's own left or right edge isn't allowed.

```
wall = [[1,2,2,1], [3,1,2], [1,3,2], [2,4], [3,1,2], [1,3,1,1]]

        0   1   2   3   4   5   6
row 0   |1  |2      |2      |1  |
row 1   |3          |1  |2      |
row 2   |1  |3          |2      |
row 3   |2      |4              |
row 4   |3          |1  |2      |
row 5   |1  |3          |1  |1  |
                        ^
```

A line at position 4 runs along edges in rows 1, 2, 4 and 5 and crosses only
rows 0 and 3, so the answer is `2`. `[[1], [1], [1]]` has no inner edges at all,
so any line crosses all three rows: `3`.

**Constraints:** `1 <= n <= 10^4`, `1 <= wall[i].length <= 10^4`, at most
`2 * 10^4` bricks in total, `1 <= wall[i][j] <= 2^31 - 1`, and every row has
the same total width.

```
brick-wall/
├── java/
│   └── BrickWall.java
├── python/
│   └── BrickWall.py
└── README.md
```

Each file implements both approaches side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Brute force | O(width · n) | O(N) | Try every position, count the rows with no edge there. |
| 2 | **Hash map** | **O(N)** | **O(N)** | Count edges per position; answer is `n` minus the most. |

`N` is the total number of bricks (at most 20,000) and `width` is the wall's
width (up to about 2 · 10^13: 10,000 bricks of width 2^31 - 1). Both approaches are NeetCode's.

**Approach 2 is the one to use.** Brute force loops once per unit of width,
and a wall can be trillions of units wide, so it only works on narrow walls.

## The idea: count edges, not bricks

A line crosses exactly one brick in every row that doesn't have an edge where
the line is. So:

```
bricks crossed  =  n  -  (rows with an edge at the line)
```

Minimizing bricks crossed is the same as maximizing edges lined up. And the
best line has to be at a position where at least one row has an edge, unless
there are no inner edges at all. So instead of trying every position, record
every edge position:

```python
for row in wall:
    pos = 0
    for brick in row[:-1]:     # leave out the wall's right edge
        pos += brick           # running width = where this brick's right edge is
        edge_count[pos] += 1
return len(wall) - max(edge_count.values(), default=0)
```

The edge positions are the row's running widths (its prefix sums), and the
hash map counts how many rows share each one. On the example, position 4 gets 4
and nothing gets more, so the answer is `6 - 4 = 2`.

## Two easy ways to get it wrong

| Bug | Smallest input that breaks it | Result |
|---|---|---|
| Counting each row's last brick too | `[[1], [1]]` | `0`, from a line on the wall's right edge. The answer is `2`. |
| `max()` of an empty map | `[[1], [1], [1]]` | Crashes in Python. Start the max at 0 (`default=0`, or NeetCode's `{0: 0}`). |

The first one is the important one. Every row's right edge is at the same
position, the wall's width, so counting it always "finds" a perfect line.

## Overflow in Java

A single brick can be `2^31 - 1` wide, so a row's running width can go past
`Integer.MAX_VALUE`. NeetCode's Java keeps positions in an `int`. These files
use `long`.

`int` doesn't fail right away. Wrapped values stay distinct until two positions
differ by exactly 2^32, which needs a row wider than 2^32. So the test set
includes this wall:

```
[[1, 2^31-1, 2^31-1, 2^31-2],
 [2^31-1, 2^31-1, 3, 2^31-4]]
```

Row 0 has an edge at 1. Row 1 has one at 2^32 + 1, which an `int` wraps to 1.
All the edges are really in different places, so the answer is 1, but with
`int` keys the two edges collide and the answer comes out 0. (I checked that an
`int` version returns 0 here.) Python's integers don't overflow.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 brick-wall/python/BrickWall.py
```

Or from inside the folder:

```bash
cd brick-wall/python
python3 BrickWall.py
```

Expected output:

```
PASS  brute force: 14/14 cases (skips 3 too wide to scan)
PASS  hash map: 17/17 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java brick-wall/java/BrickWall.java
```

### Option 2 — compile, then run

```bash
cd brick-wall/java
javac BrickWall.java   # produces BrickWall.class
java BrickWall         # note: no .class extension
```

Expected output (either option):

```
PASS  brute force: 14/14 cases (skips 3 too wide to scan)
PASS  hash map: 17/17 cases
```

To clean up the compiled artifacts from option 2:

```bash
rm brick-wall/java/BrickWall*.class
```

## Notes

- **Test cases:** the example, a wall with no inner edges, one row, one brick,
  a full-width row among others, the two overflow walls (bricks of width
  2^31 - 1), 10,000 rows (the row limit), 20,000 bricks in one row (the brick
  limit), and six random walls (seeded with `554`) checked against brute
  force. Brute force skips the three walls wider than 10,000.
- **Brute force uses sets.** NeetCode keeps each row's edges in a list, so each
  check is a linear scan and the total is O(width · n · g) for `g` edges per
  row. A set makes each check O(1).
- **Verified exhaustively, in both languages:** every wall of width 1–6 with
  1–3 rows, and width 1–5 with 4 rows (108,782 walls, 217,564 checks per
  language), against a separate reference that counts, for each line
  position, the bricks that strictly span it. Zero failures in either
  language.
- **The Java signature is LeetCode's** `List<List<Integer>>`. Positions are
  `long`, the bricks stay `Integer`.
- **Method naming:** LeetCode uses `leastBricks`. These files use
  `leastBricks*` / `least_bricks_*` with an approach suffix, so both can
  coexist. Rename to plain `leastBricks` when submitting.
- The Java file is named `BrickWall.java` to match its `public class
  BrickWall`, as `javac` requires.
