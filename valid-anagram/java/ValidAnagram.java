import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Valid Anagram (LeetCode 242).
 *
 * <p>Given two strings s and t, return true if t is an anagram of s -- the same
 * characters in the same quantities, in any order.
 *
 * <p>The three approaches from https://neetcode.io/solutions/valid-anagram,
 * ordered from brute force to optimal.
 */
public class ValidAnagram {

    private static final int ALPHABET_SIZE = 26;

    /**
     * Sorts both strings and compares. Time: O(n log n). Space: O(n).
     *
     * <p>Anagrams are the same multiset of characters, so sorting collapses them
     * to the same sequence. The length check is not just an optimization -- it is
     * cheap, and it lets the counting approaches below stay in a single loop.
     *
     * <p>Sorts copies (toCharArray already allocates), so the caller's strings are
     * untouched -- String is immutable in Java anyway.
     */
    static boolean isAnagramSorting(String s, String t) {
        if (s.length() != t.length()) {
            return false;
        }
        char[] sorted = s.toCharArray();
        char[] sortedOther = t.toCharArray();
        Arrays.sort(sorted);
        Arrays.sort(sortedOther);
        return Arrays.equals(sorted, sortedOther);
    }

    /**
     * Counts characters in two maps and compares. Time: O(n). Space: O(k).
     *
     * <p>k is the number of distinct characters -- capped at 26 for lowercase
     * English, but O(n) in the worst case for arbitrary input. HashMap::equals
     * compares entries, not iteration order, so the two maps match regardless of
     * the order characters first appeared.
     */
    static boolean isAnagramHashMap(String s, String t) {
        if (s.length() != t.length()) {
            return false;
        }

        Map<Character, Integer> countS = new HashMap<>();
        Map<Character, Integer> countT = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            countS.merge(s.charAt(i), 1, Integer::sum);
            countT.merge(t.charAt(i), 1, Integer::sum);
        }
        return countS.equals(countT);
    }

    /**
     * One fixed array, incremented for s and decremented for t.
     *
     * <p>Time: O(n). Space: O(1) -- 26 slots regardless of input size.
     *
     * <p>The optimal solution when the alphabet is known and small. Every
     * character of s bumps a slot up and every character of t bumps one down, so
     * the array lands back on all zeros exactly when the two strings match.
     *
     * <p>Only valid for lowercase a-z: anything else indexes out of bounds or,
     * worse, lands on a different slot and silently returns the wrong answer.
     */
    static boolean isAnagramArray(String s, String t) {
        if (s.length() != t.length()) {
            return false;
        }

        int[] count = new int[ALPHABET_SIZE];
        for (int i = 0; i < s.length(); i++) {
            count[s.charAt(i) - 'a']++;
            count[t.charAt(i) - 'a']--;
        }

        for (int value : count) {
            if (value != 0) {
                return false;
            }
        }
        return true;
    }

    /** One test case: two inputs and the expected answer. */
    private static class Case {
        final String s;
        final String t;
        final boolean expected;

        Case(String s, String t, boolean expected) {
            this.s = s;
            this.t = t;
            this.expected = expected;
        }
    }

    public static void main(String[] args) {
        Case[] cases = {
            new Case("racecar", "carrace", true),
            new Case("jar", "jam", false),
            new Case("anagram", "nagaram", true),
            new Case("rat", "car", false),
            new Case("a", "ab", false), // different lengths
            new Case("ab", "a", false), // different lengths, other way round
            new Case("", "", true), // both empty
            new Case("a", "a", true), // single character
            new Case("aacc", "ccac", false), // same letters, different counts
            new Case("aabbcc", "abcabc", true), // repeats interleaved
        };

        Map<String, BiPredicate<String, String>> solutions = new LinkedHashMap<>();
        solutions.put("sorting", ValidAnagram::isAnagramSorting);
        solutions.put("hash map", ValidAnagram::isAnagramHashMap);
        solutions.put("array", ValidAnagram::isAnagramArray);

        for (Map.Entry<String, BiPredicate<String, String>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                if (solution.getValue().test(testCase.s, testCase.t) == testCase.expected) {
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
