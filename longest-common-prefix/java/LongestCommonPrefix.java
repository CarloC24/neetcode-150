import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Longest Common Prefix (LeetCode 14).
 *
 * <p>Given an array of strings {@code strs}, return the longest string that is
 * a prefix of every one of them, or "" if there is none.
 *
 * <p>The four approaches from
 * https://neetcode.io/solutions/longest-common-prefix, ordered as NeetCode
 * presents them. All are O(n * m) in the worst case, where n is the number of
 * strings and m the length of the shortest one; they differ in how much they
 * read before stopping and in extra space.
 */
public class LongestCommonPrefix {

    /**
     * Shrinks a running prefix against each string in turn.
     * Time: O(n * m). Space: O(1) beyond the answer.
     *
     * <p>Start with prefix = strs[0]. For each next string, keep only the part
     * of the prefix it matches. Once the prefix is "", stop early.
     */
    static String longestCommonPrefixHorizontal(String[] strs) {
        String prefix = strs[0];
        for (int i = 1; i < strs.length && !prefix.isEmpty(); i++) {
            String s = strs[i];
            int j = 0;
            while (j < Math.min(prefix.length(), s.length()) && prefix.charAt(j) == s.charAt(j)) {
                j++;
            }
            prefix = prefix.substring(0, j);
        }
        return prefix;
    }

    /**
     * Checks one column at a time across every string.
     * Time: O(n * m). Space: O(1) beyond the answer.
     *
     * <p>Column i is in the prefix if every string has a character there and
     * it equals strs[0].charAt(i). It never reads past the first mismatch.
     * The {@code i == s.length()} check must come first, or a shorter string
     * like "a" in ["ab", "a"] throws StringIndexOutOfBoundsException.
     */
    static String longestCommonPrefixVertical(String[] strs) {
        String first = strs[0];
        for (int i = 0; i < first.length(); i++) {
            for (String s : strs) {
                if (i == s.length() || s.charAt(i) != first.charAt(i)) {
                    return first.substring(0, i);
                }
            }
        }
        return first;
    }

    /**
     * Sorts a copy, then compares only the first and last strings.
     * Time: O(n * m * log n). Space: O(n) for the copy.
     *
     * <p>After sorting, the first and last strings are the furthest apart, so
     * whatever prefix they share, everything between them shares too. The
     * copy keeps the caller's array in its original order.
     */
    static String longestCommonPrefixSorting(String[] strs) {
        String[] ordered = strs.clone();
        Arrays.sort(ordered);
        String first = ordered[0];
        String last = ordered[ordered.length - 1];
        int j = 0;
        while (j < Math.min(first.length(), last.length()) && first.charAt(j) == last.charAt(j)) {
            j++;
        }
        return first.substring(0, j);
    }

    /** A trie node for lowercase letters. */
    private static class TrieNode {
        final TrieNode[] children = new TrieNode[26];
        int childCount;
        boolean end;
    }

    /**
     * Inserts every string into a trie, then walks down the single path.
     * Time: O(S), S = total characters. Space: O(S) for the trie.
     *
     * <p>The prefix continues while a node has exactly one child and doesn't
     * end a word. Without the end check, ["ab", "a"] would return "ab".
     */
    static String longestCommonPrefixTrie(String[] strs) {
        TrieNode root = new TrieNode();
        for (String s : strs) {
            TrieNode node = root;
            for (int k = 0; k < s.length(); k++) {
                int c = s.charAt(k) - 'a';
                if (node.children[c] == null) {
                    node.children[c] = new TrieNode();
                    node.childCount++;
                }
                node = node.children[c];
            }
            node.end = true;
        }

        StringBuilder prefix = new StringBuilder();
        TrieNode node = root;
        while (node.childCount == 1 && !node.end) {
            for (int c = 0; c < 26; c++) {
                if (node.children[c] != null) {
                    prefix.append((char) ('a' + c));
                    node = node.children[c];
                    break;
                }
            }
        }
        return prefix.toString();
    }

    /** One test case: an input and the expected answer. */
    private static class Case {
        final String[] strs;
        final String expected;

        Case(String[] strs, String expected) {
            this.strs = strs;
            this.expected = expected;
        }
    }

    public static void main(String[] args) {
        String a199 = "a".repeat(199);
        String a200 = "a".repeat(200);
        String[] allEqualCeiling = new String[200];
        Arrays.fill(allEqualCeiling, a200);

        Case[] cases = {
            new Case(new String[] {"bat", "bag", "bank", "band"}, "ba"),
            new Case(new String[] {"dance", "dag", "danger", "damage"}, "da"),
            new Case(new String[] {"neet", "feet"}, ""), // first characters differ
            new Case(new String[] {"flower", "flow", "flight"}, "fl"),
            new Case(new String[] {"a"}, "a"), // one string is its own prefix
            new Case(new String[] {""}, ""), // one empty string
            new Case(new String[] {"abc", ""}, ""), // an empty string anywhere ends it
            new Case(new String[] {"", "abc"}, ""),
            new Case(new String[] {"ab", "a"}, "a"), // vertical's index check
            new Case(new String[] {"a", "ab"}, "a"), // the trie's end check
            new Case(new String[] {"abc", "abc", "abc"}, "abc"), // all equal
            new Case(new String[] {"prefix", "pre", "prefixes"}, "pre"),
            new Case(new String[] {"c", "acc", "ccc"}, ""), // sorting moves "acc" first
            new Case(new String[] {"ab", "abc", "abd", "a"}, "a"), // branch vs end depth
            new Case(allEqualCeiling, a200), // n = 200, every string 200 long
            new Case(new String[] {a199 + "b", a200}, a199), // mismatch on the last char
        };

        Map<String, Function<String[], String>> solutions = new LinkedHashMap<>();
        solutions.put("horizontal", LongestCommonPrefix::longestCommonPrefixHorizontal);
        solutions.put("vertical", LongestCommonPrefix::longestCommonPrefixVertical);
        solutions.put("sorting", LongestCommonPrefix::longestCommonPrefixSorting);
        solutions.put("trie", LongestCommonPrefix::longestCommonPrefixTrie);

        for (Map.Entry<String, Function<String[], String>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                if (solution.getValue().apply(testCase.strs.clone()).equals(testCase.expected)) {
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

        // Sorting must not reorder the caller's array.
        String[] strs = {"c", "b", "a"};
        for (Function<String[], String> solve : solutions.values()) {
            solve.apply(strs);
        }
        boolean ok = Arrays.equals(strs, new String[] {"c", "b", "a"});
        System.out.printf("%s  input left unmodified: %s%n", ok ? "PASS" : "FAIL", Arrays.toString(strs));
    }
}
