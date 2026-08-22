# Best Time to Buy and Sell Stock

LeetCode 121 · [NeetCode solution page](https://neetcode.io/solutions/best-time-to-buy-and-sell-stock)

Given an array `prices` where `prices[i]` is the price on day `i`, return the
maximum profit from buying on one day and selling on a **strictly later** day.
If no such pair turns a profit, return `0`.

**Constraints:** `1 <= prices.length <= 10^5`, `0 <= prices[i] <= 10^4`.

```
best-time-to-buy-and-sell-stock/
├── java/
│   └── BestTimeToBuyAndSellStock.java
├── python/
│   └── BestTimeToBuyAndSellStock.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Brute force | O(n²) | O(1) | Try every buy/sell pair. |
| 2 | Two pointers | O(n) | O(1) | Drag a buy pointer behind a sell pointer. |
| 3 | **One pass** | **O(n)** | **O(1)** | Track the cheapest price so far; sell against it each day. |

**Approach 3 is the one to reach for.** The insight is that the two halves of
the problem decouple: on any given day, the best sale available is today's price
minus the cheapest price on any *earlier* day. That makes the running minimum
the only state worth carrying, and it collapses to one variable and one pass.

NeetCode files approach 3 under dynamic programming, which is fair — `minBuy` is
the optimal answer to the subproblem "cheapest purchase in `prices[0..i]`", and
each day extends it in O(1). The tabulated DP array that framing suggests would
be O(n) space to store a value that is never read more than one step later.

**Approaches 2 and 3 are the same algorithm.** In the two-pointer version,
`left` jumps to `right` whenever the price there is not higher — which means
`left` is only ever the index of the cheapest price seen so far. It is approach
3 storing its running minimum as an index instead of an integer. Worth writing
out once to see that, then reaching for approach 3.

## Buy before you sell

The whole problem lives in that one word. The naive read — "biggest number minus
smallest number" — is wrong whenever the peak comes before the trough:

```
prices = [2, 4, 1]

max - min      = 4 - 1 = 3    ← wrong: day 2 sells before day 3 buys
correct answer = 4 - 2 = 2
```

`[2, 4, 1]` is in the test set of both files for exactly this reason, and each
approach enforces the rule differently:

| Approach | How |
|---|---|
| Brute force | Inner loop starts at `j = i + 1` |
| Two pointers | `right` starts at `1` and `left` only ever jumps forward to `right` |
| One pass | The profit is computed *before* `minBuy` is updated |

That last one is the subtle one, and it is more forgiving than it looks. Swap
the two lines so `minBuy` updates first and you permit a same-day round trip —
but that trade is worth exactly `0`, and `0` is already the floor, so the answer
never changes. I checked this against brute force on every array of length ≤ 5
over values 0–3 plus 3,000 random larger ones: zero mismatches. The order in
these files is still the one that says what it means, and the forgiveness
evaporates in any variant that charges a transaction fee.

## Why the answer floors at 0

`res` / `maxProfit` starts at `0`, not negative infinity. Doing nothing is
always an available move, so a loss is never the best answer — `[10, 8, 7, 5, 2]`
returns `0`, not `-8`. This is the difference between "the best trade" and "the
best trade you would actually make", and initializing to `-inf` to "be safe" is
the common way to get it wrong.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 best-time-to-buy-and-sell-stock/python/BestTimeToBuyAndSellStock.py
```

Or from inside the folder:

```bash
cd best-time-to-buy-and-sell-stock/python
python3 BestTimeToBuyAndSellStock.py
```

Expected output:

```
PASS  brute force: 14/14 cases
PASS  two pointers: 14/14 cases
PASS  one pass: 14/14 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java best-time-to-buy-and-sell-stock/java/BestTimeToBuyAndSellStock.java
```

### Option 2 — compile, then run

```bash
cd best-time-to-buy-and-sell-stock/java
javac BestTimeToBuyAndSellStock.java   # produces BestTimeToBuyAndSellStock.class
java BestTimeToBuyAndSellStock         # note: no .class extension
```

Expected output (either option):

```
PASS  brute force: 14/14 cases
PASS  two pointers: 14/14 cases
PASS  one pass: 14/14 cases
```

To clean up the compiled artifact from option 2:

```bash
rm best-time-to-buy-and-sell-stock/java/BestTimeToBuyAndSellStock.class
```

## Notes

- **Only approach 3 needs an empty-input guard.** `prices[0]` throws on an empty
  array, while the other two return `0` from a loop that never runs. Both files
  add the length check so all three agree, even though the constraints promise
  at least one day. NeetCode's published version omits it.
- **This is Maximum Subarray in disguise.** Take the day-to-day differences and
  the answer is the largest sum of any contiguous run of them — Kadane's
  algorithm, floored at 0. For `[7, 1, 5, 3, 6, 4]` the differences are
  `[-6, 4, -2, 3, -2]` and the best run is `4 - 2 + 3 = 5`, which is the answer.
  Worth holding onto: Maximum Subarray shows up later in the roadmap, and this
  is the same recurrence wearing a different problem statement. (Verified
  equivalent on the same 4,365 inputs as the claim above.)
- **No overflow risk, unusually.** Prices are non-negative and capped at 10⁴, so
  the widest possible difference is 10⁴ — five orders of magnitude clear of
  `Integer.MAX_VALUE`. Worth noting only because the neighbouring array problems
  are not so relaxed: Two Sum's brute-force sum gets within 7% of that ceiling
  under LeetCode's stated range.
- **`10^5` days makes approach 1 a timeout, not just a slow answer.** At the
  constraint ceiling the brute force performs about 5×10⁹ pair comparisons. It
  is here to show what the other two are improving on, not as a fallback.
- **Method naming:** both NeetCode and LeetCode use `maxProfit`. These files use
  `maxProfit*` / `max_profit_*` suffixed by approach so all three can coexist —
  rename to plain `maxProfit` when submitting.
- The Java file is named `BestTimeToBuyAndSellStock.java` to match its
  `public class BestTimeToBuyAndSellStock`, as `javac` requires.
- **The constraints above are LeetCode 121's.** NeetCode restates this problem
  with its own (smaller) bounds; the algorithms are unaffected either way, but
  the `[10000, 0, 10000]` test case is sized to LeetCode's value range.
