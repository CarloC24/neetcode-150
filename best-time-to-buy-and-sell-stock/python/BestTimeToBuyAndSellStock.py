"""Best Time to Buy and Sell Stock (LeetCode 121).

Given an array prices where prices[i] is the price on day i, return the maximum
profit from buying on one day and selling on a strictly later day. If no such
pair turns a profit, return 0.

The three approaches from
https://neetcode.io/solutions/best-time-to-buy-and-sell-stock, ordered from
brute force to optimal.
"""

from typing import Callable, List, Sequence, Tuple


def max_profit_brute_force(prices: List[int]) -> int:
    """Try every buy/sell pair. Time: O(n^2). Space: O(1).

    The inner loop starts at i + 1, which is what enforces "sell strictly after
    buy". Starting it at i would allow a same-day round trip -- harmless here,
    since that profit is 0 and 0 is already the floor, but it stops being
    harmless the moment a variant of this problem charges a transaction fee.

    res starts at 0 rather than negative infinity: holding the stock and doing
    nothing is always available, so a loss is never the answer.
    """
    res = 0
    for i in range(len(prices)):
        buy = prices[i]
        for j in range(i + 1, len(prices)):
            res = max(res, prices[j] - buy)
    return res


def max_profit_two_pointers(prices: List[int]) -> int:
    """Walk a sell pointer forward, dragging a buy pointer behind it.

    Time: O(n). Space: O(1).

    Whenever the price at right is not above the price at left, that day is a
    better purchase than anything before it, so left jumps to right. Neither
    pointer ever moves backwards, so this is a single pass despite looking like
    two.

    The framing is a bit of a disguise: left is only ever the index of the
    cheapest price seen so far, which is exactly what the one-pass version
    below tracks as a plain integer. Same algorithm, fewer moving parts.
    """
    left, right = 0, 1
    max_profit = 0

    while right < len(prices):
        if prices[left] < prices[right]:
            max_profit = max(max_profit, prices[right] - prices[left])
        else:
            left = right
        right += 1

    return max_profit


def max_profit_one_pass(prices: List[int]) -> int:
    """Track the cheapest price so far and sell against it. Time: O(n). Space: O(1).

    The optimal solution. For each day, the best possible sale on that day is
    today's price minus the cheapest price on any earlier day -- so one variable
    holding that running minimum is the entire state needed.

    NeetCode files this under dynamic programming, which is fair: min_buy is the
    optimal answer to the subproblem "cheapest purchase in prices[0..i]", and
    each day extends it in O(1).

    The profit is computed before min_buy is updated, so min_buy covers only the
    earlier days and the buy-before-sell rule holds. Swapping those two lines
    happens to give the same answer anyway -- it would permit a same-day sale,
    which is worth exactly 0 and can never beat a floor of 0 -- but the order
    here is the one that says what it means.
    """
    if not prices:
        return 0

    max_profit = 0
    min_buy = prices[0]
    for sell in prices:
        max_profit = max(max_profit, sell - min_buy)
        min_buy = min(min_buy, sell)
    return max_profit


SOLUTIONS: Tuple[Tuple[str, Callable[[List[int]], int]], ...] = (
    ("brute force", max_profit_brute_force),
    ("two pointers", max_profit_two_pointers),
    ("one pass", max_profit_one_pass),
)

CASES: Sequence[Tuple[List[int], int]] = (
    ([10, 1, 5, 6, 7, 1], 6),
    ([10, 8, 7, 5, 2], 0),  # strictly decreasing, so never buy
    ([7, 1, 5, 3, 6, 4], 5),
    ([7, 6, 4, 3, 1], 0),
    ([2, 4, 1], 2),  # global max precedes global min; see the README
    ([1, 2, 3, 4, 5], 4),  # monotonic, buy first and sell last
    ([3, 1, 4, 1, 5, 9, 2, 6], 8),
    ([1, 2], 1),  # smallest profitable input
    ([2, 1], 0),  # smallest unprofitable input
    ([2, 2, 2], 0),  # all equal, no strict gain anywhere
    ([5], 0),  # one day, nothing to sell into
    ([0, 0], 0),  # the lower value bound
    ([10000, 0, 10000], 10000),  # the widest swing the constraints allow
    ([], 0),  # empty, though the constraints promise at least one day
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(prices) == expected for prices, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()
