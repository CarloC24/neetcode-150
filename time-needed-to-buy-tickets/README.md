# Time Needed to Buy Tickets

LeetCode 2073 · [NeetCode solution page](https://neetcode.io/solutions/time-needed-to-buy-tickets)

`n` people queue to buy tickets; person `i` wants `tickets[i]` of them. Each
second the person at the front buys exactly **one** ticket and then rejoins the
back of the line if they still want more. Return the second at which person `k`
finishes.

**Constraints:** `1 <= n <= 100`, `1 <= tickets[i] <= 100`, `0 <= k < n`.

```
time-needed-to-buy-tickets/
├── java/
│   └── TimeNeededToBuyTickets.java
├── python/
│   └── TimeNeededToBuyTickets.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Input | Idea |
|---|----------|------|-------|-------|------|
| 1 | Queue simulation | O(answer) | O(n) | **destroyed** | Put everyone in a queue and sell tickets one per second. |
| 2 | Circular iteration | O(answer) | O(1) | **destroyed** | The same, with a modular index instead of a queue. |
| 3 | **Direct calculation** | **O(n)** | **O(1)** | intact | Count what each person can buy before `k` finishes. |

NeetCode publishes all three and there is no fourth worth writing — the ladder
already goes brute force → the same thing without the queue → closed form, which
is the entire progression this problem has to offer.

**Approach 3 is the one to write.** It is also the only one you could safely
call twice.

## Two of these three destroy their input

This is the first problem in this repo where the approaches are not
interchangeable, and nothing in their signatures says so. Approaches 1 and 2
count *down* to zero in the caller's array. When they return, everyone at or
before `k` holds 0:

```python
tickets = [2, 3, 2]
queue(tickets, 2)     # -> 6      tickets is now [0, 1, 0]
direct(tickets, 2)    # -> 0      the correct answer is 6
```

That is NeetCode's own code on NeetCode's own example, and the second call is
not wrong — it is answering correctly about a line where nobody wants a ticket.
LeetCode never surfaces this because it hands each submission a private copy of
the array, so the mutation has nowhere to be observed. In any other caller it is
a bug that reports a plausible number rather than crashing.

The harness in both files therefore hands **every approach its own copy** —
`list(tickets)` in Python, `tickets.clone()` in Java — per approach, not per
case. Sharing one array would silently test approaches 2 and 3 against approach
1's leftovers, and they would "fail" for a reason that has nothing to do with
them. The Python `CASES` are tuples rather than lists for the same reason: the
case data itself cannot be damaged even by accident.

Worth contrasting with [Binary Search](../binary-search/), where the note reads
the other way — nothing there mutates, so one array is shared across all six
approaches. The harness shape is a consequence of the algorithms, not a style
choice.

## Why the formula works

Approach 3 needs no simulation because the clock stops at a known instant: **the
moment person `k` buys their last ticket**. Person `k` needs `tickets[k]` turns
at the counter, and every other person's contribution can be read off from that
one number.

- Someone **at or before** `k` in line reaches the counter on every one of those
  turns, the last one included. They buy `min(tickets[i], tickets[k])` —
  whichever runs out first, their own demand or the clock.
- Someone **behind** `k` gets exactly one fewer opportunity, because `k`'s final
  purchase ends the process before the line wraps around to them. They buy
  `min(tickets[i], tickets[k] - 1)`.

Both are a `min` against a bound that `k` sets, and the only difference is that
one bound is a turn smaller. On LeetCode's second example:

```
tickets = [5, 1, 1, 1],  k = 0        tickets[k] = 5

  i = 0  (i <= k)   min(5, 5) = 5
  i = 1  (i >  k)   min(1, 4) = 1
  i = 2  (i >  k)   min(1, 4) = 1
  i = 3  (i >  k)   min(1, 4) = 1
                              ---
                                8
```

Person 0 wants 5 tickets and everyone else wants 1, so the others each buy their
single ticket in the first round and are gone; person 0 spends the remaining
four seconds alone. The formula never simulates that — it just observes that
nobody behind `k` can buy more than 4.

## Two one-character mistakes, with different signatures

Both live in approach 3, and they fail in ways worth telling apart. Measured
against all 2,930 cases with `n <= 4` and tickets in 1–5:

| mistake | wrong on | how |
|---|---:|---|
| dropping the `- 1` for `i > k` | 1,560 / 2,930 | overcounts, **data-dependent** |
| `i < k` instead of `i <= k` | 2,930 / 2,930 | undercounts by **exactly 1**, always |

Dropping the `- 1` is the more dangerous one despite being wrong less often. It
is correct whenever nobody behind `k` wanted that extra turn anyway — that is
just over half the cases here — so a small or lucky test set passes it. It fails
only when someone behind `k` has enough tickets to have wanted the round that
never happened.

Writing `i < k` sends person `k` down the wrong branch, where they buy
`min(tickets[k], tickets[k] - 1)` — one short of what they actually need. Every
case is wrong and every case is wrong **by exactly one**, which makes it trivial
to spot and easy to misdiagnose as an off-by-one in the clock rather than in the
branch.

## "O(n × m)" is an upper bound on the answer, not a count of work

The simulations are usually quoted as O(n × m) with `m` the largest ticket
count. That is a correct bound and it is not what they do. The loop in approach
1 runs exactly once per ticket sold and returns at `time`, so **the iteration
count is the return value** — verified equal on all 2,930 exhaustive cases.

So both simulations are Θ(answer), and the real question is how large the answer
can be. Person `k` contributes `tickets[k] ≤ 100` and each of the other `n - 1 ≤
99` people contributes at most that much, so:

```
max answer = (k + 1) · 100 + (n − 1 − k) · 99      with every ticket at the cap
           = 10,000                                at n = 100, k = 99
```

Confirmed by evaluating every `(n, k)` pair with all tickets at 100: the maximum
is exactly **10,000**, at `k = n - 1 = 99`. The minimum is 1. The entire range of
possible answers is `[1, 10000]`, and both extremes are in the test set at full
length — as is the `k = 0` corner, which gives 9,901 because 99 people each lose
a turn.

There is a clean special case inside that: when `tickets[k] == 1`, everyone ahead
is capped at 1 and everyone behind at 0, so the answer is just `k + 1`. Verified
on 20,000 random arrays, zero violations.

## What the rewrites are actually worth

All at the ceiling — `n = 100`, every ticket 100, `k = 99`, so the answer is
10,000 and the simulations do 10,000 steps.

**Python** (3.9.6), ms per call, best of five over 2,000 calls:

| approach | time | vs. approach 1 |
|---|---:|---|
| 1 — queue | 2.39 | — |
| 2 — circular | 2.19 | 1.09× faster |
| 3 — direct | **0.027** | **89× faster** |

**Java** (JDK 26), ms per call, after a 20,000-call warmup:

| approach | time | vs. approach 1 |
|---|---:|---|
| 1 — queue (`LinkedList`) | 0.074 | — |
| 1 — queue (`ArrayDeque`) | 0.055 | 1.34× faster |
| 2 — circular | 0.081 | 1.09× **slower** |
| 3 — direct | **0.0002** | **~370× faster** |

Approach 3's win is the one that is real in both languages and it is enormous,
because it is a complexity difference rather than a constant: O(n) against
Θ(answer), 100 steps against 10,000.

Approach 2 is the interesting row. It is a genuine space win — O(1) against O(n)
— and in Java it is a small *time loss*. The boxing it removes was already free:
`Integer.valueOf` caches −128..127, and this problem caps `n` at 100, so every
boxed index in every run is a cached object and nothing is ever allocated. What
approach 2 adds in exchange is a `%` per simulated second. Push `n` past 128 and
the trade would start paying. Python measures the other way round, where the
deque operations it removes are real interpreter work.

That is the same pattern as [Score of a String](../score-of-a-string/): the
identical source rewrite is worth a little in one language and slightly negative
in the other, and the space argument for approach 2 stands on its own without
needing the time argument to be true.

`ArrayDeque` beats `LinkedList` by 1.34× on the same method — one node object
per element, and nothing else. NeetCode uses `LinkedList` and this file keeps it.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 time-needed-to-buy-tickets/python/TimeNeededToBuyTickets.py
```

Or from inside the folder:

```bash
cd time-needed-to-buy-tickets/python
python3 TimeNeededToBuyTickets.py
```

Expected output:

```
PASS  queue: 16/16 cases
PASS  circular: 16/16 cases
PASS  direct: 16/16 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java time-needed-to-buy-tickets/java/TimeNeededToBuyTickets.java
```

### Option 2 — compile, then run

```bash
cd time-needed-to-buy-tickets/java
javac TimeNeededToBuyTickets.java   # produces TimeNeededToBuyTickets*.class
java TimeNeededToBuyTickets         # note: no .class extension
```

Expected output (either option):

```
PASS  queue: 16/16 cases
PASS  circular: 16/16 cases
PASS  direct: 16/16 cases
```

To clean up the compiled artifacts from option 2 — note the plural, since the
nested `Solution` and `Case` types compile to their own files:

```bash
rm time-needed-to-buy-tickets/java/TimeNeededToBuyTickets*.class
```

## Notes

- **Approach 2 has no termination proof of its own.** Its only exit is the
  `return`, and the inner `while tickets[idx] == 0` skip loop has no bound. It
  is trusting that person `k` is still owed a ticket, so some unfinished person
  always exists to land on. Hand it an already-zeroed array and it spins forever
  rather than raising — measured, it does not return within a second. The
  constraints guarantee `tickets[i] >= 1`, so the trust is well placed, but it
  is trust and not a guard. Approach 1 on the same input terminates and returns
  `time`, because `while q` actually empties.
- **Approach 3 re-reads `tickets[k]` on every iteration.** It never changes, so
  hoisting it to a local would be marginally faster — and would also make the
  method immune to a caller mutating the array underneath it. Left as NeetCode
  writes it, since the point of the file is what the page publishes.
- **Both simulations are `Θ(answer)`, so the "brute force" is not slow here.**
  10,000 steps is the worst input the constraints permit, and every approach
  finishes in well under a millisecond in Java. Approach 3 is the right answer
  for the reason it is right, not because the others time out.
- **Verified beyond the test set, in both languages.** All three approaches were
  checked against an independent oracle — a clock advanced one second at a time,
  sharing no code with any of them — on every array with `n <= 4` and tickets in
  1–5, for every `k` (2,930 cases), and on 20,000 random arrays with `n` up to
  100 and tickets up to 100. 68,790 checks per language, zero failures. Every
  call received its own copy.
- **Method naming:** both NeetCode and LeetCode use `timeRequiredToBuy`. These
  files suffix by approach so all three can coexist — rename to plain
  `timeRequiredToBuy` / `time_required_to_buy` when submitting.
- The Java file is named `TimeNeededToBuyTickets.java` to match its
  `public class TimeNeededToBuyTickets`, as `javac` requires — which is the
  folder name rather than the method name, unlike the other problems here.
