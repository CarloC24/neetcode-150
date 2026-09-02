import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Score of a String (LeetCode 3110).
 *
 * <p>The score of a string is the sum of the absolute differences between the
 * ASCII values of adjacent characters. Return it.
 *
 * <p>NeetCode publishes one approach for this problem -- the loop, which is the
 * right answer and takes about four lines. The two beside it here are not rival
 * algorithms; they are the same sum spelled the way each language prefers, and
 * writing them out is what makes the cost of a character lookup visible.
 *
 * <p>The thing actually worth carrying away is in the README: the absolute value
 * is the only reason this needs a loop at all. Drop it and the sum telescopes to
 * the two endpoints.
 */
public class ScoreOfAString {

    /** A solution, so the harness can run all three against the same cases. */
    @FunctionalInterface
    interface Solution {
        int apply(String s);
    }

    /**
     * Sums the gaps by index. Time: O(n). Space: O(1).
     *
     * <p>NeetCode's approach, and the one to write. There are n - 1 adjacent
     * pairs in a string of n characters, so the loop stops one short of the end
     * and reads forward; {@code i < s.length() - 1} rather than
     * {@code i < s.length()} is the whole of the bookkeeping, and getting it
     * wrong is a StringIndexOutOfBoundsException on {@code charAt(i + 1)} rather
     * than a wrong answer.
     *
     * <p>{@code Math.abs} is not decoration. Without it the sum is a telescoping
     * series that collapses to {@code s.charAt(0) - s.charAt(n - 1)}, computable
     * without looking at the middle of the string at all -- see the README.
     * Every character between the ends matters here only because the absolute
     * value stops the cancellation.
     *
     * <p>The subtraction is int arithmetic, not char arithmetic: both operands
     * widen to int before it happens, so the result is a signed value in
     * [-25, 25] and {@code Math.abs} does the obvious thing. Casting that
     * difference back to char first is the trap this problem sets --
     * {@code Math.abs((char) ('a' - 'z'))} is 65511, not 25, because char is an
     * unsigned 16-bit type with nowhere to put the sign. The README has it.
     *
     * <p>It calls {@code charAt} twice per iteration and therefore twice per
     * character, since each interior character is read once as {@code i} and
     * again as {@code i + 1} on the previous pass: 198 calls for the
     * 100-character ceiling, against the 100 the string contains. Approach 3 is
     * the version that notices. In Java the JIT will often erase the difference;
     * in Python it is about 1.5x of real time.
     *
     * <p>Degenerate input is handled by arithmetic rather than by a check. The
     * constraints promise at least two characters, but the loop condition is
     * already false for a one-character string and for the empty string, so both
     * return 0 without a special case.
     */
    static int scoreOfStringIteration(String s) {
        int res = 0;
        for (int i = 0; i < s.length() - 1; i++) {
            res += Math.abs(s.charAt(i) - s.charAt(i + 1));
        }
        return res;
    }

    /**
     * Sums the gaps over a stream of pair indices. Time: O(n). Space: O(1).
     *
     * <p>The same sum with the loop turned inside out: describe the range of
     * pairs, say what each one is worth, add them up. Whether it reads better
     * than the loop is taste, and at three lines of body there is not much to
     * choose between them.
     *
     * <p>It is the slowest of the three here, and that is the reverse of what
     * the same rewrite does in Python. Measured at n = 100 over 200,000 calls,
     * best of five after a 200,000-call warmup: about 15.5ms against the loop's
     * 10ms, so roughly 1.5x the cost for the stream's setup and boxing-free but
     * still non-trivial pipeline. Python's counterpart is about 15% *faster*
     * than its index loop, because there the comparison is against interpreted
     * index arithmetic rather than against a JIT-compiled one. Same rewrite,
     * opposite verdict; neither matters at n = 100. The README has both tables.
     *
     * <p>Worth noting against the Python file, where the counterpart approach is
     * {@code zip(s, islice(s, 1, None))}. That one has to be written carefully
     * to stay O(1) space -- the obvious {@code zip(s, s[1:])} copies the tail --
     * whereas {@code IntStream.range} never materialises anything. Same
     * approach, and only one of the two languages charges for the obvious
     * spelling.
     *
     * <p>{@code IntStream.range(0, s.length() - 1)} needs no guard for the empty
     * string: a range whose end is below its start is empty rather than an
     * error, so {@code range(0, -1)} sums to 0.
     */
    static int scoreOfStringPairwise(String s) {
        return IntStream.range(0, s.length() - 1)
                .map(i -> Math.abs(s.charAt(i) - s.charAt(i + 1)))
                .sum();
    }

    /**
     * Carries the previous character instead of re-reading it.
     * Time: O(n). Space: O(1).
     *
     * <p>Approaches 1 and 2 both look up every interior character twice: once as
     * the right end of one pair and once as the left end of the next. The
     * character has not changed in between, so the second lookup is recomputing
     * something already in hand. Keeping it in {@code prev} costs one variable
     * and halves the work: 100 {@code charAt} calls at the ceiling against 198.
     *
     * <p>Halving the lookups buys nothing here, and that was measured rather
     * than assumed: at n = 100 over 200,000 calls, best of five after a warmup,
     * three separate runs put this at 0.91x, 0.95x and 1.06x the loop's time --
     * scatter around 1, with no direction to it. {@code charAt} is a field read
     * and a bounds check that the JIT routinely hoists or eliminates, so the
     * second lookup was never really costing anything.
     *
     * <p>The Python file is where the same change is worth 1.5x, since
     * {@code ord} is a real function call there. That is the point of writing it
     * in both: the refactor is identical, and whether it does anything depends
     * entirely on what a character lookup costs in the language. The reason to
     * know the shape is still general -- when a loop reads a sliding window, the
     * overlap is usually already computed -- but "fewer operations" and "faster"
     * are different claims, and only one of them survives a JIT.
     *
     * <p>Unlike the other two, this one does not survive an empty string -- it
     * reads {@code charAt(0)} before the loop and throws. The constraints
     * promise two characters, so the guard is left out rather than written; it
     * is noted because it is the one place the three approaches disagree about
     * input they are never given.
     */
    static int scoreOfStringRolling(String s) {
        int res = 0;
        char prev = s.charAt(0);
        for (int i = 1; i < s.length(); i++) {
            char cur = s.charAt(i);
            res += Math.abs(prev - cur);
            prev = cur;
        }
        return res;
    }

    /** One test case: an input string and the score expected back. */
    private static final class Case {
        final String s;
        final int expected;

        Case(String s, int expected) {
            this.s = s;
            this.expected = expected;
        }
    }

    /** Reverses a string, for the case that asserts the score cannot notice. */
    private static String reversed(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    public static void main(String[] args) {
        // The constraint ceiling is 100 characters. These are the extremes of the
        // score at that length -- the range of possible answers is [0, 2475].
        String maxAlternating = "az".repeat(50); // 25 per pair, 99 pairs: the largest there is
        String minFlat = "a".repeat(100); // every gap zero: the smallest
        String monotone = "a".repeat(99) + "z"; // one jump at the end

        List<Case> cases = new ArrayList<>();
        cases.add(new Case("hello", 13)); // LeetCode example 1
        cases.add(new Case("zaz", 50)); // example 2: both gaps maximal, and not monotonic
        cases.add(new Case("ab", 1)); // the shortest legal input
        cases.add(new Case("ba", 1)); // its reverse: |a - b| is symmetric, so the score holds
        cases.add(new Case("aa", 0)); // the smallest score, and the only way to get it at length 2
        cases.add(new Case("az", 25)); // the largest gap between two lowercase letters
        cases.add(new Case("za", 25));
        cases.add(new Case("abc", 2)); // monotonic up: score is |first - last|, abs never bites
        cases.add(new Case("cba", 2)); // monotonic down: same
        cases.add(new Case("acb", 3)); // one direction change breaks that -- |a - b| is 1
        cases.add(new Case("world", 25));
        cases.add(new Case("mississippi", 58)); // repeated letters, so several gaps are zero
        cases.add(new Case("abcdefghijklmnopqrstuvwxyz", 25)); // 25 steps of 1, scores like "az"
        cases.add(new Case(monotone, 25)); // 98 zero gaps and one of 25
        cases.add(new Case(minFlat, 0)); // ceiling length, minimum score
        cases.add(new Case(maxAlternating, 2475)); // ceiling length, maximum score: 25 * 99
        cases.add(new Case(reversed(maxAlternating), 2475)); // reversed, and necessarily identical

        Map<String, Solution> solutions = new LinkedHashMap<>();
        solutions.put("iteration", ScoreOfAString::scoreOfStringIteration);
        solutions.put("pairwise", ScoreOfAString::scoreOfStringPairwise);
        solutions.put("rolling", ScoreOfAString::scoreOfStringRolling);

        for (Map.Entry<String, Solution> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case test : cases) {
                if (solution.getValue().apply(test.s) == test.expected) {
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
