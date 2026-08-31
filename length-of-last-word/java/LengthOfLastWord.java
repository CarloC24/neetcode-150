import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Length of Last Word (LeetCode 58).
 *
 * <p>Given a string {@code s} of words and spaces, return the length of the last
 * word, where a word is a maximal run of non-space characters.
 *
 * <p>The three approaches from
 * https://neetcode.io/solutions/length-of-last-word, plus one: NeetCode's
 * approach 3 is a <em>different algorithm</em> in each language -- Python splits
 * into a list of words, Java trims and finds the last space -- so both are
 * written here, in both languages. See the README.
 *
 * <p>The problem is easy and the interesting part is not the answer. It is that
 * the input is scanned from the wrong end by every approach but one: the answer
 * lives in the suffix, and only the backward scan declines to read the rest.
 */
public class LengthOfLastWord {

    /** A solution, so the harness can run all four against the same cases. */
    @FunctionalInterface
    interface Solution {
        int apply(String s);
    }

    /**
     * Scans forward, restarting the count after every run of spaces.
     * Time: O(n). Space: O(1).
     *
     * <p>Walks left to right holding the length of the word in progress. A space
     * ends that word, so skip the whole run of spaces and reset the counter --
     * unless the run reaches the end of the string, in which case the word just
     * finished was the last one and its length is the answer.
     *
     * <p>That {@code if (i == s.length())} check is the entire difficulty.
     * Without it the reset still happens on trailing spaces and the method
     * returns 0 for every input that ends in one, which is precisely the case
     * the problem is testing for. Note where the check sits: after the inner
     * loop, not inside it -- the question is not "is this a space" but "did the
     * spaces run out the string".
     *
     * <p>Correct, and the wrong end to start from. It reads the whole string to
     * report something determined entirely by the tail. Worse, the constant is
     * above 1: a space is read twice, once by the outer {@code if} and again by
     * the inner {@code while}. On 3,333 two-letter words -- 9,998 characters --
     * that is 16,662 character reads, against the backward scan's 4. The README
     * has the table.
     */
    static int lengthOfLastWordForward(String s) {
        int length = 0;
        int i = 0;

        while (i < s.length()) {
            if (s.charAt(i) == ' ') {
                while (i < s.length() && s.charAt(i) == ' ') {
                    i++;
                }
                if (i == s.length()) {
                    return length;
                }
                length = 0;
            } else {
                length++;
                i++;
            }
        }

        return length;
    }

    /**
     * Skips the trailing spaces, then counts back to the previous one.
     * Time: O(n) worst case, O(k) on the tail. Space: O(1).
     *
     * <p>The one to write, and the one to see the point of: the answer depends
     * on the suffix, so start at the suffix. Two loops, no counter to reset, and
     * nothing before the last word is ever looked at.
     *
     * <p>"O(n)" is the honest worst case -- a string that is one long word
     * forces a read of all of it -- but it is not what this does on most input.
     * The reads are trailing spaces plus the last word, so the cost is O(k) in
     * the length of the tail, independent of how much string precedes it. At the
     * constraint ceiling of 10,000 characters: 4 reads for "a" followed by 9,998
     * spaces and a two-letter word, against the forward scan's 10,002. Both are
     * "O(n)" and only one of them reads n characters.
     *
     * <p>{@code i >= 0} in the second loop is not defensive padding. It fires
     * exactly when the last word reaches the front of the string with no space
     * before it -- "hello", or any single-word input -- and dropping it throws
     * {@code StringIndexOutOfBoundsException: Index -1 out of bounds} on every
     * such string, which is 2,046 of the 88,562 constraint-satisfying strings
     * over {a, b, space} up to length 10.
     *
     * <p>Python needs the same guard for a weaker reason. Negative indices are
     * legal there, so {@code s[-1]} wraps to the end of the string and the loop
     * keeps counting instead of failing at once -- it survives only because it
     * eventually walks off the front and raises there. Java has no wrap: the
     * first read past the front is the one that throws. Same line, loud in both
     * languages, immediate in only one.
     *
     * <p>The first loop has no such guard, and NeetCode's does not either. It is
     * unguarded because the constraints promise a word exists, so the scan is
     * guaranteed to hit a non-space before running out of string. On an
     * all-spaces input -- which the constraints forbid -- it walks off the front
     * and throws. See the README.
     */
    static int lengthOfLastWordBackward(String s) {
        int i = s.length() - 1;
        int length = 0;

        while (s.charAt(i) == ' ') {
            i--;
        }

        while (i >= 0 && s.charAt(i) != ' ') {
            i--;
            length++;
        }

        return length;
    }

    /**
     * Cuts the string into words and measures the last. Time: O(n). Space: O(n).
     *
     * <p>NeetCode's approach 3 for <em>Python</em> -- {@code len(s.split().pop())}
     * -- written in Java. Python's bare {@code split()} splits on runs of
     * whitespace and discards the empty strings at both ends, and
     * {@code trim().split("\\s+")} is what that means here: the trim kills the
     * leading empty, the {@code +} collapses the repeated spaces.
     *
     * <p>That is also the whole cost. It builds an array of every word in the
     * string -- O(n) space -- to read one of them, which is the only approach
     * here that allocates in proportion to the input.
     *
     * <p>Plain {@code s.split(" ")} also works in Java, and that is a genuine
     * difference from Python rather than a coincidence: Java's {@code split}
     * drops trailing empty strings by default, so {@code "hello world  "} gives
     * {@code [hello, world]} while Python's {@code "hello world  ".split(' ')}
     * gives {@code ['hello', 'world', '', '']} and reports 0. Leading empties
     * survive in Java -- {@code "  hello"} gives {@code [, , hello]} -- but the
     * answer is read off the last element, so they do no harm. Both spellings
     * verified against the oracle on all 88,562 strings; see the README.
     *
     * <p>Unlike Python, this returns 0 rather than throwing on an all-spaces
     * input: {@code "".split("\\s+")} is a one-element array holding the empty
     * string, not an empty array. Outside the constraints either way.
     */
    static int lengthOfLastWordSplit(String s) {
        String[] words = s.trim().split("\\s+");
        return words[words.length - 1].length();
    }

    /**
     * Drops the trailing spaces, then measures from the last space to the end.
     * Time: O(n). Space: O(n).
     *
     * <p>NeetCode's approach 3 for Java. No scan and no word array: once the
     * trailing spaces are gone, the last word runs from just after the final
     * space to the end of the string, so its length is the distance between
     * them.
     *
     * <p>{@code lastIndexOf} returning -1 when there is no space is the case
     * that makes this work without a branch. A single-word string then measures
     * {@code length() - (-1) - 1}, which is {@code length()} -- the whole
     * string, correct, and arrived at by the same arithmetic as every other
     * input rather than by a special case.
     *
     * <p>NeetCode calls {@code trim()}, which strips <em>both</em> ends, and the
     * leading half of that is dead work. The last space is the last space
     * whether or not anything precedes it: verified identical on all 88,562
     * constraint-satisfying strings over {a, b, space} up to length 10, both-end
     * strip against trailing-only, zero differences. Drop the trailing strip
     * instead and 29,514 of them break. Java has no {@code rstrip}, so
     * {@code trim()} stays here -- it is the idiomatic spelling and the extra
     * work is bounded by the leading spaces -- but only one end is doing
     * anything.
     *
     * <p>The space is O(n) for the copy {@code trim()} makes -- but only when
     * there is something to trim, since it returns {@code this} otherwise.
     * CPython's {@code rstrip} has the same optimisation. Verified by reference
     * comparison in both languages.
     *
     * <p>{@code trim()} is not {@code strip()} and neither is Python's: it cuts
     * every character {@code <= U+0020}, so tabs and newlines go while a
     * non-breaking space stays, where Python's {@code rstrip()} takes Unicode
     * whitespace and removes the non-breaking space too. Invisible under this
     * problem's constraints, which allow only English letters and spaces.
     *
     * <p>This is the only approach that survives an all-spaces input, returning
     * 0. Outside the constraints, and not a reason to prefer it.
     */
    static int lengthOfLastWordTrimIndex(String s) {
        String t = s.trim();
        return t.length() - t.lastIndexOf(" ") - 1;
    }

    /** One test case: an input string and the length expected back. */
    private static final class Case {
        final String s;
        final int expected;

        Case(String s, int expected) {
            this.s = s;
            this.expected = expected;
        }
    }

    public static void main(String[] args) {
        // The constraint ceiling is 10,000 characters. These are the three shapes
        // that sit at it, and they disagree wildly about how much of the string
        // matters.
        String maxOneWord = "a".repeat(10000); // no space anywhere: the backward scan's worst case
        String maxTinyTail = "a" + " ".repeat(9997) + "bc"; // the answer is 2, after 9,998 junk chars
        String maxLeadingSpaces = " ".repeat(9999) + "a"; // one word, at the very end

        List<Case> cases = new ArrayList<>();
        cases.add(new Case("hello world", 5)); // LeetCode example 1
        cases.add(new Case("   fly me   to   the moon  ", 4)); // example 2: leading, repeated, trailing
        cases.add(new Case("luffy is still joyboy", 6)); // example 3: no padding at all
        cases.add(new Case("a", 1)); // the smallest legal input, and what the `i >= 0` guard is for
        cases.add(new Case("a ", 1)); // single word, trailing space: where Python's wrap survives
        cases.add(new Case(" a", 1)); // single word, leading space
        cases.add(new Case("day", 3)); // no spaces anywhere
        cases.add(new Case("  day  ", 3)); // the same word, padded on both ends
        cases.add(new Case("a b", 1)); // the last word shorter than the first
        cases.add(new Case("ab c", 1));
        cases.add(new Case("a bc", 2)); // and longer
        cases.add(new Case("word          ", 4)); // trailing spaces alone -- the forward scan's trap
        cases.add(new Case("          word", 4)); // leading spaces alone
        cases.add(new Case("a  b  c", 1)); // repeated separators, which Python's split(' ') breaks on
        cases.add(new Case("Hello World", 5)); // mixed case: allowed, and nothing here folds case
        cases.add(new Case(maxOneWord, 10000)); // ceiling: every character is the answer
        cases.add(new Case(maxTinyTail, 2)); // ceiling: 4 of 10,000 characters decide it
        cases.add(new Case(maxLeadingSpaces, 1)); // ceiling: 9,999 spaces then the word

        Map<String, Solution> solutions = new LinkedHashMap<>();
        solutions.put("forward", LengthOfLastWord::lengthOfLastWordForward);
        solutions.put("backward", LengthOfLastWord::lengthOfLastWordBackward);
        solutions.put("split", LengthOfLastWord::lengthOfLastWordSplit);
        solutions.put("trim+index", LengthOfLastWord::lengthOfLastWordTrimIndex);

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
