import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Best Time to Buy and Sell Stock (LeetCode 121).
 *
 * <p>Given an array prices where prices[i] is the price on day i, return the
 * maximum profit from buying on one day and selling on a strictly later day. If
 * no such pair turns a profit, return 0.
 *
 * <p>The three approaches from
 * https://neetcode.io/solutions/best-time-to-buy-and-sell-stock, ordered from
 * brute force to optimal.
 */
public class BestTimeToBuyAndSellStock {

    /**
     * Tries every buy/sell pair. Time: O(n^2). Space: O(1).
     *
     * <p>The inner loop starts at i + 1, which is what enforces "sell strictly
     * after buy". Starting it at i would allow a same-day round trip --
     * harmless here, since that profit is 0 and 0 is already the floor, but it
     * stops being harmless the moment a variant of this problem charges a
     * transaction fee.
     *
     * <p>res starts at 0 rather than Integer.MIN_VALUE: holding the stock and
     * doing nothing is always available, so a loss is never the answer.
     */
    static int maxProfitBruteForce(int[] prices) {
        int res = 0;
        for (int i = 0; i < prices.length; i++) {
            int buy = prices[i];
            for (int j = i + 1; j < prices.length; j++) {
                res = Math.max(res, prices[j] - buy);
            }
        }
        return res;
    }

    /**
     * Walks a sell pointer forward, dragging a buy pointer behind it.
     *
     * <p>Time: O(n). Space: O(1).
     *
     * <p>Whenever the price at right is not above the price at left, that day is
     * a better purchase than anything before it, so left jumps to right.
     * Neither pointer ever moves backwards, so this is a single pass despite
     * looking like two.
     *
     * <p>The framing is a bit of a disguise: left is only ever the index of the
     * cheapest price seen so far, which is exactly what the one-pass version
     * below tracks as a plain int. Same algorithm, fewer moving parts.
     */
    static int maxProfitTwoPointers(int[] prices) {
        int left = 0;
        int right = 1;
        int maxProfit = 0;

        while (right < prices.length) {
            if (prices[left] < prices[right]) {
                maxProfit = Math.max(maxProfit, prices[right] - prices[left]);
            } else {
                left = right;
            }
            right++;
        }

        return maxProfit;
    }

    /**
     * Tracks the cheapest price so far and sells against it.
     *
     * <p>Time: O(n). Space: O(1).
     *
     * <p>The optimal solution. For each day, the best possible sale on that day
     * is today's price minus the cheapest price on any earlier day -- so one
     * variable holding that running minimum is the entire state needed.
     *
     * <p>NeetCode files this under dynamic programming, which is fair: minBuy is
     * the optimal answer to the subproblem "cheapest purchase in prices[0..i]",
     * and each day extends it in O(1).
     *
     * <p>The profit is computed before minBuy is updated, so minBuy covers only
     * the earlier days and the buy-before-sell rule holds. Swapping those two
     * lines happens to give the same answer anyway -- it would permit a same-day
     * sale, which is worth exactly 0 and can never beat a floor of 0 -- but the
     * order here is the one that says what it means.
     *
     * <p>The length guard is load-bearing in a way the other two approaches do
     * not need: prices[0] on an empty array throws.
     */
    static int maxProfitOnePass(int[] prices) {
        if (prices.length == 0) {
            return 0;
        }

        int maxProfit = 0;
        int minBuy = prices[0];
        for (int sell : prices) {
            maxProfit = Math.max(maxProfit, sell - minBuy);
            minBuy = Math.min(minBuy, sell);
        }
        return maxProfit;
    }

    /** One test case: an input and the expected answer. */
    private static class Case {
        final int[] prices;
        final int expected;

        Case(int[] prices, int expected) {
            this.prices = prices;
            this.expected = expected;
        }
    }

    public static void main(String[] args) {
        Case[] cases = {
            new Case(new int[] {10, 1, 5, 6, 7, 1}, 6),
            new Case(new int[] {10, 8, 7, 5, 2}, 0), // strictly decreasing, so never buy
            new Case(new int[] {7, 1, 5, 3, 6, 4}, 5),
            new Case(new int[] {7, 6, 4, 3, 1}, 0),
            new Case(new int[] {2, 4, 1}, 2), // global max precedes global min; see the README
            new Case(new int[] {1, 2, 3, 4, 5}, 4), // monotonic, buy first and sell last
            new Case(new int[] {3, 1, 4, 1, 5, 9, 2, 6}, 8),
            new Case(new int[] {1, 2}, 1), // smallest profitable input
            new Case(new int[] {2, 1}, 0), // smallest unprofitable input
            new Case(new int[] {2, 2, 2}, 0), // all equal, no strict gain anywhere
            new Case(new int[] {5}, 0), // one day, nothing to sell into
            new Case(new int[] {0, 0}, 0), // the lower value bound
            new Case(new int[] {10000, 0, 10000}, 10000), // the widest swing allowed
            new Case(new int[] {}, 0), // empty, though the constraints promise one day
        };

        Map<String, Function<int[], Integer>> solutions = new LinkedHashMap<>();
        solutions.put("brute force", BestTimeToBuyAndSellStock::maxProfitBruteForce);
        solutions.put("two pointers", BestTimeToBuyAndSellStock::maxProfitTwoPointers);
        solutions.put("one pass", BestTimeToBuyAndSellStock::maxProfitOnePass);

        for (Map.Entry<String, Function<int[], Integer>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                if (solution.getValue().apply(testCase.prices) == testCase.expected) {
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
    }
}
