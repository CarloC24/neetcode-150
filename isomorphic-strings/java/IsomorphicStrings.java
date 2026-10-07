import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Isomorphic Strings (LeetCode 205).
 *
 * <p>Given two strings s and t of the same length, return true if they are
 * isomorphic: if the characters of s can be replaced to get t, where every
 * occurrence of a character is replaced by the same character, and no two
 * characters map to the same one. A character may map to itself.
 *
 * <p>The two approaches from https://neetcode.io/solutions/isomorphic-strings,
 * plus a brute-force baseline, a fixed-array version and the set-counting
 * idiom, ordered from brute force to optimal and then the idiom.
 */
public class IsomorphicStrings {

    /**
     * Compares every pair of positions. Time: O(n^2). Space: O(1).
     *
     * <p>Two strings are isomorphic exactly when they repeat in the same places:
     * for every pair of positions i, j, s[i] == s[j] if and only if
     * t[i] == t[j]. That is the definition with no mapping in sight, and
     * checking it directly is the baseline. At the ceiling of 5 * 10^4
     * characters it is about 1.25 * 10^9 comparisons, so it is here to define
     * the answer, not to submit.
     */
    static boolean isIsomorphicBruteForce(String s, String t) {
        int n = s.length();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if ((s.charAt(i) == s.charAt(j)) != (t.charAt(i) == t.charAt(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    /** True if every character of src always lines up with the same one in dst. */
    private static boolean mapsConsistently(String src, String dst) {
        Map<Character, Character> mapping = new HashMap<>();
        for (int i = 0; i < src.length(); i++) {
            char a = src.charAt(i);
            char b = dst.charAt(i);
            Character mapped = mapping.get(a);
            if (mapped != null && mapped != b) {
                return false;
            }
            mapping.put(a, b);
        }
        return true;
    }

    /**
     * Checks the mapping s -&gt; t, then t -&gt; s. Time: O(n). Space: O(k).
     *
     * <p>k is the number of distinct characters, at most 128 under these
     * constraints. One direction is not enough: "ab" -&gt; "aa" maps a to a and
     * b to a without a single conflict, so a one-way check accepts it. The
     * reverse pass is what catches two characters landing on the same one.
     *
     * <p>{@code mapped != b} compares a Character with a char, so Java unboxes
     * and compares values. Compare two Character objects with != and it
     * compares references instead -- see the README.
     */
    static boolean isIsomorphicTwoPass(String s, String t) {
        return mapsConsistently(s, t) && mapsConsistently(t, s);
    }

    /**
     * Keeps both maps at once. Time: O(n). Space: O(k).
     *
     * <p>The same two checks as approach 2, interleaved, so the walk can stop at
     * the first conflict in either direction instead of finishing one direction
     * before starting the other.
     */
    static boolean isIsomorphicOnePass(String s, String t) {
        Map<Character, Character> sToT = new HashMap<>();
        Map<Character, Character> tToS = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            char a = s.charAt(i);
            char b = t.charAt(i);
            if (sToT.getOrDefault(a, b) != b || tToS.getOrDefault(b, a) != a) {
                return false;
            }
            sToT.put(a, b);
            tToS.put(b, a);
        }
        return true;
    }

    /**
     * Compares where each character was last seen. Time: O(n). Space: O(1).
     *
     * <p>The optimal solution. Instead of mapping characters to characters,
     * record for each character the position (plus one) where it last appeared,
     * in two 128-slot arrays -- one per string, sized for ASCII. Two characters
     * at the same position belong together exactly when they were last seen at
     * the same place, which covers both directions in one comparison: a new
     * pairing shows up as one side remembering a position the other side does
     * not.
     *
     * <p>The +1 keeps 0 free to mean "never seen", so position 0 is not mistaken
     * for it. The arrays are fixed-size, so the space is O(1) rather than O(k).
     */
    static boolean isIsomorphicLastSeen(String s, String t) {
        int[] lastS = new int[128];
        int[] lastT = new int[128];
        for (int i = 0; i < s.length(); i++) {
            char a = s.charAt(i);
            char b = t.charAt(i);
            if (lastS[a] != lastT[b]) {
                return false;
            }
            lastS[a] = i + 1;
            lastT[b] = i + 1;
        }
        return true;
    }

    /**
     * Counts distinct characters and distinct pairs. Time: O(n). Space: O(k).
     *
     * <p>If the strings are isomorphic, each distinct character of s pairs with
     * exactly one character of t and vice versa, so s, t and their pairs all
     * have the same number of distinct members. If any character pairs with two
     * partners, the pairs outnumber that side. A one-liner in Python; here each
     * pair is packed into one int as a * 128 + b, which is unique because both
     * characters are below 128. It builds three sets and cannot stop early, so
     * it does more work than approach 4 on every input.
     */
    static boolean isIsomorphicSets(String s, String t) {
        Set<Character> sChars = new HashSet<>();
        Set<Character> tChars = new HashSet<>();
        Set<Integer> pairs = new HashSet<>();
        for (int i = 0; i < s.length(); i++) {
            char a = s.charAt(i);
            char b = t.charAt(i);
            sChars.add(a);
            tChars.add(b);
            pairs.add(a * 128 + b);
        }
        return sChars.size() == tChars.size() && tChars.size() == pairs.size();
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
            new Case("egg", "add", true),
            new Case("foo", "bar", false), // o would have to map to both a and r
            new Case("paper", "title", true),
            new Case("ab", "aa", false), // one-way check passes; needs t -> s too
            new Case("aa", "ab", false), // the mirror image: fails s -> t
            new Case("badc", "baba", false), // the conflict only appears on the reverse map
            new Case("abc", "abc", true), // every character maps to itself
            new Case("abc", "bca", true), // a cycle of reassignments is still one-to-one
            new Case("13", "42", true), // any ASCII, not just letters
            new Case("a b", "x!y", true), // space and punctuation are characters too
            new Case("a", "a", true),
            new Case("", "", true), // the constraints allow length 0
            new Case("ab".repeat(150), "cd".repeat(150), true), // long, every repeat agrees
            new Case("ab".repeat(150), "cd".repeat(149) + "cc", false), // one conflict at the end
        };

        Map<String, BiPredicate<String, String>> solutions = new LinkedHashMap<>();
        solutions.put("brute force", IsomorphicStrings::isIsomorphicBruteForce);
        solutions.put("hash map (two pass)", IsomorphicStrings::isIsomorphicTwoPass);
        solutions.put("hash map (one pass)", IsomorphicStrings::isIsomorphicOnePass);
        solutions.put("last seen", IsomorphicStrings::isIsomorphicLastSeen);
        solutions.put("set counts", IsomorphicStrings::isIsomorphicSets);

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
