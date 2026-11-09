import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.ToIntFunction;

/**
 * Brick Wall (LeetCode 554).
 *
 * <p>A wall has n rows of bricks, each one unit tall, every row the same total
 * width. Draw one vertical line from top to bottom that crosses the fewest
 * bricks, and return that number. A line along the edge between two bricks
 * doesn't cross either one. The wall's own left and right edges don't count.
 *
 * <p>The two approaches from https://neetcode.io/solutions/brick-wall.
 *
 * <p>Positions are longs. A single brick can be 2^31 - 1 wide, so a row's
 * running width can pass Integer.MAX_VALUE.
 */
public class BrickWall {

    static final long BRUTE_FORCE_MAX_WIDTH = 10_000;

    /**
     * Tries every line position 1 .. width - 1.
     * Time: O(width * n). Space: O(N) for N bricks.
     *
     * <p>First record where each row's brick edges are (its running widths,
     * leaving out the wall's right edge). Then, for each position, count the
     * rows that have no edge there: that's how many bricks a line there
     * crosses.
     *
     * <p>NeetCode's Java keeps each row's edges in a List, which makes every
     * contains() a linear scan. A HashSet makes it O(1). Either way, the loop
     * runs once per unit of width, so the harness only runs this on narrow
     * walls.
     */
    static int leastBricksBruteForce(List<List<Integer>> wall) {
        long width = 0;
        for (int brick : wall.get(0)) {
            width += brick;
        }

        List<Set<Long>> edges = new ArrayList<>();
        for (List<Integer> row : wall) {
            Set<Long> rowEdges = new HashSet<>();
            long pos = 0;
            for (int i = 0; i < row.size() - 1; i++) {
                pos += row.get(i);
                rowEdges.add(pos);
            }
            edges.add(rowEdges);
        }

        int res = wall.size(); // a line can never cross more than one brick per row
        for (long line = 1; line < width; line++) {
            int crossed = 0;
            for (Set<Long> rowEdges : edges) {
                if (!rowEdges.contains(line)) {
                    crossed++;
                }
            }
            res = Math.min(res, crossed);
        }
        return res;
    }

    /**
     * Counts the rows with an edge at each position.
     * Time: O(N) for N bricks. Space: O(N).
     *
     * <p>Flip the question: a line crosses one brick in every row that has no
     * edge where the line is. So the best line goes where the most rows have an
     * edge, and the answer is n minus that count.
     *
     * <p>Only positions where some row has an edge can be best, so there's no
     * need to try the others: walk each row, add up the widths, and count each
     * running total. Skip each row's last brick, whose right edge is the wall's
     * right edge. maxEdges starts at 0 for walls with no inner edges at all,
     * like [[1], [1], [1]], where every line crosses every row. Tracking the
     * max while counting saves a pass over the map.
     */
    static int leastBricksHashMap(List<List<Integer>> wall) {
        Map<Long, Integer> edgeCount = new HashMap<>();
        int maxEdges = 0;
        for (List<Integer> row : wall) {
            long pos = 0;
            for (int i = 0; i < row.size() - 1; i++) { // the last brick ends at the wall's edge
                pos += row.get(i);
                maxEdges = Math.max(maxEdges, edgeCount.merge(pos, 1, Integer::sum));
            }
        }
        return wall.size() - maxEdges;
    }

    private static List<Integer> randomRow(Random rng, int width, int maxBrick) {
        List<Integer> row = new ArrayList<>();
        int left = width;
        while (left > 0) {
            int brick = 1 + rng.nextInt(Math.min(maxBrick, left));
            row.add(brick);
            left -= brick;
        }
        return row;
    }

    private static List<List<Integer>> randomWall(Random rng, int rows, int width, int maxBrick) {
        List<List<Integer>> wall = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            wall.add(randomRow(rng, width, maxBrick));
        }
        return wall;
    }

    private static List<List<Integer>> wallOf(int[][] rows) {
        List<List<Integer>> wall = new ArrayList<>();
        for (int[] row : rows) {
            List<Integer> list = new ArrayList<>();
            for (int brick : row) {
                list.add(brick);
            }
            wall.add(list);
        }
        return wall;
    }

    public static void main(String[] args) {
        final int big = Integer.MAX_VALUE;
        Random rng = new Random(554);

        // (wall, expected). Expected is null where it's checked against brute force.
        List<Object[]> cases = new ArrayList<>();
        cases.add(new Object[] {wallOf(new int[][] {
            {1, 2, 2, 1}, {3, 1, 2}, {1, 3, 2}, {2, 4}, {3, 1, 2}, {1, 3, 1, 1}}), 2});
        cases.add(new Object[] {wallOf(new int[][] {{1}, {1}, {1}}), 3}); // no inner edges
        cases.add(new Object[] {wallOf(new int[][] {{1, 1}}), 0}); // one row
        cases.add(new Object[] {wallOf(new int[][] {{5}}), 1}); // one row, one brick
        cases.add(new Object[] {wallOf(new int[][] {{1, 2}, {2, 1}}), 1}); // two edges, one row each
        cases.add(new Object[] {wallOf(new int[][] {{3}, {1, 2}, {2, 1}}), 2}); // one full-width row
        cases.add(new Object[] {wallOf(new int[][] {{2, 2}, {2, 2}, {1, 3}}), 1});
        // Brick widths at the limit: the running width passes 2^31 - 1, which
        // overflows an int. The best line is at 2^31 - 1 (rows 1 and 2).
        cases.add(new Object[] {wallOf(new int[][] {
            {big, big}, {big, big}, {big - 1, 2, big - 1}}), 1});
        // Every edge is in a different place, so the answer is 1. In an int,
        // row 2's edge at 2^32 + 1 wraps around to 1, lands on row 1's edge at
        // 1, and the answer comes out 0.
        cases.add(new Object[] {wallOf(new int[][] {
            {1, big, big, big - 1}, {big, big, 3, big - 3}}), 1});
        // 10,000 rows, the row limit
        cases.add(new Object[] {new ArrayList<>(Collections.nCopies(10_000, List.of(1, 1))), 0});
        // 20,000 bricks in one row, the brick limit
        cases.add(new Object[] {List.of(Collections.nCopies(20_000, 1)), 0});
        // random walls, checked against brute force (seeded)
        for (int k = 0; k < 5; k++) {
            cases.add(new Object[] {randomWall(rng, 30, 60, 6), null});
        }
        cases.add(new Object[] {randomWall(rng, 200, 1_000, 50), null});

        for (Object[] c : cases) {
            if (c[1] == null) {
                @SuppressWarnings("unchecked")
                List<List<Integer>> wall = (List<List<Integer>>) c[0];
                c[1] = leastBricksBruteForce(wall);
            }
        }

        Map<String, ToIntFunction<List<List<Integer>>>> solutions = new LinkedHashMap<>();
        solutions.put("brute force", BrickWall::leastBricksBruteForce);
        solutions.put("hash map", BrickWall::leastBricksHashMap);

        for (Map.Entry<String, ToIntFunction<List<List<Integer>>>> solution : solutions.entrySet()) {
            boolean isBruteForce = solution.getKey().equals("brute force");
            int passed = 0;
            int total = 0;
            for (Object[] c : cases) {
                @SuppressWarnings("unchecked")
                List<List<Integer>> wall = (List<List<Integer>>) c[0];
                long width = 0;
                for (int brick : wall.get(0)) {
                    width += brick;
                }
                if (isBruteForce && width > BRUTE_FORCE_MAX_WIDTH) {
                    continue;
                }
                total++;
                if (solution.getValue().applyAsInt(wall) == (int) c[1]) {
                    passed++;
                }
            }
            int skipped = cases.size() - total;
            System.out.printf(
                    "%s  %s: %d/%d cases%s%n",
                    passed == total ? "PASS" : "FAIL",
                    solution.getKey(),
                    passed,
                    total,
                    skipped > 0 ? " (skips " + skipped + " too wide to scan)" : "");
        }
    }
}
