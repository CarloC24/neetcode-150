import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;

/**
 * Valid Parentheses (LeetCode 20).
 *
 * <p>Given a string s of the characters ( ) [ ] { }, return true if every
 * closing bracket matches the most recent unclosed opening bracket of the same
 * type, and nothing is left open at the end.
 *
 * <p>The two approaches from https://neetcode.io/solutions/valid-parentheses,
 * plus the spelling that stacks the expected closer, ordered from brute force to
 * optimal.
 */
public class ValidParentheses {

    private static final Map<Character, Character> CLOSE_TO_OPEN =
            Map.of(')', '(', ']', '[', '}', '{');

    private static final Map<Character, Character> OPEN_TO_CLOSE =
            Map.of('(', ')', '[', ']', '{', '}');

    /**
     * Strips adjacent matching pairs until none remain. Time: O(n^2). Space: O(n).
     *
     * <p>Every pass deletes at least one pair -- the loop condition guarantees
     * one exists -- so there are at most n/2 passes, each scanning and
     * rebuilding an O(n) string. What is left when no pair can be removed is
     * exactly the set of brackets that never matched, so an empty result means
     * the string was valid.
     *
     * <p>String.replace(CharSequence, CharSequence) is a literal replacement.
     * replaceAll is not: it takes a regex, and all six of these characters are
     * regex metacharacters. {@code replaceAll("[]", "")} throws
     * PatternSyntaxException for an unclosed character class, and
     * {@code replaceAll("()", "")} is worse -- an empty capturing group matches
     * the empty string at every position, so it deletes nothing while contains()
     * keeps reporting true, and the loop spins forever.
     *
     * <p>The parameter is copied into a local rather than reassigned, and the
     * three contains() calls scan the string again on every pass. Both are the
     * shape of the naive answer; the point of this method is what the next two
     * improve on.
     */
    static boolean isValidBruteForce(String s) {
        String remaining = s;
        while (remaining.contains("()") || remaining.contains("{}") || remaining.contains("[]")) {
            remaining = remaining.replace("()", "").replace("{}", "").replace("[]", "");
        }
        return remaining.isEmpty();
    }

    /**
     * Pushes openers, pops on a matching closer. Time: O(n). Space: O(n).
     *
     * <p>The stack holds every bracket opened but not yet closed, which is the
     * only state the problem actually depends on: a closer is legal exactly when
     * it matches the most recent unclosed opener. "Most recent" is what makes
     * this a stack and what a pair of counters can never capture -- see the
     * README on "([)]".
     *
     * <p>Space is O(n), not O(1): "((((((((((" stacks the whole input.
     *
     * <p>charValue() is deliberate. Both peek() and get() hand back a boxed
     * Character, so NeetCode's {@code stack.peek() == closeToOpen.get(c)}
     * compares references -- and only works because Character.valueOf caches
     * every value up to 127 and all six brackets are below it. The same line
     * over characters outside that cache returns false for equal characters.
     * Unbox first, or call equals().
     */
    static boolean isValidStack(String s) {
        Stack<Character> stack = new Stack<>();

        for (char c : s.toCharArray()) {
            Character open = CLOSE_TO_OPEN.get(c);
            if (open != null) {
                if (!stack.isEmpty() && stack.peek().charValue() == open.charValue()) {
                    stack.pop();
                } else {
                    return false;
                }
            } else {
                stack.push(c);
            }
        }

        return stack.isEmpty();
    }

    /**
     * Stacks the closer you expect to see next. Time: O(n). Space: O(n).
     *
     * <p>The same algorithm as above with the translation moved to the push
     * side: on '(' the stack records ')', so a closing bracket compares directly
     * against the top instead of mapping itself back to an opener first. One map
     * lookup per character rather than one per character plus one per closer,
     * and the comparison reads as the question being asked -- is this the
     * bracket I am waiting for?
     *
     * <p>ArrayDeque rather than Stack. java.util.Stack extends Vector, so every
     * method is synchronized for a lock nobody here contends, and it iterates
     * bottom-to-top, which is the opposite of the order it pops in. The Deque
     * javadoc recommends this class over it outright.
     *
     * <p>Note the else-if: anything that is not an opener is treated as a
     * closer. The constraints promise nothing but brackets, so that is safe
     * here; on wider input a stray letter would be compared against the expected
     * closer and rejected.
     */
    static boolean isValidExpectedCloser(String s) {
        Deque<Character> stack = new ArrayDeque<>();

        for (char c : s.toCharArray()) {
            Character close = OPEN_TO_CLOSE.get(c);
            if (close != null) {
                stack.push(close);
            } else if (stack.isEmpty() || stack.pop().charValue() != c) {
                return false;
            }
        }

        return stack.isEmpty();
    }

    /** One test case: an input and the expected answer. */
    private static class Case {
        final String s;
        final boolean expected;

        Case(String s, boolean expected) {
            this.s = s;
            this.expected = expected;
        }
    }

    public static void main(String[] args) {
        Case[] cases = {
            new Case("()", true),
            new Case("()[]{}", true),
            new Case("(]", false),
            new Case("([])", true),
            new Case("([)]", false), // correctly paired, wrongly ordered; see the README
            new Case("{[()]}", true), // every type, fully nested
            new Case("(", false), // opened, never closed
            new Case(")", false), // closed, never opened
            new Case("][", false), // right pair, backwards
            new Case("))((", false), // balanced counts, invalid string
            new Case("(){}}{", false), // valid prefix, then a closer with nothing open
            new Case("(".repeat(10) + ")".repeat(10), true), // deep nesting: stack grows to n/2
            new Case("{[}", false), // odd length, so invalid before you read it
            new Case("", true), // empty, though the constraints promise one character
        };

        Map<String, Predicate<String>> solutions = new LinkedHashMap<>();
        solutions.put("brute force", ValidParentheses::isValidBruteForce);
        solutions.put("stack", ValidParentheses::isValidStack);
        solutions.put("stack (expected closer)", ValidParentheses::isValidExpectedCloser);

        for (Map.Entry<String, Predicate<String>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                if (solution.getValue().test(testCase.s) == testCase.expected) {
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
