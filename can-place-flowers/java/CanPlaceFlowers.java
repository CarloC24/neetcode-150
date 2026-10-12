import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Can Place Flowers (LeetCode 605).
 *
 * <p>Given a flowerbed of 0s (empty) and 1s (planted), where no two flowers are
 * adjacent, return true if n new flowers can be planted without any two flowers
 * ending up adjacent.
 *
 * <p>The two approaches from https://neetcode.io/solutions/can-place-flowers,
 * plus the in-place greedy LeetCode publishes and a version of it that leaves
 * the input alone and stops early, ordered from the most memory to the least.
 */
public class CanPlaceFlowers {

    /** The shape every approach shares, so they can sit in one table. */
    @FunctionalInterface
    interface Solution {
        boolean canPlaceFlowers(int[] flowerbed, int n);
    }

    /**
     * Pads with an empty plot at each end, then plants greedily.
     * Time: O(L). Space: O(L).
     *
     * <p>L is flowerbed.length; the problem already uses n for the number of
     * flowers. The padding means every real plot has a left and a right
     * neighbour, so the "is it at the edge?" checks disappear. The price is a
     * full copy of the input. Planting writes to the copy, which is why the
     * next plot sees the flower just placed.
     */
    static boolean canPlaceFlowersPadded(int[] flowerbed, int n) {
        int[] f = new int[flowerbed.length + 2];
        System.arraycopy(flowerbed, 0, f, 1, flowerbed.length);
        for (int i = 1; i < f.length - 1; i++) {
            if (f[i - 1] == 0 && f[i] == 0 && f[i + 1] == 0) {
                f[i] = 1;
                n--;
            }
        }
        return n <= 0;
    }

    /**
     * Plants greedily into the input itself. Time: O(L). Space: O(1).
     *
     * <p>The same greedy with the edges checked explicitly instead of padded
     * away: a missing neighbour counts as empty. It is O(1) extra space only
     * because it writes the new flowers into the caller's array -- the input
     * comes back changed. The test harness passes each approach a copy for
     * exactly this reason.
     */
    static boolean canPlaceFlowersInPlace(int[] flowerbed, int n) {
        int planted = 0;
        for (int i = 0; i < flowerbed.length; i++) {
            boolean leftEmpty = i == 0 || flowerbed[i - 1] == 0;
            boolean rightEmpty = i == flowerbed.length - 1 || flowerbed[i + 1] == 0;
            if (flowerbed[i] == 0 && leftEmpty && rightEmpty) {
                flowerbed[i] = 1;
                planted++;
            }
        }
        return planted >= n;
    }

    /**
     * Plants greedily, skips past each new flower, stops once n fit.
     * Time: O(L). Space: O(1).
     *
     * <p>The optimal solution. After planting at i, plot i + 1 can never take a
     * flower, so jump straight to i + 2. That jump is what stands in for
     * writing the flower down: the only plot that would ever have read it is
     * the one skipped. So the input is never touched, and the left neighbour
     * never needs checking -- it is either the edge, an original empty plot,
     * or a plot just skipped because the one before it was planted.
     *
     * <p>It also returns as soon as n flowers fit, rather than walking the rest
     * of the bed. n == 0 returns true before looking at anything.
     */
    static boolean canPlaceFlowersGreedy(int[] flowerbed, int n) {
        int i = 0;
        int length = flowerbed.length;
        while (i < length && n > 0) {
            if (flowerbed[i] == 1) {
                i += 2; // the next plot is next to a flower
            } else if (i + 1 < length && flowerbed[i + 1] == 1) {
                i += 3; // this plot touches the flower at i + 1; so does i + 2
            } else {
                n--; // plant here: the left side is clear by construction
                i += 2;
            }
        }
        return n <= 0;
    }

    /**
     * Counts each run of empty plots and how many it can hold.
     * Time: O(L). Space: O(1).
     *
     * <p>A run of k empty plots between two flowers holds (k - 1) / 2: the
     * plots touching each flower are out, and the k - 2 left alternate. A run
     * at an edge has only one flower to avoid, which works out the same as
     * pretending there is one extra empty plot past the edge. So the count
     * starts at 1 if the bed starts empty, and the run left over at the end is
     * charged empty / 2 rather than (empty - 1) / 2.
     *
     * <p>When the bed starts with a flower, empty is 0 at that first flower and
     * (0 - 1) / 2 is 0 in Java, because integer division truncates toward
     * zero. Python's // rounds down to -1 instead, which is why NeetCode's
     * Python spells it int((empty - 1) / 2). The Math.max here makes the intent
     * explicit instead of relying on either rounding rule.
     */
    static boolean canPlaceFlowersCountRuns(int[] flowerbed, int n) {
        int empty = flowerbed[0] == 0 ? 1 : 0;
        for (int plot : flowerbed) {
            if (plot == 1) {
                n -= Math.max(0, (empty - 1) / 2);
                empty = 0;
            } else {
                empty++;
            }
        }
        n -= empty / 2;
        return n <= 0;
    }

    /** One test case: a flowerbed, a flower count and the expected answer. */
    private static class Case {
        final int[] flowerbed;
        final int n;
        final boolean expected;

        Case(int[] flowerbed, int n, boolean expected) {
            this.flowerbed = flowerbed;
            this.n = n;
            this.expected = expected;
        }
    }

    public static void main(String[] args) {
        int[] ceiling = new int[20000]; // the length ceiling, all empty

        Case[] cases = {
            new Case(new int[] {1, 0, 0, 0, 1}, 1, true),
            new Case(new int[] {1, 0, 0, 0, 1}, 2, false),
            new Case(new int[] {0}, 1, true), // a single empty plot has no neighbours at all
            new Case(new int[] {1}, 0, true), // n == 0 fits in any bed, even a full one
            new Case(new int[] {1}, 1, false),
            new Case(new int[] {0, 0}, 1, true), // an edge run of 2 holds 1
            new Case(new int[] {0, 0}, 2, false),
            new Case(new int[] {1, 0, 1}, 1, false), // a gap of one holds nothing
            new Case(new int[] {1, 0, 0, 1}, 1, false), // a gap of two holds nothing either
            new Case(new int[] {1, 0, 0, 0, 0, 1}, 2, false), // a gap of four holds only one
            new Case(new int[] {0, 0, 1, 0, 0}, 2, true), // one at each edge
            new Case(new int[] {1, 0, 0, 0, 0, 0, 1}, 2, true), // a gap of five holds two
            new Case(new int[] {0, 0, 0, 0, 0}, 3, true), // an all-empty bed of 5 holds 3
            new Case(new int[] {0, 0, 0, 0, 0}, 4, false),
            new Case(new int[] {1, 0, 0, 0, 0}, 2, true), // starts with a flower; see the README
            new Case(new int[] {1, 0, 1, 0, 1}, 0, true),
            new Case(ceiling, 10000, true), // every other plot
            new Case(ceiling, 10001, false),
        };

        Map<String, Solution> solutions = new LinkedHashMap<>();
        solutions.put("padded copy", CanPlaceFlowers::canPlaceFlowersPadded);
        solutions.put("in place", CanPlaceFlowers::canPlaceFlowersInPlace);
        solutions.put("greedy, skip ahead", CanPlaceFlowers::canPlaceFlowersGreedy);
        solutions.put("count empty runs", CanPlaceFlowers::canPlaceFlowersCountRuns);
        Set<String> mutatesInput = Set.of("in place");

        for (Map.Entry<String, Solution> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                int[] copy = testCase.flowerbed.clone();
                if (solution.getValue().canPlaceFlowers(copy, testCase.n) == testCase.expected) {
                    passed++;
                }
            }
            System.out.printf(
                    "%s  %s: %d/%d cases%n",
                    passed == cases.length ? "PASS" : "FAIL",
                    solution.getKey(),
                    passed,
                    cases.length);
        }

        int[] original = {1, 0, 0, 0, 1};
        for (Map.Entry<String, Solution> solution : solutions.entrySet()) {
            if (!mutatesInput.contains(solution.getKey())) {
                solution.getValue().canPlaceFlowers(original, 1);
            }
        }
        System.out.printf(
                "%s  input left unmodified (all but in place): %s%n",
                Arrays.equals(original, new int[] {1, 0, 0, 0, 1}) ? "PASS" : "FAIL",
                Arrays.toString(original));
    }
}
