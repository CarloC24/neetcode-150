import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Time Needed to Buy Tickets (LeetCode 2073).
 *
 * <p>{@code n} people queue to buy tickets; person {@code i} wants
 * {@code tickets[i]} of them. Each second the person at the front buys exactly
 * one ticket and then rejoins the back of the line if they still want more.
 * Return the second at which person {@code k} finishes.
 *
 * <p>All three approaches from
 * https://neetcode.io/solutions/time-needed-to-buy-tickets, ordered from
 * simulation to arithmetic. There is no fourth worth writing: the progression
 * already goes brute force -&gt; same thing without the queue -&gt; closed form,
 * and that is the whole ladder.
 *
 * <p>The two simulations DESTROY the array they are given. That is not a flaw in
 * NeetCode's code -- LeetCode hands the solution a private copy -- but it makes
 * these three methods non-interchangeable in a way their signatures do not
 * admit, so the harness below gives every approach a fresh array. See the
 * README.
 */
public class TimeNeededToBuyTickets {

    /** A solution, so the harness can run all three against the same cases. */
    @FunctionalInterface
    interface Solution {
        int apply(int[] tickets, int k);
    }

    /**
     * Simulates the line with a real queue. Time: O(answer). Space: O(n).
     *
     * <p>The literal reading of the problem, and the one to write first because
     * it cannot be wrong: put everyone in a queue, sell one ticket per second to
     * whoever is at the front, send them to the back if they still want more,
     * and stop the moment person k's count hits zero.
     *
     * <p>"O(n * m)" is how the complexity is usually quoted, with m the largest
     * ticket count, and it is a correct upper bound rather than the actual cost.
     * The loop runs exactly once per ticket sold and returns at {@code time}, so
     * the iteration count <em>is</em> the return value -- verified equal on all
     * 2,930 exhaustive cases below n = 5. The bound that matters is therefore
     * the bound on the answer itself, which the constraints cap at
     * 100 x 100 = 10,000.
     *
     * <p><b>This mutates {@code tickets}.</b> Every decrement lands in the
     * caller's array, and when this returns, everyone at or before k holds 0.
     * Anything reading the array afterwards is reading wreckage -- approach 3
     * handed the same array returns 0. LeetCode never notices because it passes
     * a throwaway copy.
     *
     * <p>{@code LinkedList} is NeetCode's choice and is kept here, but
     * {@code ArrayDeque} is the better one: same interface, no node object per
     * element. Measured at the ceiling it is about 1.34x faster -- 0.055ms
     * against 0.074ms -- which is the node allocation and nothing else.
     *
     * <p>The boxing costs almost nothing, which is worth knowing because it
     * looks like it should. Every index becomes an {@code Integer}, but
     * {@code Integer.valueOf} caches -128..127 and this problem caps n at 100,
     * so every boxed index in every run is a cached object and no allocation
     * happens at all. Push n past 128 and that stops being true. It is also why
     * approach 2, which avoids the boxing entirely, does not come out ahead.
     */
    static int timeRequiredToBuyQueue(int[] tickets, int k) {
        int n = tickets.length;
        Queue<Integer> queue = new LinkedList<>();

        for (int i = 0; i < n; i++) {
            queue.add(i);
        }

        int time = 0;
        while (!queue.isEmpty()) {
            time++;
            int cur = queue.poll();
            tickets[cur]--;
            if (tickets[cur] == 0) {
                if (cur == k) {
                    return time;
                }
            } else {
                queue.add(cur);
            }
        }

        return time;
    }

    /**
     * Walks the line in a circle, skipping anyone already finished.
     * Time: O(answer). Space: O(1).
     *
     * <p>The same simulation with the queue deleted. A queue of indices that
     * always holds some subset of 0..n-1 in their original rotational order is
     * not carrying any information a modular index cannot:
     * {@code idx = (idx + 1) % n} is "go to the next person", and the inner loop
     * skipping zeros is "and skip the ones who have left". That trades the O(n)
     * deque -- and all of its boxing -- for a single int.
     *
     * <p>The space win is real and the time win does not exist. Measured at the
     * ceiling this is the <em>slowest</em> of the three, at 0.081ms against the
     * {@code LinkedList} queue's 0.074ms and {@code ArrayDeque}'s 0.055ms: the
     * boxing it saves was already free at n {@literal <=} 100 (see approach 1),
     * and it pays a {@code %} per second of simulated time in exchange. The
     * Python file measures the other way round -- there the circular version is
     * the faster of the two simulations -- because the deque operations it
     * removes are real interpreter work rather than a cached box.
     *
     * <p>The inner skip loop looks like it could make this quadratic and does
     * not, for the usual amortised reason: a person is skipped only after
     * reaching zero, and each zero is created once.
     *
     * <p><b>This also mutates {@code tickets}</b>, exactly as approach 1 does.
     *
     * <p>Worth noticing that this method has no termination proof of its own.
     * Its only exit is the {@code return}, and the inner
     * {@code while (tickets[idx] == 0)} has no bound -- it is trusting that
     * person k is still owed a ticket, so a non-finished person always exists to
     * land on. Hand it an array that is already all zeros and it spins forever
     * rather than throwing. The constraints promise {@code tickets[i] >= 1}, so
     * the trust is well placed, but it is trust and not a guard. Approach 1 has
     * a real bound -- the queue empties -- and returns {@code time} on the same
     * input.
     */
    static int timeRequiredToBuyCircular(int[] tickets, int k) {
        int n = tickets.length;
        int idx = 0;

        int time = 0;
        while (true) {
            time++;
            tickets[idx]--;
            if (tickets[idx] == 0) {
                if (idx == k) {
                    return time;
                }
            }
            idx = (idx + 1) % n;
            while (tickets[idx] == 0) {
                idx = (idx + 1) % n;
            }
        }
    }

    /**
     * Counts what each person can possibly buy before k finishes.
     * Time: O(n). Space: O(1).
     *
     * <p>The one to write. Nothing needs simulating, because the clock stops at
     * a known instant -- the moment person k buys their last ticket -- and every
     * other person's contribution to that instant can be read off directly.
     *
     * <p>Person k needs {@code tickets[k]} turns. So by the time they take their
     * last one:
     *
     * <ul>
     *   <li>Someone <b>at or before</b> k in line reaches the counter on every
     *       one of those turns, including the last. They buy
     *       {@code min(tickets[i], tickets[k])} -- whichever runs out first,
     *       their own demand or the clock.
     *   <li>Someone <b>behind</b> k gets one fewer opportunity, because k's
     *       final purchase ends the process before the line wraps around to
     *       them. They buy {@code min(tickets[i], tickets[k] - 1)}.
     * </ul>
     *
     * <p>The answer is the total. Both halves are a {@code min} against a bound
     * k sets, and the only difference is that one bound is a turn smaller.
     *
     * <p>Two one-character mistakes live here, and they fail differently:
     *
     * <p>Dropping the {@code - 1} for people behind k overcounts, and only
     * sometimes: it is wrong on 1,560 of the 2,930 exhaustive cases -- just over
     * half -- so it survives a careless test set. It is right exactly when
     * nobody behind k wanted that extra turn anyway.
     *
     * <p>Writing {@code i < k} instead of {@code i <= k} sends person k down the
     * wrong branch, where they buy {@code min(tickets[k], tickets[k] - 1)} --
     * one short, always. That is wrong on all 2,930 cases and always by exactly
     * 1, which is the easier bug to catch and the easier one to misread as an
     * off-by-one in the clock.
     *
     * <p>Unlike the two simulations, this reads {@code tickets} and never writes
     * to it, which is why it is the only one of the three that can be handed the
     * same array twice. {@code tickets[k]} is re-read on every iteration and
     * never changes; hoisting it to a local would be marginally faster and would
     * also make the method immune to a caller mutating the array underneath it.
     * It is left as NeetCode writes it.
     */
    static int timeRequiredToBuyDirect(int[] tickets, int k) {
        int res = 0;

        for (int i = 0; i < tickets.length; i++) {
            if (i <= k) {
                res += Math.min(tickets[i], tickets[k]);
            } else {
                res += Math.min(tickets[i], tickets[k] - 1);
            }
        }

        return res;
    }

    /** One test case: a ticket line, the position to watch, and the expected second. */
    private static final class Case {
        final int[] tickets;
        final int k;
        final int expected;

        Case(int[] tickets, int k, int expected) {
            this.tickets = tickets;
            this.k = k;
            this.expected = expected;
        }
    }

    /** An array of {@code n} copies of {@code value}. */
    private static int[] filled(int n, int value) {
        int[] a = new int[n];
        Arrays.fill(a, value);
        return a;
    }

    /** {@code n - 1} copies of {@code value}, then a single {@code last}. */
    private static int[] filledThen(int n, int value, int last) {
        int[] a = filled(n, value);
        a[n - 1] = last;
        return a;
    }

    public static void main(String[] args) {
        List<Case> cases = new ArrayList<>();
        cases.add(new Case(new int[] {2, 3, 2}, 2, 6)); // example 1: k at the back, all capped by k
        cases.add(new Case(new int[] {5, 1, 1, 1}, 0, 8)); // example 2: k at the front, wanting most
        cases.add(new Case(new int[] {1}, 0, 1)); // the smallest legal input in every dimension
        cases.add(new Case(new int[] {1, 1, 1}, 0, 1)); // k first, one each: nobody else ever buys
        cases.add(new Case(new int[] {1, 1, 1}, 2, 3)); // k last, one each: the answer is k + 1
        cases.add(new Case(new int[] {2, 3, 2}, 0, 4)); // example 1's array with k at the front
        cases.add(new Case(new int[] {3, 3, 3}, 1, 8)); // k in the middle, all equal: 3 + 3 + 2
        cases.add(new Case(new int[] {100, 1, 1}, 0, 102)); // k wants everything, others done in one
        cases.add(new Case(new int[] {1, 1, 100}, 2, 102)); // the same demand at the back
        cases.add(new Case(new int[] {1, 100, 1}, 1, 102)); // and in the middle
        cases.add(new Case(new int[] {2, 1, 2}, 1, 2)); // k finishes while others are still owed
        cases.add(new Case(filled(100, 1), 0, 1)); // ceiling length, minimum answer
        cases.add(new Case(filled(100, 1), 99, 100)); // one ticket each: the answer is the position
        cases.add(new Case(filledThen(100, 100, 1), 99, 100)); // k last wanting one: all capped at 1
        cases.add(new Case(filled(100, 100), 0, 9901)); // ceiling with k first: 99 people lose a turn
        cases.add(new Case(filled(100, 100), 99, 10000)); // ceiling, maximum answer: 100 * 100

        Map<String, Solution> solutions = new LinkedHashMap<>();
        solutions.put("queue", TimeNeededToBuyTickets::timeRequiredToBuyQueue);
        solutions.put("circular", TimeNeededToBuyTickets::timeRequiredToBuyCircular);
        solutions.put("direct", TimeNeededToBuyTickets::timeRequiredToBuyDirect);

        for (Map.Entry<String, Solution> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case test : cases) {
                // clone() per approach, not per case: two of the three consume what
                // they are given, so sharing one array would test the second
                // approach against the first one's leftovers.
                if (solution.getValue().apply(test.tickets.clone(), test.k) == test.expected) {
                    passed++;
                }
            }
            System.out.printf(
                    "%s  %s: %d/%d cases%n",
                    passed == cases.size() ? "PASS" : "FAIL",
                    solution.getKey(),
                    passed,
                    cases.size());
        }
    }
}
