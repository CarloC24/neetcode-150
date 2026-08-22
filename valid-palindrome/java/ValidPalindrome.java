import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Valid Palindrome (LeetCode 125).
 *
 * <p>Given a string s, return true if it reads the same forwards and backwards
 * once every non-alphanumeric character is dropped and case is ignored.
 *
 * <p>The two approaches from https://neetcode.io/solutions/valid-palindrome,
 * plus the idiomatic Java spelling of the first, ordered from brute force to
 * optimal.
 */
public class ValidPalindrome {

    /**
     * True for A-Z, a-z and 0-9 -- nothing else.
     *
     * <p>Three separate ranges, deliberately. {@code c >= 'A' && c <= 'z'} looks
     * like a shortcut but the gap between 'Z' (90) and 'a' (97) holds
     * {@code [ \ ] ^ _ `}, so it would treat six punctuation marks as letters.
     *
     * <p>Character.isLetterOrDigit is the built-in equivalent, but it answers a
     * wider question: it is Unicode-aware, so accented letters and non-Latin
     * scripts count too. Identical under these constraints, which promise
     * printable ASCII.
     */
    static boolean isAlnumAscii(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
    }

    /**
     * Filters to alphanumeric lowercase, then compares against the reverse.
     *
     * <p>Time: O(n). Space: O(n) -- the cleaned copy.
     *
     * <p>The forward string is snapshotted into a local before reverse() runs.
     * StringBuilder.reverse() mutates in place and returns the same builder, so
     * {@code cleaned.toString().equals(cleaned.reverse().toString())} only works
     * because Java evaluates the receiver before the argument. Swap the two
     * sides of that equals and it compares the reversed builder against itself,
     * returning true for everything.
     */
    static boolean isPalindromeReverse(String s) {
        StringBuilder cleaned = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (isAlnumAscii(c)) {
                cleaned.append(Character.toLowerCase(c));
            }
        }

        String forward = cleaned.toString();
        return forward.equals(cleaned.reverse().toString());
    }

    /**
     * The same idea, handed to the standard library. Time: O(n). Space: O(n).
     *
     * <p>The idiomatic Java spelling, and worth knowing it exists -- but write
     * the loop above or the two pointers below in an interview, since the
     * filtering and the comparison are the part being asked about. It also walks
     * the string three times (regex, lowercase, reverse) where the loop walks it
     * once; same O(n), more constant factor.
     *
     * <p>Locale.ROOT is not optional. Bare toLowerCase() uses the default
     * locale, and under a Turkish locale 'I' lowercases to dotless 'i' rather
     * than 'i' -- so "Ill i" stops matching itself and returns false on a
     * machine configured for tr-TR and nowhere else. Character.toLowerCase(char)
     * used above has no such problem; it is locale-independent by definition.
     */
    static boolean isPalindromeReverseBuiltIn(String s) {
        String cleaned = s.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
        return cleaned.equals(new StringBuilder(cleaned).reverse().toString());
    }

    /**
     * Closes in from both ends. Time: O(n). Space: O(1).
     *
     * <p>The optimal solution: no cleaned copy, just two indices walking toward
     * each other. Each pointer skips anything non-alphanumeric before comparing,
     * and across the whole run every index is visited at most once by one
     * pointer or the other, so the skipping does not break the linear bound.
     *
     * <p>Both inner loops re-test left &lt; right. Without that, a string
     * holding no alphanumeric characters at all -- " " or ".," -- walks a
     * pointer clean off its end and throws
     * StringIndexOutOfBoundsException.
     */
    static boolean isPalindromeTwoPointers(String s) {
        int left = 0;
        int right = s.length() - 1;

        while (left < right) {
            while (left < right && !isAlnumAscii(s.charAt(left))) {
                left++;
            }
            while (left < right && !isAlnumAscii(s.charAt(right))) {
                right--;
            }
            if (Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) {
                return false;
            }
            left++;
            right--;
        }

        return true;
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
            new Case("Was it a car or a cat I saw?", true),
            new Case("tab a cat", false),
            new Case("A man, a plan, a canal: Panama", true),
            new Case("race a car", false),
            new Case("Madam, I'm Adam", true),
            new Case("0P", false), // '0' and 'P' sit exactly 32 apart; see the README
            new Case("12321", true), // digits only
            new Case("Aa", true), // case folding alone decides it
            new Case("Ill i", true), // pairs 'I' against 'i'; see the locale note
            new Case("ab", false), // shortest false case
            new Case("a.", true), // trailing punctuation, odd length after cleaning
            new Case(" ", true), // no alphanumeric characters at all
            new Case(".,", true), // likewise, even length
            new Case("", true), // empty, though the constraints promise one character
        };

        Map<String, Predicate<String>> solutions = new LinkedHashMap<>();
        solutions.put("reverse", ValidPalindrome::isPalindromeReverse);
        solutions.put("reverse (built-in)", ValidPalindrome::isPalindromeReverseBuiltIn);
        solutions.put("two pointers", ValidPalindrome::isPalindromeTwoPointers);

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
